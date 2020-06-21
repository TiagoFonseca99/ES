package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.ReplyDto
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationService
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
class EditReplyTest extends Specification {
    public static final String ACADEMIC_TERM = "academic term"
    public static final String ACRONYM = "acronym"
    public static final String COURSE_NAME = "course name"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String DISCUSSION_CONTENT = "discussion content"
    public static final String REPLY_CONTENT = "reply content"
    public static final String NEW_CONTENT = "new reply content"
    public static final String USER_USERNAME = "user username"
    public static final String USER_NAME = "user name"

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

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

    def course
    def courseExecution
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

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecution.addUser(student)
        courseExecutionRepository.save(courseExecution)

        student.addCourse(courseExecution)
        userRepository.save(student)
    }

    def "student edit reply"(){
        given: "a discussion"
        def discussion = new DiscussionDto()
        discussion.setContent(DISCUSSION_CONTENT)
        discussion.setUserId(student.getId())
        discussion.setQuestion(new QuestionDto(question))
        discussion.setCourseId(course.getId())
        discussionService.createDiscussion(discussion)
        and: "a reply"
        def reply = new ReplyDto()
        reply.setMessage(REPLY_CONTENT)
        reply.setUserId(student.getId())
        def replyDto = discussionService.createReply(reply, discussion)
        and: "a changed reply"
        replyDto.setMessage(NEW_CONTENT)

        when: "editing the reply"
        def result = discussionService.editReply(student.getId(), replyDto)

        then:
        result.getMessage() == NEW_CONTENT
        replyRepository.count() == 1L
        replyRepository.findAll().get(0).getMessage() == NEW_CONTENT
    }

    def "student change to empty reply"(){
        given: "a discussion"
        def discussion = new DiscussionDto()
        discussion.setContent(DISCUSSION_CONTENT)
        discussion.setUserId(student.getId())
        discussion.setQuestion(new QuestionDto(question))
        discussion.setCourseId(course.getId())
        discussionService.createDiscussion(discussion)
        and: "a reply"
        def reply = new ReplyDto()
        reply.setMessage(REPLY_CONTENT)
        reply.setUserId(student.getId())
        def replyDto = discussionService.createReply(reply, discussion)
        and: "a changed reply"
        replyDto.setMessage("")

        when: "editing the reply"
        discussionService.editReply(student.getId(), replyDto)

        then:
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.REPLY_MISSING_DATA
        replyRepository.count() == 1L
        replyRepository.findAll().get(0).getMessage() == REPLY_CONTENT
    }


    def "teacher edit student's reply"(){
        given: "a discussion"
        def discussion = new DiscussionDto()
        discussion.setContent(DISCUSSION_CONTENT)
        discussion.setUserId(student.getId())
        discussion.setQuestion(new QuestionDto(question))
        discussion.setCourseId(course.getId())
        discussionService.createDiscussion(discussion)
        and: "a teacher"
        def teacher = new User(USER_NAME + "1", USER_USERNAME + "1", 3, User.Role.TEACHER);
        userRepository.save(teacher)
        courseExecution.addUser(teacher)
        courseExecutionRepository.save(courseExecution)
        teacher.addCourse(courseExecution)
        userRepository.save(teacher)
        and: "a reply"
        def reply = new ReplyDto()
        reply.setMessage(REPLY_CONTENT)
        reply.setUserId(student.getId())
        def replyDto = discussionService.createReply(reply, discussion)
        and: "a changed reply"
        replyDto.setMessage(NEW_CONTENT)

        when: "editing the reply"
        def result = discussionService.editReply(teacher.getId(), replyDto)

        then:
        result.getMessage() == NEW_CONTENT
        replyRepository.count() == 1L
        replyRepository.findAll().get(0).getMessage() == NEW_CONTENT
    }

    def "student edit other student's reply"(){
        given: "a discussion"
        def discussion = new DiscussionDto()
        discussion.setContent(DISCUSSION_CONTENT)
        discussion.setUserId(student.getId())
        discussion.setQuestion(new QuestionDto(question))
        discussion.setCourseId(course.getId())
        discussionService.createDiscussion(discussion)
        and: "another student"
        def other = new User(USER_NAME + "1", USER_USERNAME + "1", 3, User.Role.STUDENT);
        userRepository.save(other)
        courseExecution.addUser(other)
        courseExecutionRepository.save(courseExecution)
        other.addCourse(courseExecution)
        userRepository.save(other)
        and: "a reply"
        def reply = new ReplyDto()
        reply.setMessage(REPLY_CONTENT)
        reply.setUserId(student.getId())
        def replyDto = discussionService.createReply(reply, discussion)
        and: "a changed reply"
        replyDto.setMessage(NEW_CONTENT)

        when: "editing the reply"
        discussionService.editReply(other.getId(), replyDto)

        then:
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.REPLY_UNAUTHORIZED_EDITOR
        replyRepository.count() == 1L
        replyRepository.findAll().get(0).getMessage() == REPLY_CONTENT
    }

    def "student edit invalid reply"(){
        given: "an invalid reply"
        def reply = new ReplyDto()
        reply.setMessage(REPLY_CONTENT)
        reply.setUserId(student.getId())
        reply.setId(-1)

        when: "editing the reply"
        discussionService.editReply(student.getId(), reply)

        then:
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.REPLY_NOT_FOUND
    }

    def "student not in course edit reply"(){
        given: "a discussion"
        def discussion = new DiscussionDto()
        discussion.setContent(DISCUSSION_CONTENT)
        discussion.setUserId(student.getId())
        discussion.setQuestion(new QuestionDto(question))
        discussion.setCourseId(course.getId())
        discussionService.createDiscussion(discussion)
        and: "another student"
        def other = new User(USER_NAME + "1", USER_USERNAME + "1", 3, User.Role.STUDENT);
        userRepository.save(other)
        and: "a reply"
        def reply = new ReplyDto()
        reply.setMessage(REPLY_CONTENT)
        reply.setUserId(student.getId())
        def replyDto = discussionService.createReply(reply, discussion)
        and: "a changed reply"
        replyDto.setMessage(NEW_CONTENT)

        when: "editing the reply"
        discussionService.editReply(other.getId(), replyDto)

        then:
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.USER_NOT_IN_COURSE
        replyRepository.count() == 1L
        replyRepository.findAll().get(0).getMessage() == REPLY_CONTENT
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
