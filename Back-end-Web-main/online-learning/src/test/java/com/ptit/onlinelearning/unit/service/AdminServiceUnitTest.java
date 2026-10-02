package com.ptit.onlinelearning.unit.service;

import com.ptit.onlinelearning.common.type.*;
import com.ptit.onlinelearning.model.*;
import com.ptit.onlinelearning.repository.*;
import com.ptit.onlinelearning.response.SystemStatisticsResponse;
import com.ptit.onlinelearning.service.admin.AdminService;
import com.ptit.onlinelearning.unit.config.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AdminServiceUnitTest extends BaseUnitTest {
    @Autowired
    AdminService adminService;

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRoleRepository userRoleRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    InstructorRepository instructorRepository;

    @Test
    @Transactional
    void getSystemStatistics_testStandard1() {
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
        course.setPrice(BigDecimal.ZERO);
        course.setCurrency(Currency.VND);
        course.setStatus(CourseStatus.DRAFT);
        course.setIsFree(true);
        course.setIsPreOrder(false);
        course.setEnrollmentType(EnrollmentType.LIFETIME);

        courseRepository.save(course);

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setTotalMoney(BigDecimal.TWO);
        order.setCurrency(Currency.VND);
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setOrderDate(LocalDateTime.now());
        orderRepository.save(order);

        SystemStatisticsResponse res = adminService.getSystemStatistics();

        assertEquals(1L, res.getTotalUsers());
        assertEquals(1L, res.getTotalInstructors());
        assertEquals(BigDecimal.TWO, res.getTotalRevenue());
        // System income is 30% of total revenue
        assertEquals(new BigDecimal("0.60"), res.getSystemIncome());
        assertEquals(1L, res.getTotalCourses());
        assertEquals(1L, res.getTotalOrders());
        assertEquals(1L, res.getTotalSuccessOrders());
    }
}
