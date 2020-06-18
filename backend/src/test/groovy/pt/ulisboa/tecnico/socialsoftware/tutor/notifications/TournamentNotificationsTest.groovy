package pt.ulisboa.tecnico.socialsoftware.tutor.notifications

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.repository.NotificationRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicConjunctionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class TournamentNotificationsTest extends Specification {
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
    public static final int NUMBER_OF_QUESTIONS = 1

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

    @Autowired
    TopicConjunctionRepository topicConjunctionRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    QuizRepository quizRepository

    @Autowired
    QuizQuestionRepository quizQuestionRepository

    @Autowired
    QuizAnswerRepository quizAnswerRepository

    @Autowired
    NotificationRepository notificationRepository

    def user
    def course
    def courseExecution
    def topic1
    def topic2
    def topicDto1
    def topicDto2
    def topics = new ArrayList<Integer>()
    def endTime = DateHandler.now().plusHours(2)
    def tournamentDto = new TournamentDto()
    def questionOne
    def questionTwo

    def setup() {
        user = new User(USER_NAME1, USERNAME1, KEY1, User.Role.STUDENT)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)

        course.addCourseExecution(courseExecution)
        courseExecution.setCourse(course)

        courseRepository.save(course)

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

        tournamentDto.setStartTime(DateHandler.toISOString(DateHandler.now()))
        tournamentDto.setEndTime(DateHandler.toISOString(endTime))
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDto)

        questionOne = new Question()
        questionOne.setKey(1)
        questionOne.setContent("Question Content")
        questionOne.setTitle("Question Title")
        questionOne.setStatus(Question.Status.AVAILABLE)
        questionOne.setCourse(course)
        questionOne.addTopic(topic1)
        questionRepository.save(questionOne)

        questionTwo = new Question()
        questionTwo.setKey(2)
        questionTwo.setContent("Question Content")
        questionTwo.setTitle("Question Title")
        questionTwo.setStatus(Question.Status.AVAILABLE)
        questionTwo.setCourse(course)
        questionTwo.addTopic(topic1)
        questionRepository.save(questionTwo)
    }

    def "user joins tournament, cancels it and receives a notification"() {
        given: "user joins a tournament"
        tournamentService.joinTournament(user.getId(), tournamentDto, "")

        expect: "0 notifications"
        notificationRepository.getUserNotifications(user.getId()).isEmpty()

        when:
        tournamentService.cancelTournament(user.getId(), tournamentDto)

        then:
        def result = notificationRepository.getUserNotifications(user.getId())
        result.size() == 1
        result.get(0).getType() == Notification.Type.TOURNAMENT
    }

    def "user joins tournament, edits start time and receives a notification"() {
        given: "user joins a tournament"
        tournamentService.joinTournament(user.getId(), tournamentDto, "")

        and: "new startTime"
        def newStartTime = DateHandler.now().plusMinutes(10)
        tournamentDto.setStartTime(DateHandler.toISOString(newStartTime));

        expect: "0 notifications"
        notificationRepository.getUserNotifications(user.getId()).isEmpty()

        when:
        tournamentService.editStartTime(user.getId(), tournamentDto)

        then:
        def result = notificationRepository.getUserNotifications(user.getId())
        result.size() == 1
        result.get(0).getType() == Notification.Type.TOURNAMENT
    }

    def "user joins tournament, edits start time, leaves tournament and still has notifications"() {
        given: "user joins a tournament"
        tournamentService.joinTournament(user.getId(), tournamentDto, "")

        and: "new startTime"
        def newStartTime = DateHandler.now().plusMinutes(10)
        tournamentDto.setStartTime(DateHandler.toISOString(newStartTime));

        expect: "0 notifications"
        notificationRepository.getUserNotifications(user.getId()).isEmpty()

        when:
        tournamentService.editStartTime(user.getId(), tournamentDto)

        then: "1 notification"
        def result = notificationRepository.getUserNotifications(user.getId())
        result.size() == 1
        result.get(0).getType() == Notification.Type.TOURNAMENT

        when:
        tournamentService.leaveTournament(user.getId(), tournamentDto)

        then: "1 notification"
        def result2 = notificationRepository.getUserNotifications(user.getId())
        result2.size() == 1
        result2.get(0).getType() == Notification.Type.TOURNAMENT
    }

    def "user joins tournament, edits end time and receives a notification"() {
        given: "user joins a tournament"
        tournamentService.joinTournament(user.getId(), tournamentDto, "")

        and: "new endTime"
        def newEndTime = endTime.plusMinutes(10)
        tournamentDto.setEndTime(DateHandler.toISOString(newEndTime));

        expect: "0 notifications"
        notificationRepository.getUserNotifications(user.getId()).isEmpty()

        when:
        tournamentService.editEndTime(user.getId(), tournamentDto)

        then:
        def result = notificationRepository.getUserNotifications(user.getId())
        result.size() == 1
        result.get(0).getType() == Notification.Type.TOURNAMENT
    }

    def "user joins tournament, edits end time, leaves tournament and still has notifications"() {
        given: "user joins a tournament"
        tournamentService.joinTournament(user.getId(), tournamentDto, "")

        and: "new endTime"
        def newEndTime = endTime.plusMinutes(10)
        tournamentDto.setEndTime(DateHandler.toISOString(newEndTime));

        expect: "0 notifications"
        notificationRepository.getUserNotifications(user.getId()).isEmpty()

        when:
        tournamentService.editEndTime(user.getId(), tournamentDto)

        then: "1 notification"
        def result = notificationRepository.getUserNotifications(user.getId())
        result.size() == 1
        result.get(0).getType() == Notification.Type.TOURNAMENT

        when:
        tournamentService.leaveTournament(user.getId(), tournamentDto)

        then: "1 notification"
        def result2 = notificationRepository.getUserNotifications(user.getId())
        result2.size() == 1
        result2.get(0).getType() == Notification.Type.TOURNAMENT
    }

    def "user joins tournament, adds a topic and receives a notification"() {
        given: "user joins a tournament"
        tournamentService.joinTournament(user.getId(), tournamentDto, "")

        and: "a new topic"
        def topicDto3 = new TopicDto()
        topicDto3.setName("new topic")
        def topic3 = new Topic(course, topicDto3)
        topicRepository.save(topic3)

        expect: "0 notifications"
        notificationRepository.getUserNotifications(user.getId()).isEmpty()

        when:
        tournamentService.addTopic(user.getId(), tournamentDto, topic3.getId())

        then:
        def result = notificationRepository.getUserNotifications(user.getId())
        result.size() == 1
        result.get(0).getType() == Notification.Type.TOURNAMENT
    }

    def "user joins tournament, adds a topic, leaves tournament and still has notifications"() {
        given: "user joins a tournament"
        tournamentService.joinTournament(user.getId(), tournamentDto, "")

        and: "a new topic"
        def topicDto3 = new TopicDto()
        topicDto3.setName("new topic")
        def topic3 = new Topic(course, topicDto3)
        topicRepository.save(topic3)

        expect: "0 notifications"
        notificationRepository.getUserNotifications(user.getId()).isEmpty()

        when:
        tournamentService.addTopic(user.getId(), tournamentDto, topic3.getId())

        then: "1 notification"
        def result = notificationRepository.getUserNotifications(user.getId())
        result.size() == 1
        result.get(0).getType() == Notification.Type.TOURNAMENT

        when:
        tournamentService.leaveTournament(user.getId(), tournamentDto)

        then: "1 notification"
        def result2 = notificationRepository.getUserNotifications(user.getId())
        result2.size() == 1
        result2.get(0).getType() == Notification.Type.TOURNAMENT
    }

    def "user joins tournament, removes a topic and receives a notification"() {
        given: "user joins a tournament"
        tournamentService.joinTournament(user.getId(), tournamentDto, "")

        expect: "0 notifications"
        notificationRepository.getUserNotifications(user.getId()).isEmpty()

        when:
        tournamentService.removeTopic(user.getId(), tournamentDto, topic2.getId())

        then:
        def result = notificationRepository.getUserNotifications(user.getId())
        result.size() == 1
        result.get(0).getType() == Notification.Type.TOURNAMENT
    }

    def "user joins tournament, removes a topic, leaves tournament and still has notifications"() {
        given: "user joins a tournament"
        tournamentService.joinTournament(user.getId(), tournamentDto, "")

        expect: "0 notifications"
        notificationRepository.getUserNotifications(user.getId()).isEmpty()

        when:
        tournamentService.removeTopic(user.getId(), tournamentDto, topic2.getId())

        then: "1 notification"
        def result = notificationRepository.getUserNotifications(user.getId())
        result.size() == 1
        result.get(0).getType() == Notification.Type.TOURNAMENT

        when:
        tournamentService.leaveTournament(user.getId(), tournamentDto)

        then: "1 notification"
        def result2 = notificationRepository.getUserNotifications(user.getId())
        result2.size() == 1
        result2.get(0).getType() == Notification.Type.TOURNAMENT
    }

    def "two users have both one notification"() {
        given: "a new user"
        def user2 = new User(USER_NAME2, USERNAME2, KEY2, User.Role.STUDENT)
        courseExecution.addUser(user2)
        courseExecutionRepository.save(courseExecution)

        user2.addCourse(courseExecution)
        userRepository.save(user2)

        and: "a new tournament"
        def tournamentDto2 = tournamentService.createTournament(user2.getId(), topics, tournamentDto)

        and: "users join a tournament"
        tournamentService.joinTournament(user.getId(), tournamentDto, "")
        tournamentService.joinTournament(user2.getId(), tournamentDto2, "")

        and: "new startTime"
        def newStartTime = DateHandler.now().plusMinutes(10)
        tournamentDto.setStartTime(DateHandler.toISOString(newStartTime));
        tournamentDto2.setStartTime(DateHandler.toISOString(newStartTime));

        expect: "0 notifications"
        notificationRepository.getUserNotifications(user.getId()).isEmpty()
        notificationRepository.getUserNotifications(user2.getId()).isEmpty()

        when:
        tournamentService.editStartTime(user.getId(), tournamentDto)
        tournamentService.editStartTime(user2.getId(), tournamentDto2)

        then:
        def result = notificationRepository.getUserNotifications(user.getId())
        result.size() == 1
        result.get(0).getType() == Notification.Type.TOURNAMENT
        def result2 = notificationRepository.getUserNotifications(user2.getId())
        result2.size() == 1
        result2.get(0).getType() == Notification.Type.TOURNAMENT
    }

    def "two users have both one notification and one leaves tournament"() {
        given: "a new user"
        def user2 = new User(USER_NAME2, USERNAME2, KEY2, User.Role.STUDENT)
        courseExecution.addUser(user2)
        courseExecutionRepository.save(courseExecution)

        user2.addCourse(courseExecution)
        userRepository.save(user2)

        and: "a new tournament"
        def tournamentDto2 = tournamentService.createTournament(user2.getId(), topics, tournamentDto)

        and: "users join a tournament"
        tournamentService.joinTournament(user.getId(), tournamentDto, "")
        tournamentService.joinTournament(user2.getId(), tournamentDto2, "")

        and: "new startTime"
        def newStartTime = DateHandler.now().plusMinutes(10)
        tournamentDto.setStartTime(DateHandler.toISOString(newStartTime));
        tournamentDto2.setStartTime(DateHandler.toISOString(newStartTime));

        expect: "0 notifications"
        notificationRepository.getUserNotifications(user.getId()).isEmpty()
        notificationRepository.getUserNotifications(user2.getId()).isEmpty()

        when:
        tournamentService.editStartTime(user.getId(), tournamentDto)
        tournamentService.editStartTime(user2.getId(), tournamentDto2)

        then:
        def result = notificationRepository.getUserNotifications(user.getId())
        result.size() == 1
        result.get(0).getType() == Notification.Type.TOURNAMENT
        def result2 = notificationRepository.getUserNotifications(user2.getId())
        result2.size() == 1
        result2.get(0).getType() == Notification.Type.TOURNAMENT

        when:
        tournamentService.leaveTournament(user2.getId(), tournamentDto2)

        then:
        def result3 = notificationRepository.getUserNotifications(user.getId())
        result3.size() == 1
        result3.get(0).getType() == Notification.Type.TOURNAMENT
        def result4 = notificationRepository.getUserNotifications(user2.getId())
        result4.size() == 1
        result4.get(0).getType() == Notification.Type.TOURNAMENT
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
