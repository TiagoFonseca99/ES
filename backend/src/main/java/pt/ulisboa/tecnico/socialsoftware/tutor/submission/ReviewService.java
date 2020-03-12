package pt.ulisboa.tecnico.socialsoftware.tutor.submission;

import org.springframework.beans.factory.annotation.Autowired;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.SubmissionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Review;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.ReviewDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

public class ReviewService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @PersistenceContext
    EntityManager entityManager;


    public ReviewDto reviewSubmission(Integer teacherId, ReviewDto reviewDto, Review.Status status) {

        checkIfConsistentReview(reviewDto, status);
        checkIfReviewHasJustification(reviewDto);

        User user = getTeacher(teacherId);
        Submission submission = getSubmission(reviewDto);

        checkIfSubmissionIsApproved(reviewDto, teacherId);

        reviewDto.setStatus(status);

        Review review = new Review(user, submission, reviewDto);

        entityManager.persist(review);
        return new ReviewDto(review);
    }


    private void checkIfConsistentReview(ReviewDto reviewDto, Review.Status status){

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
