package pt.ulisboa.tecnico.socialsoftware.tutor.user.service;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.SubmissionService
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.ReviewDto
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.SubmissionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import spock.lang.Specification

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository

@DataJpaTest
class GetDashboardInfoTest extends Specification {
    public static final String DISCUSSION_CONTENT = "discussion content"
    public static final String APPROVED = 'APPROVED'
    public static final String REJECTED = 'REJECTED'
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String USER_USERNAME = "user username"
    public static final String USER_NAME = "user name"
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String TOPIC_NAME1 = "Informática"
    public static final String TOPIC_NAME2 = "Engenharia de Software"
    public static final String REVIEW_JUSTIFICATION = 'Porque me apeteceu'
    public static final int NUMBER_OF_QUESTIONS = 1


    @Autowired
    DiscussionRepository discussionRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    SubmissionRepository submissionRepository

    @Autowired
    UserService userService

    @Autowired
    TournamentService tournamentService

    @Autowired
    SubmissionService submissionService

    def student1
    def student2
    def teacher
    def tournamentDto1 = new TournamentDto()
    def tournamentDto2 = new TournamentDto()
    def submission1
    def submission2

    def setup(){
        student1 = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        student2 = new User(USER_NAME + "1", USER_USERNAME + "1", 2, User.Role.STUDENT)
        teacher = new User(USER_NAME + "3", USER_USERNAME + "3", 4, User.Role.TEACHER)
        userRepository.save(teacher)

        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        def courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)

        course.addCourseExecution()
        courseRepository.save(course)
        courseExecution.addUser(student1)
        courseExecutionRepository.save(courseExecution)

        student1.addCourse(courseExecution)
        student2.addCourse(courseExecution)

        userRepository.save(student1)
        userRepository.save(student2)

        def topics = new ArrayList<Integer>()

        def topicDto1 = new TopicDto()
        topicDto1.setName(TOPIC_NAME1)
        def topic1 = new Topic(course, topicDto1)
        topicRepository.save(topic1)

        def topicDto2 = new TopicDto()
        topicDto2.setName(TOPIC_NAME2)
        def topic2 = new Topic(course, topicDto2)
        topicRepository.save(topic2)

        topics.add(topic1.getId())
        topics.add(topic2.getId())

        def question1 = new Question()
        question1.setKey(1)
        question1.setContent(QUESTION_CONTENT)
        question1.setTitle(QUESTION_TITLE)
        question1.setStatus(Question.Status.AVAILABLE)
        question1.setCourse(course)
        question1.addTopic(topic1)
        questionRepository.save(question1)

        def question2 = new Question()
        question2.setKey(2)
        question2.setContent(QUESTION_CONTENT + '1')
        question2.setTitle(QUESTION_TITLE + '1')
        question2.setStatus(Question.Status.AVAILABLE)
        question2.setCourse(course)
        question2.addTopic(topic2)
        questionRepository.save(question2)

        def submittedQuestion1 = new Question()
        submittedQuestion1.setKey(3)
        submittedQuestion1.setContent(QUESTION_CONTENT + '2')
        submittedQuestion1.setTitle(QUESTION_TITLE + '2')
        submittedQuestion1.setStatus(Question.Status.SUBMITTED)
        submittedQuestion1.setCourse(course)
        questionRepository.save(submittedQuestion1)

        def submittedQuestion2 = new Question()
        submittedQuestion2.setKey(4)
        submittedQuestion2.setContent(QUESTION_CONTENT + '3')
        submittedQuestion2.setTitle(QUESTION_TITLE + '3')
        submittedQuestion2.setStatus(Question.Status.SUBMITTED)
        submittedQuestion2.setCourse(course)
        questionRepository.save(submittedQuestion2)

        def discussion1 = new Discussion()
        discussion1.setContent(DISCUSSION_CONTENT)
        discussion1.setUser(student1)
        discussion1.setQuestion(question1)

        def discussion2 = new Discussion()
        discussion2.setContent(DISCUSSION_CONTENT)
        discussion2.setUser(student1)
        discussion2.setQuestion(question2)

        discussionRepository.save(discussion1)
        discussionRepository.save(discussion2)

        student1.addDiscussion(discussion1)
        student1.addDiscussion(discussion2)

        userRepository.save(student1)

        def startTime = DateHandler.now().plusHours(1)
        def endTime = DateHandler.now().plusHours(2)

        tournamentDto1 = new TournamentDto()
        tournamentDto1.setStartTime(DateHandler.toISOString(startTime))
        tournamentDto1.setEndTime(DateHandler.toISOString(endTime))
        tournamentDto1.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto1.setState(Tournament.Status.NOT_CANCELED)

        tournamentDto2 = new TournamentDto()
        tournamentDto2.setStartTime(DateHandler.toISOString(startTime))
        tournamentDto2.setEndTime(DateHandler.toISOString(endTime))
        tournamentDto2.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        tournamentDto2.setState(Tournament.Status.NOT_CANCELED)

        tournamentDto1 = tournamentService.createTournament(student1.getId(), topics, tournamentDto1)
        tournamentDto2 = tournamentService.createTournament(student1.getId(), topics, tournamentDto2)

        submission1 = new Submission()
        submission1.setQuestion(submittedQuestion1)
        submission1.setUser(student2)
        submissionRepository.save(submission1)

        submission2 = new Submission()
        submission2.setQuestion(submittedQuestion2)
        submission2.setUser(student2)
        submissionRepository.save(submission2)

        student2.addSubmission(submission1)
        student2.addSubmission(submission2)

        userRepository.save(student2)

    }

    def "get number of discussions from student with 2 discussions"(){
        when: "requesting information"
        def result = userService.getDashboardInfo(student1.getId())

        then:
        result.getNumDiscussions() == 2
    }

    def "get number of discussions from student with 0 discussions"(){
        when: "requesting information"
        def result = userService.getDashboardInfo(student2.getId())

        then:
        result.getNumDiscussions() == 0
    }

    def "get number of public discussions from student with 0 public discussions"(){
        when: "requesting information"
        def result = userService.getDashboardInfo(student1.getId())

        then:
        result.getNumPublicDiscussions() == 0
    }

    def "get number of public discussions from student with 1 public discussion"(){
        given: "1 discussion made public"
        userRepository.findByUsername("user username").getDiscussions().stream().findFirst().get().setAvailability(true);

        when: "requesting information"
        def result = userService.getDashboardInfo(student1.getId())

        then:
        result.getNumPublicDiscussions() == 1
    }

    def "get number of discussions from teacher"(){
        when: "requesting information"
        userService.getDashboardInfo(teacher.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_STUDENT
    }

    def "get dashboard info from teacher"(){
        when: "requesting information"
        userService.getDashboardInfo(teacher.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_STUDENT
    }

    def "get number of submissions from student without submissions"(){
        when: "requesting information"
        def result = userService.getDashboardInfo(student1.getId())

        then:
        result.getNumSubmissions() == 0
        result.getNumApprovedSubmissions() == 0
        result.getNumRejectedSubmissions() == 0
    }

    def "get number of submissions from student with 2 submissions without reviews"(){
        when: "requesting information"
        def result = userService.getDashboardInfo(student2.getId())

        then:
        result.getNumSubmissions() == 2
        result.getNumApprovedSubmissions() == 0
        result.getNumRejectedSubmissions() == 0
    }

    def "get number of submissions from student with 1 approved submission and 1 rejected submission"(){
        given: "2 reviews"
        def reviewDto1 = new ReviewDto()
        reviewDto1.setTeacherId(teacher.getId())
        reviewDto1.setStudentId(student2.getId())
        reviewDto1.setSubmissionId(submission1.getId())
        reviewDto1.setStatus(APPROVED)
        reviewDto1.setJustification(REVIEW_JUSTIFICATION)
        submissionService.reviewSubmission(teacher.getId(), reviewDto1)
        def reviewDto2 = new ReviewDto()
        reviewDto2.setTeacherId(teacher.getId())
        reviewDto2.setStudentId(student2.getId())
        reviewDto2.setSubmissionId(submission2.getId())
        reviewDto2.setStatus(REJECTED)
        reviewDto2.setJustification(REVIEW_JUSTIFICATION)
        submissionService.reviewSubmission(teacher.getId(), reviewDto2)

        when: "requesting information"
        def result = userService.getDashboardInfo(student2.getId())

        then:
        result.getNumSubmissions() == 2
        result.getNumApprovedSubmissions() == 1
        result.getNumRejectedSubmissions() == 1
    }

    def "get number of tournaments from student enrolled in 1 tournament"() {
        given: "student enrolled in tournament 1"
        tournamentService.joinTournament(student1.getId(), tournamentDto1, "")

        when: "requesting information"
        def result = userService.getDashboardInfo(student1.getId())

        then:
        result.getJoinedTournaments().size() == 1
    }

    def "get number of tournaments from student enrolled in 2 tournaments"() {
        given: "student enrolled in tournament 1"
        tournamentService.joinTournament(student1.getId(), tournamentDto1, "")

        and: "student enrolled in tournament 2"
        tournamentService.joinTournament(student1.getId(), tournamentDto2, "")

        when: "requesting information"
        def result = userService.getDashboardInfo(student1.getId())

        then:
        result.getJoinedTournaments().size() == 2
    }

    def "get number of tournaments from student enrolled in 0 tournament"() {
        given:

        when: "requesting information"
        def result = userService.getDashboardInfo(student1.getId())

        then:
        result.getJoinedTournaments().size() == 0
    }

    @TestConfiguration
    static class GetDashboardServicesImplTestContextConfiguration {
        @Bean
        UserService userService() {
            return new UserService()
        }

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }

        @Bean
        StatementService statementService() {
            return new StatementService()
        }

        @Bean
        QuizService quizService() {
            return new QuizService()
        }

        @Bean
        AnswerService answerService() {
            return new AnswerService()
        }
        @Bean
        AnswersXmlImport answersXmlImport() {
            return new AnswersXmlImport()
        }

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }

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
