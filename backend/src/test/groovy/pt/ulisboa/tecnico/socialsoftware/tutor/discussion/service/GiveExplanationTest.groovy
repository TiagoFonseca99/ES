package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.lang.Specification

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


@DataJpaTest
class GiveExplanationTest extends Specification {
    public static final String DISCUSSION_REPLY = "discussion reply"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"

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


    def teacher
    def student
    def question

    def setup() {
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_TITLE)
        question.setContent(QUESTION_CONTENT)

        teacher = new User(USER_NAME + "1", USER_USERNAME + "1", 1, User.Role.TEACHER)
        userRepository.save(teacher)

        def quiz = new Quiz()
        quiz.setKey(1)

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

    def "give reply to discussion"(){
        given: "a discussion"
        def discussionDto = new DiscussionDto()
        and: "a student"
        discussionDto.setUserId(user.getId())
        discussionDto.setQuestion(new QuestionDto(question))
        discussionService.createDiscussion(discussionDto)
        and: "a response"
        def replyDto = new ReplyDto(DISCUSSION_REPLY, teacher)

        when: "a reply is given"
        discussionService.giveReply(replyDto, discussionDto)

        then: "the correct reply was given"
        def result = discussionRepository.findAll().get(0)
        result.getReply().getMessage() == DISCUSSION_REPLY
        result.getReply().getTeacher() == teacher
    }

    def "ensure user is teacher"(){
        given: "a discussion"
        def discussionDto = new DiscussionDto()
        and: "a student"
        discussionDto.setUserId(user.getId())
        discussionDto.setQuestion(new QuestionDto(question))
        discussionService.createDiscussion(discussionDto)
        and: "a response created by a student"
        def replyDto = new ReplyDto(DISCUSSION_REPLY, student)
       
        when: "a user creates a reply"
        discussionService.giveReply(replyDto, discussionDto)

        then: "exception given"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.REPLY_NOT_TEACHER_CREATOR
    }

    def "teacher can't submit 2 replies to the same discussion"(){
        given: "a discussion"
        def discussionDto = new DiscussionDto()
        and: "a student"
        discussionDto.setUserId(user.getId())
        discussionDto.setQuestion(new QuestionDto(question))
        discussionService.createDiscussion(discussionDto)
        def replyDto = new ReplyDto(DISCUSSION_REPLY, teacher)
        def replyDto2 = new ReplyDto(DISCUSSION_REPLY, teacher)
        discussionService.giveReply(replyDto, discussionDto)

        when: "another reply is given"
        discussionService.giveReply(replyDto2, discussionDto)

        then: 
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.ANOTHER_REPLY_SUBMITTED
    }

    @TestConfiguration
    static class DiscussionServiceImplTestContextConfiguration {
        @Bean
        DiscussionService discussionService() {
            return new DiscussionService()
        }
    }

}