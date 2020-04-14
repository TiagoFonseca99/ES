package pt.ulisboa.tecnico.socialsoftware.tutor.submission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.SubmissionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Review;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.ReviewDto;

import javax.validation.Valid;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

import java.security.Principal;
import java.util.List;

@RestController
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private QuestionService questionService;

    @PostMapping(value = "/student/submissions")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public SubmissionDto createSubmission(Principal principal, @Valid @RequestBody SubmissionDto submissionDto){
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        QuestionDto question = questionService.createQuestion(submissionDto.getCourseId(), submissionDto.getQuestionDto());
        submissionDto.setQuestionDto(question);
        submissionDto.setStudentId(user.getId());

        return submissionService.createSubmission(question.getId(), submissionDto);
    }

    @PostMapping(value = "/management/reviews")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ReviewDto createReview(Principal principal, @RequestBody ReviewDto reviewDto) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        reviewDto.setTeacherId(user.getId());

        return submissionService.reviewSubmission(user.getId(), reviewDto);
    }

    @GetMapping(value = "/management/reviews")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public List<SubmissionDto> getSubsToTeacher(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return submissionService.getSubsToTeacher();
    }

    @GetMapping(value = "/management/reviews/showReviews")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public List<ReviewDto> getReviewsToTeacher(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return submissionService.getReviewsToTeacher();
    }


    @GetMapping(value = "/student/submissions/{submissionId}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public List<ReviewDto> getSubmissionStatus(Principal principal, @PathVariable int submissionId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        return submissionService.getSubmissionStatus(submissionId);
    }

    @GetMapping(value = "/student/reviews")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public List<ReviewDto> getSubmissionReviews(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        return submissionService.getSubmissionReviews(user.getId());
    }

    @GetMapping(value = "/student/submissions")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public List<SubmissionDto> getSubmissions(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        return submissionService.getSubmissions(user.getId());
    }
}
