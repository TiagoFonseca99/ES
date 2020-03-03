package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.lang.Specification

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository

@DataJpaTest
class CreateDiscussionTest extends Specification {
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String DISCUSSION_CONTENT = "discussion content"

    @Autowired
    DiscussionService discussionService

    @Autowired
    DiscussionRepository discussionRepository

    def quizquestion1
    def quizquestion2
    def question1
    def question2
    def questionanswer
    def quizanswer
    def teacher
    def student

    def setup(){
        question1 = new Question()
        question1.setKey(1)
        question1.setContent(QUESTION_TITLE)
        question1.setContent(QUESTION_CONTENT)

        question2 = new Question()
        question2.setKey(2)
        question2.setContent(QUESTION_TITLE)
        question2.setContent(QUESTION_CONTENT)

        teacher = new User()
        teacher.setKey(1)
        teacher.setRole(User.Role.TEACHER)

        student = new User()
        student.setKey(1)
        student.setRole(User.Role.STUDENT)

        quiz = new Quiz()
        quiz.setKey(1)

        quizquestion1 = new QuizQuestion(quiz, question1, 3)
        quizquestion2 = new QuizQuestion(quiz, question2, 4)

        quizanswer = new QuizAnswer()

        questionanswer = new QuestionAnswer()
        questionanswer.setQuizQuestion(quizquestion1)
        questionanswer.setQuizAnswer(quizanswer)

        quizanswer.addQuestionAnswer(questionanswer)

        student.addQuizAnswer(quizanswer)
    }

    def "create discussion"(){
        given: "a discussionDto"
        def discussionDto = new DiscussionDto()
        discussionDto.setKey(1)
        discussionDto.setContent(DISCUSSION_CONTENT)
        discussionDto.setUser(student)

        when:
        discussionService(question1.getId(), discussionDto)

        then: "the correct discussion is inside the repository"
        discussionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getContent() == DISCUSSION_CONTENT
    }

    def "ensure user is student"(){
        given: "a discussionDto"
        def discussionDto = new DiscussionDto()

        when: "adding teacher as creator"
        discussionDto.setUser(teacher)

        then:
        thrown(TutorException)
    }

    def "student answered the question in discussion"(){
        given: "a discussionDto"
        def discussionDto = new DiscussionDto()
        discussionDto.setKey(1)
        discussionDto.setContent(DISCUSSION_CONTENT)
        discussionDto.setUser(student)

        when: "creating a discussion on a non answered question"
        discussionService(question2.getId(), discussionDto)

        then:
        thrown(TutorException)
    }

    def "student can't create 2 discussions to same question"(){
        given: "a discussionDto"
        def discussionDto1 = new DiscussionDto()
        discussionDto1.setKey(1)
        discussionDto1.setContent(DISCUSSION_CONTENT)
        discussionDto1.setUser(student)
        discussionService(question1.getId(), discussionDto1)

        and: "another discussionDto"
        def discussionDto2 = new DiscussionDto()
        discussionDto2.setKey(2)
        discussionDto2.setContent(DISCUSSION_CONTENT)
        discussionDto2.setUser(student)

        when: "creating the second discussion"
        discussionService(question2.getId(), discussionDto)

        then:
        thrown(TutorException)
    }

    @TestConfiguration
    static class DiscussionServiceImplTestContextConfiguration {
        @Bean
        DiscussionService discussionService() {
            return new DiscussionService()
        }
    }
}
