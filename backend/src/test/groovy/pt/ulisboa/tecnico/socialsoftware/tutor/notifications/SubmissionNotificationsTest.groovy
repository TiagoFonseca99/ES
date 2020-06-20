package pt.ulisboa.tecnico.socialsoftware.tutor.submission;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.repository.NotificationRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.ReviewDto
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.SubmissionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.SubmissionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Shared
import spock.lang.Specification

@DataJpaTest
class SubmissionNotificationsTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = "Question?"
    public static final String QUESTION_CONTENT = "Answer"
    public static final String STUDENT_NAME = "João Silva"
    public static final String STUDENT_USERNAME = "joaosilva"
    public static final String TEACHER_NAME = "Ana Rita"
    public static final String TEACHER_USERNAME = "anarita"
    public static final String APPROVED = 'APPROVED'
    public static final String REJECTED = 'REJECTED'
    public static final String REVIEW_JUSTIFICATION = 'Porque me apeteceu'

    @Autowired
    SubmissionService submissionService

    @Autowired
    QuestionService questionService

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    SubmissionRepository submissionRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    NotificationRepository notificationRepository

    def student
    def question1
    def question2
    def question3
    def course
    def courseExecution
    def teacher
    def submissionDto
    def submission
    def reviewDto1
    def reviewDto2
    def reviewDto3


    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        student = new User(STUDENT_NAME, STUDENT_USERNAME, 1, User.Role.STUDENT)
        student.setEnrolledCoursesAcronyms(courseExecution.getAcronym())
        userRepository.save(student)
        teacher = new User(TEACHER_NAME, TEACHER_USERNAME, 2, User.Role.TEACHER)
        courseExecution.addUser(teacher)
        userRepository.save(teacher)
        question1 = new Question()
        question1.setKey(1)
        question1.setTitle(QUESTION_TITLE)
        question1.setContent(QUESTION_CONTENT)
        question1.setCourse(course)
        question1.setStatus(Question.Status.SUBMITTED)
        question2 = new Question()
        question2.setKey(2)
        question2.setTitle(QUESTION_TITLE)
        question2.setContent(QUESTION_CONTENT)
        question2.setCourse(course)
        question2.setStatus(Question.Status.SUBMITTED)
        question3 = new Question()
        question3.setKey(3)
        question3.setTitle(QUESTION_TITLE)
        question3.setContent(QUESTION_CONTENT)
        question3.setCourse(course)
        question3.setStatus(Question.Status.SUBMITTED)
        questionRepository.save(question1)
        questionRepository.save(question2)
        questionRepository.save(question3)
        submissionDto = new SubmissionDto()
        submissionDto.setCourseId(course.getId())
        submissionDto.setCourseExecutionId(courseExecution.getId())
        submissionDto.setStudentId(student.getId())
        submission = new Submission()
        submission.setQuestion(question1)
        submission.setUser(student)
        submission.setCourseExecution(courseExecution)
        submissionRepository.save(submission)
        reviewDto1 = new ReviewDto()
        reviewDto1.setStatus(APPROVED)
        reviewDto1.setJustification(REVIEW_JUSTIFICATION)
        reviewDto1.setSubmissionId(submission.getId())
        reviewDto1.setStudentId(submission.getStudentId())
        reviewDto2 = new ReviewDto()
        reviewDto2.setStatus(APPROVED)
        reviewDto2.setJustification(REVIEW_JUSTIFICATION)
        reviewDto2.setSubmissionId(submission.getId())
        reviewDto2.setStudentId(submission.getStudentId())
        reviewDto3 = new ReviewDto()
        reviewDto3.setStatus(APPROVED)
        reviewDto3.setJustification(REVIEW_JUSTIFICATION)
        reviewDto3.setSubmissionId(submission.getId())
        reviewDto3.setStudentId(submission.getStudentId())
    }

    def "student create submission and teacher receives notification"() {
        expect: "0 notifications"
        notificationRepository.getUserNotifications(teacher.getId()).isEmpty()

        when:
        submissionService.createSubmission(question1.getId(), submissionDto)

        then:
        notificationRepository.getUserNotifications(teacher.getId()).size() == 1

    }

    def "student creates 3 submissions and teacher receives notification"() {
        expect: "0 notifications"
        notificationRepository.getUserNotifications(teacher.getId()).isEmpty()

        when:
        submissionService.createSubmission(question1.getId(), submissionDto)
        submissionService.createSubmission(question2.getId(), submissionDto)
        submissionService.createSubmission(question3.getId(), submissionDto)

        then:
        notificationRepository.getUserNotifications(teacher.getId()).size() == 3

    }

    def "teacher reviews submission and student receives notification"() {
        expect: "0 notifications"
        notificationRepository.getUserNotifications(student.getId()).isEmpty()

        when:
        submissionService.reviewSubmission(teacher.getId(), reviewDto1)

        then:
        notificationRepository.getUserNotifications(student.getId()).size() == 1

    }

    def "teacher reviews 3 submission and student receives notification"() {
        expect: "0 notifications"
        notificationRepository.getUserNotifications(student.getId()).isEmpty()

        when:
        submissionService.reviewSubmission(teacher.getId(), reviewDto1)
        submissionService.reviewSubmission(teacher.getId(), reviewDto2)
        submissionService.reviewSubmission(teacher.getId(), reviewDto3)

        then:
        notificationRepository.getUserNotifications(student.getId()).size() == 3
    }

    def "teacher deletes question and student receives notification"() {
        given: "a question submitted"
        submissionService.createSubmission(question3.getId(), submissionDto)

        expect: "0 notifications"
        notificationRepository.getUserNotifications(student.getId()).isEmpty()

        when:
        questionService.removeQuestion(question3.getId())

        then:
        notificationRepository.getUserNotifications(student.getId()).size() == 1

    }

    def "teacher deletes 2 questions and student receives notification"() {
        given: "2 questions submitted"
        submissionService.createSubmission(question2.getId(), submissionDto)
        submissionService.createSubmission(question3.getId(), submissionDto)

        expect: "0 notifications"
        notificationRepository.getUserNotifications(student.getId()).isEmpty()

        when:
        questionService.removeQuestion(question2.getId())
        questionService.removeQuestion(question3.getId())

        then:
        notificationRepository.getUserNotifications(student.getId()).size() == 2

    }


    @TestConfiguration
    static class SubmissionServiceImplTestContextConfiguration {

        @Bean
        SubmissionService submissionService() {
            return new SubmissionService()
        }

        @Bean
        NotificationService notificationService() {
            return new NotificationService()
        }

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }
    }
}