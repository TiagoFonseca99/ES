package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class CreateDiscussionPerformanceTest extends Specification {
    public static final String ACADEMIC_TERM = "academic term"
    public static final String ACRONYM = "acronym"
    public static final String COURSE_NAME = "course name"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String DISCUSSION_CONTENT = "discussion content"
    public static final String USER_USERNAME = "user username"
    public static final String USER_NAME = "user name"

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    CourseRepository courseRepository

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
    QuizAnswerRepository quizAnswerRepository;

    @Autowired
    QuizQuestionRepository quizQuestionRepository;

    @Autowired
    UserRepository userRepository


    def "performance test to create 5000 discussions"(){
        given: "a student"
        def student = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        userRepository.save(student)
        and: "a course"
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        def courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecution.addUser(student)
        courseExecutionRepository.save(courseExecution)

        student.addCourse(courseExecution)
        userRepository.save(student)

        and: "a quiz"
        def quiz = new Quiz()
        quiz.setKey(1)
        quiz.setType("TEST")
        quizRepository.save(quiz)

        when:
        1.upto(1, {
            def question = new Question()
            question.setKey(1)
            question.setTitle(QUESTION_TITLE)
            question.setContent(QUESTION_CONTENT)
            questionRepository.save(question)
            def quizanswer = new QuizAnswer()

            def questionanswer = new QuestionAnswer()
            questionanswer.setTimeTaken(1)
            def quizquestion = new QuizQuestion(quiz, question, 3)
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

            questionRepository.save(question)
            student.addQuizAnswer(quizanswer)
            userRepository.save(student)

            def discussion = new DiscussionDto()
            discussion.setUserId(student.getId())
            discussion.setQuestion(new QuestionDto(question))
            discussion.setContent(DISCUSSION_CONTENT)
            discussion.setCourseId(course.getId())

            discussionService.createDiscussion(discussion)
        }
        )

        then:
        true
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
    }
}
