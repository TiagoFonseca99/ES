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


    public ReviewDto reviewSubmission(Integer teacher_id, ReviewDto reviewDto) {

        User user = getTeacher(teacher_id);
        Submission submission = getSubmission(reviewDto);

        checkIfReviewHasJustification(reviewDto, user);

        Review review = new Review(user, submission, reviewDto);

        entityManager.persist(review);
        return new ReviewDto(review);
    }


    private User getTeacher(Integer teacher_id) {

        User user = userRepository.findById(teacher_id).orElseThrow(() -> new TutorException(USER_NOT_FOUND, teacher_id));
        if (!user.isTeacher())
            throw new TutorException(USER_NOT_TEACHER, user.getUsername());
        return user;
    }


    private void checkIfReviewHasJustification(ReviewDto reviewDto, User user) {

        String justification = reviewDto.getJustification();
        if (justification == null || justification.isBlank()) {
            throw new TutorException(REVIEW_MISSING_DATA, user.getUsername());
        }
    }


    private Submission getSubmission(ReviewDto reviewDto){

        int submission_id = reviewDto.getSubmissionId();
        Submission submission = submissionRepository.findById(submission_id).orElseThrow(() -> new TutorException(SUBMISSION_NOT_FOUND, submission_id));
        return submission;
    }

}
