package pt.ulisboa.tecnico.socialsoftware.tutor.announcement.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.AnnouncementService
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.domain.Announcement
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.dto.AnnouncementDto
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.repository.AnnouncementRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.ANNOUNCEMENT_NOT_FOUND


@DataJpaTest
class RemoveAnnouncementTest extends Specification {
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

    def teacher
    def courseExecution
    def student
    def course
    def announcement

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        student = new User(STUDENT_NAME, STUDENT_USERNAME, 1, User.Role.STUDENT)
        student.setEnrolledCoursesAcronyms(courseExecution.getAcronym())
        userRepository.save(student)
        teacher = new User(TEACHER_NAME, TEACHER_USERNAME, 2, User.Role.TEACHER)
        announcement = new Announcement()
        announcement.setTitle(ANNOUNCEMENT_TITLE)
        announcement.setContent(ANNOUNCEMENT_CONTENT)
        announcement.setCourseExecution(courseExecution)
        announcement.setUser(teacher)
        announcementRepository.save(announcement)
        teacher.addAnnouncement(announcement)
        userRepository.save(teacher)
    }

    def "remove an announcement"() {
        when: announcementService.removeAnnouncement(announcement.getId())

        then: "there are no announcements in the repository"
        announcementRepository.count() == 0L
    }

    def "create an announcement and delete another"(){
        given: "an announcementDto"
        def announcementDto = new AnnouncementDto()
        announcementDto.setTitle(ANNOUNCEMENT_TITLE)
        announcementDto.setContent(ANNOUNCEMENT_CONTENT)
        announcementDto.setCourseExecutionId(courseExecution.getId())
        announcementDto.setUserId(teacher.getId())
        announcementService.createAnnouncement(announcementDto)

        when: announcementService.removeAnnouncement(announcement.getId())

        then: "there is only one announcement in the repository"
        announcementRepository.count() == 1L
    }

    def "remove an announcement that doesn't exist"(){
        when: announcementService.removeAnnouncement(announcement.getId() + 1)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == ANNOUNCEMENT_NOT_FOUND
    }

    @TestConfiguration
    static class AnnouncementServiceImplTestContextConfiguration {

        @Bean
        AnnouncementService announcementService() {
            return new AnnouncementService()
        }
    }
}

