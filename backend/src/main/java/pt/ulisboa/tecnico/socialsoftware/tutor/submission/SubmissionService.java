package pt.ulisboa.tecnico.socialsoftware.tutor.submission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationService;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.dto.NotificationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.SubmissionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Review;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.ReviewDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.ReviewRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.SubmissionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationsCreation;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;
import static pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationsMessage.*;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @Autowired
    private NotificationService notificationService;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public SubmissionDto createSubmission(Integer questionId, SubmissionDto submissionDto){

        checkIfConsistentSubmission(questionId, submissionDto.getStudentId(), submissionDto.getCourseExecutionId(), submissionDto.getCourseId());

        CourseExecution courseExecution = getCourseExecution(submissionDto.getCourseExecutionId());

        Question question = getQuestion(questionId);

        User user = getStudent(submissionDto.getStudentId());

        checkIfQuestionAlreadySubmitted(question, user);

        Submission submission = new Submission(courseExecution, question, user);

        submission.setAnonymous(submissionDto.isAnonymous());

        User student = getStudent(submissionDto.getStudentId());
        submission.getQuestion().Attach(student);

        for (User teacher : courseExecution.getUsers()) {
            if (teacher.isTeacher()) {
                submission.Attach(teacher);
            }
        }
        prepareNotification(submission);

        if (submissionDto.getArgument() != null && !submissionDto.getArgument().isBlank())
            submission.setArgument(submissionDto.getArgument());


        entityManager.persist(submission);
        return new SubmissionDto(submission);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public SubmissionDto resubmitQuestion(Integer oldQuestionId, Integer newQuestionId, SubmissionDto submissionDto) {
        checkIfConsistentSubmission(newQuestionId, submissionDto.getStudentId(), submissionDto.getCourseExecutionId(), submissionDto.getCourseId());
        if(oldQuestionId == null )
            throw new TutorException(SUBMISSION_MISSING_QUESTION);

        CourseExecution courseExecution = getCourseExecution(submissionDto.getCourseExecutionId());

        Question oldQuestion = getQuestion(oldQuestionId);
        oldQuestion.setStatus("DEPRECATED");

        Question newQuestion = getQuestion(newQuestionId);
        setNewOptionsId(submissionDto, newQuestion);
        newQuestion.setStatus("SUBMITTED");
        newQuestion.update(submissionDto.getQuestionDto());


        User user = getStudent(submissionDto.getStudentId());

        Submission submission = new Submission(courseExecution, newQuestion, user);

        submission.setAnonymous(submissionDto.isAnonymous());

        User student = getStudent(submissionDto.getStudentId());
        submission.getQuestion().Attach(student);

        for (User teacher : courseExecution.getUsers()) {
            if (teacher.isTeacher()) {
                submission.Attach(teacher);
            }
        }
        prepareNotification(submission);

        if (submissionDto.getArgument() != null && !submissionDto.getArgument().isBlank())
            submission.setArgument(submissionDto.getArgument());


        entityManager.persist(submission);

        return new SubmissionDto(submission);

    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ReviewDto reviewSubmission(Integer teacherId, ReviewDto reviewDto) {

        checkIfConsistentReview(reviewDto);

        User user = getTeacher(teacherId);
        Submission submission = getSubmission(reviewDto.getSubmissionId());

        if (reviewDto.getCreationDate() == null) {
            reviewDto.setCreationDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }

        updateQuestionStatus(submission, reviewDto.getStatus());

        Review review = new Review(user, submission, reviewDto);

        User student = getStudent(reviewDto.getStudentId());
        review.Attach(student);

        entityManager.persist(review);

        prepareNotification(review);

        return new ReviewDto(review);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ReviewDto> getSubmissionReviews(Integer studentId, Integer courseExecutionId) {
        if(studentId == null)
            throw new TutorException(REVIEW_MISSING_STUDENT);
        else if(courseExecutionId == null)
            throw new TutorException(COURSE_EXECUTION_MISSING);

        List<Integer> courseExecutionSubmissions = getSubmissions(studentId, courseExecutionId).stream().map(SubmissionDto::getId).collect(Collectors.toList());
        return reviewRepository.getSubmissionReviews(studentId).stream().map(ReviewDto::new).filter(r -> courseExecutionSubmissions.contains(r.getSubmissionId())).collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<SubmissionDto> getSubsToTeacher(Integer courseExecutionId) {
        if(courseExecutionId == null)
            throw new TutorException(COURSE_EXECUTION_MISSING);
        return submissionRepository.getCourseExecutionSubmissions(courseExecutionId).stream().map(SubmissionDto::new).collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ReviewDto> getReviewsToTeacher(Integer courseExecutionId) {
        if(courseExecutionId == null)
            throw new TutorException(COURSE_EXECUTION_MISSING);

        List<Integer> courseExecutionSubmissions = getSubsToTeacher(courseExecutionId).stream().map(SubmissionDto::getId).collect(Collectors.toList());
        return reviewRepository.findAll().stream().map(ReviewDto::new).filter(r -> courseExecutionSubmissions.contains(r.getSubmissionId())).collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<SubmissionDto> getSubmissions(Integer studentId, Integer courseExecutionId) {
        if(studentId == null)
            throw new TutorException(SUBMISSION_MISSING_STUDENT);
        else if(courseExecutionId == null)
            throw new TutorException(COURSE_EXECUTION_MISSING);
        return submissionRepository.getSubmissions(studentId, courseExecutionId).stream().map(SubmissionDto::new).collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void changeSubmission(SubmissionDto submission) {
        if (submission.getStudentId() == null) {
            throw new TutorException(SUBMISSION_MISSING_STUDENT);
        }

        if (submission.getQuestionDto().getId() == null) {
            throw new TutorException(SUBMISSION_MISSING_QUESTION);
        }

        Question question = questionRepository.getOne(submission.getQuestionDto().getId());

        question.update(submission.getQuestionDto());

        entityManager.persist(question);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<SubmissionDto> getStudentsSubmissions(Integer courseExecutionId) {
        List<SubmissionDto> submissions = submissionRepository.getCourseExecutionSubmissions(courseExecutionId).stream().map(SubmissionDto::new).collect(Collectors.toList());

        for(SubmissionDto submission : submissions) {
            if (submission.isAnonymous()) submission.setUsername(null);
        }

        return submissions;
    }

    private void updateQuestionStatus(Submission submission, String status) {
        Question question = getQuestion(submission.getQuestion().getId());
        if(status.equals("APPROVED")){
            question.setStatus("AVAILABLE");
        } else {
            question.setStatus("DEPRECATED");
        }
    }

    private void setNewOptionsId(SubmissionDto submissionDto, Question newQuestion) {
        List<OptionDto> newOptions = newQuestion.getOptions().stream().map(OptionDto::new).collect(Collectors.toList());

        int i = 0;
        for (OptionDto option: submissionDto.getQuestionDto().getOptions()) {
            option.setId(newOptions.get(i++).getId());
        }
    }

    private void checkIfConsistentSubmission(Integer questionId, Integer studentId, Integer executionId, Integer courseId) {
        if(questionId == null)
            throw new TutorException(SUBMISSION_MISSING_QUESTION);
        else if(studentId == null)
            throw new TutorException(SUBMISSION_MISSING_STUDENT);
        else if(executionId == null || courseId == null)
            throw new TutorException(SUBMISSION_MISSING_COURSE);
    }

    private void checkIfQuestionAlreadySubmitted(Question question, User user) {
        if(user.getSubmittedQuestions().contains(question))
            throw new TutorException(QUESTION_ALREADY_SUBMITTED, user.getUsername());
    }

    private CourseExecution getCourseExecution(Integer executionId) {
        return courseExecutionRepository.findById(executionId).orElseThrow(() -> new TutorException(COURSE_EXECUTION_NOT_FOUND, executionId));
    }

    private Question getQuestion(Integer questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));
    }

    private User getStudent(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));
        if(!user.isStudent())
            throw new TutorException(USER_NOT_STUDENT, user.getUsername());
        return user;
    }

    private void checkIfConsistentReview(ReviewDto reviewDto) {

        checkIfReviewHasJustification(reviewDto);

        if(reviewDto.getSubmissionId() == null)
            throw new TutorException(REVIEW_MISSING_SUBMISSION);
        if(reviewDto.getStudentId() == null)
            throw new TutorException(REVIEW_MISSING_STUDENT);
        if(reviewDto.getStatus() == null)
            throw new TutorException(REVIEW_MISSING_STATUS);
    }

    private User getTeacher(Integer teacherId) {

        if(teacherId == null)
            throw new TutorException(USER_NOT_FOUND);
        User user = userRepository.findById(teacherId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, teacherId));
        if (!user.isTeacher())
            throw new TutorException(USER_NOT_TEACHER, user.getUsername());
        return user;
    }


    private Submission getSubmission(Integer submissionId) {
        return submissionRepository.findById(submissionId).orElseThrow(() -> new TutorException(SUBMISSION_NOT_FOUND, submissionId));
    }

    private void checkIfReviewHasJustification(ReviewDto reviewDto) {

        String justification = reviewDto.getJustification();
        if (justification == null || justification.isBlank()) {
            throw new TutorException(REVIEW_MISSING_JUSTIFICATION);
        }
    }

    private void prepareNotification(Review review) {
        String title = NotificationsCreation.createTitle(NEW_REVIEW_TITLE, review.getSubmission().getId());
        String content = "";
        if (review.getStatus() == Review.Status.APPROVED)
            content = NotificationsCreation.createContent(NEW_REVIEW_CONTENT, review.getSubmission().getId(), "approved", review.getUser().getName());
        else
            content = NotificationsCreation.createContent(NEW_REVIEW_CONTENT, review.getSubmission().getId(), "rejected", review.getUser().getName());
        review.Notify(notificationService.createNotification(title, content, Notification.Type.REVIEW));
    }

    public void prepareNotification(Question question, User user) {
        String title = NotificationsCreation.createTitle(DELETED_QUESTION_TITLE, question.getId());
        String content = NotificationsCreation.createContent(DELETED_QUESTION_CONTENT, question.getId(), user.getName());
        question.Notify(notificationService.createNotification(title, content, Notification.Type.QUESTION));
    }

    public void prepareNotification(Submission submission) {
        String title = NotificationsCreation.createTitle(NEW_SUBMISSION_TITLE, submission.getQuestion().getId());
        String content = NotificationsCreation.createContent(NEW_SUBMISSION_CONTENT, submission.getUser().getName());
        submission.Notify(notificationService.createNotification(title, content, Notification.Type.SUBMISSION));
    }
}
