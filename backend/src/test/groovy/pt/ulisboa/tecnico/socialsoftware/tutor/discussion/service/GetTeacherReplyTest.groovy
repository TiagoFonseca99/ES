package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Reply
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.ReplyDto
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.ReplyRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class GetTeacherReplyTest extends Specification {
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String DISCUSSION_CONTENT = "discussion content"
    public static final String DISCUSSION_REPLY = "discussion reply"
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
    def discussion1
    def discussion2
    def reply
    def student
    def teacher

    def setup(){
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_TITLE)
        question.setContent(QUESTION_CONTENT)

        student = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        def student2 = new User(USER_NAME + "1", USER_USERNAME + "1", 2, User.Role.STUDENT)
        teacher = new User(USER_NAME + "2", USER_USERNAME + "2", 3, User.Role.TEACHER)
        userRepository.save(teacher)

        def quiz = new Quiz()
        quiz.setKey(1)
        quiz.setType(Quiz.QuizType.TEST)

        def quizanswer1 = new QuizAnswer()
        def quizanswer2 = new QuizAnswer()

        def questionanswer1 = new QuestionAnswer()
        def questionanswer2 = new QuestionAnswer()
        questionanswer1.setTimeTaken(1)
        questionanswer2.setTimeTaken(1)
        def quizquestion = new QuizQuestion(quiz, question, 1)
        questionanswer1.setQuizQuestion(quizquestion)
        questionanswer1.setQuizAnswer(quizanswer1)
        questionanswer2.setQuizQuestion(quizquestion)
        questionanswer2.setQuizAnswer(quizanswer2)
        questionAnswerRepository.save(questionanswer1)
        questionAnswerRepository.save(questionanswer2)

        quizquestion.addQuestionAnswer(questionanswer1)
        quizanswer1.addQuestionAnswer(questionanswer1)
        quizquestion.addQuestionAnswer(questionanswer2)
        quizanswer2.addQuestionAnswer(questionanswer2)

        quizQuestionRepository.save(quizquestion)
        quizAnswerRepository.save(quizanswer1)
        quizAnswerRepository.save(quizanswer2)

        quiz.addQuizAnswer(quizanswer1)
        quiz.addQuizAnswer(quizanswer2)
        quiz.addQuizQuestion(quizquestion)

        quizRepository.save(quiz)

        questionRepository.save(question)

        student.addQuizAnswer(quizanswer1)
        student2.addQuizAnswer(quizanswer2)

        userRepository.save(student)
        userRepository.save(student2)

        discussion1 = new Discussion()
        discussion2 = new Discussion()
        discussion1.setContent(DISCUSSION_CONTENT)
        discussion1.setQuestion(question)
        discussion1.setUser(student)
        discussion2.setContent(DISCUSSION_CONTENT)
        discussion2.setQuestion(question)
        discussion2.setUser(student2)

        discussionRepository.save(discussion1)
        discussionRepository.save(discussion2)

        reply = new Reply()
        reply.setMessage(DISCUSSION_REPLY)
        reply.setTeacher(teacher)

        replyRepository.save(reply)

        student.addDiscussion(discussion1)
        student2.addDiscussion(discussion2)

        userRepository.save(student)
        userRepository.save(student2)
    }

    def "get reply of submitted discussion"(){
        given: "a teacher with a submitted reply on student's discussion"
        discussionService.giveReply(new ReplyDto(reply), new DiscussionDto(discussion1))

        when: "the student requests the reply to his discussion"
        def result = discussionService.getReply(student.getId(), new DiscussionDto(discussion1))

        then:
        result != null
        result.getMessage() == DISCUSSION_REPLY
        result.getTeacherId() == teacher.getId()
    }

    def "get reply of submitted discussion without response"(){
        given: "a discussion with no responses"

        when: "the student requests the reply to his discussion"
        def result = discussionService.getReply(student.getId(), new DiscussionDto(discussion1))

        then: "nothing is returned"
        result == null
    }

    def "get reply of non-submitted discussion on answered question"(){
        given: "a teacher with a submitted reply on other student's discussion"
        discussionService.giveReply(new ReplyDto(reply), new DiscussionDto(discussion2))

        when: "the student requests the reply to his discussion"
        discussionService.getReply(student.getId(), new DiscussionDto(discussion2))

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.DISCUSSION_NOT_SUBMITTED_BY_REQUESTER
    }

    @TestConfiguration
    static class DiscussionServiceImplTestContextConfiguration {
        @Bean
        DiscussionService discussionService() {
            return new DiscussionService()
        }
    }
}
