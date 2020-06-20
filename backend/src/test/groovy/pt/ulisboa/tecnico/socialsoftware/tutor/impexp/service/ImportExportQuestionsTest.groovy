package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionService
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.SubmissionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_NOT_FOUND

@DataJpaTest
class ImportExportQuestionsTest extends Specification {
    public static final String COURSE_NAME = "Arquitetura de Software"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content\n ![image][image]\n question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String URL = 'URL'
    public static final String STUDENT_NAME = "João Silva"
    public static final String STUDENT_USERNAME = "joaosilva"

    @Autowired
    QuestionService questionService

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    SubmissionRepository submissionRepository

    @Autowired
    UserRepository userRepository

    def course
    def questionId
    def courseExecution
    def submission
    def student

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        student = new User(STUDENT_NAME, STUDENT_USERNAME, 1, User.Role.STUDENT)
        student.setEnrolledCoursesAcronyms(courseExecution.getAcronym())
        userRepository.save(student)

        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def image = new ImageDto()
        image.setUrl(URL)
        image.setWidth(20)
        questionDto.setImage(image)

        def optionDto = new OptionDto()
        optionDto.setSequence(0)
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setSequence(1)
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        questionDto.setOptions(options)

        def question = questionService.createQuestion(course.getId(), questionDto)
        questionId = question.getId()

        submission = new Submission()
        submission.setQuestion(questionRepository.getOne(questionId))
        submission.setUser(student)
        submission.setCourseExecution(courseExecution)
        submissionRepository.save(submission)
    }

    def 'export and import questions to xml'() {
        given: 'a xml with questions'
        def questionsXml = questionService.exportQuestionsToXml()
        and: 'a clean database'
        questionService.removeQuestion(questionId)

        when:
        questionService.importQuestionsFromXml(questionsXml)

        then:
        questionRepository.findQuestions(course.getId()).size() == 1
        def questionResult = questionService.findQuestions(course.getId()).get(0)
        questionResult.getKey() == null
        questionResult.getTitle() == QUESTION_TITLE
        questionResult.getContent() == QUESTION_CONTENT
        questionResult.getStatus() == Question.Status.AVAILABLE.name()
        def imageResult = questionResult.getImage()
        imageResult.getWidth() == 20
        imageResult.getUrl() == URL
        questionResult.getOptions().size() == 2
        def optionOneResult = questionResult.getOptions().get(0)
        def optionTwoResult = questionResult.getOptions().get(1)
        optionOneResult.getSequence() + optionTwoResult.getSequence() == 1
        optionOneResult.getContent() == OPTION_CONTENT
        optionTwoResult.getContent() == OPTION_CONTENT
        !(optionOneResult.getCorrect() && optionTwoResult.getCorrect())
        optionOneResult.getCorrect() || optionTwoResult.getCorrect()
    }

    def 'export to latex'() {
        when:
        def questionsLatex = questionService.exportQuestionsToLatex()

        then:
        questionsLatex != null
    }

    @TestConfiguration
    static class QuestionServiceImplTestContextConfiguration {

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }
        @Bean
        SubmissionService submissionService() {
            return new SubmissionService()
        }
        @Bean
        NotificationService NotificationService() {
            return new NotificationService()
        }
    }
}
