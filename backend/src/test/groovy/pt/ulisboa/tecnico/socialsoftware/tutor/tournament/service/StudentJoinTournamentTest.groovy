package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import spock.lang.Specification

import java.time.LocalDateTime

@DataJpaTest
class StudentJoinTournamentTest extends Specification {

    public static final String USER_NAME1 = "Dinis"
    public static final String USERNAME1 = "JDinis99"
    public static final String USER_NAME2 = "Tiago"
    public static final String USERNAME2 = "TiagoFonseca99"
    public static final String USER_NAME3 = "Tomas"
    public static final String USERNAME3 = "TomasInacio99"
    public static final String USER_NAME4 = "Daniel"
    public static final String USERNAME4 = "DanielMatos99"
    public static final Integer KEY1 = 1
    public static final Integer KEY2 = 2
    public static final Integer KEY3 = 3
    public static final Integer KEY4 = 4
    public static final String COURSE_NAME = "Software Architecture"
    public static final String TOPIC_NAME1 = "Informática"
    public static final String TOPIC_NAME2 = "Engenharia de Software"
    public static final int NUMBER_OF_QUESTIONS1 = 5

    @Autowired
    UserService userService

    @Autowired
    TournamentService tournamentService

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    TopicRepository topicRepository

    def user
    def course
    def topic1
    def topic2
    def topicDto1
    def topicDto2
    def topics = new ArrayList<Integer>()
    def startTime_Now = LocalDateTime.now()
    def endTime_Now = LocalDateTime.now().plusHours(2)
    def tournamentDtoInit = new TournamentDto()
    def tournamentDto = new TournamentDto()

    def setup() {
        user = userService.createUser(USER_NAME1, USERNAME1, User.Role.STUDENT)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        topicDto1 = new TopicDto()
        topicDto1.setName(TOPIC_NAME1)
        topic1 = new Topic(course, topicDto1)
        topicRepository.save(topic1)

        topicDto2 = new TopicDto()
        topicDto2.setName(TOPIC_NAME2)
        topic2 = new Topic(course, topicDto2)
        topicRepository.save(topic2)

        topics.add(topic1.getId())
        topics.add(topic2.getId())


        tournamentDtoInit.setStartTime(startTime_Now)
        tournamentDtoInit.setEndTime(endTime_Now)
        tournamentDtoInit.setNumberOfQuestions(NUMBER_OF_QUESTIONS1)
        tournamentDtoInit.setState(Tournament.Status.NOT_CANCELED)
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDtoInit)
    }

    def "2 student join an open tournament and get participants" () {
        given:
        def user2 = userService.createUser(USER_NAME2, USERNAME2, User.Role.STUDENT)

        and:
        def user3 = userService.createUser(USER_NAME3, USERNAME3, User.Role.STUDENT)

        tournamentService.joinTournament(user2.getId(), tournamentDto)
        tournamentService.joinTournament(user3.getId(), tournamentDto)

        when:
        def result = tournamentService.getTournamentParticipants(tournamentDto)

        then: "the students have joined the tournament"
        result.size() == 2
        def resTournamentParticipant1 = result.get(0)
        def resTournamentParticipant2 = result.get(1)

        resTournamentParticipant1.getId() == user2.getId()
        resTournamentParticipant1.getUsername() == USERNAME2
        resTournamentParticipant1.getName() == USER_NAME2
        resTournamentParticipant1.getRole() == User.Role.STUDENT

        resTournamentParticipant2.getId() == user3.getId()
        resTournamentParticipant2.getUsername() == USERNAME3
        resTournamentParticipant2.getName() == USER_NAME3
        resTournamentParticipant2.getRole() == User.Role.STUDENT

    }

    def "2 student and 1 teacher join an open tournament and get participants" () {
        given:
        def user2 = userService.createUser(USER_NAME2, USERNAME2, User.Role.STUDENT)

        and:
        def user3 = userService.createUser(USER_NAME3, USERNAME3, User.Role.STUDENT)

        and:
        def user4 = userService.createUser(USER_NAME4, USERNAME4, User.Role.TEACHER)

        tournamentService.joinTournament(user2.getId(), tournamentDto)
        tournamentService.joinTournament(user3.getId(), tournamentDto)

        when:
        tournamentService.joinTournament(user4.getId(), tournamentDto)

        then: "the teacher cannot join the tournament"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_STUDENT

        and: "the students have joined the tournament"
        def result = tournamentService.getTournamentParticipants(tournamentDto)
        result.size() == 2
        def resTournamentParticipant1 = result.get(0)
        def resTournamentParticipant2 = result.get(1)

        resTournamentParticipant1.getId() == user2.getId()
        resTournamentParticipant1.getUsername() == USERNAME2
        resTournamentParticipant1.getName() == USER_NAME2
        resTournamentParticipant1.getRole() == User.Role.STUDENT

        resTournamentParticipant2.getId() == user3.getId()
        resTournamentParticipant2.getUsername() == USERNAME3
        resTournamentParticipant2.getName() == USER_NAME3
        resTournamentParticipant2.getRole() == User.Role.STUDENT
    }

    def "2 student and 1 admin join an open tournament and get participants" () {
        given:
        def user2 = userService.createUser(USER_NAME2, USERNAME2, User.Role.STUDENT)

        and:
        def user3 = userService.createUser(USER_NAME3, USERNAME3, User.Role.STUDENT)

        and:
        def user4 = userService.createUser(USER_NAME4, USERNAME4, User.Role.ADMIN)

        tournamentService.joinTournament(user2.getId(), tournamentDto)
        tournamentService.joinTournament(user3.getId(), tournamentDto)


        when:
        tournamentService.joinTournament(user4.getId(), tournamentDto)

        then: "the admin cannot join the tournament"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_STUDENT

        and: "the students have joined the tournament"
        def result = tournamentService.getTournamentParticipants(tournamentDto)

        result.size() == 2
        def resTournamentParticipant1 = result.get(0)
        def resTournamentParticipant2 = result.get(1)

        resTournamentParticipant1.getId() == user2.getId()
        resTournamentParticipant1.getUsername() == USERNAME2
        resTournamentParticipant1.getName() == USER_NAME2
        resTournamentParticipant1.getRole() == User.Role.STUDENT

        resTournamentParticipant2.getId() == user3.getId()
        resTournamentParticipant2.getUsername() == USERNAME3
        resTournamentParticipant2.getName() == USER_NAME3
        resTournamentParticipant2.getRole() == User.Role.STUDENT
    }

    def "1 teacher join an open tournament and get participants" () {
        given:
        def user4 = userService.createUser(USER_NAME4, USERNAME4, User.Role.TEACHER)

        when:
        tournamentService.joinTournament(user4.getId(), tournamentDto)

        then: "the teacher cannot join the tournament"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_STUDENT

        and: "the tournament has no participants"
        def result = tournamentService.getTournamentParticipants(tournamentDto)
        result.size() == 0
    }

    def "1 admin join an open tournament and get participants" () {
        given:
        def user4 = userService.createUser(USER_NAME4, USERNAME4, User.Role.ADMIN)

        when:
        tournamentService.joinTournament(user4.getId(), tournamentDto)

        then: "the admin cannot join the tournament"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_STUDENT

        and: "the tournament has no participants"
        def result = tournamentService.getTournamentParticipants(tournamentDto)
        result.size() == 0
    }

    def "Student tries to join canceled tournament" () {
        given:
        def user2 = userService.createUser(USER_NAME2, USERNAME2, User.Role.STUDENT)

        and:
        def canceledTournamentDtoInit = new TournamentDto()
        def canceledTournamentDto = new TournamentDto()
        canceledTournamentDtoInit.setStartTime(startTime_Now)
        canceledTournamentDtoInit.setEndTime(endTime_Now)
        canceledTournamentDtoInit.setNumberOfQuestions(NUMBER_OF_QUESTIONS1)
        canceledTournamentDtoInit.setState(Tournament.Status.CANCELED)
        canceledTournamentDto = tournamentService.createTournament(user.getId(), topics, canceledTournamentDtoInit)

        when:
        tournamentService.joinTournament(user2.getId(), canceledTournamentDto)

        then: "student cannot join"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_CANCELED

        and: "the tournament has no participants"
        def result = tournamentService.getTournamentParticipants(tournamentDto)
        result.size() == 0
    }

    def "Student tries to join not open (later) tournament" () {
        given:
        def user2 = userService.createUser(USER_NAME2, USERNAME2, User.Role.STUDENT)

        and:
        def notOpenTournamentDtoInit = new TournamentDto()
        def notOpenTournamentDto = new TournamentDto()
        notOpenTournamentDtoInit.setStartTime(startTime_Now.plusHours(10))
        notOpenTournamentDtoInit.setEndTime(endTime_Now.plusHours(10))
        notOpenTournamentDtoInit.setNumberOfQuestions(NUMBER_OF_QUESTIONS1)
        notOpenTournamentDtoInit.setState(Tournament.Status.NOT_CANCELED)
        notOpenTournamentDto = tournamentService.createTournament(user.getId(), topics, notOpenTournamentDtoInit)

        when:
        tournamentService.joinTournament(user2.getId(), notOpenTournamentDto)

        then: "student cannot join"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_OPEN

        and: "the tournament has no participants"
        def result = tournamentService.getTournamentParticipants(tournamentDto)
        result.size() == 0
    }

    def "Student tries to join not open (early) tournament" () {
        given:
        def user2 = userService.createUser(USER_NAME2, USERNAME2, User.Role.STUDENT)

        and:
        def notOpenTournamentDtoInit = new TournamentDto()
        def notOpenTournamentDto = new TournamentDto()
        notOpenTournamentDtoInit.setStartTime(startTime_Now)
        notOpenTournamentDtoInit.setEndTime(LocalDateTime.now())
        notOpenTournamentDtoInit.setNumberOfQuestions(NUMBER_OF_QUESTIONS1)
        notOpenTournamentDtoInit.setState(Tournament.Status.NOT_CANCELED)
        notOpenTournamentDto = tournamentService.createTournament(user.getId(), topics, notOpenTournamentDtoInit)
        sleep(2000)

        when:
        tournamentService.joinTournament(user2.getId(), notOpenTournamentDto)

        then: "student cannot join"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_OPEN

        and: "the tournament has no participants"
        def result = tournamentService.getTournamentParticipants(tournamentDto)
        result.size() == 0
    }

    def "Student tries to join tournament twice" () {
        given:
        def user2 = userService.createUser(USER_NAME2, USERNAME2, User.Role.STUDENT)
        tournamentService.joinTournament(user2.getId(), tournamentDto)

        when:
        tournamentService.joinTournament(user2.getId(), tournamentDto)

        then: "student cannot join"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.DUPLICATE_TOURNAMENT_PARTICIPANT

        and: "the tournament 1 participants"
        def result = tournamentService.getTournamentParticipants(tournamentDto)
        result.size() == 1

        def resTournamentParticipant1 = result.get(0)

        resTournamentParticipant1.getId() == user2.getId()
        resTournamentParticipant1.getUsername() == USERNAME2
        resTournamentParticipant1.getName() == USER_NAME2
        resTournamentParticipant1.getRole() == User.Role.STUDENT

    }

    def "Non-existing student tries to join canceled tournament" () {
        given:
        def fakeUserId = 99

        when:
        tournamentService.joinTournament(fakeUserId, tournamentDto)

        then: "student cannot join"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_FOUND

        and: "the tournament has no participants"
        def result = tournamentService.getTournamentParticipants(tournamentDto)
        result.size() == 0
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {
        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }

    @TestConfiguration
    static class UserServiceImplTestContextConfiguration {
        @Bean
        UserService userService() {
            return new UserService()
        }
    }

}
