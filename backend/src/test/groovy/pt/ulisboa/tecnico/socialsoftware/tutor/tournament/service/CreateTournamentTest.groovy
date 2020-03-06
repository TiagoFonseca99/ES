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
class CreateTournamentTest extends Specification {
    public static final String USER_NAME = "Tiago"
    public static final String USERNAME = "TiagoFonseca99"
    public static final Integer KEY = 1
    public static final String COURSE_NAME = "Software Architecture"
    public static final String TOPIC_NAME1 = "Informática"
    public static final String TOPIC_NAME2 = "Engenharia de Software"
    public static final int NUMBER_OF_QUESTIONS = 5

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
    def topicsDto = new ArrayList<TopicDto>()
    def startTime = LocalDateTime.now().plusHours(1)
    def endTime = LocalDateTime.now().plusHours(2)

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

        topicsDto.add(topicDto1)
        topicsDto.add(topicDto2)
    }

    def "create tournament"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(startTime)
        tournamentDto.setEndTime(endTime)
        tournamentDto.addTopics(topicsDto)
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getId() != null
        result.getStartTime() == startTime
        result.getEndTime() == endTime
        result.getTopics() == [topic1, topic2]
        result.getNumberOfQuestions() == NUMBER_OF_QUESTIONS
        result.getState() == Tournament.Status.NOT_CANCELED
        result.getCreator() == user
    }

    def "start time is lower then current time"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(LocalDateTime.now().minusHours(2))
        tournamentDto.setEndTime(LocalDateTime.now().plusHours(2))
        tournamentDto.addTopics(topicsDto)
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L

    }

    //def "tournament is created by a student"() TODO
    //def "tournament is created by a teacher"() TODO
    //def "add existing topic"() TODO
    //def "remove topic when only one left"() TODO

    def "start time is higher then end time"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(LocalDateTime.now().plusHours(2))
        tournamentDto.setEndTime(LocalDateTime.now())
        tournamentDto.addTopics(topicsDto)
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "topic not exists"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(LocalDateTime.now())
        tournamentDto.setEndTime(LocalDateTime.now().plusHours(2))
        tournamentDto.addTopics(topicsDto)
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)
        topics = new ArrayList<Integer>()

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "number of questions is negative"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(LocalDateTime.now())
        tournamentDto.setEndTime(LocalDateTime.now().plusHours(2))
        tournamentDto.addTopics(topicsDto)
        tournamentDto.setNumberOfQuestions(-1)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "number of questions is zero"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(LocalDateTime.now())
        tournamentDto.setEndTime(LocalDateTime.now().plusHours(2))
        tournamentDto.addTopics(topicsDto)
        tournamentDto.setNumberOfQuestions(0)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    /*def "start time is empty"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(LocalDateTime.now())
        tournamentDto.setEndTime(LocalDateTime.now().plusHours(2))
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "end time is empty"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(LocalDateTime.now())
        tournamentDto.setEndTime(LocalDateTime.now().plusHours(2))
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }*/

    /*def "number of questions is empty"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(START_TIME)
        tournamentDto.setEndTime(END_TIME)
        tournamentDto.setNumberOfQuestions(null)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }*/

    /*def "invalid arguments"() {
        // TODO FAZER O QUE O STOR FEZ
        given:

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        tournamentDto.getStartTime().getClass().equals(String.class)
        tournamentDto.getEndTime().getClass().equals(String.class)
        tournamentDto.getTopicName().getClass().equals(String.class)
        tournamentDto.getNumberOfQuestions().getClass().equals(Integer.class)
    }*/

    // TODO
    /*def "duplicate tournament"() {
        given:
        // dar set a um Id que exista
        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        tournamentRepository.count() == 0L
    }*/

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {
        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}
