package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import spock.lang.Specification

@DataJpaTest
class ChangeTournamentPermissionsTest extends Specification {
    public static final String USER_NAME = "Tiago"
    public static final String USERNAME = "TiagoFonseca99"
    public static final Integer KEY = 1

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository

    def user


    def setup() {

    }

    def "Switch name permission to on" () {
        given:
        user = new User(USER_NAME, USERNAME, KEY, User.Role.STUDENT)
        userRepository.save(user)

        when:
        userService.switchTournamentNamePermission(user.getId())

        then:
        userService.getTournamentNamePermission(user.getId())
        !userService.getTournamentScorePermission(user.getId())
    }

    def "Switch name permission to off" () {
        given:
        user = new User(USER_NAME, USERNAME, KEY, User.Role.STUDENT)
        userRepository.save(user)
        userService.switchTournamentNamePermission(user.getId())

        when:
        userService.switchTournamentNamePermission(user.getId())

        then:
        !userService.getTournamentNamePermission(user.getId())
        !userService.getTournamentScorePermission(user.getId())
    }

    def "Switch name permission to on and score permission to on" () {
        given:
        user = new User(USER_NAME, USERNAME, KEY, User.Role.STUDENT)
        userRepository.save(user)
        userService.switchTournamentNamePermission(user.getId())

        when:
        userService.switchTournamentScorePermission(user.getId())

        then:
        userService.getTournamentNamePermission(user.getId())
        userService.getTournamentScorePermission(user.getId())
    }

    def "Switch name permission to on and score permission to off" () {
        given:
        user = new User(USER_NAME, USERNAME, KEY, User.Role.STUDENT)
        userRepository.save(user)
        userService.switchTournamentNamePermission(user.getId())
        userService.switchTournamentScorePermission(user.getId())

        when:
        userService.switchTournamentScorePermission(user.getId())

        then:
        userService.getTournamentNamePermission(user.getId())
        !userService.getTournamentScorePermission(user.getId())
    }

    def "Switch name and score permissions to on and then switch name permission to off"() {
        given:
        user = new User(USER_NAME, USERNAME, KEY, User.Role.STUDENT)
        userRepository.save(user)
        userService.switchTournamentNamePermission(user.getId())
        userService.switchTournamentScorePermission(user.getId())

        when:
        userService.switchTournamentNamePermission(user.getId())

        then:
        !userService.getTournamentNamePermission(user.getId())
        !userService.getTournamentScorePermission(user.getId())
    }

    def "Switch name permission to off and switch score permission" () {
        given:
        user = new User(USER_NAME, USERNAME, KEY, User.Role.STUDENT)
        userRepository.save(user)

        when:
        userService.switchTournamentScorePermission(user.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_TOURNAMENT_PERMISSIONS_NOT_CONSISTENT
    }


    @TestConfiguration
    static class UserServiceImplTestContextConfiguration {

        @Bean
        UserService userService() {
            return new UserService()
        }

    }
}