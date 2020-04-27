package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_MISSING_NUMBER_OF_QUESTIONS
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_MISSING_START_TIME
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_MISSING_END_TIME
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_MISSING

@DataJpaTest
class CreateTournamentTest extends Specification {
    public static final String USER_NAME = "Tiago"
    public static final String USERNAME = "TiagoFonseca99"
    public static final Integer KEY = 1
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
    def startTime = LocalDateTime.now().plusHours(1)
    def endTime = LocalDateTime.now().plusHours(2)
    def formatter

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        user = new User(USER_NAME, USERNAME, KEY, User.Role.STUDENT)

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
    }

    def "create tournament with existing user (student)"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(startTime.format(formatter))
        tournamentDto.setEndTime(endTime.format(formatter))
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        when:
        tournamentService.createTournament(user.getId(), topics, tournamentDto)

        then:
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getId() != null
        result.getStartTime().format(formatter) == startTime.format(formatter)
        result.getEndTime().format(formatter) == endTime.format(formatter)
        result.getTopics() == [topic1, topic2]
        result.getNumberOfQuestions() == NUMBER_OF_QUESTIONS
        result.getState() == Tournament.Status.NOT_CANCELED
        result.getCreator() == user
        result.getCourseExecution() == courseExecution
    }

    def "create tournament with existing user (teacher)" () {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(startTime.format(formatter))
        tournamentDto.setEndTime(endTime.format(formatter))
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        and:
        def teacher = new User("Teste", "Teste", 2, User.Role.TEACHER)
        courseExecution.addUser(teacher)
        courseExecutionRepository.save(courseExecution)
        teacher.addCourse(courseExecution)
        userRepository.save(teacher)

        when:
        tournamentService.createTournament(teacher.getId(), topics, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_STUDENT
        tournamentRepository.count() == 0L
    }

    def "create tournament with existing user (admin)" () {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(startTime.format(formatter))
        tournamentDto.setEndTime(endTime.format(formatter))
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        and:
        def admin = new User("Teste", "Teste", 2, User.Role.ADMIN)
        courseExecution.addUser(admin)
        courseExecutionRepository.save(courseExecution)
        admin.addCourse(courseExecution)
        userRepository.save(admin)

        when:
        tournamentService.createTournament(admin.getId(), topics, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_STUDENT
        tournamentRepository.count() == 0L
    }

    def "create tournament with not existing user"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(startTime.format(formatter))
        tournamentDto.setEndTime(endTime.format(formatter))
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        and:
        def fakeUserId = 99

        when:
        tournamentService.createTournament(fakeUserId, topics, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_FOUND
        tournamentRepository.count() == 0L
    }

    def "create tournament with existing user and topics from different courses"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(startTime.format(formatter))
        tournamentDto.setEndTime(endTime.format(formatter))
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        and: "new course"
        def differentCourse = new Course("TESTE", Course.Type.TECNICO)
        courseRepository.save(differentCourse)

        and: "new topic"
        def topicDto3 = new TopicDto()
        topicDto3.setName("TOPIC3")
        def topic3 = new Topic(differentCourse, topicDto3)
        topicRepository.save(topic3)
        topics.add(topic3.getId())

        when:
        tournamentService.createTournament(user.getId(), topics, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_TOPIC_COURSE
        tournamentRepository.count() == 0L
    }

    def "start time is lower then current time"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(LocalDateTime.now().minusHours(2).format(formatter))
        tournamentDto.setEndTime(endTime.format(formatter))
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        when:
        tournamentService.createTournament(user.getId(), topics, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "start time is higher then end time"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(LocalDateTime.now().plusHours(2).format(formatter))
        tournamentDto.setEndTime(LocalDateTime.now().format(formatter))
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        when:
        tournamentService.createTournament(user.getId(), topics, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "0 topics"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(startTime.format(formatter))
        tournamentDto.setEndTime(endTime.format(formatter))
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)
        topics = new ArrayList<Integer>()

        when:
        tournamentService.createTournament(user.getId(), topics, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "number of questions is negative"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(startTime.format(formatter))
        tournamentDto.setEndTime(endTime.format(formatter))
        tournamentDto.setNumberOfQuestions(-1)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        when:
        tournamentService.createTournament(user.getId(), topics, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "number of questions is zero"() {
        given:
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(startTime.format(formatter))
        tournamentDto.setEndTime(endTime.format(formatter))
        tournamentDto.setNumberOfQuestions(0)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        when:
        tournamentService.createTournament(user.getId(), topics, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    @Unroll
    def "invalid arguments: userId=#userId | num=#num | startTimeE=#startTimeE | endTimeE=#endTimeE || errorMessage"(){
        given: "a tournamentDto"
        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(startTimeE)
        tournamentDto.setEndTime(endTimeE)
        tournamentDto.setNumberOfQuestions(num)
        tournamentDto.setState(Tournament.Status.NOT_CANCELED)

        when:
        tournamentService.createTournament(userId, topics, tournamentDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == errorMessage

        where:
        userId            | num                 | startTimeE          | endTimeE            | errorMessage
        null              | NUMBER_OF_QUESTIONS | "2020-12-20 12:12"  | "2020-12-24 12:12"  | USER_MISSING
        1                 | null                | "2020-12-20 12:12"  | "2020-12-24 12:12"  | TOURNAMENT_MISSING_NUMBER_OF_QUESTIONS
        1                 | NUMBER_OF_QUESTIONS | null                | "2020-12-24 12:12"  | TOURNAMENT_MISSING_START_TIME
        1                 | NUMBER_OF_QUESTIONS | "2020-12-20 12:12"  | null                | TOURNAMENT_MISSING_END_TIME
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {
        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}
