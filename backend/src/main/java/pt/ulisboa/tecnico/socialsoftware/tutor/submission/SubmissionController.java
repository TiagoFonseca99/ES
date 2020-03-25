package pt.ulisboa.tecnico.socialsoftware.tutor.submission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
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
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private QuestionService questionService;

    @PostMapping(value = "/submissions")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STUDENT')")
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

    @PostMapping(value = "/submissions/reviews/{status}")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ReviewDto createReview(Principal principal, @RequestBody ReviewDto reviewDto, @PathVariable Review.Status status) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return submissionService.reviewSubmission(user.getId(), reviewDto, status);
    }

    @GetMapping(value = "/submissions/status")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STUDENT') or hasRole('ROLE_TEACHER')")
    public List<ReviewDto> getSubmissionStatus(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        return submissionService.getSubmissionStatus(user.getId());
    }

}
