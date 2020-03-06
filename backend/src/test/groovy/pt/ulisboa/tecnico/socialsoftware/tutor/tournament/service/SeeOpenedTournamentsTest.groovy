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
import spock.lang.Specification

import java.time.LocalDateTime

@DataJpaTest
class SeeOpenedTournamentsTest extends Specification {

    public static final String USER_NAME = "Dinis"
    public static final String USERNAME = "JDinis99"
    public static final Integer KEY = 1
    public static final String COURSE_NAME = "Software Architecture"
    public static final String TOPIC_NAME1 = "Informática"
    public static final String TOPIC_NAME2 = "Engenharia de Software"
    public static final int NUMBER_OF_QUESTIONS1 = 5
    public static final int NUMBER_OF_QUESTIONS2 = 7

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

    def startTime_Later = LocalDateTime.now().plusHours(1)
    def endTime_Later = LocalDateTime.now().plusHours(2)

    def setup() {
        user = new User(USER_NAME, USERNAME, KEY, User.Role.STUDENT)
        userRepository.save(user)

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



    }

    def "create 2 tournaments "() {
        given:
        def tournamentDto1 = new TournamentDto()
        tournamentDto1.setStartTime(startTime_Now)
        tournamentDto1.setEndTime(endTime_Now)
        tournamentDto1.setNumberOfQuestions(NUMBER_OF_QUESTIONS1)
        tournamentDto1.setState(Tournament.Status.NOT_CANCELED)
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto1)

        and:
        def tournamentDto2 = new TournamentDto()
        tournamentDto2.setStartTime(startTime_Now)
        tournamentDto2.setEndTime(endTime_Now)
        tournamentDto2.setNumberOfQuestions(NUMBER_OF_QUESTIONS2)
        tournamentDto2.setState(Tournament.Status.NOT_CANCELED)
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto2)

        when:
        def result = tournamentService.seeOpenedTournaments()

        then: "the returned data are correct"
        result().size == 2
        def resTournament1 = result.get(0)
        def resTournament2 = result.get(1)

        resTournament1.getCreator() == user
        resTournament1.getStartTime == startTime_Now
        resTournament2.getEndTime == endTime_Now
        resTournament1.getNumberofQuestions == NUMBER_OF_QUESTIONS1
        resTournament1.getState == Not_CANCELED
        resTournament1.getTopics

        expect:false
    }

    def "criar 2 torneios abertos e 1 fechado e ver abertos"() {
        expect:false
    }

    def "criar 0 torneios e ver abertos"() {
        expect:false
    }

    def "criar 1 torneio fechado e ver abertos"() {
        expect:false
    }



}