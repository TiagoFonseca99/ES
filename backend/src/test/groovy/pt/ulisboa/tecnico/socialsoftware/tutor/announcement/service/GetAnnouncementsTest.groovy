package pt.ulisboa.tecnico.socialsoftware.tutor.announcement.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.AnnouncementService
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.dto.AnnouncementDto
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.repository.AnnouncementRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_EXECUTION_MISSING
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TEACHER_MISSING


@DataJpaTest
class GetAnnouncementsTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String STUDENT_NAME = "João Silva"
    public static final String ANNOUNCEMENT_TITLE1 = "Announcement 1"
    public static final String ANNOUNCEMENT_TITLE2 = "Announcement 2"
    public static final String ANNOUNCEMENT_CONTENT1 = "Here is an announcement 1"
    public static final String ANNOUNCEMENT_CONTENT2 = "Here is an announcement 2"
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

    @Shared
    def teacher
    @Shared
    def courseExecution
    def student
    def course

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        student = new User(STUDENT_NAME, STUDENT_USERNAME, 1, User.Role.STUDENT)
        student.setEnrolledCoursesAcronyms(courseExecution.getAcronym())
        userRepository.save(student)
        teacher = new User(TEACHER_NAME, TEACHER_USERNAME, 2, User.Role.TEACHER)
        userRepository.save(teacher)
    }

    def "create 2 announcements and get announcements"() {
        given: "2 created announcements"
        def announcementDto1 = new AnnouncementDto()
        announcementDto1.setTitle(ANNOUNCEMENT_TITLE1)
        announcementDto1.setContent(ANNOUNCEMENT_CONTENT1)
        announcementDto1.setCourseExecutionId(courseExecution.getId())
        announcementDto1.setUserId(teacher.getId())
        announcementService.createAnnouncement(announcementDto1)

        def announcementDto2 = new AnnouncementDto()
        announcementDto2.setTitle(ANNOUNCEMENT_TITLE2)
        announcementDto2.setContent(ANNOUNCEMENT_CONTENT2)
        announcementDto2.setCourseExecutionId(courseExecution.getId())
        announcementDto2.setUserId(teacher.getId())
        announcementDto2.setEdited(true)
        announcementService.createAnnouncement(announcementDto2)

        when:
        def result = announcementService.getAnnouncements(teacher.getId(), courseExecution.getId())

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
        a1.getUserId() == teacher.getId()
        a2.getUserId() == teacher.getId()
        a1.getCourseExecutionId() == courseExecution.getId()
        a2.getCourseExecutionId() == courseExecution.getId()
        a1.getCreationDate() != null
        a2.getCreationDate() != null
        !a1.isEdited()
        a2.isEdited()
    }

    def "get announcements with no created announcements"(){
        when:
        def result = announcementService.getAnnouncements(teacher.getId(), courseExecution.getId())

        then: "the returned data is correct"
        result.size() == 0
    }

    @Unroll
    def "invalid arguments: courseExecutionId=#courseExecutionId | userId=#userId || errorMessage"() {
        when:
        announcementService.getAnnouncements(userId, courseExecutionId)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == errorMessage

        where:
        courseExecutionId       | userId          || errorMessage
        null                    | teacher.getId() || COURSE_EXECUTION_MISSING
        courseExecution.getId() | null            || TEACHER_MISSING
    }

    @TestConfiguration
    static class AnnouncementServiceImplTestContextConfiguration {

        @Bean
        AnnouncementService announcementService() {
            return new AnnouncementService()
        }
    }
}