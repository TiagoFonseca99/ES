package pt.ulisboa.tecnico.socialsoftware.tutor.announcement

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.repository.AnnouncementRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.dto.AnnouncementDto
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.repository.NotificationRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class AnnouncementNotificationsTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String STUDENT_NAME = "João Silva"
    public static final String ANNOUNCEMENT_TITLE = "Announcement"
    public static final String ANNOUNCEMENT_CONTENT = "Here is an announcement"
    public static final String STUDENT_USERNAME = "joaosilva"
    public static final String TEACHER_NAME = "Ana Rita"
    public static final String TEACHER_USERNAME = "anarita"

    @Autowired
    AnnouncementService announcementService

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    AnnouncementRepository announcementRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    NotificationRepository notificationRepository

    def teacher
    def courseExecution
    def student
    def course
    def announcementDto

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        student = new User(STUDENT_NAME, STUDENT_USERNAME, 1, User.Role.STUDENT)
        student.setEnrolledCoursesAcronyms(courseExecution.getAcronym())
        student.addCourse(courseExecution)
        courseExecution.addUser(student)
        courseExecutionRepository.save(courseExecution)
        userRepository.save(student)
        teacher = new User(TEACHER_NAME, TEACHER_USERNAME, 2, User.Role.TEACHER)
        userRepository.save(teacher)
        announcementDto = new AnnouncementDto()
        announcementDto.setTitle(ANNOUNCEMENT_TITLE)
        announcementDto.setContent(ANNOUNCEMENT_CONTENT)
        announcementDto.setCourseExecutionId(courseExecution.getId())
        announcementDto.setUserId(teacher.getId())
    }

    def "teacher creates announcement and student receives notification"() {
        expect: "0 notifications"
        notificationRepository.getUserNotifications(student.getId()).isEmpty()

        when:
        announcementService.createAnnouncement(announcementDto)

        then:
        def result = notificationRepository.getUserNotifications(student.getId())
        result.size() == 1
        result.get(0).getType() == Notification.Type.ANNOUNCEMENT

    }

    def "teacher creates  3 announcements and student receives notification"() {
        expect: "0 notifications"
        notificationRepository.getUserNotifications(student.getId()).isEmpty()

        when:
        announcementService.createAnnouncement(announcementDto)
        announcementService.createAnnouncement(announcementDto)
        announcementService.createAnnouncement(announcementDto)

        then:
        def result = notificationRepository.getUserNotifications(student.getId())
        result.size() == 3
        result.get(0).getType() == Notification.Type.ANNOUNCEMENT
        result.get(1).getType() == Notification.Type.ANNOUNCEMENT
        result.get(2).getType() == Notification.Type.ANNOUNCEMENT

    }


    @TestConfiguration
    static class AnnouncementServiceImplTestContextConfiguration {

        @Bean
        AnnouncementService announcementService() {
            return new AnnouncementService()
        }

        @Bean
        NotificationService notificationService() {
            return new NotificationService()
        }
    }
}