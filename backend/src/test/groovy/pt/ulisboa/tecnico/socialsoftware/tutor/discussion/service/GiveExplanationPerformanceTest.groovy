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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
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
import java.time.LocalDateTime


@DataJpaTest
class GiveExplanationPerformanceTest extends Specification {
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String DISCUSSION_CONTENT = "discussion content"
    public static final String USER_USERNAME = "user username"
    public static final String USER_NAME = "user name"
    public static final String TEACHER_USERNAME = "teacher username"
    public static final String TEACHER_NAME = "teacher name"
    public static final String REPLY_MESSAGE = "reply message"

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


    def "performance test to create 1000 replies"(){
        given: "a student and a teacher"
        def student = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        userRepository.save(student)
        def teacher = new User(TEACHER_NAME, TEACHER_USERNAME, 2, User.Role.TEACHER)
        userRepository.save(teacher)

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

            discussionService.createDiscussion(discussion)

            def reply = new ReplyDto()
            reply.setMessage(REPLY_MESSAGE)
            reply.setUserId(teacher.getId())
            reply.setDate(LocalDateTime.now())

            discussionService.giveReply(reply, discussion)
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
    }


}
