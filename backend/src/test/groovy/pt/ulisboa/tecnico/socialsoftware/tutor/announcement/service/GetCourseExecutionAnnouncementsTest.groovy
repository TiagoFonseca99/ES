package pt.ulisboa.tecnico.socialsoftware.tutor.announcement.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.AnnouncementService
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.dto.AnnouncementDto
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.repository.AnnouncementRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseService
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification


@DataJpaTest
class GetCourseExecutionAnnouncementsTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String ANNOUNCEMENT_TITLE1 = "Announcement 1"
    public static final String ANNOUNCEMENT_TITLE2 = "Announcement 2"
    public static final String ANNOUNCEMENT_CONTENT1 = "Here is an announcement 1"
    public static final String ANNOUNCEMENT_CONTENT2 = "Here is an announcement 2"
    public static final String TEACHER_NAME1 = "João Silva"
    public static final String TEACHER_USERNAME1 = "joaosilva"
    public static final String TEACHER_NAME2 = "Ana Rita"
    public static final String TEACHER_USERNAME2 = "anarita"

    @Autowired
    AnnouncementService announcementService

    @Autowired
    CourseService courseService

    @Autowired
    UserService userService

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    AnnouncementRepository announcementRepository

    @Autowired
    UserRepository userRepository

    def teacher1
    def teacher2
    def courseExecution
    def course

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        teacher1 = new User(TEACHER_NAME1, TEACHER_USERNAME1, 2, User.Role.TEACHER)
        userRepository.save(teacher1)
        teacher2 = new User(TEACHER_NAME2, TEACHER_USERNAME2, 3, User.Role.TEACHER)
        userRepository.save(teacher2)
    }

    def "2 teachers create an announcement and get course execution announcements"() {
        given: "2 created announcements"
        def announcementDto1 = new AnnouncementDto()
        announcementDto1.setTitle(ANNOUNCEMENT_TITLE1)
        announcementDto1.setContent(ANNOUNCEMENT_CONTENT1)
        announcementDto1.setCourseExecutionId(courseExecution.getId())
        announcementDto1.setUserId(teacher1.getId())
        announcementService.createAnnouncement(announcementDto1)

        def announcementDto2 = new AnnouncementDto()
        announcementDto2.setTitle(ANNOUNCEMENT_TITLE2)
        announcementDto2.setContent(ANNOUNCEMENT_CONTENT2)
        announcementDto2.setCourseExecutionId(courseExecution.getId())
        announcementDto2.setUserId(teacher2.getId())
        announcementService.createAnnouncement(announcementDto2)

        when:
        def result = courseService.getCourseExecutionAnnouncements(courseExecution.getId())

        then: "the returned data is correct"
        result.size() == 2
        def a1 = result.get(0)
        def a2 = result.get(1)
        a1.getId() != null
        a2.getId() != null
        a1.getTitle() == ANNOUNCEMENT_TITLE1
        a2.getTitle() == ANNOUNCEMENT_TITLE2
        a1.getContent() == ANNOUNCEMENT_CONTENT1
        a2.getContent() == ANNOUNCEMENT_CONTENT2
        a1.getUserId() == teacher1.getId()
        a2.getUserId() == teacher2.getId()
        a1.getCourseExecutionId() == courseExecution.getId()
        a2.getCourseExecutionId() == courseExecution.getId()
        a1.getCreationDate() != null
        a2.getCreationDate() != null
    }

    def "get announcements with no created announcements"(){
        when:
        def result = courseService.getCourseExecutionAnnouncements(courseExecution.getId())

        then: "the returned data is correct"
        result.size() == 0
    }

    def "invalid course execution input"() {
        when:
        def result = courseService.getCourseExecutionAnnouncements(courseExecution.getId() + 1)

        then: "the returned data is correct"
        result.size() == 0
    }

    @TestConfiguration
    static class AnnouncementServiceImplTestContextConfiguration {

        @Bean
        AnnouncementService announcementService() {
            return new AnnouncementService()
        }
    }

    @TestConfiguration
    static class CourseServiceImplTestContextConfiguration {
        @Bean
        CourseService courseService() {
            return new CourseService()
        }

        @Bean
        UserService userService() {
            return new UserService()
        }

        @Bean
        AnswerService answerService() {
            return new AnswerService()
        }

        @Bean
        AnswersXmlImport answersXmlImport() {
            return new AnswersXmlImport()
        }
    }
}
