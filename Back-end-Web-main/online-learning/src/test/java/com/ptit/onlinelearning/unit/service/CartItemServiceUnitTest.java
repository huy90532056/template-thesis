package com.ptit.onlinelearning.unit.service;

import com.ptit.onlinelearning.model.CartItem;
import com.ptit.onlinelearning.model.Course;
import com.ptit.onlinelearning.model.CourseGroup;
import com.ptit.onlinelearning.model.Instructor;
import com.ptit.onlinelearning.model.User;
import com.ptit.onlinelearning.request.CartItemRequest;
import com.ptit.onlinelearning.response.order.CartItemResponse;
import com.ptit.onlinelearning.common.type.CourseLevel;
import com.ptit.onlinelearning.common.type.CourseStatus;
import com.ptit.onlinelearning.common.type.Currency;
import com.ptit.onlinelearning.common.type.EnrollmentType;
import com.ptit.onlinelearning.exception.DataNotFoundException;
import com.ptit.onlinelearning.exception.InvalidParamException;
import com.ptit.onlinelearning.repository.CartItemRepository;
import com.ptit.onlinelearning.repository.CourseGroupRepository;
import com.ptit.onlinelearning.repository.CourseRepository;
import com.ptit.onlinelearning.repository.EnrollmentRepository;
import com.ptit.onlinelearning.repository.InstructorRepository;
import com.ptit.onlinelearning.repository.UserRepository;
import com.ptit.onlinelearning.service.cartitem.CartItemService;
import com.ptit.onlinelearning.unit.config.BaseUnitTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class CartItemServiceUnitTest extends BaseUnitTest {

    private record CartSeed(User user, Course course, CourseGroup courseGroup, CartItem cartItem) {
    }

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseGroupRepository courseGroupRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Test
    @Transactional
    public void getCartItems_testStandard1() {
        // user id = null, course id = null
        long beforeCount = cartItemRepository.count();
        createCartItemSeed();

        Page<CartItem> result = cartItemService.getCartItems(1, 10, "id", "ASC", null, null);

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= beforeCount + 1);
    }

    @Test
    @Transactional
    public void getCartItems_testStandard2() {
        // pageSize = 1 => toi da 1 phan tu trong page
        createCartItemSeed();

        Page<CartItem> result = cartItemService.getCartItems(1, 1, "id", "ASC", null, null);

        assertNotNull(result);
        assertEquals(1, result.getSize());
        assertTrue(result.getContent().size() <= 1);
    }

    @Test
    @Transactional
    public void getCartItems_testStandard3() {
        // sort DESC hop le, khong nem ngoai le
        createCartItemSeed();

        Page<CartItem> result = cartItemService.getCartItems(1, 10, "id", "DESC", null, null);

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 1);
    }

    @Test
    @Transactional
    public void getCartItems_testException1() {
        // page = 0 khong hop le
        assertThrows(IllegalArgumentException.class,
                () -> cartItemService.getCartItems(0, 10, "id", "ASC", null, null));
    }

    @Test
    @Transactional
    public void getCartItems_testException2() {
        // sort order khong hop le
        assertThrows(IllegalArgumentException.class,
                () -> cartItemService.getCartItems(1, 10, "id", "INVALID_ORDER", null, null));
    }

    @Test
    @Transactional
    public void getCartItemsByUserId_testStandard1() {
        // Loc theo userId: chi tra ve cart item cua user do
        CartSeed seedTargetUser = createCartItemSeed();
        CartSeed seedOtherUser = createCartItemSeed();

        Page<CartItemResponse> result = cartItemService.getCartItemsByUserId(
            0, 10, "id", "ASC", seedTargetUser.user().getId());

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 1);

        Set<Long> responseIds = result.getContent().stream()
            .map(CartItemResponse::getId)
            .collect(Collectors.toSet());

        assertTrue(responseIds.contains(seedTargetUser.cartItem().getId()));
        assertFalse(responseIds.contains(seedOtherUser.cartItem().getId()));
    }

    @Test
    @Transactional
    public void getCartItemsByUserId_testStandard2() {
        // pageSize = 1 va sortBy = null => su dung field mac dinh createdAt
        CartSeed seed = createCartItemSeed();

        Page<CartItemResponse> result = cartItemService.getCartItemsByUserId(
            0, 1, null, "DESC", seed.user().getId());

        assertNotNull(result);
        assertEquals(1, result.getSize());
        assertTrue(result.getContent().size() <= 1);
    }

    @Test
    @Transactional
    public void getCartItemsByUserId_testException1() {
        // page am => PageRequest.of() nem IllegalArgumentException
        CartSeed seed = createCartItemSeed();

        assertThrows(IllegalArgumentException.class,
                () -> cartItemService.getCartItemsByUserId(-1, 10, "id", "ASC", seed.user().getId()));
    }

    @Test
    @Transactional
    public void getCartItemById_testStandard1() {
        // Lay cart item bang chinh owner => tra ve du lieu hop le
        CartSeed seed = createCartItemSeed();

        CartItem result = cartItemService.getCartItemById(seed.cartItem().getId(), seed.user());

        assertNotNull(result);
        assertEquals(seed.cartItem().getId(), result.getId());
    }

    @Test
    @Transactional
    public void getCartItemById_testException1() {
        // id khong ton tai => DataNotFoundException
        User anyUser = createUserSeed();

        assertThrows(DataNotFoundException.class,
                () -> cartItemService.getCartItemById(99999999L, anyUser));
    }

    @Test
    @Transactional
    public void getCartItemById_testException2() {
        // user khac owner => AccessDeniedException
        CartSeed ownerSeed = createCartItemSeed();
        User anotherUser = createUserSeed();

        assertThrows(AccessDeniedException.class,
                () -> cartItemService.getCartItemById(ownerSeed.cartItem().getId(), anotherUser));
    }

    @Test
    @Transactional
    public void createCartItem_testStandard1() {
        // Tao cart item theo courseId thanh cong
        User user = createUserSeed();
        CourseGroup group = createCourseGroupSeed();
        Course course = createCourseSeed(group, null);

        CartItemRequest request = new CartItemRequest();
        request.setCourseId(course.getId());

        CartItem result = cartItemService.createCartItem(user, request);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(user.getId(), result.getUser().getId());
        assertEquals(course.getId(), result.getCourse().getId());
    }

    @Test
    @Transactional
    public void createCartItem_testStandard2() {
        // Tao cart item theo courseGroupId thanh cong (group co danh sach course)
        User user = createUserSeed();
        CourseGroup group = createCourseGroupSeed();
        Course c1 = createCourseSeed(group, null);
        Course c2 = createCourseSeed(group, null);
        group.setCourses(new ArrayList<>(List.of(c1, c2)));
        courseGroupRepository.save(group);

        CartItemRequest request = new CartItemRequest();
        request.setCourseGroupId(group.getId());

        CartItem result = cartItemService.createCartItem(user, request);

        assertNotNull(result);
        assertNotNull(result.getId());
        long totalByUser = cartItemRepository.findAll().stream()
                .filter(ci -> ci.getUser().getId().equals(user.getId()))
                .count();
        assertTrue(totalByUser >= 2);

        // avoid unused variable warnings while preserving seeded data intent
        assertNotNull(c1.getId());
        assertNotNull(c2.getId());
    }

    @Test
    @Transactional
    public void createCartItem_testException1() {
        // Khong truyen courseId/courseGroupId => InvalidParamException
        User user = createUserSeed();
        CartItemRequest request = new CartItemRequest();

        assertThrows(InvalidParamException.class,
                () -> cartItemService.createCartItem(user, request));
    }

    @Test
    @Transactional
    public void createCartItem_testException2() {
        // Course da co trong cart cua user => InvalidParamException
        User user = createUserSeed();
        CourseGroup group = createCourseGroupSeed();
        Course course = createCourseSeed(group, null);

        CartItem existed = new CartItem();
        existed.setUser(user);
        existed.setCourse(course);
        existed.setCourseGroup(group);
        cartItemRepository.save(existed);

        CartItemRequest request = new CartItemRequest();
        request.setCourseId(course.getId());

        assertThrows(InvalidParamException.class,
                () -> cartItemService.createCartItem(user, request));
    }

    @Test
    @Transactional
    public void createCartItem_testException3() {
        // User them khoa hoc cua chinh minh => InvalidParamException
        User user = createUserSeed();
        CourseGroup group = createCourseGroupSeed();

        // Course with instructor owned by the same user -> forbidden by business rule
        Instructor instructor = createInstructorSeed(user.getId());
        Course ownCourse = createCourseSeed(group, instructor.getId());

        CartItemRequest request = new CartItemRequest();
        request.setCourseId(ownCourse.getId());

        assertThrows(InvalidParamException.class,
                () -> cartItemService.createCartItem(user, request));
    }

    @Test
    @Transactional
    public void deleteCartItem_testStandard1() {
        // Owner xoa cart item cua minh thanh cong
        CartSeed seed = createCartItemSeed();

        cartItemService.deleteCartItem(seed.cartItem().getId(), seed.user());

        assertFalse(cartItemRepository.findById(seed.cartItem().getId()).isPresent());
    }

    @Test
    @Transactional
    public void deleteCartItem_testException1() {
        // User khac owner xoa cart item => AccessDeniedException
        CartSeed seed = createCartItemSeed();
        User anotherUser = createUserSeed();

        assertThrows(AccessDeniedException.class,
                () -> cartItemService.deleteCartItem(seed.cartItem().getId(), anotherUser));
    }

    @Test
    @Transactional
    public void deleteAllCartItemsByUserId_testStandard1() {
        // Xoa toan bo cart item theo userId
        CartSeed s1 = createCartItemSeed();
        User targetUser = s1.user();
        CourseGroup anotherGroup = createCourseGroupSeed();
        Course anotherCourse = createCourseSeed(anotherGroup, null);

        CartItem second = new CartItem();
        second.setUser(targetUser);
        second.setCourse(anotherCourse);
        second.setCourseGroup(anotherGroup);
        second.setQuantity(1);
        cartItemRepository.save(second);

        cartItemService.deleteAllCartItemsByUserId(targetUser.getId());

        long remain = cartItemRepository.findAll().stream()
                .filter(ci -> ci.getUser().getId().equals(targetUser.getId()))
                .count();
        assertEquals(0, remain);
    }

    private CartSeed createCartItemSeed() {
        // Seed du lieu day du cho 1 cart item (user + group + course)
        User savedUser = createUserSeed();
        CourseGroup savedCourseGroup = createCourseGroupSeed();
        Course savedCourse = createCourseSeed(savedCourseGroup, null);

        CartItem cartItem = new CartItem();
        cartItem.setUser(savedUser);
        cartItem.setCourse(savedCourse);
        cartItem.setCourseGroup(savedCourseGroup);
        cartItem.setQuantity(1);
        CartItem savedCartItem = cartItemRepository.save(cartItem);

        return new CartSeed(savedUser, savedCourse, savedCourseGroup, savedCartItem);
    }

    private User createUserSeed() {
        String unique = String.valueOf(System.nanoTime());
        User user = new User();
        user.setEmail("student_" + unique + "@mail.com");
        user.setAccountName("student_" + unique);
        user.setPhone("090" + unique.substring(0, 7));
        user.setPassword("unit-test-password");
        user.setFirstName("Unit");
        user.setLastName("Tester");
        user.setAvatar("https://example.com/avatar.png");
        user.setGender("MALE");
        user.setBio("Seed data for cart item unit test");
        user.setProvider("LOCAL");
        user.setIsActive(true);
        user.setEmailVerified(true);
        user.setPhoneVerified(true);
        return userRepository.save(user);
    }

    private CourseGroup createCourseGroupSeed() {
        String unique = String.valueOf(System.nanoTime());
        CourseGroup courseGroup = new CourseGroup();
        courseGroup.setTitle("Bundle " + unique);
        courseGroup.setSlug("bundle-" + unique);
        courseGroup.setDescription("JUnit seed course group");
        courseGroup.setThumbnail("https://example.com/group-thumb.png");
        courseGroup.setEnrollmentType(EnrollmentType.LIFETIME);
        courseGroup.setCurrency(Currency.VND);
        courseGroup.setPrice(new BigDecimal("299000"));
        courseGroup.setCustomPrice(new BigDecimal("249000"));
        courseGroup.setWhatYouLearn("Testing bundle");
        courseGroup.setIsPreOrder(false);
        return courseGroupRepository.save(courseGroup);
    }

    private Course createCourseSeed(CourseGroup group, Long instructorId) {
        String unique = String.valueOf(System.nanoTime());
        Long resolvedInstructorId = instructorId;
        if (resolvedInstructorId == null) {
            User instructorUser = createUserSeed();
            Instructor instructor = createInstructorSeed(instructorUser.getId());
            resolvedInstructorId = instructor.getId();
        }

        Course course = new Course();
        course.setTitle("Course " + unique);
        course.setSlug("course-" + unique);
        course.setCode("COURSE-" + unique);
        course.setDescription("JUnit seed course");
        course.setThumbnail("https://example.com/course-thumb.png");
        course.setPreviewVideo("https://example.com/preview.mp4");
        course.setLevel(CourseLevel.BEGINNER);
        course.setStatus(CourseStatus.DRAFT);
        course.setLanguage("vi");
        course.setWhatYouLearn("Write reliable unit tests");
        course.setPrice(new BigDecimal("199000"));
        course.setCurrency(Currency.VND);
        course.setExpiredDays(365);
        course.setSkillAcquired("Testing");
        course.setTargetAudiences("Backend developer");
        course.setEnrollmentType(EnrollmentType.LIFETIME);
        course.setIsFree(true);
        course.setIsPreOrder(false);
        course.setCourseGroup(group);
        course.setInstructorId(resolvedInstructorId);
        return courseRepository.save(course);
    }

    private Instructor createInstructorSeed(Long userId) {
        String unique = String.valueOf(System.nanoTime());
        Instructor instructor = new Instructor();
        instructor.setUserId(userId);
        instructor.setSlug("instructor-" + unique);
        instructor.setExpertise("Testing and Backend Development");
        instructor.setExperienceYears(3);
        instructor.setQualification("Bachelor of Information Technology");
        instructor.setBankName("Vietcombank");
        instructor.setBankAccount("0123456789");
        instructor.setTaxCode("TAX-UNIT-001");
        return instructorRepository.save(instructor);
    }
}