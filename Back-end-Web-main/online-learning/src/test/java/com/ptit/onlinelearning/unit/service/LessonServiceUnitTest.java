// package com.ptit.onlinelearning.unit.service;

// import com.ptit.onlinelearning.common.type.CourseLevel;
// import com.ptit.onlinelearning.common.type.CourseStatus;
// import com.ptit.onlinelearning.common.type.Currency;
// import com.ptit.onlinelearning.common.type.EnrollmentType;
// import com.ptit.onlinelearning.exception.DataNotFoundException;
// import com.ptit.onlinelearning.exception.InvalidParamException;
// import com.ptit.onlinelearning.model.Course;
// import com.ptit.onlinelearning.model.CourseModule;
// import com.ptit.onlinelearning.model.Instructor;
// import com.ptit.onlinelearning.model.Lesson;
// import com.ptit.onlinelearning.model.User;
// import com.ptit.onlinelearning.repository.CourseModuleRepository;
// import com.ptit.onlinelearning.repository.CourseRepository;
// import com.ptit.onlinelearning.repository.InstructorRepository;
// import com.ptit.onlinelearning.repository.LessonRepository;
// import com.ptit.onlinelearning.repository.UserRepository;
// import com.ptit.onlinelearning.request.CourseModuleRequest;
// import com.ptit.onlinelearning.request.CourseRequest;
// import com.ptit.onlinelearning.request.LessonRequest;
// import com.ptit.onlinelearning.request.UpdateLessonRequest;
// import com.ptit.onlinelearning.service.lesson.LessonService;
// import com.ptit.onlinelearning.unit.config.BaseUnitTest;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.domain.Page;
// import org.springframework.security.access.AccessDeniedException;
// import org.springframework.transaction.annotation.Transactional;

// import java.math.BigDecimal;
// import java.util.ArrayList;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.jupiter.api.Assertions.assertTrue;

// public class LessonServiceUnitTest extends BaseUnitTest {

//     @Autowired
//     private LessonService lessonService;

//     @Autowired
//     private LessonRepository lessonRepository;

//     @Autowired
//     private CourseModuleRepository courseModuleRepository;

//     @Autowired
//     private CourseRepository courseRepository;

//     @Autowired
//     private InstructorRepository instructorRepository;

//     @Autowired
//     private UserRepository userRepository;

//     @Test
//     @Transactional
//     public void getLessons_testStandard1() {
//         // Khong filter, lay danh sach co phan trang va sort hop le
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Module A", "Desc", 1, false);

//         createLessonSeed(module.getId(), "Java Basics", "Video Java", "video", true, 1);
//         createLessonSeed(module.getId(), "Spring Intro", "Document Spring", "document", false, 2);

//         lessonRepository.flush();

//         Page<Lesson> page = lessonService.getLessons(1, 10, null, null, null, null, "id", "ASC");

//         assertNotNull(page);
//         assertTrue(page.getTotalElements() >= 2);
//     }

//     @Test
//     @Transactional
//     public void getLessons_testStandard2() {
//         // Filter theo moduleId chi tra ve lesson cua module do
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());

//         CourseModule targetModule = createCourseModuleSeed(course.getId(), "Target module", "Desc", 1, false);
//         CourseModule otherModule = createCourseModuleSeed(course.getId(), "Other module", "Desc", 2, false);

//         Lesson l1 = createLessonSeed(targetModule.getId(), "Target lesson 1", "Video", "video", true, 1);
//         Lesson l2 = createLessonSeed(targetModule.getId(), "Target lesson 2", "Doc", "document", false, 2);
//         Lesson lOther = createLessonSeed(otherModule.getId(), "Other lesson", "Video", "video", true, 1);

//         lessonRepository.flush();

//         Page<Lesson> page = lessonService.getLessons(1, 10, targetModule.getId(), null, null, null, "sortOrder", "ASC");

//         assertNotNull(page);
//         assertEquals(2, page.getTotalElements());
//         List<Long> ids = page.getContent().stream().map(Lesson::getId).toList();
//         assertTrue(ids.contains(l1.getId()));
//         assertTrue(ids.contains(l2.getId()));
//         assertTrue(!ids.contains(lOther.getId()));
//     }

//     @Test
//     @Transactional
//     public void getLessons_testStandard3() {
//         // Ket hop filter search + contentType + isMandatory
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Module search", "Desc", 1, false);

//         createLessonSeed(module.getId(), "Kotlin Basic", "intro topic", "video", true, 1);
//         createLessonSeed(module.getId(), "Kotlin Document", "doc topic", "document", true, 2);
//         createLessonSeed(module.getId(), "Java Basic", "intro topic", "video", false, 3);

//         lessonRepository.flush();

//         Page<Lesson> page = lessonService.getLessons(
//                 1,
//                 10,
//                 module.getId(),
//                 "kotlin",
//                 "video",
//                 true,
//                 "id",
//                 "ASC"
//         );

//         assertNotNull(page);
//         assertEquals(1, page.getTotalElements());
//         assertEquals("Kotlin Basic", page.getContent().getFirst().getTitle());
//     }

//     @Test
//     @Transactional
//     public void getLessons_testStandard4() {
//         // Kiem tra phan trang pageSize = 1 va sort DESC
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Module paging", "Desc", 1, false);

//         createLessonSeed(module.getId(), "Lesson 1", "Desc", "video", true, 1);
//         createLessonSeed(module.getId(), "Lesson 2", "Desc", "video", true, 2);

//         lessonRepository.flush();

//         Page<Lesson> page = lessonService.getLessons(1, 1, module.getId(), null, null, null, "sortOrder", "DESC");

//         assertNotNull(page);
//         assertEquals(1, page.getSize());
//         assertEquals(1, page.getContent().size());
//         assertEquals(2, page.getContent().getFirst().getSortOrder());
//     }

//     @Test
//     @Transactional
//     public void getLessons_testException1() {
//         // page = 0 => PageRequest index am
//         assertThrows(IllegalArgumentException.class,
//                 () -> lessonService.getLessons(0, 10, null, null, null, null, "id", "ASC"));
//     }

//     @Test
//     @Transactional
//     public void getLessons_testException2() {
//         // sort order khong hop le
//         assertThrows(IllegalArgumentException.class,
//                 () -> lessonService.getLessons(1, 10, null, null, null, null, "id", "INVALID"));
//     }

//     @Test
//     @Transactional
//     public void getLessonById_testStandard1() {
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Module", "Desc", 1, false);
//         Lesson savedLesson = createLessonSeed(module.getId(), "Find lesson", "Desc", "video", true, 1);

//         Lesson result = lessonService.getLessonById(savedLesson.getId());

//         assertNotNull(result);
//         assertEquals(savedLesson.getId(), result.getId());
//         assertEquals("Find lesson", result.getTitle());
//     }

//     @Test
//     @Transactional
//     public void getLessonById_testException1() {
//         assertThrows(DataNotFoundException.class,
//                 () -> lessonService.getLessonById(Long.MAX_VALUE));
//     }

//     @Test
//     @Transactional
//     public void createLesson_testStandard1() {
//         // createLesson(LessonRequest, Instructor)
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Create module", "Desc", 1, false);

//         LessonRequest request = createLessonRequestSeed(module.getId(), "Create by request");

//         Lesson result = lessonService.createLesson(request, ownerInstructor);

//         assertNotNull(result);
//         assertNotNull(result.getId());
//         assertEquals(module.getId(), result.getModuleId());
//         assertEquals("Create by request", result.getTitle());
//         assertEquals("video", result.getContentType());
//     }

//     @Test
//     @Transactional
//     public void createLesson_testException1() {
//         // module khong ton tai
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());

//         LessonRequest request = createLessonRequestSeed(Long.MAX_VALUE, "Missing module");

//         assertThrows(RuntimeException.class,
//                 () -> lessonService.createLesson(request, ownerInstructor));
//     }

//     @Test
//     @Transactional
//     public void createLesson_testException2() {
//         // instructor khong phai owner
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Forbidden module", "Desc", 1, false);

//         User otherUser = createUserSeed();
//         Instructor otherInstructor = createInstructorSeed(otherUser.getId());
//         LessonRequest request = createLessonRequestSeed(module.getId(), "Forbidden lesson");

//         assertThrows(AccessDeniedException.class,
//                 () -> lessonService.createLesson(request, otherInstructor));
//     }

//     @Test
//     @Transactional
//     public void createLesson_testException3() {
//         // module da dat nguong 50 lessons
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Max lesson module", "Desc", 1, false);

//         for (int i = 1; i <= 50; i++) {
//             createLessonSeed(module.getId(), "Seed lesson " + i, "Desc", "video", true, i);
//         }
//         lessonRepository.flush();

//         LessonRequest request = createLessonRequestSeed(module.getId(), "Lesson 51");

//         assertThrows(InvalidParamException.class,
//                 () -> lessonService.createLesson(request, ownerInstructor));
//     }

//     @Test
//     @Transactional
//     public void createLesson_testStandard2() {
//         // createLesson(Long, CourseRequest.CourseModuleDTO.LessonDTO)
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Module dto course", "Desc", 1, false);

//         CourseRequest.CourseModuleDTO.LessonDTO lessonDTO = createCourseRequestLessonDTO("From CourseRequest DTO");

//         Lesson result = lessonService.createLesson(module.getId(), lessonDTO);

//         assertNotNull(result);
//         assertNotNull(result.getId());
//         assertEquals(module.getId(), result.getModuleId());
//         assertEquals("From CourseRequest DTO", result.getTitle());
//         assertEquals("document", result.getContentType());
//     }

//     @Test
//     @Transactional
//     public void createLesson_testStandard3() {
//         // createLesson(Long, CourseModuleRequest.LessonDTO)
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Module dto module", "Desc", 1, false);

//         CourseModuleRequest.LessonDTO lessonDTO = createCourseModuleRequestLessonDTO("From CourseModuleRequest DTO");

//         Lesson result = lessonService.createLesson(module.getId(), lessonDTO);

//         assertNotNull(result);
//         assertNotNull(result.getId());
//         assertEquals(module.getId(), result.getModuleId());
//         assertEquals("From CourseModuleRequest DTO", result.getTitle());
//         assertEquals("text", result.getContentType());
//     }

//     @Test
//     @Transactional
//     public void updateLesson_testStandard1() {
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Update module", "Desc", 1, false);
//         Lesson lesson = createLessonSeed(module.getId(), "Old title", "Old desc", "video", true, 1);

//         UpdateLessonRequest request = new UpdateLessonRequest();
//         request.setTitle("New title");
//         request.setDescription("New desc");
//         request.setSortOrder(10);
//         request.setDuration(180L);
//         request.setContentType("document");
//         request.setDocumentUrl("https://example.com/new-doc");
//         request.setIsMandatory(false);

//         Lesson updated = lessonService.updateLesson(lesson.getId(), request, ownerInstructor);

//         assertNotNull(updated);
//         assertEquals("New title", updated.getTitle());
//         assertEquals("New desc", updated.getDescription());
//         assertEquals(10, updated.getSortOrder());
//         assertEquals(180L, updated.getDuration());
//         assertEquals("document", updated.getContentType());
//         assertEquals(false, updated.getIsMandatory());
//     }

//     @Test
//     @Transactional
//     public void updateLesson_testException1() {
//         // lesson khong ton tai
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());

//         UpdateLessonRequest request = new UpdateLessonRequest();
//         request.setTitle("Any");

//         assertThrows(DataNotFoundException.class,
//                 () -> lessonService.updateLesson(Long.MAX_VALUE, request, ownerInstructor));
//     }

//     @Test
//     @Transactional
//     public void updateLesson_testException2() {
//         // instructor khac owner
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Update forbidden", "Desc", 1, false);
//         Lesson lesson = createLessonSeed(module.getId(), "Title", "Desc", "video", true, 1);

//         User otherUser = createUserSeed();
//         Instructor otherInstructor = createInstructorSeed(otherUser.getId());

//         UpdateLessonRequest request = new UpdateLessonRequest();
//         request.setTitle("Blocked");

//         assertThrows(AccessDeniedException.class,
//                 () -> lessonService.updateLesson(lesson.getId(), request, otherInstructor));
//     }

//     @Test
//     @Transactional
//     public void deleteLesson_testStandard1() {
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Delete module", "Desc", 1, false);
//         Lesson lesson = createLessonSeed(module.getId(), "Delete me", "Desc", "video", true, 1);

//         lessonService.deleteLesson(lesson.getId(), ownerInstructor);

//         assertTrue(lessonRepository.findById(lesson.getId()).isEmpty());
//     }

//     @Test
//     @Transactional
//     public void deleteLesson_testException1() {
//         // instructor khac owner
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());
//         Course course = createCourseSeed(ownerInstructor.getId());
//         CourseModule module = createCourseModuleSeed(course.getId(), "Delete forbidden", "Desc", 1, false);
//         Lesson lesson = createLessonSeed(module.getId(), "Cannot delete", "Desc", "video", true, 1);

//         User otherUser = createUserSeed();
//         Instructor otherInstructor = createInstructorSeed(otherUser.getId());

//         assertThrows(AccessDeniedException.class,
//                 () -> lessonService.deleteLesson(lesson.getId(), otherInstructor));
//     }

//     @Test
//     @Transactional
//     public void deleteLesson_testException2() {
//         // lesson khong ton tai
//         User ownerUser = createUserSeed();
//         Instructor ownerInstructor = createInstructorSeed(ownerUser.getId());

//         assertThrows(DataNotFoundException.class,
//                 () -> lessonService.deleteLesson(Long.MAX_VALUE, ownerInstructor));
//     }

//     private User createUserSeed() {
//         String unique = String.valueOf(System.nanoTime());
//         User user = new User();
//         user.setEmail("lesson-" + unique + "@test.com");
//         user.setAccountName("lesson_user_" + unique);
//         user.setPassword("secret");
//         user.setPhone("09" + unique.substring(Math.max(0, unique.length() - 8)));
//         user.setFirstName("Unit");
//         user.setLastName("Tester");
//         user.setAvatar("https://example.com/avatar.png");
//         user.setGender("MALE");
//         user.setBio("Seed data for lesson unit test");
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
//         instructor.setSlug("instructor-lesson-" + unique);
//         instructor.setExpertise("Lesson testing");
//         instructor.setExperienceYears(4);
//         instructor.setQualification("Bachelor of Information Technology");
//         instructor.setBankName("Vietcombank");
//         instructor.setBankAccount("0123456789");
//         instructor.setTaxCode("TAX-LESSON-001");
//         return instructorRepository.save(instructor);
//     }

//     private Course createCourseSeed(Long instructorId) {
//         String unique = String.valueOf(System.nanoTime());
//         Course course = new Course();
//         course.setTitle("Lesson course " + unique);
//         course.setSlug("lesson-course-" + unique);
//         course.setCode("LESSON-" + unique);
//         course.setDescription("JUnit seed course for lesson tests");
//         course.setThumbnail("https://example.com/course-thumb.png");
//         course.setPreviewVideo("https://example.com/preview.mp4");
//         course.setLevel(CourseLevel.BEGINNER);
//         course.setStatus(CourseStatus.DRAFT);
//         course.setLanguage("vi");
//         course.setWhatYouLearn("Filter and sort lessons");
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
//         Course course = courseRepository.findById(courseId)
//             .orElseThrow(() -> new DataNotFoundException("Course not found " + courseId));

//         CourseModule module = new CourseModule();
//         module.setCourseId(courseId);
//         module.setCourse(course);
//         module.setTitle(title);
//         module.setDescription(description);
//         module.setSortOrder(sortOrder);
//         module.setIsPreview(isPreview);
//         module.setLessons(new ArrayList<>());
//         return courseModuleRepository.save(module);
//     }

//     private Lesson createLessonSeed(Long moduleId, String title, String description,
//                                     String contentType, Boolean isMandatory, Integer sortOrder) {
//         CourseModule module = courseModuleRepository.findById(moduleId)
//             .orElseThrow(() -> new DataNotFoundException("Course Module not found " + moduleId));

//         Lesson lesson = new Lesson();
//         lesson.setModuleId(moduleId);
//         lesson.setCourseModule(module);
//         lesson.setTitle(title);
//         lesson.setDescription(description);
//         lesson.setContentType(contentType);
//         lesson.setVideoUrl("https://example.com/video");
//         lesson.setDuration(90L);
//         lesson.setDocumentUrl("https://example.com/doc");
//         lesson.setContent("Lesson content");
//         lesson.setSortOrder(sortOrder);
//         lesson.setIsMandatory(isMandatory);
//         lesson.setIsPreview(false);
//         Lesson savedLesson = lessonRepository.save(lesson);

//         if (module.getLessons() == null) {
//             module.setLessons(new ArrayList<>());
//         }
//         module.getLessons().add(savedLesson);

//         return savedLesson;
//     }

//     private LessonRequest createLessonRequestSeed(Long moduleId, String title) {
//         LessonRequest request = new LessonRequest();
//         request.setModuleId(moduleId);
//         request.setTitle(title);
//         request.setDescription("Lesson request description");
//         request.setContentType("video");
//         request.setVideoUrl("https://example.com/request-video");
//         request.setDocumentUrl("https://example.com/request-doc");
//         request.setContent("Request content");
//         request.setDuration(120L);
//         request.setSortOrder(1);
//         request.setIsMandatory(true);
//         return request;
//     }

//     private CourseRequest.CourseModuleDTO.LessonDTO createCourseRequestLessonDTO(String title) {
//         CourseRequest.CourseModuleDTO.LessonDTO lessonDTO = new CourseRequest.CourseModuleDTO.LessonDTO();
//         lessonDTO.setTitle(title);
//         lessonDTO.setDescription("Course request DTO description");
//         lessonDTO.setContentType("document");
//         lessonDTO.setVideoUrl("https://example.com/course-request-video");
//         lessonDTO.setDocumentUrl("https://example.com/course-request-doc");
//         lessonDTO.setContent("Course request content");
//         lessonDTO.setDuration(100L);
//         lessonDTO.setSortOrder(2);
//         lessonDTO.setIsMandatory(false);
//         return lessonDTO;
//     }

//     private CourseModuleRequest.LessonDTO createCourseModuleRequestLessonDTO(String title) {
//         CourseModuleRequest.LessonDTO lessonDTO = new CourseModuleRequest.LessonDTO();
//         lessonDTO.setTitle(title);
//         lessonDTO.setDescription("Course module request DTO description");
//         lessonDTO.setContentType("text");
//         lessonDTO.setVideoUrl("https://example.com/course-module-request-video");
//         lessonDTO.setDocumentUrl("https://example.com/course-module-request-doc");
//         lessonDTO.setContent("Course module request content");
//         lessonDTO.setDuration(80L);
//         lessonDTO.setSortOrder(3);
//         lessonDTO.setIsMandatory(true);
//         return lessonDTO;
//     }
// }
