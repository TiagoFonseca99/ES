package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService
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
import java.time.format.DateTimeFormatter

@DataJpaTest
class JoinTournamentPerformanceTest extends Specification {

    public static final String USER_NAME1 = "Dinis"
    public static final String USERNAME1 = "JDinis99"
    public static final String USER_NAME2 = "Tiago"
    public static final String USERNAME2 = "TiagoFonseca99"
    public static final Integer KEY1 = 1
    public static final Integer KEY2 = 2
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String TOPIC_NAME1 = "Informática"
    public static final String TOPIC_NAME2 = "Engenharia de Software"
    public static final int NUMBER_OF_QUESTIONS1 = 1

    @Autowired
    UserService userService

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
    QuestionRepository questionRepository

    def user
    def course
    def courseExecution
    def topic1
    def topic2
    def topicDto1
    def topicDto2
    def topics = new ArrayList<Integer>()
    def endTime_Now = LocalDateTime.now().plusHours(2)
    def tournamentDtoInit = new TournamentDto()
    def tournamentDto = new TournamentDto()
    def formatter
    def questionOne

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

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

        tournamentDtoInit.setStartTime(LocalDateTime.now().format(formatter))
        tournamentDtoInit.setEndTime(endTime_Now.format(formatter))
        tournamentDtoInit.setNumberOfQuestions(NUMBER_OF_QUESTIONS1)
        tournamentDtoInit.setState(Tournament.Status.NOT_CANCELED)
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDtoInit)

        questionOne = new Question()
        questionOne.setKey(1)
        questionOne.setContent("Question Content")
        questionOne.setTitle("Question Title")
        questionOne.setStatus(Question.Status.AVAILABLE)
        questionOne.setCourse(course)
        questionOne.addTopic(topic1)
        questionRepository.save(questionOne)
    }

    def "performance test to join user 10000 times" () {
        given: "10000 users"
        def user2
        1.upto(1, {
            user2 = new User(USER_NAME2 +it, USERNAME2 +it, KEY2 +it, User.Role.STUDENT)
            user2.addCourse(courseExecution)
            courseExecution.addUser(user2)
            userRepository.save(user2)

            //userRepository.save(new User(USER_NAME2 +it, USERNAME2 +it, KEY2 +it, User.Role.STUDENT))
        })

        when:
        0.upto(1, {tournamentService.joinTournament(user.getId() + it, tournamentDto)})
        1.upto(1, {tournamentService.getTournamentParticipants(tournamentDto)})

        then: "the student has joined the tournament"
        def result = tournamentService.getTournamentParticipants(tournamentDto)
        result.size() == 2
        def resTournamentParticipant1 = result.get(0)

        resTournamentParticipant1.getId() == user.getId()
        resTournamentParticipant1.getUsername() == USERNAME1
        resTournamentParticipant1.getName() == USER_NAME1
        resTournamentParticipant1.getRole() == User.Role.STUDENT

    }




    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {
        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }

        @Bean
        UserService userService() {
            return new UserService()
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

    }


}
