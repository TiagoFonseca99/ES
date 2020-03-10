package pt.ulisboa.tecnico.socialsoftware.tutor.submission.service

import org.h2.engine.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionService
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import spock.lang.Specification

@DataJpaTest
class CreateSubmissionTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = "Question?"
    public static final String QUESTION_CONTENT = "Answer"
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
    UserRepository userRepository

    @Autowired
    QuestionRepository questionRepository

    def course
    def student
    def acronym
    def question
    def teacher
    def submission
    def submissionDto

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
        submission = new Submission()
        submission.setKey(1)
        submission.setQuestion(question)
        submission.setUser(student)
        student.addSubmission(submission)
    }

    def "submission in review and check review status"(){
        given: "a review"
        def review = new Review()
        review.setJustification(REVIEW_JUSTIFICATION1)
        review.setStatus(Review.Status.IN_REVIEW)
        review.setSubmission(submission)
        review.setUser(teacher)

        when:
        def result = submissionService.getSubmissionStatus(student)

        then: "the returned data is correct"
        result.size() == 1
        def subStatus = result.get(0)
        subStatus.getKey() == 1
        subStatus.getJustification() == REVIEW_JUSTIFICATION1
        subStatus.getRole() == Review.Status.IN_REVIEW
        subStatus.getTeacherId() == teacher.getId()
    }

    def "approve 2 submissions and check review status"(){
        given: "an approved submission review"
        def review1 = new Review()
        review1.setJustification(REVIEW_JUSTIFICATION1)
        review1.setStatus(Review.Status.APPROVED)
        review1.setSubmission(submission)
        review1.setUser(teacher)

        and: "another approved submission review"
        def review2 = new Review()
        review2.setJustification(REVIEW_JUSTIFICATION2)
        review2.setStatus(Review.Status.APPROVED)
        review2.setSubmission(submission)
        review2.setUser(teacher)

        when:
        def result = submissionService.getSubmissionStatus(student)

        then: "the returned data is correct"
        result.size() == 2
        def subStatus1 = result.get(0)
        def subStatus2 = result.get(1)
        subStatus1.getKey() == 1
        subStatus2.getKey() == 1
        subStatus1.getJustification() == REVIEW_JUSTIFICATION1
        subStatus2.getJustification() == REVIEW_JUSTIFICATION2
        subStatus1.getRole() == Review.Status.APPROVED
        subStatus2.getRole() == Review.Status.APPROVED
        subStatus1.getTeacherId() == teacher.getId()
        subStatus2.getTeacherId() == teacher.getId()
    }

    def "approve 1 submission and reject 1 submission and check review status"(){
        given: "an approved submission review"
        def review1 = new Review()
        review1.setJustification(REVIEW_JUSTIFICATION1)
        review1.setStatus(Review.Status.ACCEPT)
        review1.setSubmission(submission)
        review1.setUser(teacher)

        and: "a rejected submission review"
        def review2 = new Review()
        review2.setJustification(REVIEW_JUSTIFICATION2)
        review2.setStatus(Review.Status.REJECTED)
        review2.setSubmission(submission)
        review2.setUser(teacher)

        when:
        def result = submissionService.getSubmissionStatus(student)

        then: "the returned data is correct"
        result.size() == 2
        def subStatus1 = result.get(0)
        def subStatus2 = result.get(1)
        subStatus1.getKey() == 1
        subStatus2.getKey() == 1
        subStatus1.getJustification() == REVIEW_JUSTIFICATION1
        subStatus2.getJustification() == REVIEW_JUSTIFICATION2
        subStatus1.getRole() == Review.Status.APPROVED
        subStatus2.getRole() == Review.Status.REJECTED
        subStatus1.getTeacherId() == teacher.getId()
        subStatus2.getTeacherId() == teacher.getId()
    }

    def "check review status with no submissions"(){
        when:
        def result = submissionService.getSubmissionStatus(student)

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
