package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import spock.lang.Specification

@DataJpaTest
class CreateTournamentTest extends Specification {
    public static final String USER_NAME = "Tiago"
    public static final String USERNAME = "TiagoFonseca99"
    public static final Integer KEY = 1
    public static final String COURSE_NAME = "Software Architecture"
    public static final String START_TIME = "2020-03-03 10:00"
    public static final String END_TIME = "2020-03-03 12:00"
    public static final String TOPIC_NAME1 = "Informática"
    public static final String TOPIC_NAME2 = "Engenharia de Software"
    public static final int NUMBER_OF_QUESTIONS = 5

    @Autowired
    TournamentService tournamentService

    @Autowired
    TopicService topicService

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
    def topicDto1
    def topicDto2
    def topics
    def tournamentDto

    def setup() {
        user = new User(USER_NAME, USERNAME, KEY, User.Role.STUDENT)
        userRepository.save(user)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        topicDto1 = new TopicDto()
        topicDto1.setName(TOPIC_NAME1)
        topicService.createTopic(course.getId(), topicDto1)

        topicDto2 = new TopicDto()
        topicDto2.setName(TOPIC_NAME2)
        topicService.createTopic(course.getId(), topicDto2)

        //topics.add(topicRepository.findAll().get(0).getId())
        //topics.add(topicRepository.findAll().get(1).getId())
        topics = topicRepository.findAll()
        
        tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(START_TIME)
        tournamentDto.setEndTime(END_TIME)
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.SCHEDULED)
    }

    def "create tournament"() {
        given:

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getId() != null
        result.getStartTime() == START_TIME
        result.getEndTime() == END_TIME
        result.getTopics() == [TOPIC_NAME1, TOPIC_NAME2]
        result.getNumberOfQuestions() == NUMBER_OF_QUESTIONS
        result.getState() == STATE
        result.getCreator() != null
    }

    /*def "start time is lower then current time"() {
        given:


        when:


        then:

    }*/

    def "start time is higher then end time"() {
        given:
        tournamentDto.setStartTime("2020-03-03 12:00")
        tournamentDto.setEndTime("2020-03-03 10:00")

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    // Makes sense??!?!?!?! TODO TODO TODO TODO TODO
    def "topic not exists"() {
        given:
        //tournamentDto.setTopicName("<Tópico inexistente>")

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "number of questions is negative"() {
        given:
        tournamentDto.setNumberOfQuestions(-1)

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "number of questions is zero"() {
        given:
        tournamentDto.setNumberOfQuestions(0)

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "start time is empty"() {
        given:
        tournamentDto.setStartTime("")

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "end time is empty"() {
        given:
        tournamentDto.setEndTime("")

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "topic is empty"() {
        given:
        topics = []

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "number of questions is empty"() {
        given:
        tournamentDto.setNumberOfQuestions(null)

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "invalid arguments"() {
        // TODO FAZER O QUE O STOR FEZ
        given:

        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        tournamentDto.getStartTime().getClass().equals(String.class)
        tournamentDto.getEndTime().getClass().equals(String.class)
        tournamentDto.getTopicName().getClass().equals(String.class)
        tournamentDto.getNumberOfQuestions().getClass().equals(Integer.class)
    }

    // TODO
    def "duplicate tournament"() {
        given:
        // dar set a um Id que exista
        when:
        tournamentService.createTournament(user.getUsername(), topics, tournamentDto)

        then:
        tournamentRepository.count() == 0L
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {
        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }

        @Bean
        TopicService topicService() {
            return new TopicService()
        }
    }
}
