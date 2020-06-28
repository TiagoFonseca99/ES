package pt.ulisboa.tecnico.socialsoftware.tutor.user.service;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import spock.lang.Specification

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository

@DataJpaTest
class DashboardInfoVisibilityTest extends Specification {
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

    def "student change tournament stats from public to private"(){
        when:
        def result = userService.toggleTournamentStatsVisibility(student.getId())

        then:
        !result.tournamentStatsPublic
    }

    def "student change discussion stats from public to private"(){
        when:
        def result = userService.toggleDiscussionStatsVisibility(student.getId())

        then:
        !result.discussionStatsPublic
    }

    def "student change submission stats from public to private"(){
        when:
        def result = userService.toggleSubmissionStatsVisibility(student.getId())

        then:
        !result.submissionStatsPublic
    }

    def "student change user stats from public to private"(){
        when:
        def result = userService.toggleUserStatsVisibility(student.getId())

        then:
        !result.userStatsPublic
    }

    def "student change tournament stats from private to public"(){
        given: "a student with private information (default is public = true)"
        student.toggleTournamentStatsVisibility()

        when:
        def result = userService.toggleTournamentStatsVisibility(student.getId())

        then:
        result.tournamentStatsPublic
    }

    def "student change discussion stats from private to public"(){
        given: "a student with private information (default is public = true)"
        student.toggleDiscussionStatsVisibility()

        when:
        def result = userService.toggleDiscussionStatsVisibility(student.getId())

        then:
        result.discussionStatsPublic
    }

    def "student change submission stats from private to public"(){
        given: "a student with private information (default is public = true)"
        student.toggleSubmissionStatsVisibility()

        when:
        def result = userService.toggleSubmissionStatsVisibility(student.getId())

        then:
        result.submissionStatsPublic
    }

    def "student change user stats from private to public"(){
        given: "a student with private information (default is public = true)"
        student.toggleUserStatsVisibility()

        when:
        def result = userService.toggleUserStatsVisibility(student.getId())

        then:
        result.userStatsPublic
    }

    def "teacher change tournament stats visibility"(){
        given: "a teacher"
        def teacher = new User(USER_NAME + "1", USER_USERNAME + "1", 2, User.Role.TEACHER)
        userRepository.save(teacher)

        when:
        userService.toggleTournamentStatsVisibility(teacher.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_STUDENT
    }

    def "teacher change discussion stats visibility"(){
        given: "a teacher"
        def teacher = new User(USER_NAME + "1", USER_USERNAME + "1", 2, User.Role.TEACHER)
        userRepository.save(teacher)

        when:
        userService.toggleDiscussionStatsVisibility(teacher.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_STUDENT
    }

    def "teacher change submission stats visibility"(){
        given: "a teacher"
        def teacher = new User(USER_NAME + "1", USER_USERNAME + "1", 2, User.Role.TEACHER)
        userRepository.save(teacher)

        when:
        userService.toggleSubmissionStatsVisibility(teacher.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_STUDENT
    }

    def "teacher change user stats visibility"(){
        given: "a teacher"
        def teacher = new User(USER_NAME + "1", USER_USERNAME + "1", 2, User.Role.TEACHER)
        userRepository.save(teacher)

        when:
        userService.toggleUserStatsVisibility(teacher.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_STUDENT
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
