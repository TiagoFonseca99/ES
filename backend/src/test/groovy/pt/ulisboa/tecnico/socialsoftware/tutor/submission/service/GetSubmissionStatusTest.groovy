package pt.ulisboa.tecnico.socialsoftware.tutor.submission.service

import org.h2.engine.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.SubmissionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionService
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.ReviewService
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.ReviewDto
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Review
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import spock.lang.Specification

@DataJpaTest
class GetSubmissionStatusTest extends Specification {
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
    ReviewService reviewService

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
        submission1.setKey(1)
        submission1.setQuestion(question1)
        submission1.setUser(student)
        student.addSubmission(submission1)
        submissionRepository.save(submission1)
        submission2 = new Submission()
        submission2.setKey(2)
        submission2.setQuestion(question2)
        submission2.setUser(student)
        student.addSubmission(submission2)
        submissionRepository.save(submission2)
    }

    def "submission in review and check review status"(){
        given: "a review"
        def reviewDto = new ReviewDto()
        reviewDto.setKey(1)
        reviewDto.setJustification(REVIEW_JUSTIFICATION1)
        reviewDto.setStatus(Review.Status.IN_REVIEW)
        reviewDto.setSubmissionId(submission1.getId())
        reviewDto.setStudentId(submission1.getStudentId())
        reviewService.reviewSubmission(teacher.getId(), reviewDto)

        when:
        def result = submissionService.getSubmissionStatus(student.getId())

        then: "the returned data is correct"
        result.size() == 1
        def subStatus = result.get(0)
        subStatus.getKey() == 1
        subStatus.getJustification() == REVIEW_JUSTIFICATION1
        subStatus.getStatus() == Review.Status.IN_REVIEW
        subStatus.getTeacherId() == teacher.getId()
        subStatus.getStudentId() == submission1.getStudentId()
    }

    def "approve 2 submissions and check review status"(){
        given: "an approved submission review"
        def reviewDto1 = new ReviewDto()
        reviewDto1.setKey(1)
        reviewDto1.setJustification(REVIEW_JUSTIFICATION1)
        reviewDto1.setStatus(Review.Status.APPROVED)
        reviewDto1.setSubmissionId(submission1.getId())
        reviewDto1.setStudentId(submission1.getStudentId())
        reviewService.reviewSubmission(teacher.getId(), reviewDto1)

        and: "another approved submission review"
        def reviewDto2 = new ReviewDto()
        reviewDto2.setKey(2)
        reviewDto2.setJustification(REVIEW_JUSTIFICATION2)
        reviewDto2.setStatus(Review.Status.APPROVED)
        reviewDto2.setSubmissionId(submission2.getId())
        reviewDto2.setStudentId(submission2.getStudentId())
        reviewService.reviewSubmission(teacher.getId(), reviewDto2)

        when:
        def result = submissionService.getSubmissionStatus(student.getId())

        then: "the returned data is correct"
        result.size() == 2
        def subStatus1 = result.get(0)
        def subStatus2 = result.get(1)
        subStatus1.getKey() == 1
        subStatus2.getKey() == 2
        subStatus1.getJustification() == REVIEW_JUSTIFICATION1
        subStatus2.getJustification() == REVIEW_JUSTIFICATION2
        subStatus1.getStatus() == Review.Status.APPROVED
        subStatus2.getStatus() == Review.Status.APPROVED
        subStatus1.getTeacherId() == teacher.getId()
        subStatus2.getTeacherId() == teacher.getId()
        subStatus1.getStudentId() == submission1.getStudentId()
        subStatus2.getStudentId() == submission2.getStudentId()
    }

    def "approve 1 submission and reject 1 submission and check review status"(){
        given: "an approved submission review"
        def reviewDto1 = new ReviewDto()
        reviewDto1.setKey(1)
        reviewDto1.setJustification(REVIEW_JUSTIFICATION1)
        reviewDto1.setStatus(Review.Status.APPROVED)
        reviewDto1.setSubmissionId(submission1.getId())
        reviewDto1.setStudentId(submission1.getStudentId())
        reviewService.reviewSubmission(teacher.getId(), reviewDto1)

        and: "a rejected submission review"
        def reviewDto2 = new ReviewDto()
        reviewDto2.setKey(2)
        reviewDto2.setJustification(REVIEW_JUSTIFICATION2)
        reviewDto2.setStatus(Review.Status.REJECTED)
        reviewDto2.setSubmissionId(submission2.getId())
        reviewDto2.setStudentId(submission2.getStudentId())
        reviewService.reviewSubmission(teacher.getId(), reviewDto2)

        when:
        def result = submissionService.getSubmissionStatus(student.getId())

        then: "the returned data is correct"
        result.size() == 2
        def subStatus1 = result.get(0)
        def subStatus2 = result.get(1)
        subStatus1.getKey() == 1
        subStatus2.getKey() == 2
        subStatus1.getJustification() == REVIEW_JUSTIFICATION1
        subStatus2.getJustification() == REVIEW_JUSTIFICATION2
        subStatus1.getStatus() == Review.Status.APPROVED
        subStatus2.getStatus() == Review.Status.REJECTED
        subStatus1.getTeacherId() == teacher.getId()
        subStatus2.getTeacherId() == teacher.getId()
        subStatus1.getStudentId() == submission1.getStudentId()
        subStatus2.getStudentId() == submission2.getStudentId()
    }

    def "check review status with no submissions"(){
        when:
        def result = submissionService.getSubmissionStatus(student.getId())

        then: "the returned data is correct"
        result.size() == 0
    }

    @TestConfiguration
    static class SubmissionServiceImplTestContextConfiguration {

        @Bean
        SubmissionService submissionService() {
            return new SubmissionService()
        }
    }

    @TestConfiguration
    static class ReviewServiceImplTestContextConfiguration {

        @Bean
        ReviewService reviewService() {
            return new ReviewService()
        }
    }



}
