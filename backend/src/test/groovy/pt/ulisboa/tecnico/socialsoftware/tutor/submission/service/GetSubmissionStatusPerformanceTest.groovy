package pt.ulisboa.tecnico.socialsoftware.tutor.submission.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.SubmissionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionService
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.ReviewDto
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Review
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import spock.lang.Specification


@DataJpaTest
class GetSubmissionStatusPerformanceTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE1 = "Question1?"
    public static final String QUESTION_CONTENT1 = "Answer1"
    public static final String QUESTION_TITLE2 = "Question2?"
    public static final String QUESTION_CONTENT2 = "Answer2"
    public static final String STUDENT_NAME = "João Silva"
    public static final String STUDENT_USERNAME = "joaosilva"
    public static final String TEACHER_NAME = "Ana Rita"
    public static final String TEACHER_USERNAME = "anarita"
    public static final String REVIEW_JUSTIFICATION1 = "justificacao 1"
    public static final String REVIEW_JUSTIFICATION2 = "justificacao 2"


    @Autowired
    SubmissionService submissionService

    @Autowired
    CourseRepository courseRepository

    @Autowired
    SubmissionRepository submissionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    QuestionRepository questionRepository

    def course
    def student
    def acronym
    def question1
    def question2
    def teacher
    def submission1
    def submission2

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        student = new User(STUDENT_NAME, STUDENT_USERNAME, 1, User.Role.STUDENT)
        userRepository.save(student)
        teacher = new User(TEACHER_NAME, TEACHER_USERNAME, 2, User.Role.TEACHER)
        userRepository.save(teacher)

    }

    def "performance testing to get 1000 submission reviews"(){

        when:
        1.upto(1, {
            question1 = new Question()
            question1.setKey(1)
            question1.setTitle(QUESTION_TITLE1)
            question1.setContent(QUESTION_CONTENT1)
            question1.setCourse(course)
            question1.setStatus(Question.Status.SUBMITTED)
            questionRepository.save(question1)
            question2 = new Question()
            question2.setKey(2)
            question2.setTitle(QUESTION_TITLE2)
            question2.setContent(QUESTION_CONTENT2)
            question2.setCourse(course)
            question2.setStatus(Question.Status.SUBMITTED)
            questionRepository.save(question2)
            submission1 = new Submission()
            submission1.setQuestion(question1)
            submission1.setUser(student)
            student.addSubmission(submission1)
            submissionRepository.save(submission1)
            submission2 = new Submission()
            submission2.setQuestion(question2)
            submission2.setUser(student)
            student.addSubmission(submission2)
            submissionRepository.save(submission2)
            def reviewDto1 = new ReviewDto()
            reviewDto1.setJustification(REVIEW_JUSTIFICATION1)
            reviewDto1.setSubmissionId(submission1.getId())
            reviewDto1.setStudentId(submission1.getStudentId())
            submissionService.reviewSubmission(teacher.getId(), reviewDto1, Review.Status.APPROVED)
            def reviewDto2 = new ReviewDto()
            reviewDto2.setJustification(REVIEW_JUSTIFICATION2)
            reviewDto2.setSubmissionId(submission2.getId())
            reviewDto2.setStudentId(submission2.getStudentId())
            submissionService.reviewSubmission(teacher.getId(), reviewDto2, Review.Status.APPROVED)
            submissionService.getSubmissionStatus(student.getId())
        })

        then:
        true

    }

    @TestConfiguration
    static class SubmissionServiceImplTestContextConfiguration {

        @Bean
        SubmissionService submissionService() {
            return new SubmissionService()
        }
    }

}
