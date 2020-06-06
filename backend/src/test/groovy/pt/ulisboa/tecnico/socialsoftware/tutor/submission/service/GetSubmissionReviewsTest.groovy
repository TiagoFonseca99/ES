package pt.ulisboa.tecnico.socialsoftware.tutor.submission.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.SubmissionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionService
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.ReviewDto
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import spock.lang.Specification

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_EXECUTION_MISSING
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.REVIEW_MISSING_STUDENT

@DataJpaTest
class GetSubmissionReviewsTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String APPROVED = 'APPROVED'
    public static final String REJECTED = 'REJECTED'
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
    public static final String REVIEW_JUSTIFICATION3 = "justificacao 3"


    @Autowired
    SubmissionService submissionService

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    SubmissionRepository submissionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    QuestionRepository questionRepository

    def course
    def courseExecution
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
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        student = new User(STUDENT_NAME, STUDENT_USERNAME, 1, User.Role.STUDENT)
        userRepository.save(student)
        teacher = new User(TEACHER_NAME, TEACHER_USERNAME, 2, User.Role.TEACHER)
        userRepository.save(teacher)
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
        submission1.setCourseExecution(courseExecution)
        student.addSubmission(submission1)
        submissionRepository.save(submission1)
        submission2 = new Submission()
        submission2.setQuestion(question2)
        submission2.setUser(student)
        submission2.setCourseExecution(courseExecution)
        student.addSubmission(submission2)
        submissionRepository.save(submission2)
    }

    def "check submission review"(){
        given: "a review"
        def reviewDto = new ReviewDto()
        reviewDto.setStatus(APPROVED)
        reviewDto.setJustification(REVIEW_JUSTIFICATION1)
        reviewDto.setSubmissionId(submission1.getId())
        reviewDto.setStudentId(submission1.getStudentId())
        submissionService.reviewSubmission(teacher.getId(), reviewDto,)

        when:
        def result = submissionService.getSubmissionReviews(student.getId(), courseExecution.getId())

        then: "the returned data is correct"
        result.size() == 1
        def rev = result.get(0)
        rev.getJustification() == REVIEW_JUSTIFICATION1
        rev.getStatus() == APPROVED
        rev.getTeacherId() == teacher.getId()
        rev.getStudentId() == submission1.getStudentId()
        rev.getSubmissionId() == submission1.getId()
    }

    def "check 3 submission reviews"(){
        given: "an approved submission review by a teacher"
        def reviewDto1 = new ReviewDto()
        reviewDto1.setStatus(APPROVED)
        reviewDto1.setJustification(REVIEW_JUSTIFICATION1)
        reviewDto1.setSubmissionId(submission1.getId())
        reviewDto1.setStudentId(submission1.getStudentId())
        submissionService.reviewSubmission(teacher.getId(), reviewDto1)

        and: "a rejected submission review by a teacher"
        def reviewDto2 = new ReviewDto()
        reviewDto2.setStatus(REJECTED)
        reviewDto2.setJustification(REVIEW_JUSTIFICATION2)
        reviewDto2.setSubmissionId(submission1.getId())
        reviewDto2.setStudentId(submission1.getStudentId())
        submissionService.reviewSubmission(teacher.getId(), reviewDto2)

        and: "another submission review by a teacher"
        def reviewDto3 = new ReviewDto()
        reviewDto3.setStatus(APPROVED)
        reviewDto3.setJustification(REVIEW_JUSTIFICATION3)
        reviewDto3.setSubmissionId(submission2.getId())
        reviewDto3.setStudentId(submission2.getStudentId())
        submissionService.reviewSubmission(teacher.getId(), reviewDto3)
        when:
        def result = submissionService.getSubmissionReviews(student.getId(), courseExecution.getId())

        then: "the returned data is correct"
        result.size() == 3
        def rev1 = result.get(0)
        def rev2 = result.get(1)
        def rev3 = result.get(2)
        rev1.getJustification() == REVIEW_JUSTIFICATION1
        rev2.getJustification() == REVIEW_JUSTIFICATION2
        rev3.getJustification() == REVIEW_JUSTIFICATION3
        rev1.getStatus() == APPROVED
        rev2.getStatus() == REJECTED
        rev3.getStatus() == APPROVED
        rev1.getTeacherId() == teacher.getId()
        rev2.getTeacherId() == teacher.getId()
        rev3.getTeacherId() == teacher.getId()
        rev1.getStudentId() == student.getId()
        rev2.getStudentId() == student.getId()
        rev3.getStudentId() == student.getId()
        rev1.getStudentId() == submission1.getStudentId()
        rev2.getStudentId() == submission1.getStudentId()
        rev3.getStudentId() == submission2.getStudentId()
        rev1.getSubmissionId() == submission1.getId()
        rev2.getSubmissionId() == submission1.getId()
        rev3.getSubmissionId() == submission2.getId()
    }

    def "check review status with no submissions"(){
        when:
        def result = submissionService.getSubmissionReviews(student.getId(), courseExecution.getId())

        then: "the returned data is correct"
        result.size() == 0
    }

    def "invalid student input"(){
        when:
        submissionService.getSubmissionReviews(null, courseExecution.getId())

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == REVIEW_MISSING_STUDENT
    }

    def "invalid course execution input"(){
        when:
        submissionService.getSubmissionReviews(student.getId(), null)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == COURSE_EXECUTION_MISSING
    }

    @TestConfiguration
    static class SubmissionServiceImplTestContextConfiguration {

        @Bean
        SubmissionService submissionService() {
            return new SubmissionService()
        }
    }

}
