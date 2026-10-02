package com.ptit.onlinelearning.unit.service;

import com.ptit.onlinelearning.common.type.UserRegisterRole;
import com.ptit.onlinelearning.exception.DataNotFoundException;
import com.ptit.onlinelearning.exception.InvalidParamException;
import com.ptit.onlinelearning.common.type.RoleName;
import com.ptit.onlinelearning.model.Role;
import com.ptit.onlinelearning.model.User;
import com.ptit.onlinelearning.model.UserRole;
import com.ptit.onlinelearning.repository.RoleRepository;
import com.ptit.onlinelearning.repository.UserRepository;
import com.ptit.onlinelearning.repository.UserRoleRepository;
import com.ptit.onlinelearning.request.ChangePasswordWithTokenRequest;
import com.ptit.onlinelearning.request.UserRegisterRequest;
import com.ptit.onlinelearning.request.VerifyRequest;
import com.ptit.onlinelearning.producer.EventPublisher;
import com.ptit.onlinelearning.service.auth.AuthService;
import com.ptit.onlinelearning.service.auth.VerificationCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthenServiceUnitTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InMemoryVerificationCode inMemoryVerificationCode;

    @Autowired
    private InMemoryEventPublisher inMemoryEventPublisher;

    @BeforeEach
    void setUp() {
        inMemoryVerificationCode.clear();
        inMemoryEventPublisher.clear();
    }

    /**
     * Exception-first:
     * - register() phải ném InvalidParamException khi email đã được verify trước đó.
     */
    @Test
    public void register_EmailAlreadyVerified_ThrowsInvalidParamException() {
        String email = "existing_verified@gmail.com";

        User existing = User.builder()
                .email(email)
                .password(passwordEncoder.encode("pass1234"))
                .accountName("Existing Verified")
                .isActive(true)
                .emailVerified(true)
                .build();
        userRepository.saveAndFlush(existing);

        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail(email);
        request.setPassword("pass1234");
        request.setAccountName("Duplicate User");
        request.setRole(UserRegisterRole.STUDENT);

        InvalidParamException ex = Assertions.assertThrows(InvalidParamException.class,
                () -> authService.register(request));
        Assertions.assertEquals("Email is already in use", ex.getMessage());
    }

    /**
     * Test mục tiêu:
     * - Xác nhận login() ném lỗi đúng khi email không tồn tại.
     * Kịch bản:
     * - Gọi login() với email chưa có trong DB.
     * Kết quả đúng:
     * - Ném DataNotFoundException.
     */
    @Test
    public void login_UserNotFound_ThrowsDataNotFoundException() {
        Assertions.assertThrows(DataNotFoundException.class,
                () -> authService.login("not_found@gmail.com", "anyPassword123"));
    }

    /**
         * Exception-first:
         * - changePassword() phải ném InvalidParamException khi newPassword và retypePassword khác nhau.
     */
    @Test
        public void changePassword_MismatchPasswords_ThrowsInvalidParamException() {
        ChangePasswordWithTokenRequest request = ChangePasswordWithTokenRequest.builder()
            .email("mismatch_password@gmail.com")
            .resetToken("any-token")
            .newPassword("newPassword123")
            .retypePassword("newPassword456")
                .build();

        InvalidParamException ex = Assertions.assertThrows(InvalidParamException.class,
            () -> authService.changePassword(request));
        Assertions.assertEquals("New password and retype password do not match", ex.getMessage());
        }

    /**
     * Exception-first:
     * - verifyUser() phải ném DataNotFoundException khi OTP hợp lệ nhưng email không tồn tại trong users.
     */
    @Test
    public void verifyUser_ValidOtpButUserMissing_ThrowsDataNotFoundException() {
        String missingEmail = "missing_after_valid_otp@gmail.com";
        inMemoryVerificationCode.putOtp(missingEmail, "123456");

        VerifyRequest request = VerifyRequest.builder()
                .email(missingEmail)
                .code("123456")
                .build();

        Assertions.assertThrows(DataNotFoundException.class,
                () -> authService.verifyUser(request));
    }

        /**
         * Case theo input thực tế người dùng:
         * - Người dùng có thể nhập email cùng giá trị nhưng khác hoa/thường.
         * Kỳ vọng nghiệp vụ:
         * - Hệ thống nên coi là trùng email và từ chối đăng ký mới.
         */
        @Test
        public void register_EmailDiffersOnlyByCase_ShouldBeRejectedAsDuplicate() {
        String existingEmail = "case_duplicate@gmail.com";
        String inputEmail = "CASE_DUPLICATE@gmail.com";

        User existing = User.builder()
            .email(existingEmail)
            .password(passwordEncoder.encode("pass1234"))
            .accountName("Existing Case")
            .isActive(true)
            .emailVerified(true)
            .build();
        userRepository.saveAndFlush(existing);

        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail(inputEmail);
        request.setPassword("pass1234");
        request.setAccountName("New Case Input");
        request.setRole(UserRegisterRole.STUDENT);

        Assertions.assertThrows(InvalidParamException.class,
            () -> authService.register(request));
        }

    /**
     * Test case: Quên mật khẩu với email không tồn tại.
     * - Input: email chưa có trong DB.
     * - Kỳ vọng: trả message an toàn (không lộ thông tin).
     * - Status: Pass (hành vi đúng).
     */
    @Test
    public void processForgotPassword_EmailNotFound_ReturnsSafeMessage() {
        String responseMessage = authService.processForgotPassword("nonexistent@gmail.com");
        Assertions.assertNotNull(responseMessage);
        Assertions.assertTrue(responseMessage.contains("If an account with that email exists"));
    }

    /**
     * Test case: Xác nhận OTP quên mật khẩu với email không tồn tại.
     * - Input: OTP không hợp lệ hoặc email không có OTP.
     * - Kỳ vọng: trả null.
     * - Status: Pass (hành vi đúng).
     */
    @Test
    public void verifyForgotPassword_InvalidOtp_ReturnsNull() {
        VerifyRequest request = VerifyRequest.builder()
                .email("forgot@gmail.com")
                .code("000000")
                .build();

        String resetToken = authService.verifyForgotPassword(request);
        Assertions.assertNull(resetToken);
    }

    /**
     * Test case: Gửi lại OTP verify email.
     * - Input: email bất kỳ.
     * - Kỳ vọng: method chạy không ném exception.
     * - Status: Pass (hành vi đúng).
     */
    @Test
    public void resendOtp_AnyEmail_ExecutesWithoutException() {
        Assertions.assertDoesNotThrow(() -> authService.resendOtp("resend@gmail.com"));
    }

    @TestConfiguration
    static class AuthServiceTestConfig {
        @Bean
        @Primary
        InMemoryVerificationCode inMemoryVerificationCode() {
            return new InMemoryVerificationCode();
        }

        @Bean
        @Primary
        InMemoryEventPublisher inMemoryEventPublisher() {
            return new InMemoryEventPublisher();
        }
    }

    static class InMemoryVerificationCode extends VerificationCode {
        private final Map<String, String> otpMap = new HashMap<>();
        private final Map<String, String> resetOtpMap = new HashMap<>();
        private final Map<String, String> resetTokenMap = new HashMap<>();

        InMemoryVerificationCode() {
            super(null, null);
        }

        @Override
        public boolean verifyOtp(String email, String inputOtp) {
            String savedOtp = otpMap.get(email);
            if (savedOtp != null && savedOtp.equals(inputOtp)) {
                otpMap.remove(email);
                return true;
            }
            return false;
        }

        @Override
        public boolean verifyOtpResetPassword(String email, String inputOtp) {
            String savedOtp = resetOtpMap.get(email);
            if (savedOtp != null && savedOtp.equals(inputOtp)) {
                resetOtpMap.remove(email);
                return true;
            }
            return false;
        }

        @Override
        public String generateResetToken(String email) {
            String token = UUID.randomUUID().toString();
            resetTokenMap.put(email, token);
            return token;
        }

        @Override
        public boolean verifyResetToken(String email, String token) {
            return token.equals(resetTokenMap.get(email));
        }

        @Override
        public void deleteResetToken(String email) {
            resetTokenMap.remove(email);
        }

        void putOtp(String email, String otp) {
            otpMap.put(email, otp);
        }

        void putResetOtp(String email, String otp) {
            resetOtpMap.put(email, otp);
        }

        void putResetToken(String email, String token) {
            resetTokenMap.put(email, token);
        }

        boolean hasResetToken(String email, String token) {
            return token.equals(resetTokenMap.get(email));
        }

        boolean containsResetToken(String email) {
            return resetTokenMap.containsKey(email);
        }

        void clear() {
            otpMap.clear();
            resetOtpMap.clear();
            resetTokenMap.clear();
        }
    }

    static class InMemoryEventPublisher extends EventPublisher {
        private final List<Object> sentEmails = new ArrayList<>();

        InMemoryEventPublisher() {
            super(null);
        }

        @Override
        public void sendEmail(Object payload) {
            sentEmails.add(payload);
        }

        List<Object> sentEmails() {
            return sentEmails;
        }

        void clear() {
            sentEmails.clear();
        }
    }
}