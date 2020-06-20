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
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.SubmissionDto
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
class GetAllStudentsSubmissionsTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE1 = "Question1?"
    public static final String QUESTION_CONTENT1 = "Answer1"
    public static final String QUESTION_TITLE2 = "Question2?"
    public static final String QUESTION_CONTENT2 = "Answer2"
    public static final String STUDENT_NAME1 = "João Silva"
    public static final String STUDENT_NAME2 = "Joaquim Couves"
    public static final String STUDENT_USERNAME1 = "joaosilva"
    public static final String STUDENT_USERNAME2 = "couves1999"

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
    def student1
    def student2
    def acronym
    def question1
    def question2
    def submission1
    def submission2

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        student1 = new User(STUDENT_NAME1, STUDENT_USERNAME1, 1, User.Role.STUDENT)
        student1.setEnrolledCoursesAcronyms(courseExecution.getAcronym())
        userRepository.save(student1)
        student2 = new User(STUDENT_NAME2, STUDENT_USERNAME2, 2, User.Role.STUDENT)
        student2.setEnrolledCoursesAcronyms(courseExecution.getAcronym())
        userRepository.save(student2)

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
        submission1.setUser(student1)
        submission1.setCourseExecution(courseExecution)
        submission1.setAnonymous(false)
        student1.addSubmission(submission1)
        submissionRepository.save(submission1)
        submission2 = new Submission()
        submission2.setQuestion(question2)
        submission2.setCourseExecution(courseExecution)
        submission2.setUser(student2)
        submission2.setAnonymous(true)
        student2.addSubmission(submission2)
        submissionRepository.save(submission2)
    }

    def "get all submissions with one anonymous and one non anonymous"(){
        when:
        def result = submissionService.getStudentsSubmissions(courseExecution.getId())

        then: "the returned data is correct"
        result.size() == 2
        def sub1 = result.get(0)
        def sub2 = result.get(1)
        sub1.getStudentId() == student1.getId()
        !sub1.isAnonymous()
        sub1.getUsername() == STUDENT_USERNAME1
        sub2.getStudentId() == student2.getId()
        sub2.isAnonymous()
        sub2.getUsername() == null
        sub1.getCourseId() == course.getId()
        sub2.getCourseId() == course.getId()
        sub1.getQuestionDto().getId() == question1.getId()
        sub2.getQuestionDto().getId() == question2.getId()
    }

    def "get all submissions with 2 anonymous"(){
        given: "another anonymous submission"
        submission1.setAnonymous(true)
        student1.addSubmission(submission1)
        submissionRepository.save(submission1)

        when:
        def result = submissionService.getStudentsSubmissions(courseExecution.getId())

        then: "the returned data is correct"
        result.size() == 2
        def sub1 = result.get(0)
        def sub2 = result.get(1)
        sub1.getStudentId() == student1.getId()
        sub1.isAnonymous()
        sub1.getUsername() == null
        sub2.getStudentId() == student2.getId()
        sub2.isAnonymous()
        sub2.getUsername() == null
        sub1.getCourseId() == course.getId()
        sub2.getCourseId() == course.getId()
        sub1.getQuestionDto().getId() == question1.getId()
        sub2.getQuestionDto().getId() == question2.getId()
    }

    def "get all submissions with 2 non anonymous"(){
        given: "another anonymous submission"
        submission2.setAnonymous(false)
        student2.addSubmission(submission2)
        submissionRepository.save(submission2)

        when:
        def result = submissionService.getStudentsSubmissions(courseExecution.getId())

        then: "the returned data is correct"
        result.size() == 2
        def sub1 = result.get(0)
        def sub2 = result.get(1)
        sub1.getStudentId() == student1.getId()
        !sub1.isAnonymous()
        sub1.getUsername() == STUDENT_USERNAME1
        sub2.getStudentId() == student2.getId()
        !sub2.isAnonymous()
        sub2.getUsername() == STUDENT_USERNAME2
        sub1.getCourseId() == course.getId()
        sub2.getCourseId() == course.getId()
        sub1.getQuestionDto().getId() == question1.getId()
        sub2.getQuestionDto().getId() == question2.getId()
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
    }

}

