package pt.ulisboa.tecnico.socialsoftware.tutor.submission.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionService
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.SubmissionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.SubmissionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;


@DataJpaTest
class ResubmitQuestionTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = "Question?"
    public static final String QUESTION_CONTENT = "Answer"
    public static final String OPTION_CONTENT = "optionId content"
    public static final String NEW_QUESTION_TITLE = "new question title"
    public static final String NEW_QUESTION_CONTENT = "new question content"
    public static final String NEW_OPTION_CONTENT = "new optionId content"
    public static final String STUDENT_NAME = "João Silva"
    public static final String STUDENT_USERNAME = "joaosilva"
    public static final String ARGUMENT = "Argumento"


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

    @Autowired
    OptionRepository optionRepository

    @Shared
    def student
    @Shared
    def question
    @Shared
    def newQuestion
    def optionOK
    def optionKO
    def option1
    def option2
    @Shared
    def course
    @Shared
    def courseExecution
    def acronym
    def submission

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        student = new User(STUDENT_NAME, STUDENT_USERNAME, 1, User.Role.STUDENT)
        student.setEnrolledCoursesAcronyms(courseExecution.getAcronym())
        userRepository.save(student)
        question = new Question()
        question.setTitle(QUESTION_TITLE)
        question.setContent(QUESTION_CONTENT)
        question.setCourse(course)
        question.setStatus(Question.Status.SUBMITTED)
        question.setNumberOfAnswers(2)
        question.setNumberOfCorrect(1)
        questionRepository.save(question)
        optionOK = new Option()
        optionOK.setContent(OPTION_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setSequence(0)
        optionOK.setQuestion(question)
        optionRepository.save(optionOK)
        optionKO = new Option()
        optionKO.setContent(OPTION_CONTENT)
        optionKO.setCorrect(false)
        optionKO.setSequence(1)
        optionKO.setQuestion(question)
        optionRepository.save(optionKO)
        questionRepository.save(question)
        submission = new Submission()
        submission.setQuestion(question)
        submission.setUser(student)
        submission.setCourseExecution(courseExecution)
        submissionRepository.save(submission)
        newQuestion = new Question()
        newQuestion.setTitle(QUESTION_TITLE)
        newQuestion.setContent(QUESTION_CONTENT)
        newQuestion.setCourse(course)
        newQuestion.setStatus(Question.Status.SUBMITTED)
        newQuestion.setNumberOfAnswers(2)
        newQuestion.setNumberOfCorrect(1)
        option1 = new Option()
        option1.setContent(OPTION_CONTENT)
        option1.setCorrect(true)
        option1.setSequence(0)
        option1.setQuestion(newQuestion)
        optionRepository.save(option1)
        option2 = new Option()
        option2.setContent(OPTION_CONTENT)
        option2.setCorrect(false)
        option2.setSequence(1)
        option2.setQuestion(newQuestion)
        optionRepository.save(option2)
        questionRepository.save(newQuestion)
    }

    def "edit question and resubmit"(){
        given: "a changed question"
        def questionDto = new QuestionDto()
        questionDto.setTitle(NEW_QUESTION_TITLE)
        questionDto.setContent(NEW_QUESTION_CONTENT)
        and: '2 changed options'
        def optionsList= new ArrayList<OptionDto>()
        def optionDto = new OptionDto()
        optionDto.setContent(NEW_OPTION_CONTENT)
        optionDto.setCorrect(false)
        optionDto.setSequence(0)
        optionsList.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setSequence(1)
        optionsList.add(optionDto)
        questionDto.setOptions(optionsList)
        and: "the new submissionDto"
        def submissionDto = new SubmissionDto()
        submissionDto.setQuestionDto(questionDto)
        submissionDto.setCourseId(course.getId())
        submissionDto.setCourseExecutionId(courseExecution.getId())
        submissionDto.setStudentId(student.getId())


        when: submissionService.resubmitQuestion(question.getId(), newQuestion.getId(), submissionDto)

        then: "a new submission is created with the question changed"
        submissionRepository.count() == 2L
        def oldSub = submissionRepository.findAll().get(0).getQuestion()
        def newSub = submissionRepository.findAll().get(1).getQuestion()
        oldSub.getId() == question.getId()
        newSub.getId() == newQuestion.getId()
        question.getId() != newQuestion.getId()
        newSub.getTitle() == NEW_QUESTION_TITLE
        newSub.getContent() == NEW_QUESTION_CONTENT
        oldSub.getStatus() == Question.Status.DEPRECATED
        and: 'are not changed'
        newSub.getStatus() == Question.Status.SUBMITTED
        newSub.getNumberOfAnswers() == oldSub.getNumberOfAnswers()
        newSub.getNumberOfCorrect() == oldSub.getNumberOfCorrect()
        and: 'an option is changed'
        newSub.getOptions().size() == 2
        def options = newSub.getOptions().stream().findAll()
        def resOptionOne = options.get(0)
        def resOptionTwo = options.get(1)
        resOptionOne.getContent() == NEW_OPTION_CONTENT
        !resOptionOne.getCorrect()
        resOptionTwo.getContent() == OPTION_CONTENT
        resOptionTwo.getCorrect()
    }

    def "edit question with argument and resubmit"() {
        given: "a changed question"
        def questionDto = new QuestionDto()
        questionDto.setTitle(NEW_QUESTION_TITLE)
        questionDto.setContent(NEW_QUESTION_CONTENT)
        and: '2 changed options'
        def optionsList= new ArrayList<OptionDto>()
        def optionDto = new OptionDto()
        optionDto.setContent(NEW_OPTION_CONTENT)
        optionDto.setCorrect(false)
        optionDto.setSequence(0)
        optionsList.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setSequence(1)
        optionsList.add(optionDto)
        questionDto.setOptions(optionsList)
        and: "the new submissionDto"
        def submissionDto = new SubmissionDto()
        submissionDto.setQuestionDto(questionDto)
        submissionDto.setCourseId(course.getId())
        submissionDto.setCourseExecutionId(courseExecution.getId())
        submissionDto.setStudentId(student.getId())
        submissionDto.setArgument(ARGUMENT)

        when: submissionService.resubmitQuestion(question.getId(), newQuestion.getId(), submissionDto)

        then: "a new submission is created with the question changed and argument"
        def result = submissionRepository.findAll().get(1)
        result.getArgument() == ARGUMENT
    }

    def "edit question with missing data"(){
        given: "a changed question"
        def questionDto = new QuestionDto()
        questionDto.setTitle(' ')
        questionDto.setContent(NEW_QUESTION_CONTENT)
        and: "the new submissionDto"
        def submissionDto = new SubmissionDto()
        submissionDto.setQuestionDto(questionDto)
        submissionDto.setCourseId(course.getId())
        submissionDto.setCourseExecutionId(courseExecution.getId())
        submissionDto.setStudentId(student.getId())

        when: submissionService.resubmitQuestion(question.getId(), newQuestion.getId(), submissionDto)

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INVALID_TITLE_FOR_QUESTION
    }

    def "edit question with 2 option true"(){
        given: "a changed question"
        def questionDto = new QuestionDto()
        questionDto.setTitle(NEW_QUESTION_TITLE)
        questionDto.setContent(NEW_QUESTION_CONTENT)
        and: '2 changed options'
        def optionsList= new ArrayList<OptionDto>()
        def optionDto = new OptionDto()
        optionDto.setContent(NEW_OPTION_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setSequence(0)
        optionsList.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setSequence(1)
        optionsList.add(optionDto)
        questionDto.setOptions(optionsList)
        and: "the new submissionDto"
        def submissionDto = new SubmissionDto()
        submissionDto.setQuestionDto(questionDto)
        submissionDto.setCourseId(course.getId())
        submissionDto.setCourseExecutionId(courseExecution.getId())
        submissionDto.setStudentId(student.getId())

        when:
        submissionService.resubmitQuestion(question.getId(), newQuestion.getId(), submissionDto)

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.ONE_CORRECT_OPTION_NEEDED
    }

    @Unroll
    def "invalid arguments: studentId=#studentId | oldQuestionId=#oldQuestionId | newQuestionId=#newQuestionId | courseId=#courseId | courseExecutionId=#courseExecutionId || errorMessage"(){
        given: "a changed question"
        def questionDto = new QuestionDto()
        questionDto.setTitle(NEW_QUESTION_TITLE)
        questionDto.setContent(NEW_QUESTION_CONTENT)
        and: '2 changed options'
        def optionsList= new ArrayList<OptionDto>()
        def optionDto = new OptionDto()
        optionDto.setContent(NEW_OPTION_CONTENT)
        optionDto.setCorrect(false)
        optionDto.setSequence(0)
        optionsList.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setSequence(1)
        optionsList.add(optionDto)
        questionDto.setOptions(optionsList)
        and: "the new submissionDto"
        def submissionDto = new SubmissionDto()
        submissionDto.setQuestionDto(questionDto)
        submissionDto.setCourseId(courseId)
        submissionDto.setCourseExecutionId(courseExecutionId)
        submissionDto.setStudentId(studentId)

        when: submissionService.resubmitQuestion(oldQuestionId, newQuestionId, submissionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == errorMessage

        where:
        studentId       | oldQuestionId     | newQuestionId       | courseId       | courseExecutionId       || errorMessage
        null            | question.getId()  | newQuestion.getId() | course.getId() | courseExecution.getId() || SUBMISSION_MISSING_STUDENT
        student.getId() | null              | newQuestion.getId() | course.getId() | courseExecution.getId() || SUBMISSION_MISSING_QUESTION
        student.getId() | question.getId()  | null                | course.getId() | courseExecution.getId() || SUBMISSION_MISSING_QUESTION
        student.getId() | question.getId()  | newQuestion.getId() | null           | courseExecution.getId() || SUBMISSION_MISSING_COURSE
        student.getId() | question.getId()  | newQuestion.getId() | course.getId() | null                    || SUBMISSION_MISSING_COURSE
    }

    @TestConfiguration
    static class SubmissionServiceImplTestContextConfiguration {

        @Bean
        SubmissionService submissionService() {
            return new SubmissionService()
        }
    }
}