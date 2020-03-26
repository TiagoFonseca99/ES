package pt.ulisboa.tecnico.socialsoftware.tutor.administration.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Review
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.SubmissionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.*
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.ReviewDto
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionService
import spock.lang.Specification

@DataJpaTest
class CreateReviewPerformanceTest extends Specification {
    public static final String REVIEW_JUSTIFICATION = 'Porque me apeteceu'
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String COURSE_NAME = "Arquitetura de Software"
    public static final String OPTION_CONTENT = "optionId content"
    public static final String STUDENT_NAME = "João Silva"
    public static final String STUDENT_USERNAME = "joaosilva"
    public static final String TEACHER_NAME = "Ana Rita"
    public static final String TEACHER_USERNAME = "anarita"

    @Autowired
    SubmissionService submissionService

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    SubmissionRepository submissionRepository

    @Autowired
    QuizRepository quizRepository

    @Autowired
    UserRepository userRepository

    def "performance testing to get 1000 reviews"() {
        given: "a student"
        def student = new User(STUDENT_NAME, STUDENT_USERNAME, 1, User.Role.STUDENT)
        userRepository.save(student)

        and: "a teacher"
        def teacher = new User(TEACHER_NAME, TEACHER_USERNAME, 2, User.Role.TEACHER)
        userRepository.save(teacher)

        and: "a quiz"
        def quiz = new Quiz()
        quiz.setKey(1)
        quiz.setType(Quiz.QuizType.TEST)
        quizRepository.save(quiz)
        quiz.setKey(1)
        quiz.setType(Quiz.QuizType.TEST)

        and: "a question"
        def question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_TITLE)
        question.setContent(QUESTION_CONTENT)
        questionRepository.save(question)

        and: "a submission"
        def submission = new Submission()
        submission.setQuestion(question)
        submission.setUser(student)
        submissionRepository.save(submission)

        when:
        1.upto(500000, {
            def reviewDto = new ReviewDto()
            reviewDto.setJustification(REVIEW_JUSTIFICATION)
            reviewDto.setSubmissionId(submission.getId())
            reviewDto.setStudentId(submission.getStudentId())

            submissionService.reviewSubmission(teacher.getId(), reviewDto, Review.Status.REJECTED);

        })

        then:
        true
    }

    @TestConfiguration
    static class ServiceImplTestContextConfiguration {

        @Bean
        SubmissionService submissionService() {
            return new SubmissionService()
        }

    }
}