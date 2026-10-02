// package com.ptit.onlinelearning.unit.service;

// import com.ptit.onlinelearning.common.type.CourseLevel;
// import com.ptit.onlinelearning.common.type.CourseStatus;
// import com.ptit.onlinelearning.common.type.Currency;
// import com.ptit.onlinelearning.common.type.EnrollmentType;
// import com.ptit.onlinelearning.exception.DataNotFoundException;
// import com.ptit.onlinelearning.model.Course;
// import com.ptit.onlinelearning.model.CourseGroup;
// import com.ptit.onlinelearning.model.CourseModule;
// import com.ptit.onlinelearning.model.Enrollment;
// import com.ptit.onlinelearning.model.Instructor;
// import com.ptit.onlinelearning.model.Lesson;
// import com.ptit.onlinelearning.model.LessonProgress;
// import com.ptit.onlinelearning.model.User;
// import com.ptit.onlinelearning.repository.CourseGroupRepository;
// import com.ptit.onlinelearning.repository.CourseModuleRepository;
// import com.ptit.onlinelearning.repository.CourseRepository;
// import com.ptit.onlinelearning.repository.EnrollmentRepository;
// import com.ptit.onlinelearning.repository.InstructorRepository;
// import com.ptit.onlinelearning.repository.LessonProgressRepository;
// import com.ptit.onlinelearning.repository.LessonRepository;
// import com.ptit.onlinelearning.repository.UserRepository;
// import com.ptit.onlinelearning.request.CourseModuleRequest;
// import com.ptit.onlinelearning.request.UpdateCourseModuleRequest;
// import com.ptit.onlinelearning.response.PageableResponse;
// import com.ptit.onlinelearning.response.course.CourseModuleResponse;
// import com.ptit.onlinelearning.service.coursemodule.CourseModuleService;
// import com.ptit.onlinelearning.unit.config.BaseUnitTest;
// import jakarta.persistence.EntityManager;
// import jakarta.persistence.PersistenceContext;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.domain.Page;
// import org.springframework.security.access.AccessDeniedException;
// import org.springframework.transaction.annotation.Transactional;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.stream.Collectors;

// import static org.junit.jupiter.api.Assertions.*;

// public class CourseModuleServiceUnitTest extends BaseUnitTest {

//     @PersistenceContext
//     private EntityManager entityManager;

//     @Autowired
//     private CourseModuleService courseModuleService;

//     @Autowired
//     private CourseModuleRepository courseModuleRepository;

//     @Autowired
//     private CourseRepository courseRepository;

//     @Autowired
//     private CourseGroupRepository courseGroupRepository;

//     @Autowired
//     private LessonRepository lessonRepository;

//     @Autowired
//     private LessonProgressRepository lessonProgressRepository;

//     @Autowired
//     private EnrollmentRepository enrollmentRepository;

//     @Autowired
//     private InstructorRepository instructorRepository;

//     @Autowired
//     private UserRepository userRepository;

//     @Test
//     @Transactional
//     public void createCourseModule_testStandard1() {
//         // Tao module khong co lessons
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());

//         CourseModuleRequest request = new CourseModuleRequest();
//         request.setTitle("Module co ban");
//         request.setDescription("Module dau tien");
//         request.setSortOrder(1);
//         request.setCourseId(course.getId());
//         request.setLessonDTOs(null);

//         CourseModule result = courseModuleService.createCourseModule(request, ownerInstructor);

//         assertNotNull(result);
//         assertNotNull(result.getId());
//         assertEquals(course.getId(), result.getCourseId());
//         assertEquals("Module co ban", result.getTitle());
//         assertEquals(1, result.getSortOrder());
//         assertTrue(courseModuleRepository.existsById(result.getId()));

//         List<Lesson> createdLessons = lessonRepository.findAll().stream()
//                 .filter(lesson -> lesson.getModuleId().equals(result.getId()))
//                 .toList();
//         assertEquals(0, createdLessons.size());
//     }

//     @Test
//     @Transactional
//     public void createCourseModule_testStandard2() {
//         // Tao module co 2 lessons
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());

//         CourseModuleRequest.LessonDTO lesson1 = new CourseModuleRequest.LessonDTO();
//         lesson1.setTitle("Bai 1");
//         lesson1.setDescription("Video introduction");
//         lesson1.setContentType("video");
//         lesson1.setVideoUrl("https://example.com/video-1");
//         lesson1.setDuration(120L);
//         lesson1.setSortOrder(1);
//         lesson1.setIsMandatory(true);

//         CourseModuleRequest.LessonDTO lesson2 = new CourseModuleRequest.LessonDTO();
//         lesson2.setTitle("Bai 2");
//         lesson2.setDescription("Tai lieu");
//         lesson2.setContentType("document");
//         lesson2.setDocumentUrl("https://example.com/doc-2");
//         lesson2.setDuration(60L);
//         lesson2.setSortOrder(2);
//         lesson2.setIsMandatory(false);

//         CourseModuleRequest request = new CourseModuleRequest();
//         request.setTitle("Module nang cao");
//         request.setDescription("Co bai hoc");
//         request.setSortOrder(2);
//         request.setCourseId(course.getId());
//         request.setLessonDTOs(List.of(lesson1, lesson2));

//         CourseModule result = courseModuleService.createCourseModule(request, ownerInstructor);

//         assertNotNull(result);
//         assertNotNull(result.getId());

//         List<Lesson> createdLessons = lessonRepository.findAll().stream()
//                 .filter(lesson -> lesson.getModuleId().equals(result.getId()))
//                 .collect(Collectors.toList());

//         assertEquals(2, createdLessons.size());
//         assertTrue(createdLessons.stream().anyMatch(lesson -> "Bai 1".equals(lesson.getTitle())));
//         assertTrue(createdLessons.stream().anyMatch(lesson -> "Bai 2".equals(lesson.getTitle())));
//     }

//     @Test
//     @Transactional
//     public void createCourseModule_testException1() {
//         // CourseId khong ton tai
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());

//         CourseModuleRequest request = new CourseModuleRequest();
//         request.setTitle("Invalid course");
//         request.setDescription("Course not found case");
//         request.setSortOrder(1);
//         request.setCourseId(Long.MAX_VALUE);

//         Exception ex = assertThrows(DataNotFoundException.class,
//                 () -> courseModuleService.createCourseModule(request, ownerInstructor));

//         assertTrue(ex.getMessage().contains("Course not found"));
//     }

//     @Test
//     @Transactional
//     public void createCourseModule_testException2() {
//         // Instructor khong phai owner cua course
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());

//         User otherUser = createUserSeed();
//         Instructor otherInstructor = createInstructorSeed(otherUser.getId());

//         CourseModuleRequest request = new CourseModuleRequest();
//         request.setTitle("Forbidden create");
//         request.setDescription("Access denied case");
//         request.setSortOrder(1);
//         request.setCourseId(course.getId());

//         Exception ex = assertThrows(AccessDeniedException.class,
//                 () -> courseModuleService.createCourseModule(request, otherInstructor));

//         assertTrue(ex.getMessage().contains("Forbidden"));
//     }

//     @Test
//     @Transactional
//     public void getCourseModuleById_testStandard1() {
//         // Lay course module theo id hop le
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Find me", "desc", 1, false);

//         entityManager.flush();
//         entityManager.clear();

//         CourseModule result = courseModuleService.getCourseModuleById(module.getId());

//         assertNotNull(result);
//         assertEquals(module.getId(), result.getId());
//         assertEquals("Find me", result.getTitle());
//     }

//     @Test
//     @Transactional
//     public void getCourseModuleById_testException1() {
//         // Id khong ton tai
//         Exception ex = assertThrows(RuntimeException.class,
//                 () -> courseModuleService.getCourseModuleById(Long.MAX_VALUE));

//         assertTrue(ex.getMessage().contains("Course Module not found"));
//     }

//     @Test
//     @Transactional
//     public void updateCourseModule_testStandard1() {
//         // Owner update title, description, sortOrder
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Old title", "Old desc", 1, false);

//         entityManager.flush();
//         entityManager.clear();

//         UpdateCourseModuleRequest request = new UpdateCourseModuleRequest();
//         request.setTitle("New title");
//         request.setDescription("New desc");
//         request.setSortOrder(9);

//         CourseModule updated = courseModuleService.updateCourseModule(module.getId(), request, ownerInstructor);

//         assertNotNull(updated);
//         assertEquals("New title", updated.getTitle());
//         assertEquals("New desc", updated.getDescription());
//         assertEquals(9, updated.getSortOrder());
//     }

//     @Test
//     @Transactional
//     public void updateCourseModule_testException1() {
//         // Instructor khac khong duoc update
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Old title", "Old desc", 1, false);

//         User otherUser = createUserSeed();
//         Instructor otherInstructor = createInstructorSeed(otherUser.getId());

//         entityManager.flush();
//         entityManager.clear();

//         UpdateCourseModuleRequest request = new UpdateCourseModuleRequest();
//         request.setTitle("Blocked title");

//         Exception ex = assertThrows(AccessDeniedException.class,
//                 () -> courseModuleService.updateCourseModule(module.getId(), request, otherInstructor));

//         assertTrue(ex.getMessage().contains("Forbidden"));
//     }

//     @Test
//     @Transactional
//     public void getCourseModules_testStandard1() {
//         // Filter theo courseId + sort asc
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course targetCourse = createCourseSeed(ownerInstructor.getId());
//         Course otherCourse = createCourseSeed(ownerInstructor.getId());

//         createCourseModuleSeed(targetCourse.getId(), "Alpha", "target", 1, false);
//         createCourseModuleSeed(targetCourse.getId(), "Beta", "target", 2, true);
//         createCourseModuleSeed(otherCourse.getId(), "Other", "other", 1, false);

//         Page<CourseModule> page = courseModuleService.getCourseModules(
//                 targetCourse.getId(), 1, 10, null, null, "sortOrder", "ASC");

//         assertNotNull(page);
//         assertEquals(2, page.getTotalElements());
//         assertEquals("Alpha", page.getContent().get(0).getTitle());
//         assertEquals("Beta", page.getContent().get(1).getTitle());
//     }

//     @Test
//     @Transactional
//     public void getCourseModules_testStandard2() {
//         // Filter theo isPreview + search
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());

//         createCourseModuleSeed(course.getId(), "Preview module", "intro", 1, true);
//         createCourseModuleSeed(course.getId(), "Hidden module", "intro", 2, false);

//         Page<CourseModule> page = courseModuleService.getCourseModules(
//                 course.getId(), 1, 10, true, "preview", "id", "DESC");

//         assertNotNull(page);
//         assertEquals(1, page.getTotalElements());
//         assertEquals("Preview module", page.getContent().get(0).getTitle());
//     }

//     @Test
//     @Transactional
//     public void getCourseModules_testException1() {
//         // page = 0 => page index am
//         assertThrows(IllegalArgumentException.class,
//                 () -> courseModuleService.getCourseModules(null, 0, 10, null, null, "id", "ASC"));
//     }

//     @Test
//     @Transactional
//     public void getCourseModules_testException2() {
//         // sort order khong hop le
//         assertThrows(IllegalArgumentException.class,
//                 () -> courseModuleService.getCourseModules(null, 1, 10, null, null, "id", "INVALID"));
//     }

//     @Test
//     @Transactional
//     public void deleteCourseModule_testStandard1() {
//         // Owner xoa module thanh cong
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Delete me", "desc", 1, false);

//         entityManager.flush();
//         entityManager.clear();

//         courseModuleService.deleteCourseModule(module.getId(), ownerInstructor);

//         assertFalse(courseModuleRepository.existsById(module.getId()));
//     }

//     @Test
//     @Transactional
//     public void deleteCourseModule_testException1() {
//         // Instructor khac khong duoc xoa
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Delete blocked", "desc", 1, false);

//         User otherUser = createUserSeed();
//         Instructor otherInstructor = createInstructorSeed(otherUser.getId());

//         entityManager.flush();
//         entityManager.clear();

//         Exception ex = assertThrows(AccessDeniedException.class,
//                 () -> courseModuleService.deleteCourseModule(module.getId(), otherInstructor));

//         assertTrue(ex.getMessage().contains("Forbidden"));
//         assertTrue(courseModuleRepository.existsById(module.getId()));
//     }

//     @Test
//     @Transactional
//     public void getAllCourseModulesOfUserEnrolledInCourse_testStandard1() {
//         // Co 2 lesson: 1 lesson completed, 1 lesson chua completed
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         CourseGroup group = createCourseGroupSeed();
//         Course course = createCourseSeed(ownerInstructor.getId());
//         course.setCourseGroup(group);
//         course = courseRepository.save(course);

//         User learner = createUserSeed();
//         Enrollment enrollment = createEnrollmentSeed(learner, course, group);

//         CourseModule module = createCourseModuleSeed(course.getId(), "Module progress", "desc", 1, false);
//         Lesson lessonCompleted = createLessonSeed(module.getId(), "Completed lesson", 1);
//         Lesson lessonNotCompleted = createLessonSeed(module.getId(), "Not completed lesson", 2);
//         createLessonProgressSeed(lessonCompleted.getId(), learner.getId(), enrollment.getId());

//         entityManager.flush();
//         entityManager.clear();

//         PageableResponse<CourseModuleResponse> response = courseModuleService.getAllCourseModulesOfUserEnrolledInCourse(
//                 learner.getId(), enrollment.getId(), course.getId(), 1, 10, "sortOrder", "ASC");

//         assertNotNull(response);
//         assertEquals(1, response.getData().size());
//         assertEquals(1, response.getCurrentPage());
//         assertEquals(10, response.getPageSize());

//         CourseModuleResponse moduleResponse = response.getData().get(0);
//         assertEquals(module.getId(), moduleResponse.getId());
//         assertEquals(2, moduleResponse.getLessons().size());

//         assertTrue(moduleResponse.getLessons().stream()
//                 .anyMatch(l -> l.getId().equals(lessonCompleted.getId()) && Boolean.TRUE.equals(l.getIsCompleted())));

//         assertTrue(moduleResponse.getLessons().stream()
//                 .anyMatch(l -> l.getId().equals(lessonNotCompleted.getId()) && Boolean.FALSE.equals(l.getIsCompleted())));
//     }

//     @Test
//     @Transactional
//     public void getAllCourseModulesOfUserEnrolledInCourse_testException1() {
//         // page = 0 => page index am
//         assertThrows(IllegalArgumentException.class,
//                 () -> courseModuleService.getAllCourseModulesOfUserEnrolledInCourse(1L, 1L, 1L,
//                         0, 10, "id", "ASC"));
//     }

//     private User createUserSeed() {
//         String unique = String.valueOf(System.nanoTime());
//         User user = new User();
//         user.setEmail("course-module-" + unique + "@test.com");
//         user.setAccountName("coursemodule_" + unique);
//         user.setPassword("secret");
//         user.setPhone("09" + unique.substring(Math.max(0, unique.length() - 8)));
//         user.setFirstName("Unit");
//         user.setLastName("Tester");
//         user.setAvatar("https://example.com/avatar.png");
//         user.setGender("MALE");
//         user.setBio("Seed data for course module unit test");
//         user.setProvider("LOCAL");
//         user.setIsActive(true);
//         user.setEmailVerified(true);
//         user.setPhoneVerified(true);
//         return userRepository.save(user);
//     }

//     private Instructor createInstructorSeed(Long userId) {
//         String unique = String.valueOf(System.nanoTime());
//         Instructor instructor = new Instructor();
//         instructor.setUserId(userId);
//         instructor.setSlug("instructor-" + unique);
//         instructor.setExpertise("Course module testing");
//         instructor.setExperienceYears(3);
//         instructor.setQualification("Bachelor of Information Technology");
//         instructor.setBankName("Vietcombank");
//         instructor.setBankAccount("0123456789");
//         instructor.setTaxCode("TAX-CM-001");
//         return instructorRepository.save(instructor);
//     }

//     private CourseGroup createCourseGroupSeed() {
//         String unique = String.valueOf(System.nanoTime());
//         CourseGroup group = new CourseGroup();
//         group.setTitle("Bundle " + unique);
//         group.setSlug("bundle-" + unique);
//         group.setDescription("Seed group for course module tests");
//         group.setThumbnail("https://example.com/group-thumb.png");
//         group.setEnrollmentType(EnrollmentType.LIFETIME);
//         group.setCurrency(Currency.VND);
//         group.setPrice(new BigDecimal("299000"));
//         group.setCustomPrice(new BigDecimal("249000"));
//         group.setWhatYouLearn("Course module testing");
//         group.setIsPreOrder(false);
//         return courseGroupRepository.save(group);
//     }

//     private Course createCourseSeed(Long instructorId) {
//         String unique = String.valueOf(System.nanoTime());

//         Course course = new Course();
//         course.setTitle("Course " + unique);
//         course.setSlug("course-" + unique);
//         course.setCode("COURSE-" + unique);
//         course.setDescription("JUnit seed course for course module");
//         course.setThumbnail("https://example.com/course-thumb.png");
//         course.setPreviewVideo("https://example.com/preview.mp4");
//         course.setLevel(CourseLevel.BEGINNER);
//         course.setStatus(CourseStatus.DRAFT);
//         course.setLanguage("vi");
//         course.setWhatYouLearn("Create module and lessons");
//         course.setPrice(new BigDecimal("199000"));
//         course.setCurrency(Currency.VND);
//         course.setExpiredDays(365);
//         course.setSkillAcquired("Testing");
//         course.setTargetAudiences("Backend developer");
//         course.setEnrollmentType(EnrollmentType.LIFETIME);
//         course.setIsFree(true);
//         course.setIsPreOrder(false);
//         course.setInstructorId(instructorId);
//         return courseRepository.save(course);
//     }

//     private CourseModule createCourseModuleSeed(Long courseId, String title, String description,
//                                                 Integer sortOrder, Boolean isPreview) {
//         CourseModule module = new CourseModule();
//         module.setCourseId(courseId);
//         module.setTitle(title);
//         module.setDescription(description);
//         module.setSortOrder(sortOrder);
//         module.setIsPreview(isPreview);
//         return courseModuleRepository.save(module);
//     }

//     private Lesson createLessonSeed(Long moduleId, String title, Integer sortOrder) {
//         Lesson lesson = new Lesson();
//         lesson.setModuleId(moduleId);
//         lesson.setTitle(title);
//         lesson.setDescription("Lesson for course module test");
//         lesson.setContentType("video");
//         lesson.setVideoUrl("https://example.com/lesson-video");
//         lesson.setDuration(90L);
//         lesson.setSortOrder(sortOrder);
//         lesson.setIsMandatory(true);
//         lesson.setIsPreview(false);
//         return lessonRepository.save(lesson);
//     }

//     private Enrollment createEnrollmentSeed(User user, Course course, CourseGroup group) {
//         Enrollment enrollment = new Enrollment();
//         enrollment.setUser(user);
//         enrollment.setCourse(course);
//         enrollment.setCourseGroup(group);
//         enrollment.setEnrollmentDate(LocalDateTime.now());
//         return enrollmentRepository.save(enrollment);
//     }

//     private LessonProgress createLessonProgressSeed(Long lessonId, Long userId, Long enrollmentId) {
//         LessonProgress progress = new LessonProgress();
//         progress.setLessonId(lessonId);
//         progress.setUserId(userId);
//         progress.setEnrollmentId(enrollmentId);
//         return lessonProgressRepository.save(progress);
//     }
// }
