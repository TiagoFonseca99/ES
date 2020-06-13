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
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Shared
import spock.lang.Unroll

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_EXECUTION_MISSING
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TEACHER_MISSING

@DataJpaTest
class CreateAnnouncementTest extends Specification {
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

    def "create announcement"() {
        given: "an announcementDto"
        def announcementDto = new AnnouncementDto()
        announcementDto.setTitle(ANNOUNCEMENT_TITLE)
        announcementDto.setContent(ANNOUNCEMENT_CONTENT)
        announcementDto.setCourseExecutionId(courseExecution.getId())
        announcementDto.setUserId(teacher.getId())

        when:
        announcementService.createAnnouncement(announcementDto)

        then: "the correct announcement is in the repository"
        announcementRepository.count() == 1L
        def result = announcementRepository.findAll().get(0)
        result.getId() != null
        result.getTitle() == ANNOUNCEMENT_TITLE
        result.getContent() == ANNOUNCEMENT_CONTENT
        result.getUser() == teacher
        result.getCourseExecution() == courseExecution
        result.getCreationDate() != null
        !result.isEdited()
    }

    def "user is not a teacher"(){
        given: "a announcementDto for a student"
        def announcementDto = new AnnouncementDto()
        announcementDto.setTitle(ANNOUNCEMENT_TITLE)
        announcementDto.setContent(ANNOUNCEMENT_CONTENT)
        announcementDto.setCourseExecutionId(courseExecution.getId())
        announcementDto.setUserId(student.getId())

        when: announcementService.createAnnouncement(announcementDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_TEACHER
    }

    @Unroll
    def "invalid arguments: courseExecutionId=#courseExecutionId | userId=#userId || errorMessage"() {
        given: "an announcementDto"
        def announcementDto = new AnnouncementDto()
        announcementDto.setTitle(ANNOUNCEMENT_TITLE)
        announcementDto.setContent(ANNOUNCEMENT_CONTENT)
        announcementDto.setCourseExecutionId(courseExecutionId)
        announcementDto.setUserId(userId)

        when:
        announcementService.createAnnouncement(announcementDto)

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