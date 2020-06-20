package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class EditTournamentTest extends Specification {
    public static final String USER_NAME1 = "Tiago"
    public static final String USERNAME1 = "TiagoFonseca99"
    public static final Integer KEY1 = 1
    public static final String USER_NAME2 = "João"
    public static final String USERNAME2 = "JoãoDinis99"
    public static final Integer KEY2 = 2
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
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
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    TopicRepository topicRepository

    def user
    def course
    def courseExecution
    def topic1
    def topic2
    def topicDto1
    def topicDto2
    def topics = new ArrayList<Integer>()
    def startTime = DateHandler.now().plusHours(1)
    def endTime = DateHandler.now().plusHours(2)
    def tournamentDto

    def setup() {
        user = new User(USER_NAME1, USERNAME1, KEY1, User.Role.STUDENT)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecution.addUser(user)
        courseExecutionRepository.save(courseExecution)

        user.addCourse(courseExecution)
        userRepository.save(user)

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

        tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(DateHandler.toISOString(startTime))
        tournamentDto.setEndTime(DateHandler.toISOString(endTime))
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)
    }

    def "user that created tournament changes start time"() {
        given:
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDto)

        and: "new startTime"
        def newStartTime = startTime.plusMinutes(10)
        tournamentDto.setStartTime(DateHandler.toISOString(newStartTime));

        when:
        tournamentService.editStartTime(user.getId(), tournamentDto)

        then:
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        DateHandler.toISOString(result.getStartTime()) == DateHandler.toISOString(newStartTime)
    }

    def "user that created tournament changes end time"() {
        given:
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDto)

        and: "new endTime"
        def newEndTime = startTime.plusMinutes(10)
        tournamentDto.setEndTime(DateHandler.toISOString(newEndTime));

        when:
        tournamentService.editEndTime(user.getId(), tournamentDto)

        then:
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        DateHandler.toISOString(result.getEndTime()) == DateHandler.toISOString(newEndTime)
    }

    def "user that created tournament changes number of questions"() {
        given: "a tournament"
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDto)

        and: "a new number of questions"
        def newNumberOfQuestions = 10

        when:
        tournamentService.editNumberOfQuestions(user.getId(), tournamentDto, newNumberOfQuestions)

        then:
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.numberOfQuestions == newNumberOfQuestions
    }

    def "user that created tournament adds topic of same course"() {
        given: "a tournament"
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDto)

        and: "new topic"
        def topicDto3 = new TopicDto()
        topicDto3.setName("TOPIC3")
        def topic3 = new Topic(course, topicDto3)
        topicRepository.save(topic3)

        when:
        tournamentService.addTopic(user.getId(), tournamentDto, topic3.getId())

        then:
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getTopics() == [topic1, topic2, topic3]
    }

    def "user that created tournament adds topic of different course"() {
        given: "a tournament"
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDto)

        and: "new course"
        def differentCourse = new Course("TESTE", Course.Type.TECNICO)
        courseRepository.save(differentCourse)

        and: "new topic"
        def topicDto3 = new TopicDto()
        topicDto3.setName("TOPIC3")
        def topic3 = new Topic(differentCourse, topicDto3)
        topicRepository.save(topic3)

        when:
        tournamentService.addTopic(user.getId(), tournamentDto, topic3.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_TOPIC_COURSE
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getTopics() == [topic1, topic2]
    }

    def "user that created tournament adds duplicate topic"() {
        given: "a tournament"
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDto)

        when:
        tournamentService.addTopic(user.getId(), tournamentDto, topic1.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.DUPLICATE_TOURNAMENT_TOPIC
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getTopics() == [topic1, topic2]
    }

    def "user that created tournament removes existing topic from tournament that contains that topic"() {
        given: "a tournament"
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDto)

        when:
        tournamentService.removeTopic(user.getId(), tournamentDto, topic2.getId())

        then:
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getTopics() == [topic1]
    }

    def "user that created tournament removes existing topic from tournament that does not contains that topic"() {
        given: "a tournament"
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDto)

        and: "new topic"
        def topicDto3 = new TopicDto()
        topicDto3.setName("TOPIC3")
        def topic3 = new Topic(course, topicDto3)
        topicRepository.save(topic3)

        when:
        tournamentService.removeTopic(user.getId(), tournamentDto, topic3.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_TOPIC_MISMATCH
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getTopics() == [topic1, topic2]
    }

    def "user that created tournament removes existing topic from tournament when only one left"() {
        given: "a tournament"
        topics = [topic1.getId()]
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDto)

        when:
        tournamentService.removeTopic(user.getId(), tournamentDto, topic1.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_HAS_ONLY_ONE_TOPIC
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getTopics() == [topic1]
    }

    def "user that not created tournament changes start time"() {
        given: "a tournament"
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDto)

        and: "a new user"
        def user2 = new User(USER_NAME2, USERNAME2, KEY2, User.Role.STUDENT)
        courseExecution.addUser(user2)
        courseExecutionRepository.save(courseExecution)

        user2.addCourse(courseExecution)
        userRepository.save(user2)

        and: "new startTime"
        def newStartTime = startTime.plusMinutes(10)
        tournamentDto.setStartTime(DateHandler.toISOString(newStartTime));

        when:
        tournamentService.editStartTime(user2.getId(), tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_CREATOR
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        DateHandler.toISOString(result.getStartTime()) == DateHandler.toISOString(startTime)
    }

    def "user that not created tournament changes end time"() {
        given: "a tournament"
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDto)

        and: "a new user"
        def user2 = new User(USER_NAME2, USERNAME2, KEY2, User.Role.STUDENT)
        courseExecution.addUser(user2)
        courseExecutionRepository.save(courseExecution)

        user2.addCourse(courseExecution)
        userRepository.save(user2)

        and: "new endTime"
        def newEndTime = startTime.plusMinutes(10)
        tournamentDto.setEndTime(DateHandler.toISOString(newEndTime));

        when:
        tournamentService.editEndTime(user2.getId(), tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_CREATOR
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        DateHandler.toISOString(result.getEndTime()) == DateHandler.toISOString(endTime)
    }

    def "user that not created tournament changes number of questions"() {
        given: "a tournament"
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDto)

        and: "a new user"
        def user2 = new User(USER_NAME2, USERNAME2, KEY2, User.Role.STUDENT)
        courseExecution.addUser(user2)
        courseExecutionRepository.save(courseExecution)

        user2.addCourse(courseExecution)
        userRepository.save(user2)

        and: "a new number of questions"
        def newNumberOfQuestions = 10

        when:
        tournamentService.editNumberOfQuestions(user2.getId(), tournamentDto, newNumberOfQuestions)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_CREATOR
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getNumberOfQuestions() == NUMBER_OF_QUESTIONS
    }

    def "user that not created tournament adds topic"() {
        given: "a tournament"
        topics = [topic1.getId()]
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDto)

        and: "a new user"
        def user2 = new User(USER_NAME2, USERNAME2, KEY2, User.Role.STUDENT)
        courseExecution.addUser(user2)
        courseExecutionRepository.save(courseExecution)

        user2.addCourse(courseExecution)
        userRepository.save(user2)

        when:
        tournamentService.addTopic(user2.getId(), tournamentDto, topic2.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_CREATOR
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getTopics() == [topic1]
    }

    def "user that not created tournament removes topic"() {
        given: "a tournament"
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDto)

        and: "a new user"
        def user2 = new User(USER_NAME2, USERNAME2, KEY2, User.Role.STUDENT)
        courseExecution.addUser(user2)
        courseExecutionRepository.save(courseExecution)

        user2.addCourse(courseExecution)
        userRepository.save(user2)

        when:
        tournamentService.removeTopic(user2.getId(), tournamentDto, topic2.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_CREATOR
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getTopics() == [topic1, topic2]
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {
        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }

        @Bean
        StatementService statementService() {
            return new StatementService()
        }

        @Bean
        QuizService quizService() {
            return new QuizService()
        }

        @Bean
        AnswerService answerService() {
            return new AnswerService()
        }
        @Bean
        AnswersXmlImport answersXmlImport() {
            return new AnswersXmlImport()
        }

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }

        @Bean
        NotificationService notificationService() {
            return new NotificationService()
        }
    }
}
