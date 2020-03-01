package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
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
    public static final String COURSE_NAME = "Software Architecture"
    public static final String START_TIME = "2020-03-03 10:00"
    public static final String END_TIME = "2020-03-03 12:00"
    public static final String TOPIC_NAME = "Informática"
    public static final int NUMBER_OF_QUESTIONS = 5
    public static final String STATE = "Scheduled"

    @Autowired
    TournamentService tournamentService

    @Autowired
    TopicService topicService

    @Autowired
    CourseRepository courseRepository

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    TopicRepository topicRepository

    def course
    def topicDto
    def tournamentDto

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        topicDto = new TopicDto()
        topicDto.setName(TOPIC_NAME)
        topicService.createTopic(course.getId(), topicDto)

        tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(START_TIME)
        tournamentDto.setEndTime(END_TIME)
        tournamentDto.setTopicName(TOPIC_NAME)
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
    }

    def "create tournament"() {
        given:

        when:
        tournamentService.createTournament(tournamentDto)

        then:
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getID() != null
        result.getStartTime() == START_TIME
        result.getEndTime() == END_TIME
        result.getTopicName() == TOPIC_NAME
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
        tournamentService.createTournament(tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "topic not exists"() {
        given:
        tournamentDto.setTopicName("<Tópico inexistente>")

        when:
        tournamentService.createTournament(tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "number of questions is negative"() {
        given:
        tournamentDto.setNumberOfQuestions(-1)

        when:
        tournamentService.createTournament(tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "number of questions is zero"() {
        given:
        tournamentDto.setNumberOfQuestions(0)

        when:
        tournamentService.createTournament(tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "start time is empty"() {
        given:
        tournamentDto.setStartTime("")

        when:
        tournamentService.createTournament(tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "end time is empty"() {
        given:
        tournamentDto.setEndTime("")

        when:
        tournamentService.createTournament(tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "topic is empty"() {
        given:
        tournamentDto.setTopicName("")

        when:
        tournamentService.createTournament(tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "number of questions is empty"() {
        given:
        tournamentDto.setNumberOfQuestions(null)

        when:
        tournamentService.createTournament(tournamentDto)

        then:
        //def exception = thrown(TournamentException)
        //exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "invalid arguments"() {
        given:

        when:
        tournamentService.createTournament(tournamentDto)

        then:
        tournamentDto.getStartTime().getClass().equals(String.class)
        tournamentDto.getEndTime().getClass().equals(String.class)
        tournamentDto.getTopicName().getClass().equals(String.class)
        tournamentDto.getNumberOfQuestions().getClass().equals(Integer.class)
    }

    @TestConfiguration
    static class QuestionServiceImplTestContextConfiguration {
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
