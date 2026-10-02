package com.ptit.onlinelearning.unit.service;

import com.ptit.onlinelearning.component.SendGridSender;
import com.ptit.onlinelearning.service.auth.VerificationCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VerificationCodeUnitTest {

    private InMemoryStringRedisTemplate redisTemplate;
    private InMemorySendGridSender sendGridSender;
    private VerificationCode verificationCode;

    @BeforeEach
    void setUp() {
        redisTemplate = new InMemoryStringRedisTemplate();
        sendGridSender = new InMemorySendGridSender();
        verificationCode = new VerificationCode(redisTemplate, sendGridSender);
    }

    /**
     * Test mục tiêu:
     * - generateAndSendOtp() phải lưu OTP vào redis và gọi gửi email OTP.
     * Kết quả đúng:
     * - key otp:email tồn tại, OTP có 6 chữ số.
     * - SendGrid sender được gọi đúng email + OTP.
     */
    @Test
    void generateAndSendOtp_StoresOtpAndCallsSender() {
        String email = "otp_user@gmail.com";

        verificationCode.generateAndSendOtp(email);

        String storedOtp = redisTemplate.getValue("otp:" + email);
        Assertions.assertNotNull(storedOtp);
        Assertions.assertTrue(storedOtp.matches("\\d{6}"));
        Assertions.assertEquals(email, sendGridSender.lastOtpEmailTo);
        Assertions.assertEquals(storedOtp, sendGridSender.lastOtpCode);
    }

    /**
     * Test mục tiêu:
     * - verifyOtp() trả true và xóa key khi OTP hợp lệ.
     */
    @Test
    void verifyOtp_ValidOtp_ReturnsTrueAndDeletesKey() {
        String email = "verify_otp@gmail.com";
        redisTemplate.putValue("otp:" + email, "123456");

        boolean result = verificationCode.verifyOtp(email, "123456");

        Assertions.assertTrue(result);
        Assertions.assertNull(redisTemplate.getValue("otp:" + email));
    }

    /**
     * Test mục tiêu:
     * - verifyOtp() trả false khi OTP không hợp lệ.
     */
    @Test
    void verifyOtp_InvalidOtp_ReturnsFalse() {
        String email = "verify_otp_invalid@gmail.com";
        redisTemplate.putValue("otp:" + email, "123456");

        boolean result = verificationCode.verifyOtp(email, "000000");

        Assertions.assertFalse(result);
        Assertions.assertNotNull(redisTemplate.getValue("otp:" + email));
    }

    /**
     * Test mục tiêu:
     * - sendOtpToResetPassword() phải lưu OTP reset và gọi sender reset password.
     */
    @Test
    void sendOtpToResetPassword_StoresOtpAndCallsSender() {
        String email = "reset_user@gmail.com";
        String accountName = "Reset User";

        verificationCode.sendOtpToResetPassword(email, accountName);

        String storedOtp = redisTemplate.getValue("otp:reset:" + email);
        Assertions.assertNotNull(storedOtp);
        Assertions.assertTrue(storedOtp.matches("\\d{6}"));
        Assertions.assertEquals(email, sendGridSender.lastResetEmailTo);
        Assertions.assertEquals(storedOtp, sendGridSender.lastResetOtpCode);
        Assertions.assertEquals(accountName, sendGridSender.lastResetAccountName);
    }

    /**
     * Test mục tiêu:
     * - generateResetToken(), verifyResetToken(), deleteResetToken() hoạt động đúng chu kỳ.
     */
    @Test
    void resetToken_Lifecycle_WorksAsExpected() {
        String email = "token_user@gmail.com";

        String token = verificationCode.generateResetToken(email);
        boolean validBeforeDelete = verificationCode.verifyResetToken(email, token);
        verificationCode.deleteResetToken(email);
        boolean validAfterDelete = verificationCode.verifyResetToken(email, token);

        Assertions.assertNotNull(token);
        Assertions.assertTrue(validBeforeDelete);
        Assertions.assertFalse(validAfterDelete);
    }

    /**
     * Test mục tiêu:
     * - sendPasswordChangeSuccessNotification() gọi sender với email/accountName đúng.
     * - changeTime truyền xuống đúng định dạng dd/MM/yyyy HH:mm:ss.
     */
    @Test
    void sendPasswordChangeSuccessNotification_CallsSenderWithFormattedTime() {
        String email = "changed@gmail.com";
        String accountName = "Changed User";

        verificationCode.sendPasswordChangeSuccessNotification(email, accountName);

        Assertions.assertEquals(email, sendGridSender.lastPasswordChangeEmailTo);
        Assertions.assertEquals(accountName, sendGridSender.lastPasswordChangeAccountName);
        Assertions.assertNotNull(sendGridSender.lastPasswordChangeTime);
        Assertions.assertDoesNotThrow(() ->
                LocalDateTime.parse(sendGridSender.lastPasswordChangeTime, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }

    static class InMemoryStringRedisTemplate extends StringRedisTemplate {
        private final Map<String, String> store = new HashMap<>();
        private final ValueOperations<String, String> valueOperations = createValueOperationsProxy();

        @Override
        public ValueOperations<String, String> opsForValue() {
            return valueOperations;
        }

        @Override
        public Boolean delete(String key) {
            return store.remove(key) != null;
        }

        void putValue(String key, String value) {
            store.put(key, value);
        }

        String getValue(String key) {
            return store.get(key);
        }

        @SuppressWarnings("unchecked")
        private ValueOperations<String, String> createValueOperationsProxy() {
            InvocationHandler handler = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
                    String methodName = method.getName();
                    if ("set".equals(methodName) && args != null && args.length >= 2) {
                        String key = (String) args[0];
                        String value = (String) args[1];
                        store.put(key, value);
                        return null;
                    }
                    if ("get".equals(methodName) && args != null && args.length == 1) {
                        return store.get((String) args[0]);
                    }
                    if ("getOperations".equals(methodName)) {
                        return InMemoryStringRedisTemplate.this;
                    }
                    if ("toString".equals(methodName)) {
                        return "InMemoryValueOperations";
                    }
                    if ("hashCode".equals(methodName)) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(methodName)) {
                        return proxy == args[0];
                    }
                    throw new UnsupportedOperationException("Method not supported in test double: " + methodName);
                }
            };

            return (ValueOperations<String, String>) Proxy.newProxyInstance(
                    ValueOperations.class.getClassLoader(),
                    new Class[]{ValueOperations.class},
                    handler
            );
        }
    }

    static class InMemorySendGridSender extends SendGridSender {
        String lastOtpEmailTo;
        String lastOtpCode;

        String lastResetEmailTo;
        String lastResetOtpCode;
        String lastResetAccountName;

        String lastPasswordChangeEmailTo;
        String lastPasswordChangeAccountName;
        String lastPasswordChangeTime;

        InMemorySendGridSender() {
            super(null, null, null, null, null, null, null, null);
        }

        @Override
        public void sendOtpEmail(String toEmail, String otp) {
            this.lastOtpEmailTo = toEmail;
            this.lastOtpCode = otp;
        }

        @Override
        public void sendOtpResetPasswordEmail(String toEmail, String otp, String accountName) {
            this.lastResetEmailTo = toEmail;
            this.lastResetOtpCode = otp;
            this.lastResetAccountName = accountName;
        }

        @Override
        public void sendPasswordChangeSuccessEmail(String toEmail, String accountName, String changeTime) {
            this.lastPasswordChangeEmailTo = toEmail;
            this.lastPasswordChangeAccountName = accountName;
            this.lastPasswordChangeTime = changeTime;
        }
    }
}
