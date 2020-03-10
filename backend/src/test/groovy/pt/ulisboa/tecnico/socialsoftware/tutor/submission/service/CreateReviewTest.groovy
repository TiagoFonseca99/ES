package pt.ulisboa.tecnico.socialsoftware.tutor.submission.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionService
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class CreateReviewTest extends Specification{
    public static final String COURSE_NAME = "Arquitetura de Software"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String IMAGE_URL = 'URL'
    public static final String REVIEW_JUSTIFICATION = 'Porque me apeteceu'
    public static final Integer IMAGE_WIDTH = 5
    public static final Integer SUBMISSION_ID = 5
    public static final String STUDENT_NAME = "João Silva"
    public static final String STUDENT_USERNAME = "joaosilva"
    public static final String TEACHER_NAME = "Ana Rita"
    public static final String TEACHER_USERNAME = "anarita"

    @Autowired
    CourseRepository courseRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    UserRepository userRepository


    def course
    def student
    def question
    def imageDto
    def submission
    def teacher

    def setup() {

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        student = new User(STUDENT_NAME, STUDENT_USERNAME, 1, User.Role.STUDENT)
        userRepository.save(student)

        teacher = new User(TEACHER_NAME, TEACHER_USERNAME, 2, User.Role.TEACHER)
        userRepository.save(teacher)

        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_TITLE)
        question.setContent(QUESTION_CONTENT)
        question.setCourse(course)
        question.setStatus(Question.Status.SUBMITTED)
        questionRepository.save(question)

        imageDto = new ImageDto()
        imageDto.setWidth(IMAGE_WIDTH)
        imageDto.setUrl(IMAGE_URL)

        submission = new Submission()
        submission.setId(SUBMISSION_ID)
        submission.setKey(1)
        submission.setQuestion(question)
        submission.setUser(student)
        }

    def "create review that accepts question with justification"() {

        given: "a reviewDto"
        def reviewDto = new reviewDto()
        reviewDto.setKey(1)
        reviewDto.setJustification(REVIEW_JUSTIFICATION)
        reviewDto.setStatus(Review.Status.ACCEPTED)
        reviewDto.setSubmissionId(submission.getId())

        when: SubmissionService.reviewSubmission(teacher.getId(), reviewDto)

        then: "the service is in the repository"
        reviewRepository.count() == 1L
        def result = reviewRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getJustification() == REVIEW_JUSTIFICATION
        result.getStatus() == Review.Status.ACCEPTED
        result.getSubmissionId() == submission.getId()

    }


    def "create review without justification"() {

        given: "a reviewDto"
        def reviewDto = new reviewDto()
        reviewDto.setKey(1)
        reviewDto.setStatus(Review.Status.REJECTED)
        reviewDto.setSubmissionId(submission.getId())

        when: SubmissionService.reviewSubmission(teacher.getId(), reviewDto)

        then: "exception is thrown"
        def exception = thrown(ReviewException)
        exception.getErrorMessage() = ErrorMessage.REVIEW_MISSING_DATA

    }


    def "create review that rejects question with justification and image"() {

        given: "a reviewDto"
        def reviewDto = new reviewDto()
        reviewDto.setKey(1)
        reviewDto.setImageDto(imageDto)
        reviewDto.setJustification(REVIEW_JUSTIFICATION)
        reviewDto.setStatus(Review.Status.REJECTED)
        reviewDto.setSubmissionId(submission.getId())

        when: SubmissionService.reviewSubmission(teacher.getId(), reviewDto)

        then: "the service is in the repository"
        reviewRepository.count() == 1L
        def result = reviewRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getJustification() == REVIEW_JUSTIFICATION
        result.getImageDto() == imageDto
        result.getStatus() == Review.Status.REJECTED
        result.getSubmissionId() == submission.getId()

    }


    def "user is not a teacher"(){

        given: "a reviewDto"
        def reviewDto = new ReviewDto()
        reviewDto.setKey(1)
        reviewDto.setJustification(REVIEW_JUSTIFICATION)
        reviewDto.setStatus(Review.Status.REJECTED)
        reviewDto.setSubmissionId(submission.getId())

        when: SubmissionService.reviewSubmission(student.getId(), reviewDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_TEACHER

    }

    /*@TestConfiguration
    static class ReviewServiceImplTestContextConfiguration {

        @Bean
        ReviewService reviewService() {
            return new ReviewService()
        }
    }*/


}

