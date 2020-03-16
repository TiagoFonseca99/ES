package pt.ulisboa.tecnico.socialsoftware.tutor.submission.service

import org.h2.engine.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionService
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.SubmissionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.SubmissionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import spock.lang.Specification
import spock.lang.Unroll
import spock.lang.Shared

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.SUBMISSION_MISSING_QUESTION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.SUBMISSION_MISSING_STUDENT

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

    @Shared
    def student
    @Shared
    def question
    def course
    def courseExecution
    def acronym
    def teacher

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        student = new User(STUDENT_NAME, STUDENT_USERNAME, 1, User.Role.STUDENT)
        student.setEnrolledCoursesAcronyms(courseExecution.getAcronym())
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
    }

    def "create submission with question not null"(){
        given: "a submissionDto"
        def submissionDto = new SubmissionDto()
        submissionDto.setKey(1)
        submissionDto.setStudentId(student.getId())

        when: submissionService.createSubmission(question.getId(), submissionDto)

        then: "the correct submission is in the repository"
        submissionRepository.count() == 1L
        def result = submissionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getUser() == student
        result.getQuestion() != null
        result.getQuestion() == question
    }


    def "user is not a student"(){
        given: "a submissionDto for a teacher"
        def submissionDto = new SubmissionDto()
        submissionDto.setKey(1)
        submissionDto.setStudentId(teacher.getId())

        when: submissionService.createSubmission(question.getId(), submissionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_STUDENT
    }

    def "student that submits a question enrolled in course"(){
        given: "a submissionDto"
        def submissionDto = new SubmissionDto()
        submissionDto.setKey(1)
        submissionDto.setStudentId(student.getId())

        when: submissionService.createSubmission(question.getId(), submissionDto)

        then:
        student.getEnrolledCoursesAcronyms().contains(courseExecution.getAcronym())
    }

    def "student submits the same question"(){
        given: "a submissionDto"
        def submissionDto = new SubmissionDto()
        submissionDto.setKey(1)
        submissionDto.setStudentId(student.getId())
        and: "a user with a previous submission of the question"
        student.addSubmission(new Submission(question, student, submissionDto))
        and: "another submissionDto"
        def submissionDto2 = new SubmissionDto()
        submissionDto2.setKey(2)
        submissionDto2.setQuestionId(question.getId())
        submissionDto2.setStudentId(student.getId())

        when: "creating a submission with a previously submitted question"
        submissionService.createSubmission(question.getId(), submissionDto2)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.QUESTION_ALREADY_SUBMITTED
    }

    def "question status is submitted" () {
        given: "a submissionDto"
        def submissionDto = new SubmissionDto()
        submissionDto.setKey(1)
        submissionDto.setStudentId(student.getId())

        when: submissionService.createSubmission(question.getId(), submissionDto)

        then: "question status is SUBMITTED"
        def result = submissionRepository.findAll().get(0)
        result.getQuestion().getStatus() == Question.Status.SUBMITTED
    }

    @Unroll
    def "invalid arguments: studentId=#studentId | questionId=#questionId || errorMessage"(){
        given: "a submissionDto"
        def submissionDto = new SubmissionDto()
        submissionDto.setKey(1)
        submissionDto.setStudentId(studentId)
        when:
        submissionService.createSubmission(questionId, submissionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == errorMessage

        where:
        studentId       | questionId        | errorMessage
        null            | question.getId()  | SUBMISSION_MISSING_STUDENT
        student.getId() | null              | SUBMISSION_MISSING_QUESTION
    }

    @TestConfiguration
    static class SubmissionServiceImplTestContextConfiguration {

        @Bean
        SubmissionService submissionService() {
            return new SubmissionService()
        }
    }


}
