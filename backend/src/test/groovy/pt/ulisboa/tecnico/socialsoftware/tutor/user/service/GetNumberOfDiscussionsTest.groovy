package pt.ulisboa.tecnico.socialsoftware.tutor.user.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.lang.Specification

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository

@DataJpaTest
class GetNumberOfDiscussionsTest extends Specification {
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
    UserService userService

    def student1
    def student2
    def setup(){
        student1 = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        student2 = new User(USER_NAME + "1", USER_USERNAME + "1", 2, User.Role.STUDENT)
        userRepository.save(student1)
        userRepository.save(student2)

        def question1 = new Question()
        question1.setKey(1)
        question1.setTitle(QUESTION_TITLE)
        question1.setContent(QUESTION_CONTENT)

        def question2 = new Question()
        question2.setKey(2)
        question2.setTitle(QUESTION_TITLE)
        question2.setContent(QUESTION_CONTENT)

        questionRepository.save(question1)
        questionRepository.save(question2)

        def discussion1 = new Discussion()
        discussion1.setContent(DISCUSSION_CONTENT)
        discussion1.setUser(student1)
        discussion1.setQuestion(question1)

        def discussion2 = new Discussion()
        discussion2.setContent(DISCUSSION_CONTENT)
        discussion2.setUser(student1)
        discussion2.setQuestion(question2)

        discussionRepository.save(discussion1)
        discussionRepository.save(discussion2)

        student1.addDiscussion(discussion1)
        student1.addDiscussion(discussion2)

        userRepository.save(student1)
    }

    def "get number of discussions from student with 2 discussions"(){
        when: "requesting information"
        def result = userService.getDashboardInfo(student1.getId())

        then:
        result.getNumDiscussions() == 2
    }

    def "get number of discussions from student with 0 discussions"(){
        when: "requesting information"
        def result = userService.getDashboardInfo(student2.getId())

        then:
        result.getNumDiscussions() == 0
    }

    def "get number of discussions from teacher"(){
        given: "a teacher"
        def teacher = new User(USER_NAME + "3", USER_USERNAME + "3", 4, User.Role.TEACHER)
        userRepository.save(teacher)

        when: "requesting information"
        userService.getDashboardInfo(teacher.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_STUDENT
    }

    @TestConfiguration
    static class UserServiceImplTestContextConfiguration {
        @Bean
        UserService userService() {
            return new UserService()
        }
    }
}
