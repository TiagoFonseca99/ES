package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.CryptoService
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.ServerKeys
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.WorkerService
import spock.lang.Shared
import spock.lang.Specification

import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository
import spock.lang.Unroll

@DataJpaTest
class CreateDiscussionTest extends Specification {
    public static final String ACADEMIC_TERM = "academic term"
    public static final String ACRONYM = "acronym"
    public static final String COURSE_NAME = "course name"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String DISCUSSION_CONTENT = "discussion content"
    public static final String USER_USERNAME = "user username"
    public static final String USER_NAME = "user name"

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    DiscussionService discussionService

    @Autowired
    DiscussionRepository discussionRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    QuestionAnswerRepository questionAnswerRepository

    @Autowired
    QuizRepository quizRepository

    @Autowired
    QuizAnswerRepository quizAnswerRepository

    @Autowired
    QuizQuestionRepository quizQuestionRepository

    @Autowired
    UserRepository userRepository

    @Shared
    def question1
    def question2
    def teacher
    @Shared
    def student
    @Shared
    def course

    def setup(){
        question1 = new Question()
        question1.setKey(1)
        question1.setTitle(QUESTION_TITLE)
        question1.setContent(QUESTION_CONTENT)

        question2 = new Question()
        question2.setKey(2)
        question2.setTitle(QUESTION_TITLE)
        question2.setContent(QUESTION_CONTENT)
        questionRepository.save(question2)

        teacher = new User(USER_NAME + "1", USER_USERNAME + "1", 1, User.Role.TEACHER)
        userRepository.save(teacher)
        student = new User(USER_NAME, USER_USERNAME, 2, User.Role.STUDENT)

        def quiz = new Quiz()
        quiz.setKey(1)
        quiz.setType("TEST")

        def quizanswer = new QuizAnswer()

        def questionanswer = new QuestionAnswer()
        questionanswer.setTimeTaken(1)
        def quizquestion = new QuizQuestion(quiz, question1, 3)
        questionanswer.setQuizQuestion(quizquestion)
        questionanswer.setQuizAnswer(quizanswer)
        questionAnswerRepository.save(questionanswer)

        quizquestion.addQuestionAnswer(questionanswer)
        quizanswer.addQuestionAnswer(questionanswer)

        quizQuestionRepository.save(quizquestion)
        quizAnswerRepository.save(quizanswer)

        quiz.addQuizAnswer(quizanswer)
        quiz.addQuizQuestion(quizquestion)

        quizRepository.save(quiz)


        questionRepository.save(question1)
        student.addQuizAnswer(quizanswer)
        userRepository.save(student)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        def courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecution.addUser(student)
        courseExecution.addUser(teacher)
        courseExecutionRepository.save(courseExecution)

        student.addCourse(courseExecution)
        teacher.addCourse(courseExecution)
        userRepository.save(student)
        userRepository.save(teacher)
    }

    def "create discussion"(){
        given: "a discussionDto"
        def discussionDto = new DiscussionDto()
        discussionDto.setContent(DISCUSSION_CONTENT)
        discussionDto.setCourseId(course.getId())
        and: "a student"
        discussionDto.setUserId(student.getId())
        discussionDto.setQuestion(new QuestionDto(question1))

        when:
        discussionService.createDiscussion(discussionDto)

        then: "the correct discussion is inside the repository"
        discussionRepository.count() == 1L

        def resultRepo = discussionRepository.findAll().get(0)
        resultRepo.getContent() == DISCUSSION_CONTENT
        resultRepo.getUser().getId() == student.getId()
        resultRepo.getQuestion().getId() == question1.getId()

        def resultUserList = discussionService.findDiscussionsByUserId(student.getId(), course.getId())
        resultUserList.size() == 1
        def resultUser = resultUserList.get(0)
        resultUser.getContent() == DISCUSSION_CONTENT
        resultUser.getUserId() == student.getId()
        resultUser.getQuestion().getId() == question1.getId()

        def resultQuestionList = discussionService.findDiscussionsByQuestionId(question1.getId())
        resultQuestionList.size() == 1
        def resultQuestion = resultQuestionList.get(0)
        resultQuestion.getContent() == DISCUSSION_CONTENT
        resultQuestion.getUserId() == student.getId()
        resultQuestion.getQuestion().getId() == question1.getId()
    }

    def "ensure user is student"(){
        given: "a discussionDto"
        def discussionDto = new DiscussionDto()
        and: "set teacher as creator"
        discussionDto.setUserId(teacher.getId())
        discussionDto.setContent(DISCUSSION_CONTENT)
        discussionDto.setQuestion(new QuestionDto(question1))
        discussionDto.setCourseId(course.getId())

        when: "creating discussion"
        discussionService.createDiscussion(discussionDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.DISCUSSION_NOT_STUDENT_CREATOR
    }

    def "student answered the question in discussion"(){
        given: "a discussionDto"
        def discussionDto = new DiscussionDto()
        discussionDto.setContent(DISCUSSION_CONTENT)
        discussionDto.setUserId(student.getId())
        discussionDto.setQuestion(new QuestionDto(question2))
        discussionDto.setCourseId(course.getId())

        when: "creating a discussion on a non answered question"
        discussionService.createDiscussion(discussionDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.QUESTION_NOT_ANSWERED
    }

    def "student can't create 2 discussions to same question"(){
        given: "a student"
        def studentId = student.getId()

        and:"a discussionDto"
        def discussionDto1 = new DiscussionDto()
        discussionDto1.setContent(DISCUSSION_CONTENT)
        discussionDto1.setUserId(studentId)
        discussionDto1.setQuestion(new QuestionDto(question1))
        discussionDto1.setCourseId(course.getId())
        discussionService.createDiscussion(discussionDto1)

        and: "another discussionDto"
        def discussionDto2 = new DiscussionDto()
        discussionDto2.setContent(DISCUSSION_CONTENT)
        discussionDto2.setUserId(studentId)
        discussionDto2.setCourseId(course.getId())
        discussionDto2.setQuestion(new QuestionDto(question1))

        when: "creating the second discussion"
        discussionService.createDiscussion(discussionDto2)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.DUPLICATE_DISCUSSION
    }

    def "student not in course can't create discussion"() {
        given: "a not enrolled student"
        def other = new User(USER_NAME + "2", USER_USERNAME + "2", 4, User.Role.STUDENT)
        userRepository.save(other)
        and: "a discussionDto"
        def discussionDto = new DiscussionDto()
        discussionDto.setContent(DISCUSSION_CONTENT)
        discussionDto.setUserId(other.getId())
        discussionDto.setQuestion(new QuestionDto(question2))
        discussionDto.setCourseId(course.getId())

        when: "creating discussion"
        discussionService.createDiscussion(discussionDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_IN_COURSE
    }

    @Unroll
    def "invalid arguments: question=#question | userId=#userId | content=#content | courseId"(){
        given: "a discusssionDto"
        def discussionDto = new DiscussionDto()
        discussionDto.setQuestion(question)
        discussionDto.setUserId(userId)
        discussionDto.setContent(content)
        discussionDto.setCourseId(courseId)

        when: "creating discussion"
        discussionService.createDiscussion(discussionDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage

        where:
        question                   | userId          | content            | courseId       || errorMessage
        null                       | student.getId() | DISCUSSION_CONTENT | course.getId() || ErrorMessage.DISCUSSION_MISSING_DATA
        new QuestionDto(question1) | null            | DISCUSSION_CONTENT | course.getId() || ErrorMessage.DISCUSSION_MISSING_DATA
        new QuestionDto(question1) | student.getId() | null               | course.getId() || ErrorMessage.DISCUSSION_MISSING_DATA
        new QuestionDto(question1) | student.getId() | "          "       | course.getId() || ErrorMessage.DISCUSSION_MISSING_DATA
        new QuestionDto(question1) | student.getId() | DISCUSSION_CONTENT | null           || ErrorMessage.DISCUSSION_MISSING_DATA
    }

    @TestConfiguration
    static class DiscussionServiceImplTestContextConfiguration {
        @Bean
        DiscussionService discussionService() {
            return new DiscussionService()
        }

        @Bean
        NotificationService notificationService() {
            return new NotificationService()
        }

        @Bean
        WorkerService workerService() {
            return new WorkerService()
        }

        @Bean
        CryptoService cryptoService() {
            return new CryptoService()
        }

        @Bean
        ServerKeys serverKeys() {
            return new ServerKeys()
        }
    }
}
