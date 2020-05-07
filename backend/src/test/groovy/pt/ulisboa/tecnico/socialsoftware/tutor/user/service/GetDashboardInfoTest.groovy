package pt.ulisboa.tecnico.socialsoftware.tutor.user.service

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
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
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
import spock.lang.Specification

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository

@DataJpaTest
class GetDashboardInfoTest extends Specification {
    public static final String DISCUSSION_CONTENT = "discussion content"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String USER_USERNAME = "user username"
    public static final String USER_NAME = "user name"
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String TOPIC_NAME1 = "Informática"
    public static final String TOPIC_NAME2 = "Engenharia de Software"
    public static final int NUMBER_OF_QUESTIONS = 1


    @Autowired
    DiscussionRepository discussionRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    UserService userService

    @Autowired
    TournamentService tournamentService

    def student1
    def student2
    def tournamentDto1 = new TournamentDto()

    def setup(){
        student1 = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        student2 = new User(USER_NAME + "1", USER_USERNAME + "1", 2, User.Role.STUDENT)

        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        def courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)

        course.addCourseExecution()
        courseRepository.save(course)
        courseExecution.addUser(student1)
        courseExecutionRepository.save(courseExecution)

        student1.addCourse(courseExecution)
        userRepository.save(student1)
        userRepository.save(student2)

        def topics = new ArrayList<Integer>()

        def topicDto1 = new TopicDto()
        topicDto1.setName(TOPIC_NAME1)
        def topic1 = new Topic(course, topicDto1)
        topicRepository.save(topic1)

        def topicDto2 = new TopicDto()
        topicDto2.setName(TOPIC_NAME2)
        def topic2 = new Topic(course, topicDto2)
        topicRepository.save(topic2)

        topics.add(topic1.getId())
        topics.add(topic2.getId())

        def question1 = new Question()
        question1.setKey(1)
        question1.setContent(QUESTION_CONTENT)
        question1.setTitle(QUESTION_TITLE)
        question1.setStatus(Question.Status.AVAILABLE)
        question1.setCourse(course)
        question1.addTopic(topic1)
        questionRepository.save(question1)

        def question2 = new Question()
        question2.setKey(2)
        question2.setContent(QUESTION_CONTENT + '1')
        question2.setTitle(QUESTION_TITLE + '1')
        question2.setStatus(Question.Status.AVAILABLE)
        question2.setCourse(course)
        question2.addTopic(topic2)
        questionRepository.save(question2)

        def discussion1 = new Discussion()
        discussion1.setContent(DISCUSSION_CONTENT)
        discussion1.setUser(student1)
        discussion1.setQuestion(question1)

        def discussion2 = new Discussion()
        discussion2.setContent(DISCUSSION_CONTENT)
        discussion2.setUser(student1)
        discussion2.setQuestion(question2)

        discussionRepository.save(discussion1)
        discussionRepository.save(discussion2)

        student1.addDiscussion(discussion1)
        student1.addDiscussion(discussion2)

        userRepository.save(student1)

        def startTime = DateHandler.now().plusHours(1)
        def endTime = DateHandler.now().plusHours(2)

        tournamentDto1 = new TournamentDto()
        tournamentDto1.setStartTime(DateHandler.toISOString(startTime))
        tournamentDto1.setEndTime(DateHandler.toISOString(endTime))
        tournamentDto1.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto1.setState(Tournament.Status.NOT_CANCELED)


        tournamentDto1 = tournamentService.createTournament(student1.getId(), topics, tournamentDto1)
    }

    def "get number of discussions from student with 2 discussions"(){
        when: "requesting information"
        def result = userService.getDashboardInfo(student1.getId())

        then:
        result.getNumDiscussions() == 2
    }

    def "get number of discussions from student with 0 discussions"(){
        when: "requesting information"
        def result = userService.getDashboardInfo(student2.getId())

        then:
        result.getNumDiscussions() == 0
    }

    def "get number of public discussions from student with 0 public discussions"(){
        when: "requesting information"
        def result = userService.getDashboardInfo(student1.getId())

        then:
        result.getNumPublicDiscussions() == 0
    }

    def "get number of public discussions from student with 1 public discussion"(){
        given: "1 discussion made public"
        userRepository.findByUsername("user username").getDiscussions().stream().findFirst().get().setAvailability(true);

        when: "requesting information"
        def result = userService.getDashboardInfo(student1.getId())

        then:
        result.getNumPublicDiscussions() == 1
    }

    def "get number of discussions from teacher"(){
        given: "a teacher"
        def teacher = new User(USER_NAME + "3", USER_USERNAME + "3", 4, User.Role.TEACHER)
        userRepository.save(teacher)

        when: "requesting information"
        userService.getDashboardInfo(teacher.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_STUDENT
    }

    def "get number of tournaments from student enrolled in 1 tournament"() {
        given: "student enrolled in tournament 1"
        tournamentService.joinTournament(student1.getId(), tournamentDto1)

        when: "requesting information"
        def result = userService.getDashboardInfo(student1.getId())

        then:
        result.getJoinedTournaments().size() == 1
    }

    def "get number of tournaments from student enrolled in 0 tournament"() {
        given:

        when: "requesting information"
        def result = userService.getDashboardInfo(student1.getId())

        then:
        result.getJoinedTournaments().size() == 0
    }

    @TestConfiguration
    static class UserServiceImplTestContextConfiguration {
        @Bean
        UserService userService() {
            return new UserService()
        }

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
    }
}
