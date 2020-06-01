package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.ReplyDto
import spock.lang.Specification
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.ReplyRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository

@DataJpaTest
class RemoveReplyTest extends Specification {
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String DISCUSSION_CONTENT = "discussion content"
    public static final String REPLY_CONTENT = "reply content"
    public static final String USER_USERNAME = "user username"
    public static final String USER_NAME = "user name"

    @Autowired
    DiscussionService discussionService

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
    DiscussionRepository discussionRepository;

    @Autowired
    ReplyRepository replyRepository;

    def question
    def student

    def setup(){
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_TITLE)
        question.setContent(QUESTION_CONTENT)

        questionRepository.save(question)

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
    }

    def "student delete his reply"(){
        given: "a discussion"
        def discussion = new DiscussionDto()
        discussion.setContent(DISCUSSION_CONTENT)
        discussion.setUserId(student.getId())
        discussion.setQuestion(new QuestionDto(question))
        discussionService.createDiscussion(discussion)
        and: "a reply"
        def reply = new ReplyDto()
        reply.setMessage(REPLY_CONTENT)
        reply.setUserId(student.getId())
        def replyDto = discussionService.giveReply(reply, discussion)

        when: "deleting the reply"
        discussionService.removeReply(student.getId(), replyDto)

        then:
        replyRepository.count() == 0L
    }

    def "teacher delete student reply"(){
        given: "a discussion"
        def discussion = new DiscussionDto()
        discussion.setContent(DISCUSSION_CONTENT)
        discussion.setUserId(student.getId())
        discussion.setQuestion(new QuestionDto(question))
        discussionService.createDiscussion(discussion)
        and: "a reply"
        def reply = new ReplyDto()
        reply.setMessage(REPLY_CONTENT)
        reply.setUserId(student.getId())
        def replyDto = discussionService.giveReply(reply, discussion)
        and: "a teacher"
        def teacher = new User(USER_NAME + "1", USER_USERNAME + "1", 3, User.Role.TEACHER);
        userRepository.save(teacher)

        when: "deleting the reply"
        discussionService.removeReply(teacher.getId(), replyDto)

        then:
        replyRepository.count() == 0L
    }

    def "student delete other student's reply"(){
        given: "a discussion"
        def discussion = new DiscussionDto()
        discussion.setContent(DISCUSSION_CONTENT)
        discussion.setUserId(student.getId())
        discussion.setQuestion(new QuestionDto(question))
        discussionService.createDiscussion(discussion)
        and: "a reply"
        def reply = new ReplyDto()
        reply.setMessage(REPLY_CONTENT)
        reply.setUserId(student.getId())
        def replyDto = discussionService.giveReply(reply, discussion)
        and: "another student"
        def other = new User(USER_NAME + "1", USER_USERNAME + "1", 3, User.Role.STUDENT);
        userRepository.save(other)

        when: "deleting the reply"
        discussionService.removeReply(other.getId(), replyDto)

        then:
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.REPLY_UNAUTHORIZED_DELETER
    }

    def "student delete invalid reply"(){
        given: "an invalid reply"
        def reply = new ReplyDto()
        reply.setMessage(REPLY_CONTENT)
        reply.setUserId(student.getId())
        reply.setId(-1)

        when: "deleting the reply"
        discussionService.removeReply(student.getId(), reply)

        then:
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.REPLY_NOT_FOUND
    }

    @TestConfiguration
    static class DiscussionServiceImplTestContextConfiguration {
        @Bean
        DiscussionService discussionService() {
            return new DiscussionService()
        }
    }
}
