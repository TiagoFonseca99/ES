package pt.ulisboa.tecnico.socialsoftware.tutor.submission.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.ImageRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.SubmissionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.ReviewDto
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.SubmissionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import spock.lang.Specification

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.SUBMISSION_MISSING_STUDENT

@DataJpaTest
class GetReviewsToTeacherTest extends Specification {

    public static final String COURSE_NAME = "Arquitetura de Software"
    public static final String APPROVED = 'APPROVED'
    public static final String REJECTED = 'REJECTED'
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String IMAGE_URL = 'URL'
    public static final String REVIEW_JUSTIFICATION1 = 'Porque me apeteceu'
    public static final String REVIEW_JUSTIFICATION2 = 'Porque não me apeteceu'
    public static final Integer IMAGE_WIDTH = 5
    public static final String STUDENT_NAME = "João Silva"
    public static final String STUDENT_USERNAME = "joaosilva"
    public static final String TEACHER_NAME = "Ana Rita"
    public static final String TEACHER_USERNAME = "anarita"


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

    @Autowired
    ImageRepository imageRepository

    def course
    def question
    def student
    def teacher
    def image
    def submission

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
        submission.setQuestion(question)
        submission.setUser(student)
        submissionRepository.save(submission)
    }

    def "submit 1 submission and get a review"(){
        given: "1 submission"
        def reviewDto = new ReviewDto()
        reviewDto.setTeacherId(teacher.getId())
        reviewDto.setStudentId(student.getId())
        reviewDto.setSubmissionId(submission.getId())
        reviewDto.setStatus(APPROVED)
        reviewDto.setJustification(REVIEW_JUSTIFICATION1)
        submissionService.reviewSubmission(teacher.getId(), reviewDto)

        when:
        def result = submissionService.getReviewsToTeacher()

        then: "the returned data is correct"
        result.size() == 1
        def rev = result.get(0)
        rev.getStudentId() == student.getId()
        rev.getSubmissionId() == submission.getId()
        rev.getTeacherId() == teacher.getId()
        rev.getStatus() == APPROVED
        rev.getJustification() == REVIEW_JUSTIFICATION1


    }

    def "submit 2 submissions and get reviews"(){
        given: "2 submissions"
        def reviewDto1 = new ReviewDto()
        reviewDto1.setTeacherId(teacher.getId())
        reviewDto1.setStudentId(student.getId())
        reviewDto1.setSubmissionId(submission.getId())
        reviewDto1.setStatus(APPROVED)
        reviewDto1.setJustification(REVIEW_JUSTIFICATION1)
        submissionService.reviewSubmission(teacher.getId(), reviewDto1)

        def reviewDto2 = new ReviewDto()
        reviewDto2.setTeacherId(teacher.getId())
        reviewDto2.setStudentId(student.getId())
        reviewDto2.setSubmissionId(submission.getId())
        reviewDto2.setStatus(REJECTED)
        reviewDto2.setJustification(REVIEW_JUSTIFICATION2)
        submissionService.reviewSubmission(teacher.getId(), reviewDto2)

        when:
        def result = submissionService.getReviewsToTeacher()

        then: "the returned data is correct"
        result.size() == 2
        def rev1 = result.get(0)
        def rev2 = result.get(1)
        rev1.getStudentId() == student.getId()
        rev1.getSubmissionId() == submission.getId()
        rev1.getTeacherId() == teacher.getId()
        rev1.getStatus() == APPROVED
        rev1.getJustification() == REVIEW_JUSTIFICATION1

        rev2.getStudentId() == student.getId()
        rev2.getSubmissionId() == submission.getId()
        rev2.getTeacherId() == teacher.getId()
        rev2.getStatus() == REJECTED
        rev2.getJustification() == REVIEW_JUSTIFICATION2

    }

    def "get submitted questions with no submissions"(){
        when:
        def result = submissionService.getReviewsToTeacher()

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

}
