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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionService
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import spock.lang.Specification

@DataJpaTest
class CreateSubmissionTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = "Pergunta exemplo?"
    public static final String QUESTION_CONTENT = "Resposta"
    public static final String STUDENT_NAME = "João Silva"
    public static final String STUDENT_USERNAME = "joaosilva"

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    SubmissionRepository submissionRepository

    def course
    def courseExecution
    def student
    def acronym
    def question


    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        acronym = courseExecution.getAcronym()
        student = new User(STUDENT_NAME, STUDENT_USERNAME, 1, User.Role.STUDENT)
        question = new Question();
        question.getTitle(QUESTION_TITLE);
        question.getKey(1);
        question.getContent(QUESTION_CONTENT);
    }

    def "create submission with question not null"(){
        given: "a submissionDto"
        def submissionDto = new SubmissionDto()
        submissionDto.setKey(1)
        submissionDto.setTitle(question.getTitle())
        submissionDto.setStudentId(student.getId())

        when: submissionService.createSubmission(student.getId(), question.getId(), submissionDto)

        then: "the correct submission is in the repository"
        submissionRepository.count() == 1L
        def result = submissionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getTitle() == QUESTION_TITLE
        result.getStudentId() == student.getId()
        result.getStudent().getSubmittedQuestions().contains(question)
        result.getQuestion() != null
        result.getQuestion().getType() == Question.Status.SUBMITTED
    }


    def "user is a student"(){
        given: "a submissionDto"
        def submissionDto = new SubmissionDto()

        when: submissionService.createSubmission(student.getId(), question.getId(), submissionDto)

        then: "user is student"
        submission.getStudent().getRole() == User.Role.STUDENT
    }

    def "student that submits a question enrolled in course"(){
        given: "a submissionDto"
        def submissionDto = new SubmissionDto()

        when: submissionService.createSubmission(student.getId(), question.getId(), submissionDto)

        then:
        def result = submissionRepository.findAll().get(0)
        def enrolledCourse = result.getCourse()
        result.getStudent().getEnrolledCourseAcronyms().contains(acronym)
    }

    def "student submits the same question title"(){
        given: "a question"
        def question2 = new Question();
        question2.getTitle(QUESTION_TITLE);
        question2.getKey(2);
        and: "a submissionDto"
        def submissionDto = new SubmissionDto()
        and: "another submissionDto"
        def submissionDto2 = new SubmissionDto()

        when: "creating submitting the question again"
        submissionService.createSubmission(student.getId(), question2.getId(), submissionDto2)

        then: "exception is thrown"
        thrown(TutorException)
    }

    @TestConfiguration
    static class SubmissionServiceImplTestContextConfiguration {

        @Bean
        SubmissionService submissionService() {
            return new SubmissionService()
        }
    }


}
