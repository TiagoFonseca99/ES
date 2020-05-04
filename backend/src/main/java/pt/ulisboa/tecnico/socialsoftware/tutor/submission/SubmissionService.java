package pt.ulisboa.tecnico.socialsoftware.tutor.submission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.SubmissionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Review;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.ReviewDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.ReviewRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.SubmissionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

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

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public SubmissionDto createSubmission(Integer questionId, SubmissionDto submissionDto){

        checkIfConsistentSubmission(questionId, submissionDto.getStudentId());

        Question question = getQuestion(questionId);

        User user = getStudent(submissionDto.getStudentId());

        checkIfQuestionAlreadySubmitted(question, user);

        Submission submission = new Submission(question, user);

        entityManager.persist(submission);
        return new SubmissionDto(submission);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public SubmissionDto resubmitQuestion(Integer oldQuestionId, Integer newQuestionId, SubmissionDto submissionDto) {
        checkIfConsistentSubmission(newQuestionId, submissionDto.getStudentId());
        if(oldQuestionId == null)
            throw new TutorException(SUBMISSION_MISSING_QUESTION);

        Question oldQuestion = getQuestion(oldQuestionId);
        oldQuestion.setStatus("DEPRECATED");

        Question newQuestion = getQuestion(newQuestionId);
        setNewOptionsId(submissionDto, newQuestion);
        newQuestion.update(submissionDto.getQuestionDto());


        User user = getStudent(submissionDto.getStudentId());

        Submission submission = new Submission(newQuestion, user);

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

        if (reviewDto.getStatus().equals("APPROVED")){
            updateQuestionStatus(submission);
        }

        Review review = new Review(user, submission, reviewDto);

        entityManager.persist(review);
        return new ReviewDto(review);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ReviewDto> getSubmissionStatus(Integer submissionId) {
        if(submissionId == null)
            throw new TutorException(SUBMISSION_NOT_FOUND, 0);

        return reviewRepository.getSubmissionStatus(submissionId).stream().map(ReviewDto::new).collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ReviewDto> getSubmissionReviews(Integer studentId) {
        if(studentId == null)
            throw new TutorException(REVIEW_MISSING_STUDENT);

        return reviewRepository.getSubmissionReviews(studentId).stream().map(ReviewDto::new).collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<SubmissionDto> getSubsToTeacher() {

        return submissionRepository.findAll().stream().map(SubmissionDto::new).collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ReviewDto> getReviewsToTeacher() {

        return reviewRepository.findAll().stream().map(ReviewDto::new).collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<SubmissionDto> getSubmissions(Integer studentId) {
        if(studentId == null)
            throw new TutorException(SUBMISSION_MISSING_STUDENT);

        return submissionRepository.getSubmissions(studentId).stream().map(SubmissionDto::new).collect(Collectors.toList());
    }

    private void updateQuestionStatus(Submission submission) {
        Question question = getQuestion(submission.getQuestion().getId());
        question.setStatus("AVAILABLE");
    }

    private void setNewOptionsId(SubmissionDto submissionDto, Question newQuestion) {
        List<OptionDto> newOptions = newQuestion.getOptions().stream().map(OptionDto::new).collect(Collectors.toList());

        int i = 0;
        for (OptionDto option: submissionDto.getQuestionDto().getOptions()) {
            option.setId(newOptions.get(i++).getId());
        }
    }

    private void checkIfConsistentSubmission(Integer questionId, Integer studentId) {
        if(questionId == null)
            throw new TutorException(SUBMISSION_MISSING_QUESTION);
        if(studentId == null)
            throw new TutorException(SUBMISSION_MISSING_STUDENT);
    }

    private void checkIfQuestionAlreadySubmitted(Question question, User user) {
        if(user.getSubmittedQuestions().contains(question))
            throw new TutorException(QUESTION_ALREADY_SUBMITTED, user.getUsername());
    }

    private Question getQuestion(Integer questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));
    }

    private User getStudent(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));
        if(user.isStudent() != null && !user.isStudent())
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
        if (user.isTeacher() != null && !user.isTeacher())
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

}
