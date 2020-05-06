package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.ReplyRepository
import spock.lang.Specification

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
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
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.ReplyDto
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository
import java.time.LocalTime;

@DataJpaTest
class SetAvailabilityTest extends Specification {
    public static final String DISCUSSION_REPLY = "discussion reply"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String DISCUSSION_CONTENT = "discussion content"
    public static final String USER_USERNAME = "user username"
    public static final String USER_NAME = "user name"

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

    @Autowired
    ReplyRepository replyRepository


    def teacher
    def student
    def question
    def discussion

    def setup() {
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_TITLE)
        question.setContent(QUESTION_CONTENT)

        teacher = new User(USER_NAME + "1", USER_USERNAME + "1", 1, User.Role.TEACHER)
        userRepository.save(teacher)
        student = new User(USER_NAME, USER_USERNAME, 2, User.Role.STUDENT)

        def quiz = new Quiz()
        quiz.setKey(1)
        quiz.setType("TEST")

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

        discussion = new Discussion()
        discussion.setContent(DISCUSSION_CONTENT)
        discussion.setUser(student)
        discussion.setQuestion(question)
        discussionRepository.save(discussion)
        userRepository.save(student)
    }

    def "set discussion public"() {
        when: "Setting the discussion public"
        discussionService.setAvailability(student.getId(), question.getId(), true)

        then: "check the discussion is public"
        def result = discussionRepository.findByUserId(student.getId()).get(0)
        result.isAvailable() == true
    }

    def "set discussion private"() {
        when: "Setting the discussion private"
        discussionService.setAvailability(student.getId(), question.getId(), false)

        then: "check the discussion is private"
        def result2 = discussionRepository.findByUserId(student.getId()).get(0)
        result2.isAvailable() == false
    }

    @TestConfiguration
    static class DiscussionServiceImplTestContextConfiguration {
        @Bean
        DiscussionService discussionService() {
            return new DiscussionService()
        }
    }
}