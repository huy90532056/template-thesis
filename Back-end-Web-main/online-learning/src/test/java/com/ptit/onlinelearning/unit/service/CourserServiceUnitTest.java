package com.ptit.onlinelearning.unit.service;

import com.ptit.onlinelearning.common.type.*;
import com.ptit.onlinelearning.exception.DataNotFoundException;
import com.ptit.onlinelearning.exception.InvalidParamException;
import com.ptit.onlinelearning.model.*;
import com.ptit.onlinelearning.repository.*;
import com.ptit.onlinelearning.request.CourseRequest;
import com.ptit.onlinelearning.request.UpdateCourseRequest;
import com.ptit.onlinelearning.service.course.CourseService;
import com.ptit.onlinelearning.unit.config.BaseUnitTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CourserServiceUnitTest extends BaseUnitTest {
    @Autowired
    CourseService courserService;

    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRoleRepository userRoleRepository;
    @Autowired
    InstructorRepository instructorRepository;
    @Autowired
    CourseRepository courseRepository;

    // TC_CS_01
    // Create free, non pre-order course
    @Test
    @Transactional
    void createCourse_testStandard1() {
        User user = User.builder()
                .bio("Test")
                .email("dat@dat.com")
                .phone("0123456789")
                .build();
        user = userRepository.save(user);
        Role role = Role.builder()
                .name(RoleName.INSTRUCTOR).build();
        role = roleRepository.save(role);

        userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(role)
                .build());

        Instructor instructor = new Instructor();
        instructor.setUserId(user.getId());
        instructor = instructorRepository.save(instructor);

        Category category = new Category();
        category.setName("Test");
        category = categoryRepository.save(category);

        CourseRequest req = new CourseRequest();
        req.setCode(UUID.randomUUID().toString());
        req.setTitle("Test");
        req.setDescription("Test");
        req.setThumbnail("localhost:8888");
        req.setPreviewVideo("localhost:8888");
        req.setWhatYouLearn(List.of("Unit test", "Pain"));
        req.setTargetAudiences(List.of("Me", "You"));
        req.setCategoryId(category.getId());
        req.setLevel(CourseLevel.BEGINNER);
        req.setLanguage("VN");
        req.setPrice(BigDecimal.ZERO);
        req.setCurrency(Currency.VND);
        req.setIsFree(true);
        req.setIsPreOrder(false);
        req.setEnrollmentType(EnrollmentType.LIFETIME);

        Course course = courserService.createCourse(req, instructor);

        assertNotNull(course);
        assertEquals("Unit test\nPain", course.getWhatYouLearn());
        assertEquals("Me\nYou", course.getTargetAudiences());
    }

    // TC_CS_02
    @Test
    @Transactional
    void createCourse_testStandard2() {
        User user = User.builder()
                .bio("Test")
                .email("dat@dat.com")
                .phone("0123456789")
                .build();
        user = userRepository.save(user);
        Role role = Role.builder()
                .name(RoleName.INSTRUCTOR).build();
        role = roleRepository.save(role);

        userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(role)
                .build());

        Instructor instructor = new Instructor();
        instructor.setUserId(user.getId());
        instructor = instructorRepository.save(instructor);

        Category category = new Category();
        category.setName("Test");
        category = categoryRepository.save(category);

        CourseRequest req = new CourseRequest();
        req.setCode(UUID.randomUUID().toString());
        req.setTitle("Test");
        req.setDescription("Test");
        req.setThumbnail("localhost:8888");
        req.setPreviewVideo("localhost:8888");
        req.setWhatYouLearn(List.of("Unit test", "Pain"));
        req.setCategoryId(category.getId());
        req.setLevel(CourseLevel.BEGINNER);
        req.setLanguage("VN");
        req.setPrice(BigDecimal.ZERO);
        req.setCurrency(Currency.VND);
        req.setIsFree(true);
        req.setIsPreOrder(false);
        req.setEnrollmentType(EnrollmentType.SUBSCRIPTION);

        Instructor finalInstructor = instructor;
        Exception exception = assertThrows(InvalidParamException.class, () -> {
            Course course = courserService.createCourse(req, finalInstructor);
        });

        assertTrue(exception.getMessage().contains("Expired days must not null"));
    }

    // TC_CS_03
    @Test
    @Transactional
    void createCourse_testStandard3() {
        User user = User.builder()
                .bio("Test")
                .email("dat@dat.com")
                .phone("0123456789")
                .build();
        user = userRepository.save(user);
        Role role = Role.builder()
                .name(RoleName.INSTRUCTOR).build();
        role = roleRepository.save(role);

        userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(role)
                .build());

        Instructor instructor = new Instructor();
        instructor.setUserId(user.getId());
        instructor = instructorRepository.save(instructor);

        Category category = new Category();
        category.setName("Test");
        category = categoryRepository.save(category);

        CourseRequest req = new CourseRequest();
        req.setCode(UUID.randomUUID().toString());
        req.setTitle("Test");
        req.setDescription("Test");
        req.setThumbnail("localhost:8888");
        req.setPreviewVideo("localhost:8888");
        req.setWhatYouLearn(List.of("Unit test", "Pain"));
        req.setCategoryId(category.getId());
        req.setLevel(CourseLevel.BEGINNER);
        req.setLanguage("VN");
        req.setPrice(BigDecimal.ZERO);
        req.setCurrency(Currency.VND);
        req.setIsFree(true);
        req.setIsPreOrder(true);
        req.setEnrollmentType(EnrollmentType.LIFETIME);

        Instructor finalInstructor = instructor;
        Exception exception = assertThrows(InvalidParamException.class, () -> {
            Course course = courserService.createCourse(req, finalInstructor);
        });

        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("Free courses cannot have pre-order"));
    }

    // TC_CS_04
    @Test
    @Transactional
    void createCourse_testStandard4() {
        User user = User.builder()
                .bio("Test")
                .email("dat@dat.com")
                .phone("0123456789")
                .build();
        user = userRepository.save(user);
        Role role = Role.builder()
                .name(RoleName.INSTRUCTOR).build();
        role = roleRepository.save(role);

        userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(role)
                .build());

        Instructor instructor = new Instructor();
        instructor.setUserId(user.getId());
        instructor = instructorRepository.save(instructor);

        Category category = new Category();
        category.setName("Test");
        category = categoryRepository.save(category);

        CourseRequest req = new CourseRequest();
        req.setCode(UUID.randomUUID().toString());
        req.setTitle("Test");
        req.setDescription("Test");
        req.setThumbnail("localhost:8888");
        req.setPreviewVideo("localhost:8888");
        req.setWhatYouLearn(List.of("Unit test", "Pain"));
        req.setCategoryId(category.getId());
        req.setLevel(CourseLevel.BEGINNER);
        req.setLanguage("VN");
        req.setPrice(BigDecimal.ONE);
        req.setCurrency(Currency.VND);
        req.setIsFree(false);
        req.setIsPreOrder(true);
        req.setEnrollmentType(EnrollmentType.LIFETIME);

        Instructor finalInstructor = instructor;
        Exception exception = assertThrows(InvalidParamException.class, () -> {
            Course course = courserService.createCourse(req, finalInstructor);
        });

        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("Pre-order requires start date, end date, price, and total slots"));
    }

    // TC_CS_05
    @Test
    @Transactional
    void createCourse_testStandard5() {
        User user = User.builder()
                .bio("Test")
                .email("dat@dat.com")
                .phone("0123456789")
                .build();
        user = userRepository.save(user);
        Role role = Role.builder()
                .name(RoleName.INSTRUCTOR).build();
        role = roleRepository.save(role);

        userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(role)
                .build());

        Instructor instructor = new Instructor();
        instructor.setUserId(user.getId());
        instructor = instructorRepository.save(instructor);

        Category category = new Category();
        category.setName("Test");
        category = categoryRepository.save(category);

        CourseRequest req = new CourseRequest();
        req.setCode(UUID.randomUUID().toString());
        req.setTitle("Test");
        req.setDescription("Test");
        req.setThumbnail("localhost:8888");
        req.setPreviewVideo("localhost:8888");
        req.setWhatYouLearn(List.of("Unit test", "Pain"));
        req.setCategoryId(category.getId());
        req.setLevel(CourseLevel.BEGINNER);
        req.setLanguage("VN");
        req.setPrice(BigDecimal.ONE);
        req.setCurrency(Currency.VND);
        req.setIsFree(false);
        req.setIsPreOrder(true);
        req.setPreOrderEndDate(LocalDateTime.MIN);
        req.setPreOrderStartDate(LocalDateTime.MAX);
        req.setPreOrderTotalSlots(1);
        req.setPreOrderPrice(BigDecimal.ZERO);
        req.setEnrollmentType(EnrollmentType.LIFETIME);

        Instructor finalInstructor = instructor;
        Exception exception = assertThrows(InvalidParamException.class, () -> {
            Course course = courserService.createCourse(req, finalInstructor);
        });

        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("Pre-order end date must be after start date"));
    }

    // TC_CS_06
    @Test
    @Transactional
    void createCourse_testStandard6() {
        User user = User.builder()
                .bio("Test")
                .email("dat@dat.com")
                .phone("0123456789")
                .build();
        user = userRepository.save(user);
        Role role = Role.builder()
                .name(RoleName.INSTRUCTOR).build();
        role = roleRepository.save(role);

        userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(role)
                .build());

        Instructor instructor = new Instructor();
        instructor.setUserId(user.getId());
        instructor = instructorRepository.save(instructor);

        Category category = new Category();
        category.setName("Test");
        category = categoryRepository.save(category);

        CourseRequest req = new CourseRequest();
        req.setCode(UUID.randomUUID().toString());
        req.setTitle("Test");
        req.setDescription("Test");
        req.setThumbnail("localhost:8888");
        req.setPreviewVideo("localhost:8888");
        req.setWhatYouLearn(List.of("Unit test", "Pain"));
        req.setCategoryId(category.getId());
        req.setLevel(CourseLevel.BEGINNER);
        req.setLanguage("VN");
        req.setPrice(BigDecimal.ONE);
        req.setCurrency(Currency.VND);
        req.setIsFree(false);
        req.setIsPreOrder(true);
        req.setPreOrderEndDate(LocalDateTime.MAX);
        req.setPreOrderStartDate(LocalDateTime.MIN);
        req.setPreOrderTotalSlots(1);
        req.setPreOrderPrice(BigDecimal.ZERO);
        req.setEnrollmentType(EnrollmentType.LIFETIME);

        Instructor finalInstructor = instructor;
        Exception exception = assertThrows(InvalidParamException.class, () -> {
            Course course = courserService.createCourse(req, finalInstructor);
        });

        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("Pre-order price must be less than regular price"));
    }

    // TC_CS_0?
    // Free is true, but price is one
    @Test
    @Transactional
    void createCourse_testException1() {
        User user = User.builder()
                .bio("Test")
                .email("dat@dat.com")
                .phone("0123456789")
                .build();
        user = userRepository.save(user);
        Role role = Role.builder()
                .name(RoleName.INSTRUCTOR).build();
        role = roleRepository.save(role);

        userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(role)
                .build());

        Instructor instructor = new Instructor();
        instructor.setUserId(user.getId());
        instructor = instructorRepository.save(instructor);

        Category category = new Category();
        category.setName("Test");
        category = categoryRepository.save(category);

        CourseRequest req = new CourseRequest();
        req.setCode(UUID.randomUUID().toString());
        req.setTitle("Test");
        req.setDescription("Test");
        req.setThumbnail("localhost:8888");
        req.setPreviewVideo("localhost:8888");
        req.setWhatYouLearn(List.of("Unit test", "Pain"));
        req.setCategoryId(category.getId());
        req.setLevel(CourseLevel.BEGINNER);
        req.setLanguage("VN");
        req.setPrice(BigDecimal.ONE);
        req.setCurrency(Currency.VND);
        req.setIsFree(true);
        req.setIsPreOrder(false);
        req.setEnrollmentType(EnrollmentType.LIFETIME);

        Instructor finalInstructor = instructor;
        assertThrows(Exception.class, () -> {
            Course course = courserService.createCourse(req, finalInstructor);
        });
    }

    // TC_CS_0?
    // Free is false, but price is 0
    @Test
    @Transactional
    void createCourse_testException2() {
        User user = User.builder()
                .bio("Test")
                .email("dat@dat.com")
                .phone("0123456789")
                .build();
        user = userRepository.save(user);
        Role role = Role.builder()
                .name(RoleName.INSTRUCTOR).build();
        role = roleRepository.save(role);

        userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(role)
                .build());

        Instructor instructor = new Instructor();
        instructor.setUserId(user.getId());
        instructor = instructorRepository.save(instructor);

        Category category = new Category();
        category.setName("Test");
        category = categoryRepository.save(category);

        CourseRequest req = new CourseRequest();
        req.setCode(UUID.randomUUID().toString());
        req.setTitle("Test");
        req.setDescription("Test");
        req.setThumbnail("localhost:8888");
        req.setPreviewVideo("localhost:8888");
        req.setWhatYouLearn(List.of("Unit test", "Pain"));
        req.setCategoryId(category.getId());
        req.setLevel(CourseLevel.BEGINNER);
        req.setLanguage("VN");
        req.setPrice(BigDecimal.ZERO);
        req.setCurrency(Currency.VND);
        req.setIsFree(false);
        req.setIsPreOrder(false);
        req.setEnrollmentType(EnrollmentType.LIFETIME);

        Instructor finalInstructor = instructor;
        assertThrows(Exception.class, () -> {
            Course course = courserService.createCourse(req, finalInstructor);
        });
    }

    @Test
    @Transactional
    void getCourseById_testStandard1() {
        User user = User.builder()
                .bio("Test")
                .email("dat@dat.com")
                .phone("0123456789")
                .build();
        user = userRepository.save(user);
        Role role = Role.builder()
                .name(RoleName.INSTRUCTOR).build();
        role = roleRepository.save(role);

        userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(role)
                .build());

        Instructor instructor = new Instructor();
        instructor.setUserId(user.getId());
        instructor = instructorRepository.save(instructor);

        Category category = new Category();
        category.setName("Test");
        category = categoryRepository.save(category);

        Course course = new Course();
        course.setCode(UUID.randomUUID().toString());
        course.setTitle("Test");
        course.setDescription("Test");
        course.setThumbnail("localhost:8888");
        course.setPreviewVideo("localhost:8888");
        course.setInstructorId(instructor.getId());
        course.setCategoryId(category.getId());
        course.setLevel(CourseLevel.BEGINNER);
        course.setLanguage("VN");
        course.setPrice(BigDecimal.ONE);
        course.setCurrency(Currency.VND);
        course.setStatus(CourseStatus.DRAFT);
        course.setIsFree(true);
        course.setIsPreOrder(false);
        course.setEnrollmentType(EnrollmentType.LIFETIME);

        course = courseRepository.save(course);
        System.out.println("Course ID: " + course.getId());

        Course finalCourse = course;
        assertDoesNotThrow(() -> {
            Course gotCourse = courserService.getCourseById(finalCourse.getId());
        });
    }

    @Test
    @Transactional
    void getCourseById_testException1() {
        User user = User.builder()
                .bio("Test")
                .email("dat@dat.com")
                .phone("0123456789")
                .build();
        user = userRepository.save(user);
        Role role = Role.builder()
                .name(RoleName.INSTRUCTOR).build();
        role = roleRepository.save(role);

        userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(role)
                .build());

        Instructor instructor = new Instructor();
        instructor.setUserId(user.getId());
        instructor = instructorRepository.save(instructor);

        Category category = new Category();
        category.setName("Test");
        category = categoryRepository.save(category);

        Course course = new Course();
        course.setCode(UUID.randomUUID().toString());
        course.setTitle("Test");
        course.setDescription("Test");
        course.setThumbnail("localhost:8888");
        course.setPreviewVideo("localhost:8888");
        course.setInstructorId(instructor.getId());
        course.setCategoryId(category.getId());
        course.setLevel(CourseLevel.BEGINNER);
        course.setLanguage("VN");
        course.setPrice(BigDecimal.ONE);
        course.setCurrency(Currency.VND);
        course.setStatus(CourseStatus.DRAFT);
        course.setIsFree(true);
        course.setIsPreOrder(false);
        course.setEnrollmentType(EnrollmentType.LIFETIME);

        course = courseRepository.save(course);
        System.out.println("Course ID: " + course.getId());

        assertThrows(DataNotFoundException.class, () -> {
            Course gotCourse = courserService.getCourseById(0L);
        });
    }

    @Test
    @Transactional
    void updateCourse_testStandard1() {
            User user = User.builder()
                            .bio("Test")
                            .email("update-course-1@dat.com")
                            .phone("0123456789")
                            .build();
            user = userRepository.save(user);
            Role role = Role.builder()
                            .name(RoleName.INSTRUCTOR).build();
            role = roleRepository.save(role);
            userRoleRepository.save(UserRole.builder().user(user).role(role).build());

            Instructor instructor = new Instructor();
            instructor.setUserId(user.getId());
            instructor = instructorRepository.save(instructor);

            Category category = new Category();
            category.setName("Test");
            category = categoryRepository.save(category);

            Course course = new Course();
            course.setCode(UUID.randomUUID().toString());
            course.setTitle("Old title");
            course.setDescription("Old description");
            course.setThumbnail("localhost:8888");
            course.setPreviewVideo("localhost:8888");
            course.setInstructorId(instructor.getId());
            course.setCategoryId(category.getId());
            course.setLevel(CourseLevel.BEGINNER);
            course.setLanguage("VN");
            course.setPrice(BigDecimal.ONE);
            course.setCurrency(Currency.VND);
            course.setStatus(CourseStatus.DRAFT);
            course.setIsFree(false);
            course.setIsPreOrder(false);
            course.setEnrollmentType(EnrollmentType.LIFETIME);
            course = courseRepository.save(course);

            UpdateCourseRequest req = new UpdateCourseRequest();
            req.setTitle("New title");
            req.setDescription("New description");
            req.setLanguage("EN");
            req.setPrice(BigDecimal.TEN);
            req.setIsFree(false);
            req.setWhatYouLearn(List.of("A", "B"));
            req.setTargetAudiences(List.of("X", "Y"));
            req.setEnrollmentType(EnrollmentType.LIFETIME);

            Course updatedCourse = courserService.updateCourse(course.getId(), req, instructor);

            assertNotNull(updatedCourse);
            assertEquals("New title", updatedCourse.getTitle());
            assertEquals("New description", updatedCourse.getDescription());
            assertEquals("EN", updatedCourse.getLanguage());
            assertEquals(BigDecimal.TEN, updatedCourse.getPrice());
            assertEquals("A\nB", updatedCourse.getWhatYouLearn());
            assertEquals("X\nY", updatedCourse.getTargetAudiences());
            assertEquals(EnrollmentType.LIFETIME, updatedCourse.getEnrollmentType());
    }

    @Test
    @Transactional
    void updateCourse_testException_courseNotFound() {
            User user = User.builder()
                            .bio("Test")
                            .email("update-course-2@dat.com")
                            .phone("0123456789")
                            .build();
            user = userRepository.save(user);
            Role role = Role.builder()
                            .name(RoleName.INSTRUCTOR).build();
            role = roleRepository.save(role);
            userRoleRepository.save(UserRole.builder().user(user).role(role).build());

            Instructor instructor = new Instructor();
            instructor.setUserId(user.getId());
            instructor = instructorRepository.save(instructor);

            UpdateCourseRequest req = new UpdateCourseRequest();
            req.setTitle("New title");

        Instructor finalInstructor = instructor;
        assertThrows(DataNotFoundException.class, () -> {
                    courserService.updateCourse(0L, req, finalInstructor);
            });
    }

    @Test
    @Transactional
    void updateCourse_testException_forbiddenInstructor() {
            User ownerUser = User.builder()
                            .bio("Test")
                            .email("update-course-3@dat.com")
                            .phone("0123456789")
                            .build();
            ownerUser = userRepository.save(ownerUser);
            Role role = Role.builder()
                            .name(RoleName.INSTRUCTOR).build();
            role = roleRepository.save(role);
            userRoleRepository.save(UserRole.builder().user(ownerUser).role(role).build());

            Instructor ownerInstructor = new Instructor();
            ownerInstructor.setUserId(ownerUser.getId());
            ownerInstructor = instructorRepository.save(ownerInstructor);

            User anotherUser = User.builder()
                            .bio("Test")
                            .email("update-course-4@dat.com")
                            .phone("0123456790")
                            .build();
            anotherUser = userRepository.save(anotherUser);
            userRoleRepository.save(UserRole.builder().user(anotherUser).role(role).build());

            Instructor anotherInstructor = new Instructor();
            anotherInstructor.setUserId(anotherUser.getId());
            anotherInstructor = instructorRepository.save(anotherInstructor);

            Category category = new Category();
            category.setName("Test");
            category = categoryRepository.save(category);

            Course course = new Course();
            course.setCode(UUID.randomUUID().toString());
            course.setTitle("Old title");
            course.setDescription("Old description");
            course.setThumbnail("localhost:8888");
            course.setPreviewVideo("localhost:8888");
            course.setInstructorId(ownerInstructor.getId());
            course.setCategoryId(category.getId());
            course.setLevel(CourseLevel.BEGINNER);
            course.setLanguage("VN");
            course.setPrice(BigDecimal.ONE);
            course.setCurrency(Currency.VND);
            course.setStatus(CourseStatus.DRAFT);
            course.setIsFree(false);
            course.setIsPreOrder(false);
            course.setEnrollmentType(EnrollmentType.LIFETIME);
            course = courseRepository.save(course);

            UpdateCourseRequest req = new UpdateCourseRequest();
            req.setTitle("Forbidden update");

        Instructor finalAnotherInstructor = anotherInstructor;
        Course finalCourse = course;
        assertThrows(AccessDeniedException.class, () -> {
                    courserService.updateCourse(finalCourse.getId(), req, finalAnotherInstructor);
            });
    }

    @Test
    @Transactional
    void updateCourse_testException_duplicateCode() {
            User user = User.builder()
                            .bio("Test")
                            .email("update-course-5@dat.com")
                            .phone("0123456789")
                            .build();
            user = userRepository.save(user);
            Role role = Role.builder()
                            .name(RoleName.INSTRUCTOR).build();
            role = roleRepository.save(role);
            userRoleRepository.save(UserRole.builder().user(user).role(role).build());

            Instructor instructor = new Instructor();
            instructor.setUserId(user.getId());
            instructor = instructorRepository.save(instructor);

            Category category = new Category();
            category.setName("Test");
            category = categoryRepository.save(category);

            Course course1 = new Course();
            course1.setCode(UUID.randomUUID().toString());
            course1.setTitle("Course 1");
            course1.setDescription("Test");
            course1.setThumbnail("localhost:8888");
            course1.setPreviewVideo("localhost:8888");
            course1.setInstructorId(instructor.getId());
            course1.setCategoryId(category.getId());
            course1.setLevel(CourseLevel.BEGINNER);
            course1.setLanguage("VN");
            course1.setPrice(BigDecimal.ONE);
            course1.setCurrency(Currency.VND);
            course1.setStatus(CourseStatus.DRAFT);
            course1.setIsFree(false);
            course1.setIsPreOrder(false);
            course1.setEnrollmentType(EnrollmentType.LIFETIME);
            course1 = courseRepository.save(course1);

            String duplicatedCode = UUID.randomUUID().toString();
            Course course2 = new Course();
            course2.setCode(duplicatedCode);
            course2.setTitle("Course 2");
            course2.setDescription("Test");
            course2.setThumbnail("localhost:8888");
            course2.setPreviewVideo("localhost:8888");
            course2.setInstructorId(instructor.getId());
            course2.setCategoryId(category.getId());
            course2.setLevel(CourseLevel.BEGINNER);
            course2.setLanguage("VN");
            course2.setPrice(BigDecimal.ONE);
            course2.setCurrency(Currency.VND);
            course2.setStatus(CourseStatus.DRAFT);
            course2.setIsFree(false);
            course2.setIsPreOrder(false);
            course2.setEnrollmentType(EnrollmentType.LIFETIME);
            courseRepository.save(course2);

            UpdateCourseRequest req = new UpdateCourseRequest();
            req.setCode(duplicatedCode);

        Course finalCourse = course1;
        Instructor finalInstructor = instructor;
        assertThrows(InvalidParamException.class, () -> {
                    courserService.updateCourse(finalCourse.getId(), req, finalInstructor);
            });
    }

    @Test
    @Transactional
    void updateCourse_testException_subscriptionWithoutExpiredDays() {
            User user = User.builder()
                            .bio("Test")
                            .email("update-course-6@dat.com")
                            .phone("0123456789")
                            .build();
            user = userRepository.save(user);
            Role role = Role.builder()
                            .name(RoleName.INSTRUCTOR).build();
            role = roleRepository.save(role);
            userRoleRepository.save(UserRole.builder().user(user).role(role).build());

            Instructor instructor = new Instructor();
            instructor.setUserId(user.getId());
            instructor = instructorRepository.save(instructor);

            Category category = new Category();
            category.setName("Test");
            category = categoryRepository.save(category);

            Course course = new Course();
            course.setCode(UUID.randomUUID().toString());
            course.setTitle("Old title");
            course.setDescription("Old description");
            course.setThumbnail("localhost:8888");
            course.setPreviewVideo("localhost:8888");
            course.setInstructorId(instructor.getId());
            course.setCategoryId(category.getId());
            course.setLevel(CourseLevel.BEGINNER);
            course.setLanguage("VN");
            course.setPrice(BigDecimal.ONE);
            course.setCurrency(Currency.VND);
            course.setStatus(CourseStatus.DRAFT);
            course.setIsFree(false);
            course.setIsPreOrder(false);
            course.setEnrollmentType(EnrollmentType.LIFETIME);
            course.setExpiredDays(null);
            course = courseRepository.save(course);

            UpdateCourseRequest req = new UpdateCourseRequest();
            req.setEnrollmentType(EnrollmentType.SUBSCRIPTION);

        Instructor finalInstructor = instructor;
        Course finalCourse = course;
        InvalidParamException exception = assertThrows(InvalidParamException.class, () -> {
                    courserService.updateCourse(finalCourse.getId(), req, finalInstructor);
            });

            assertTrue(exception.getMessage().contains("Expired days must not be null"));
    }

    @Test
    @Transactional
    void deleteCourse_testStandard_ownerCanDelete() {
        User user = User.builder()
                .bio("Test")
                .email("delete-course-owner@dat.com")
                .phone("0123456789")
                .build();
        user = userRepository.save(user);

        Role instructorRole = Role.builder()
                .name(RoleName.INSTRUCTOR)
                .build();
        instructorRole = roleRepository.save(instructorRole);

        UserRole userRole = userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(instructorRole)
                .build());

        Instructor instructor = new Instructor();
        instructor.setUserId(user.getId());
        instructor = instructorRepository.save(instructor);

        user.setUserRoles(Set.of(userRole));
        user.setInstructor(instructor);

        Category category = new Category();
        category.setName("Delete Category 1");
        category = categoryRepository.save(category);

        Course course = new Course();
        course.setCode(UUID.randomUUID().toString());
        course.setTitle("Delete me");
        course.setDescription("Delete me");
        course.setThumbnail("localhost:8888");
        course.setPreviewVideo("localhost:8888");
        course.setInstructorId(instructor.getId());
        course.setCategoryId(category.getId());
        course.setLevel(CourseLevel.BEGINNER);
        course.setLanguage("VN");
        course.setPrice(BigDecimal.ONE);
        course.setCurrency(Currency.VND);
        course.setStatus(CourseStatus.DRAFT);
        course.setIsFree(false);
        course.setIsPreOrder(false);
        course.setEnrollmentType(EnrollmentType.LIFETIME);
        course = courseRepository.save(course);

        Long courseId = course.getId();
        courserService.deleteCourse(courseId, user);

        assertFalse(courseRepository.findById(courseId).isPresent());
    }

    @Test
    @Transactional
    void deleteCourse_testStandard_adminCanDelete() {
        User admin = User.builder()
                .bio("Test")
                .email("delete-course-admin@dat.com")
                .phone("0123456790")
                .build();
        admin = userRepository.save(admin);

        Role adminRole = Role.builder()
                .name(RoleName.ADMIN)
                .build();
        adminRole = roleRepository.save(adminRole);

        UserRole adminUserRole = userRoleRepository.save(UserRole.builder()
                .user(admin)
                .role(adminRole)
                .build());
        admin.setUserRoles(Set.of(adminUserRole));

        User owner = User.builder()
                .bio("Test")
                .email("delete-course-owner-2@dat.com")
                .phone("0123456791")
                .build();
        owner = userRepository.save(owner);

        Role instructorRole = Role.builder()
                .name(RoleName.INSTRUCTOR)
                .build();
        instructorRole = roleRepository.save(instructorRole);

        userRoleRepository.save(UserRole.builder()
                .user(owner)
                .role(instructorRole)
                .build());

        Instructor ownerInstructor = new Instructor();
        ownerInstructor.setUserId(owner.getId());
        ownerInstructor = instructorRepository.save(ownerInstructor);

        Category category = new Category();
        category.setName("Delete Category 2");
        category = categoryRepository.save(category);

        Course course = new Course();
        course.setCode(UUID.randomUUID().toString());
        course.setTitle("Delete me too");
        course.setDescription("Delete me too");
        course.setThumbnail("localhost:8888");
        course.setPreviewVideo("localhost:8888");
        course.setInstructorId(ownerInstructor.getId());
        course.setCategoryId(category.getId());
        course.setLevel(CourseLevel.BEGINNER);
        course.setLanguage("VN");
        course.setPrice(BigDecimal.ONE);
        course.setCurrency(Currency.VND);
        course.setStatus(CourseStatus.DRAFT);
        course.setIsFree(false);
        course.setIsPreOrder(false);
        course.setEnrollmentType(EnrollmentType.LIFETIME);
        course = courseRepository.save(course);

        Long courseId = course.getId();
        courserService.deleteCourse(courseId, admin);

        assertFalse(courseRepository.findById(courseId).isPresent());
    }

    @Test
    @Transactional
    void deleteCourse_testException_forbiddenUser() {
        User owner = User.builder()
                .bio("Test")
                .email("delete-course-owner-3@dat.com")
                .phone("0123456792")
                .build();
        owner = userRepository.save(owner);

        Role instructorRole = Role.builder()
                .name(RoleName.INSTRUCTOR)
                .build();
        instructorRole = roleRepository.save(instructorRole);

        userRoleRepository.save(UserRole.builder()
                .user(owner)
                .role(instructorRole)
                .build());

        Instructor ownerInstructor = new Instructor();
        ownerInstructor.setUserId(owner.getId());
        ownerInstructor = instructorRepository.save(ownerInstructor);

        User anotherUser = User.builder()
                .bio("Test")
                .email("delete-course-non-owner@dat.com")
                .phone("0123456793")
                .build();
        anotherUser = userRepository.save(anotherUser);

        UserRole anotherUserRole = userRoleRepository.save(UserRole.builder()
                .user(anotherUser)
                .role(instructorRole)
                .build());

        Instructor anotherInstructor = new Instructor();
        anotherInstructor.setUserId(anotherUser.getId());
        anotherInstructor = instructorRepository.save(anotherInstructor);

        anotherUser.setUserRoles(Set.of(anotherUserRole));
        anotherUser.setInstructor(anotherInstructor);

        Category category = new Category();
        category.setName("Delete Category 3");
        category = categoryRepository.save(category);

        Course course = new Course();
        course.setCode(UUID.randomUUID().toString());
        course.setTitle("Forbidden delete");
        course.setDescription("Forbidden delete");
        course.setThumbnail("localhost:8888");
        course.setPreviewVideo("localhost:8888");
        course.setInstructorId(ownerInstructor.getId());
        course.setCategoryId(category.getId());
        course.setLevel(CourseLevel.BEGINNER);
        course.setLanguage("VN");
        course.setPrice(BigDecimal.ONE);
        course.setCurrency(Currency.VND);
        course.setStatus(CourseStatus.DRAFT);
        course.setIsFree(false);
        course.setIsPreOrder(false);
        course.setEnrollmentType(EnrollmentType.LIFETIME);
        course = courseRepository.save(course);

        Long courseId = course.getId();
        User finalAnotherUser = anotherUser;
        assertThrows(AccessDeniedException.class, () -> courserService.deleteCourse(courseId, finalAnotherUser));
    }

    @Test
    @Transactional
    void deleteCourse_testException_courseNotFound() {
        User user = User.builder()
                .bio("Test")
                .email("delete-course-not-found@dat.com")
                .phone("0123456794")
                .build();
        user = userRepository.save(user);

        Role instructorRole = Role.builder()
                .name(RoleName.INSTRUCTOR)
                .build();
        instructorRole = roleRepository.save(instructorRole);

        UserRole userRole = userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(instructorRole)
                .build());

        Instructor instructor = new Instructor();
        instructor.setUserId(user.getId());
        instructor = instructorRepository.save(instructor);

        user.setUserRoles(Set.of(userRole));
        user.setInstructor(instructor);

        User finalUser = user;
        assertThrows(DataNotFoundException.class, () -> courserService.deleteCourse(0L, finalUser));
    }
}
