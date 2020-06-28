package pt.ulisboa.tecnico.socialsoftware.tutor.submission.service


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.SubmissionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.SubmissionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.CryptoService
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.ServerKeys
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.WorkerService
import spock.lang.Specification

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_EXECUTION_MISSING
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.SUBMISSION_MISSING_STUDENT

@DataJpaTest
class GetSubmissionsTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE1 = "Question1?"
    public static final String QUESTION_CONTENT1 = "Answer1"
    public static final String QUESTION_TITLE2 = "Question2?"
    public static final String QUESTION_CONTENT2 = "Answer2"
    public static final String STUDENT_NAME = "João Silva"
    public static final String STUDENT_USERNAME = "joaosilva"



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

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        student = new User(STUDENT_NAME, STUDENT_USERNAME, 1, User.Role.STUDENT)
        student.setEnrolledCoursesAcronyms(courseExecution.getAcronym())
        userRepository.save(student)

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
    }

    def "submit 2 questions and get submissions"(){
        given: "2 submissions"
        def submissionDto1 = new SubmissionDto()
        submissionDto1.setCourseId(course.getId())
        submissionDto1.setCourseExecutionId(courseExecution.getId())
        submissionDto1.setStudentId(student.getId())
        submissionService.createSubmission(question1.getId(), submissionDto1)

        def submissionDto2 = new SubmissionDto()
        submissionDto2.setCourseId(course.getId())
        submissionDto2.setCourseExecutionId(courseExecution.getId())
        submissionDto2.setStudentId(student.getId())
        submissionService.createSubmission(question2.getId(), submissionDto2)

        when:
        def result = submissionService.getSubmissions(student.getId(), courseExecution.getId())

        then: "the returned data is correct"
        result.size() == 2
        def sub1 = result.get(0)
        def sub2 = result.get(1)
        sub1.getStudentId() == student.getId()
        sub2.getStudentId() == student.getId()
        sub1.getCourseId() == course.getId()
        sub2.getCourseId() == course.getId()
        sub1.getCourseExecutionId() == course.getId()
        sub2.getCourseExecutionId() == courseExecution.getId()
        sub1.getQuestionDto().getId() == question1.getId()
        sub2.getQuestionDto().getId() == question2.getId()
    }

    def "get submitted questions with no submissions"(){
        when:
        def result = submissionService.getSubmissions(student.getId(), courseExecution.getId())

        then: "the returned data is correct"
        result.size() == 0
    }

    def "invalid student input"(){
        when:
        submissionService.getSubmissions(null, courseExecution.getId())

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == SUBMISSION_MISSING_STUDENT
    }

    def "invalid course execution input"(){
        when:
        submissionService.getSubmissions(student.getId(), null)

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

        @Bean
        NotificationService notificationService() {
            return new NotificationService()
        }

        @Bean
        WorkerService workerService() {
            return new WorkerService()
        }

        @Bean
        CryptoService cryptoService() {
            return new CryptoService()
        }

        @Bean
        ServerKeys serverKeys() {
            return new ServerKeys()
        }

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }
    }
}
