package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.lang.Specification

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository

@DataJpaTest
class GetQuestionPublicDiscussionsTest extends Specification {
    public static final String DISCUSSION_CONTENT = "discussion content"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String USER_USERNAME = "user username"
    public static final String USER_NAME = "user name"

    @Autowired
    DiscussionRepository discussionRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    DiscussionService discussionService

    def student1
    def student2
    def student3
    def question1
    def question2
    def question3
    def question4

    def setup(){
        student1 = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        student2 = new User(USER_NAME + "1", USER_USERNAME + "1", 2, User.Role.STUDENT)
        student3 = new User(USER_NAME + "2", USER_USERNAME + "2", 3, User.Role.STUDENT)

        userRepository.save(student1)
        userRepository.save(student2)
        userRepository.save(student3)

        question1 = new Question()
        question1.setKey(1)
        question1.setContent(QUESTION_CONTENT)
        question1.setTitle(QUESTION_TITLE)
        questionRepository.save(question1)

        question2 = new Question()
        question2.setKey(2)
        question2.setContent(QUESTION_CONTENT + '1')
        question2.setTitle(QUESTION_TITLE + '1')
        questionRepository.save(question2)

        question3 = new Question()
        question3.setKey(3)
        question3.setContent(QUESTION_CONTENT + '2')
        question3.setTitle(QUESTION_TITLE + '2')
        questionRepository.save(question3)

        def discussion1 = new Discussion()
        discussion1.setContent(DISCUSSION_CONTENT)
        discussion1.setUser(student1)
        discussion1.setQuestion(question1)

        def discussion2 = new Discussion()
        discussion2.setContent(DISCUSSION_CONTENT)
        discussion2.setUser(student1)
        discussion2.setQuestion(question2)

        def discussion3 = new Discussion()
        discussion3.setContent(DISCUSSION_CONTENT)
        discussion3.setUser(student2)
        discussion3.setQuestion(question2)
        discussion3.setAvailability(true)

        discussionRepository.save(discussion1)
        discussionRepository.save(discussion2)
        discussionRepository.save(discussion3)
    }

    def "student with no discussions asks for question discussions"(){
        when: "asking for discussions"
        def result = discussionService.getPublicDiscussionsByQuestion(student3.getId(), question2.getId())

        then: "1 is returned"
        result.size == 1
        def discussion = result.get(0)
        discussion.getUserId() == student2.getId()
        discussion.getQuestionId() == question2.getId()
        discussion.getContent() == DISCUSSION_CONTENT
    }

    def "student with public discussion asks for question discussions"(){
        when: "asking for discussions"
        def result = discussionService.getPublicDiscussionsByQuestion(student2.getId(), question2.getId())

        then: "1 is returned"
        result.size == 1
        def discussion = result.get(0)
        discussion.getUserId() == student2.getId()
        discussion.getQuestionId() == question2.getId()
        discussion.getContent() == DISCUSSION_CONTENT
    }

    def "student with private discussion asks for question discussions"(){
        when: "asking for discussions"
        def result = discussionService.getPublicDiscussionsByQuestion(student1.getId(), question2.getId())

        then: "2 are returned (own and public)"
        result.size == 2
    }

    def "student asks for question without discussions"() {
        when: "asking for discussions"
        def result = discussionService.getPublicDiscussionsByQuestion(student3.getId(), question3.getId())

        then: "no discussions returned"
        result.size == 0
    }

    def "student with no discussions asks for question with only private discussions"() {
        when: "asking for discussions"
        def result = discussionService.getPublicDiscussionsByQuestion(student3.getId(), question1.getId())

        then: "no discussions returned"
        result.size == 0
    }

    def "teacher asks for question with only private discussions"(){
        given: "a teacher"
        def teacher = new User(USER_NAME + "3", USER_USERNAME + "3", 4, User.Role.TEACHER)
        userRepository.save(teacher)

        when: "asking for discussions"
        def result = discussionService.getPublicDiscussionsByQuestion(teacher.getId(), question1.getId())

        then: "no discussions returned"
        result.size == 0
    }

    def "teacher asks for question with public discussions"(){
        given: "a teacher"
        def teacher = new User(USER_NAME + "3", USER_USERNAME + "3", 4, User.Role.TEACHER)
        userRepository.save(teacher)

        when: "asking for discussions"
        def result = discussionService.getPublicDiscussionsByQuestion(teacher.getId(), question2.getId())

        then: "1 discussion is returned"
        result.size == 1
        def discussion = result.get(0)
        discussion.getUserId() == student2.getId()
        discussion.getQuestionId() == question2.getId()
        discussion.getContent() == DISCUSSION_CONTENT
    }

    @TestConfiguration
    static class DiscussionServiceImplTestContextConfiguration {
        @Bean
        DiscussionService discussionService() {
            return new DiscussionService()
        }
    }
}
