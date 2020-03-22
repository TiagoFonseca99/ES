package pt.ulisboa.tecnico.socialsoftware.tutor.submission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

@RestController
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private QuestionService questionService;

    @PostMapping(value = "/submissions")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_STUDENT') and hasPermission(#questionId, 'QUESTION.ACCESS'))")
    public SubmissionDto createSubmission(Principal principal, @Valid @RequestBody SubmissionDto submissionDto){
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        QuestionDto question = questionService.createQuestion(submissionDto.getCourseId(), submissionDto.getQuestionDto());

        return submissionService.createSubmission(question.getId(), submissionDto);
    }

    @PostMapping(value = "/submissions/reviews")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ReviewDto createReview(Principal principal, @RequestBody ReviewDto reviewDto, @RequestBody Review.Status status) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return submissionService.reviewSubmission(user.getId(), reviewDto, status);
    }
}

