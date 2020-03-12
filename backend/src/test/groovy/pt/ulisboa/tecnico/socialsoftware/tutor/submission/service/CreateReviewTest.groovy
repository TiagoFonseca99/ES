package pt.ulisboa.tecnico.socialsoftware.tutor.submission.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.ImageRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionService
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.ReviewRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.SubmissionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.ReviewService
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Review
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.ReviewDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class CreateReviewTest extends Specification {

    public static final String COURSE_NAME = "Arquitetura de Software"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String IMAGE_URL = 'URL'
    public static final String REVIEW_JUSTIFICATION = 'Porque me apeteceu'
    public static final Integer IMAGE_WIDTH = 5
    public static final String STUDENT_NAME = "João Silva"
    public static final String STUDENT_USERNAME = "joaosilva"
    public static final String TEACHER_NAME = "Ana Rita"
    public static final String TEACHER_USERNAME = "anarita"

    @Autowired
    ReviewService reviewService

    @Autowired
    CourseRepository courseRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    ReviewRepository reviewRepository

    @Autowired
    SubmissionRepository submissionRepository

    @Autowired
    ImageRepository imageRepository

    def course
    def student
    def question
    def imageDto
    def submission
    def teacher
    def image

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

        image = new Image()
        image.setWidth(IMAGE_WIDTH)
        image.setUrl(IMAGE_URL)
        imageRepository.save(image)

        submission = new Submission()
        submission.setKey(1)
        submission.setQuestion(question)
        submission.setUser(student)
        submissionRepository.save(submission)
        }


    def "create review that accepts question with justification"() {

        given: "a reviewDto"
        def reviewDto = new ReviewDto()
        reviewDto.setKey(1)
        reviewDto.setJustification(REVIEW_JUSTIFICATION)
        reviewDto.setStatus(Review.Status.APPROVED)
        reviewDto.setSubmissionId(submission.getId())
        reviewDto.setStudentId(submission.getUser().getId())

        when: reviewService.reviewSubmission(teacher.getId(), reviewDto)

        then: "the service is in the repository"
        reviewRepository.count() == 1L
        def result = reviewRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getJustification() == REVIEW_JUSTIFICATION
        result.getStatus() == Review.Status.APPROVED
        result.getSubmission() == submission

    }


    def "create review without justification"() {

        given: "a reviewDto"
        def reviewDto = new ReviewDto()
        reviewDto.setKey(1)
        reviewDto.setStatus(Review.Status.REJECTED)
        reviewDto.setSubmissionId(submission.getId())
        reviewDto.setStudentId(submission.getUser().getId())

        when: reviewService.reviewSubmission(teacher.getId(), reviewDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.REVIEW_MISSING_DATA

    }


    def "create review that rejects question with justification and image"() {

        given: "a reviewDto"
        def reviewDto = new ReviewDto()
        reviewDto.setKey(1)
        reviewDto.setImageDto(new ImageDto(image))
        reviewDto.setJustification(REVIEW_JUSTIFICATION)
        reviewDto.setStatus(Review.Status.REJECTED)
        reviewDto.setSubmissionId(submission.getId())
        reviewDto.setStudentId(submission.getUser().getId())

        when: reviewService.reviewSubmission(teacher.getId(), reviewDto)

        then: "the service is in the repository"
        reviewRepository.count() == 1L
        def result = reviewRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getJustification() == REVIEW_JUSTIFICATION
        result.getImage().getId() != null
        result.getImage().getUrl() == IMAGE_URL
        result.getImage().getWidth() == IMAGE_WIDTH
        result.getStatus() == Review.Status.REJECTED
        result.getSubmission() == submission

    }


    def "user is not a teacher"(){

        given: "a reviewDto"
        def reviewDto = new ReviewDto()
        reviewDto.setKey(1)
        reviewDto.setJustification(REVIEW_JUSTIFICATION)
        reviewDto.setStatus(Review.Status.REJECTED)
        reviewDto.setSubmissionId(submission.getId())
        reviewDto.setStudentId(submission.getUser().getId())

        when: reviewService.reviewSubmission(student.getId(), reviewDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_TEACHER

    }

    @TestConfiguration
    static class ReviewServiceImplTestContextConfiguration {

        @Bean
        ReviewService reviewService() {
            return new ReviewService()
        }
    }


}

