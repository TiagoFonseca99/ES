package pt.ulisboa.tecnico.socialsoftware.tutor.course.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.course.*
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import spock.lang.Specification

@DataJpaTest
class GetCourseExecutionsPerformanceTest extends Specification {
    static final String COURSE = "CourseOne"
    static final String ACRONYM = "C12"
    static final String ACADEMIC_TERM = "1º Semestre"

    @Autowired
    CourseService courseService

    @Autowired
    UserService userService

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    def "performance testing to get 1000 course executions"() {
        given: "a course"
        def course = new Course(COURSE, Course.Type.TECNICO)
        courseRepository.save(course)
        and: "a 1000 course executions"
        1.upto(1, {
            courseExecutionRepository.save(new CourseExecution(course, ACRONYM + it, ACADEMIC_TERM, Course.Type.TECNICO))
        })

        when:
        1.upto(1, { courseService.getCourseExecutions(User.Role.ADMIN)})

        then:
        true
    }

    @TestConfiguration
    static class ServiceImplTestContextConfiguration {

        @Bean
        CourseService courseService() {
            return new CourseService()
        }
    }

    @TestConfiguration
    static class UserServiceImplTestContextConfiguration {
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
