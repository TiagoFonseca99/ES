package pt.ulisboa.tecnico.socialsoftware.tutor.submission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
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
    private ReviewRepository reviewRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public SubmissionDto createSubmission(Question question, SubmissionDto submissionDto){
        checkIfConsistentSubmission(question, submissionDto.getStudentId());

        User user = getStudent(submissionDto);

        checkIfQuestionAlreadySubmitted(question, user);

        Submission submission = new Submission(question, user, submissionDto);

        entityManager.persist(submission);
        return new SubmissionDto(submission);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ReviewDto reviewSubmission(Integer teacherId, ReviewDto reviewDto, Review.Status status) {

        checkIfConsistentReview(reviewDto, status);

        User user = getTeacher(teacherId);
        Submission submission = getSubmission(reviewDto);

        checkIfSubmissionIsApproved(reviewDto, teacherId);

        reviewDto.setStatus(status);

        Review review = new Review(user, submission, reviewDto);

        entityManager.persist(review);
        return new ReviewDto(review);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ReviewDto> getSubmissionStatus(Integer studentId) {
        if(studentId == null)
            throw new TutorException(SUBMISSION_MISSING_STUDENT);
        return reviewRepository.getSubmissionStatus(studentId).stream().map(ReviewDto::new).collect(Collectors.toList());
    }

    private void checkIfConsistentSubmission(Question question, Integer studentId) {
        if(question == null)
            throw new TutorException(SUBMISSION_MISSING_QUESTION);
        if(studentId == null)
            throw new TutorException(SUBMISSION_MISSING_STUDENT);
    }

    private void checkIfQuestionAlreadySubmitted(Question question, User user) {
        if(user.getSubmittedQuestions().contains(question))
            throw new TutorException(QUESTION_ALREADY_SUBMITTED, user.getUsername());
    }

    private User getStudent(SubmissionDto submissionDto) {
        int userId = submissionDto.getStudentId();
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));
        if(user.isStudent() != null && !user.isStudent())
            throw new TutorException(USER_NOT_STUDENT, user.getUsername());
        return user;
    }

    private void checkIfConsistentReview(ReviewDto reviewDto, Review.Status status){

        checkIfReviewHasJustification(reviewDto);

        if(reviewDto.getSubmissionId() == null)
            throw new TutorException(REVIEW_MISSING_SUBMISSION);
        if(reviewDto.getStudentId() == null)
            throw new TutorException(REVIEW_MISSING_STUDENT);
        if(status == null)
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


    private Submission getSubmission(ReviewDto reviewDto){

        int submissionId = reviewDto.getSubmissionId();
        return submissionRepository.findById(submissionId).orElseThrow(() -> new TutorException(SUBMISSION_NOT_FOUND, submissionId));
    }


    private void checkIfReviewHasJustification(ReviewDto reviewDto) {

        String justification = reviewDto.getJustification();
        if (justification == null || justification.isBlank()) {
            throw new TutorException(REVIEW_MISSING_JUSTIFICATION);
        }
    }

    private void checkIfSubmissionIsApproved(ReviewDto reviewDto, Integer teacherId) {

        if (reviewDto.getStatus() == Review.Status.APPROVED) {
            throw new TutorException(QUESTION_ALREADY_APPROVED, teacherId);
        }
    }
}