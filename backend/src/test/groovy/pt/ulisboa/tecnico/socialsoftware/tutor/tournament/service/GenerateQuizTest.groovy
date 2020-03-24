/*package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
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
class GenerateQuizTest extends Specification {

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
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
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
    def startTime_Now
    def endTime_Now = LocalDateTime.now().plusHours(2)
    def tournamentDtoInit = new TournamentDto()
    def tournamentDto = new TournamentDto()
    def formatter

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

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

        tournamentDtoInit.setStartTime(LocalDateTime.now().format(formatter))
        tournamentDtoInit.setEndTime(endTime_Now.format(formatter))
        tournamentDtoInit.setNumberOfQuestions(NUMBER_OF_QUESTIONS1)
        tournamentDtoInit.setState(Tournament.Status.NOT_CANCELED)
        tournamentDto = tournamentService.createTournament(user.getId(), topics, tournamentDtoInit)
    }

    def "1 student join an open tournament and generates tournament"() {
        given:
        def user2 = new User(USER_NAME2, USERNAME2, KEY2, User.Role.STUDENT)
        user2.addCourse(courseExecution)
        userRepository.save(user2)

        tournamentService.joinTournament(user2.getId(), tournamentDto)

        when:
        def result = tournamentService.getQuiz(tournamentDto)

        then: "a quiz was generated"
        result.getType() == Quiz.QuizType.GENERATED
        result.version == '1'
        result.getNumberOfQuestions() == NUMBER_OF_QUESTIONS1

    }

    def "1 student join a schedule tournament and generates quiz"() {
        expect false
    }

    def "check quiz of a tournament with no quiz"() {
        expect false
    }

    def "check quiz of unexisting tournament"() {
        expect false
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
*/