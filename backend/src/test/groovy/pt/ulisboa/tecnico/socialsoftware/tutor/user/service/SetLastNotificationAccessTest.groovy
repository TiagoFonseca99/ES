package pt.ulisboa.tecnico.socialsoftware.tutor.user.service;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import spock.lang.Specification

import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository

@DataJpaTest
class SetLastNotificationAccessTest extends Specification {
    public static final String USER_NAME = "user name";
    public static final String USER_USERNAME = "user username"

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    def student

    def setup() {
        student = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        userRepository.save(student);
    }

    def "update last notification access"() {
        given: 'previous date'
        def previous = student.getLastNotificationAccess()
        when:
        def updated = userService.setLastNotificationAccess(student.getId())

        then:
        updated.getLastNotificationAccess() > previous.toString()
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
