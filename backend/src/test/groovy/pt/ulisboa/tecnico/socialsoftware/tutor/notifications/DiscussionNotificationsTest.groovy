package pt.ulisboa.tecnico.socialsoftware.tutor.notifications

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
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Reply
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.ReplyDto
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.ReplyRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.repository.NotificationRepository
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

import java.time.LocalDateTime

@DataJpaTest
class DiscussionNotificationsTest extends Specification {
    public static final String ACADEMIC_TERM = "academic term"
    public static final String ACRONYM = "acronym"
    public static final String COURSE_NAME = "course name"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String DISCUSSION_CONTENT = "discussion content"
    public static final String DISCUSSION_REPLY = "discussion reply"
    public static final String NEW_CONTENT = "new discussion content"
    public static final String REPLY_CONTENT = "reply content"
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
    NotificationService notificationService

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    QuestionAnswerRepository questionAnswerRepository

    @Autowired
    QuizRepository quizRepository

    @Autowired
    QuizAnswerRepository quizAnswerRepository

    @Autowired
    QuizQuestionRepository quizQuestionRepository

    @Autowired
    ReplyRepository replyRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    NotificationRepository notificationRepository

    def discussion
    def course
    def question
    def reply
    def student1
    def student2
    def teacher1
    def teacher2

    def setup(){
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_TITLE)
        question.setContent(QUESTION_CONTENT)

        student1 = new User(USER_NAME, USER_USERNAME, 0, User.Role.STUDENT)
        student2 = new User(USER_NAME + "1", USER_USERNAME + "1", 1, User.Role.STUDENT)
        teacher1 = new User(USER_NAME + "2", USER_USERNAME + "2", 2, User.Role.TEACHER)
        teacher2 = new User(USER_NAME + "3", USER_USERNAME + "3", 3, User.Role.TEACHER)
        userRepository.save(student1)
        userRepository.save(student2)
        userRepository.save(teacher1)
        userRepository.save(teacher2)

        def quiz = new Quiz()
        quiz.setKey(1)
        quiz.setType("TEST")

        def quizanswer1 = new QuizAnswer()
        def quizanswer2 = new QuizAnswer()

        def questionanswer1 = new QuestionAnswer()
        def questionanswer2 = new QuestionAnswer()
        questionanswer1.setTimeTaken(1)
        questionanswer2.setTimeTaken(1)
        def quizquestion1 = new QuizQuestion(quiz, question, 3)
        def quizquestion2 = new QuizQuestion(quiz, question, 3)
        questionanswer1.setQuizQuestion(quizquestion1)
        questionanswer2.setQuizQuestion(quizquestion2)
        questionanswer1.setQuizAnswer(quizanswer1)
        questionanswer2.setQuizAnswer(quizanswer2)
        questionAnswerRepository.save(questionanswer1)
        questionAnswerRepository.save(questionanswer2)

        quizquestion1.addQuestionAnswer(questionanswer1)
        quizquestion2.addQuestionAnswer(questionanswer2)
        quizanswer1.addQuestionAnswer(questionanswer1)
        quizanswer2.addQuestionAnswer(questionanswer2)

        quizQuestionRepository.save(quizquestion1)
        quizQuestionRepository.save(quizquestion2)
        quizAnswerRepository.save(quizanswer1)
        quizAnswerRepository.save(quizanswer2)

        quiz.addQuizAnswer(quizanswer1)
        quiz.addQuizAnswer(quizanswer2)
        quiz.addQuizQuestion(quizquestion1)
        quiz.addQuizQuestion(quizquestion2)

        quizRepository.save(quiz)


        questionRepository.save(question)
        student1.addQuizAnswer(quizanswer1)
        student2.addQuizAnswer(quizanswer2)
        userRepository.save(student1)
        userRepository.save(student2)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        def courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecution.addUser(student1)
        courseExecution.addUser(student2)
        courseExecution.addUser(teacher1)
        courseExecution.addUser(teacher2)
        courseExecutionRepository.save(courseExecution)

        student1.addCourse(courseExecution)
        student2.addCourse(courseExecution)
        teacher1.addCourse(courseExecution)
        teacher2.addCourse(courseExecution)
        userRepository.save(student1)
        userRepository.save(student2)
        userRepository.save(teacher1)
        userRepository.save(teacher2)

        discussion = new Discussion()
        discussion.setContent(DISCUSSION_CONTENT)
        discussion.setUser(student1)
        discussion.setQuestion(question)
        discussion.setCourse(course)
        discussion.Attach(student1)
        discussion.Attach(teacher1)
        discussion.Attach(teacher2)
        discussionRepository.save(discussion)
        userRepository.save(student1)

        reply = new Reply()
        reply.setMessage(REPLY_CONTENT)
        reply.setDiscussion(discussion)
        reply.setUser(teacher1)
        discussion.addReply(reply)
        replyRepository.save(reply)
        discussionRepository.save(discussion)
        userRepository.save(teacher1)
    }

    def "create discussion"() {
        given: "a discussion"
        def discussionDto = new DiscussionDto()
        discussionDto.setCourseId(course.getId())
        discussionDto.setQuestion(new QuestionDto(question))
        discussionDto.setUserId(student2.getId())
        discussionDto.setContent(DISCUSSION_CONTENT)

        when:
        discussionService.createDiscussion(discussionDto)

        then: "student1 has no notification"
        def result1 = notificationRepository.getUserNotifications(student1.getId())
        result1.size() == 0
        and: "own user has no notification"
        def result2 = notificationRepository.getUserNotifications(student2.getId())
        result2.size() == 0
        and: "all others got notification"
        def result3 = notificationRepository.getUserNotifications(teacher1.getId())
        def result4 = notificationRepository.getUserNotifications(teacher2.getId())
        result3.size() == 1
        result4.size() == 1
        result3.get(0) == result4.get(0)
        result3.get(0).getType() == Notification.Type.DISCUSSION
    }

    def "reply to discussion"() {
        when: "a reply is given"
        discussionService.createReply(new ReplyDto(reply), new DiscussionDto(discussion))

        then: "student2 has no notification"
        def result1 = notificationRepository.getUserNotifications(student2.getId())
        result1.size() == 0
        and: "own user has no notification"
        def result2 = notificationRepository.getUserNotifications(teacher1.getId())
        result2.size() == 0
        and: "all others got notification"
        def result3 = notificationRepository.getUserNotifications(student1.getId())
        def result4 = notificationRepository.getUserNotifications(teacher2.getId())
        result3.size() == 1
        result4.size() == 1
        result3.get(0) == result4.get(0)
        result3.get(0).getType() == Notification.Type.DISCUSSION
    }

    def "edit discussion"() {
        given: "a changed discussion"
        discussion.setContent(NEW_CONTENT)

        when: "editing the discussion"
        discussionService.editDiscussion(student1.getId(), new DiscussionDto(discussion))

        then: "student2 has no notification"
        def result1 = notificationRepository.getUserNotifications(student2.getId())
        result1.size() == 0
        and: "own user has no notification"
        def result2 = notificationRepository.getUserNotifications(student1.getId())
        result2.size() == 0
        and: "all others got notification"
        def result3 = notificationRepository.getUserNotifications(teacher1.getId())
        def result4 = notificationRepository.getUserNotifications(teacher2.getId())
        result3.size() == 1
        result4.size() == 1
        result3.get(0) == result4.get(0)
        result3.get(0).getType() == Notification.Type.DISCUSSION
    }

    def "edit reply"() {
        given: "a changed reply"
        reply.setMessage(NEW_CONTENT)

        when: "editing the reply"
        discussionService.editReply(teacher1.getId(), new ReplyDto(reply))

        then: "student2 has no notification"
        def result1 = notificationRepository.getUserNotifications(student2.getId())
        result1.size() == 0
        and: "own user has no notification"
        def result2 = notificationRepository.getUserNotifications(teacher1.getId())
        result2.size() == 0
        and: "all others got notification"
        def result3 = notificationRepository.getUserNotifications(student1.getId())
        def result4 = notificationRepository.getUserNotifications(teacher2.getId())
        result3.size() == 1
        result4.size() == 1
        result3.get(0) == result4.get(0)
        result3.get(0).getType() == Notification.Type.DISCUSSION
    }

    def "remove discussion"() {
        when: "deleting the discussion"
        discussionService.removeDiscussion(student1.getId(), discussion.getUser().getId(), discussion.getQuestion().getId())

        then: "student2 has no notification"
        def result1 = notificationRepository.getUserNotifications(student2.getId())
        result1.size() == 0
        and: "own user has no notification"
        def result2 = notificationRepository.getUserNotifications(student1.getId())
        result2.size() == 0
        and: "all others got notification"
        def result3 = notificationRepository.getUserNotifications(teacher1.getId())
        def result4 = notificationRepository.getUserNotifications(teacher2.getId())
        result3.size() == 1
        result4.size() == 1
        result3.get(0) == result4.get(0)
        result3.get(0).getType() == Notification.Type.DISCUSSION
    }

    def "remove reply"() {
        when: "deleting the reply"
        discussionService.removeReply(teacher1.getId(), reply.getId())

        then: "student2 has no notification"
        def result1 = notificationRepository.getUserNotifications(student2.getId())
        result1.size() == 0
        and: "own user has no notification"
        def result2 = notificationRepository.getUserNotifications(teacher1.getId())
        result2.size() == 0
        and: "all others got notification"
        def result3 = notificationRepository.getUserNotifications(student1.getId())
        def result4 = notificationRepository.getUserNotifications(teacher2.getId())
        result3.size() == 1
        result4.size() == 1
        result3.get(0) == result4.get(0)
        result3.get(0).getType() == Notification.Type.DISCUSSION
    }

    def "change availability"() {
        when:
        discussionService.setAvailability(teacher1.getId(), new DiscussionDto(discussion))

        then: "student2 has no notification"
        def result1 = notificationRepository.getUserNotifications(student2.getId())
        result1.size() == 0
        and: "own user has no notification"
        def result2 = notificationRepository.getUserNotifications(teacher1.getId())
        result2.size() == 0
        and: "all others got notification"
        def result3 = notificationRepository.getUserNotifications(student1.getId())
        def result4 = notificationRepository.getUserNotifications(teacher2.getId())
        result3.size() == 1
        result4.size() == 1
        result3.get(0) == result4.get(0)
        result3.get(0).getType() == Notification.Type.DISCUSSION
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
