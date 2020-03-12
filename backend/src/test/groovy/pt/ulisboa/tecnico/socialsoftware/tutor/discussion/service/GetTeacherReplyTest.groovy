package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.lang.Specification

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.DiscussionId
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Reply
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.ReplyRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository

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
    DiscussionRepository discussionRepository;

    @Autowired
    ReplyRepository replyRepository;

    def question1
    def question2
    def discussion

    def setup(){
        question1 = new Question()
        question1.setKey(1)
        question1.setTitle(QUESTION_TITLE)
        question1.setContent(QUESTION_CONTENT)

        question2 = new Question()
        question2.setKey(2)
        question2.setTitle(QUESTION_TITLE)
        question2.setContent(QUESTION_CONTENT)

        student1 = new User(USER_NAME, USER_USERNAME, 2, User.Role.STUDENT)
        student2 = new User(USER_NAME + "1", USER_USERNAME + "1", 1, User.Role.STUDENT)
        def teacher = new User(USER_NAME + "2", USER_USERNAME + "2", 2, User.Role.TEACHER)
        userRepository.save(teacher)

        def quiz = new Quiz()
        quiz.setKey(1)

        def quizanswer1 = new QuizAnswer()
        def quizanswer2 = new QuizAnswer()

        def questionanswer1 = new QuestionAnswer()
        def questionanswer2 = new QuestionAnswer()
        def questionanswer3 = new QuestionAnswer()
        questionanswer1.setTimeTaken(1)
        questionanswer2.setTimeTaken(1)
        questionanswer3.setTimeTaken(1)
        def quizquestion1 = new QuizQuestion(quiz, question1, 1)
        def quizquestion2 = new QuizQuestion(quiz, question2, 2)
        def quizquestion3 = new QuizQuestion(quiz, question3, 3)
        questionanswer1.setQuizQuestion(quizquestion1)
        questionanswer1.setQuizAnswer(quizanswer1)
        questionanswer2.setQuizQuestion(quizquestion2)
        questionanswer2.setQuizAnswer(quizanswer2)
        questionanswer3.setQuizQuestion(quizquestion3)
        questionanswer3.setQuizAnswer(quizanswer3)
        questionAnswerRepository.save(questionanswer1)
        questionAnswerRepository.save(questionanswer2)
        questionAnswerRepository.save(questionanswer3)

        quizquestion1.addQuestionAnswer(questionanswer1)
        quizanswer1.addQuestionAnswer(questionanswer1)
        quizquestion2.addQuestionAnswer(questionanswer2)
        quizanswer2.addQuestionAnswer(questionanswer2)
        quizquestion3.addQuestionAnswer(questionanswer3)
        quizanswer3.addQuestionAnswer(questionanswer3)

        quizQuestionRepository.save(quizquestion1)
        quizQuestionRepository.save(quizquestion2)
        quizQuestionRepository.save(quizquestion3)
        quizAnswerRepository.save(quizanswer1)
        quizAnswerRepository.save(quizanswer2)
        quizAnswerRepository.save(quizanswer3)

        quiz.addQuizAnswer(quizanswer1)
        quiz.addQuizQuestion(quizquestion1)
        quiz.addQuizAnswer(quizanswer2)
        quiz.addQuizQuestion(quizquestion2)
        quiz.addQuizAnswer(quizanswer3)
        quiz.addQuizQuestion(quizquestion3)

        quizRepository.save(quiz)

        questionRepository.save(question1)
        questionRepository.save(question2)
        questionRepository.save(question3)

        student1.addQuizAnswer(quizanswer1)
        student1.addQuizAnswer(quizanswer3)
        student2.addQuizAnswer(quizanswer2)

        userRepository.save(student1)
        userRepository.save(student2)

        def discussion1 = new Discussion()
        def discussion2 = new Discussion()
        discussion1.setContent(DISCUSSION_CONTENT)
        discussion1.setQuestion(question1)
        discussion1.setUser(student1)
        discussion2.setContent(DISCUSSION_CONTENT)
        discussion2.setQuestion(question1)
        discussion2.setUser(student2)

        discussionRepository.save(discussion1)
        discussionRepository.save(discussion2)

        def reply1 = new Reply()
        def reply2 = new Reply()
        reply1.setMessage(DISCUSSION_REPLY)
        reply2.setMessage(DISCUSSION_REPLY + "1")

        replyRepository.save(reply1)
        replyRepository.save(reply2)

        student1.addDiscussion(discussion1)
        student2.addDiscussion(discussion2)

        userRepository.save(student1)
        userRepository.save(student2)
    }

    def "get reply of submitted discussion"(){
        given: "a teacher with a submitted reply on student's discussion"
        reply1.setTeacher(teacher)
        discussionService.giveReply(new ReplyDto(reply1), discussion1)

        when: "the student requests the reply to his discussion"
        def result = discussion.getReply(student1.getId(), new DiscussionDto(discussion1))

        then:
        result != null
        result.getMessage() == DISCUSSION_REPLY
        result.getTeacher() == teacher
    }

    def "get reply of submitted discussion without response"(){
        given: "a discussion with no responses"

        when: "the student requests the reply to his discussion"
        def result = discussionService.getReply(student1.getId(), new DiscussionDto(discussion1))

        then: "nothing is returned"
        result == null
    }

    def "get reply of non-submitted discussion on answered question"(){
        given: "a teacher with a submitted reply on other student's discussion"
        reply1.setTeacher(teacher)
        discussionService.giveReply(new ReplyDto(reply1), discussion2)

        when: "the student requests the reply to his discussion"
        discussion.getReply(student1.getId(), new DiscussionDto(discussion2))

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == DISCUSSION_NOT_SUBMITTED_BY_REQUESTER
    }
}
