package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.api;

import java.security.Principal;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.ReplyDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

@RestController
public class DiscussionController {
    @Autowired
    private DiscussionService discussionService;

    @PostMapping(value = "/discussions")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public DiscussionDto createDiscussion(Principal principal, @Valid @RequestBody DiscussionDto discussion){
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        discussion.setUserId(user.getId());
        discussion.setDate(DateHandler.toISOString(DateHandler.now()));

        return discussionService.createDiscussion(discussion);
    }

    @PutMapping(value = "/discussions")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public DiscussionDto setAvailability(Principal principal, @Valid @RequestParam boolean available, @Valid @RequestBody DiscussionDto discussion) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        discussion.setAvailability(available);

        return discussionService.setAvailability(user.getId(), discussion);
    }

    @PostMapping(value = "/discussions/replies")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    public ReplyDto createReply(Principal principal, @Valid @RequestParam String message, @Valid @RequestBody DiscussionDto discussion){
        User user = (User) ((Authentication) principal).getPrincipal();

        ReplyDto reply = new ReplyDto();

        reply.setMessage(message);

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        reply.setUserId(user.getId());
        reply.setDate(DateHandler.toISOString(DateHandler.now()));

        return discussionService.createReply(reply, discussion);

    }

    @DeleteMapping(value = "/discussions/replies/{reply}")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    public boolean removeReply(Principal principal, @Valid @PathVariable Integer reply) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        return discussionService.removeReply(user.getId(), reply);
    }

    @PutMapping(value = "/discussions/replies/edit")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    public ReplyDto editReply(Principal principal, @Valid @RequestBody ReplyDto reply) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        return discussionService.editReply(user.getId(), reply);
    }

    @DeleteMapping(value = "/discussions/{userId}/{questionId}")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    public boolean removeDiscussion(Principal principal, @Valid @PathVariable("userId") Integer userId, @Valid @PathVariable("questionId") Integer questionId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        return discussionService.removeDiscussion(user.getId(), userId, questionId);
    }

    @PutMapping(value = "/discussions/edit")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    public DiscussionDto editDiscussion(Principal principal, @Valid @RequestBody DiscussionDto discussion) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        return discussionService.editDiscussion(user.getId(), discussion);
    }

    @GetMapping(value = "/discussions")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public List<DiscussionDto> getDiscussionsByUser(Principal principal, @Valid @RequestParam Integer userId, @Valid @RequestParam Integer courseId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }else if(!user.getId().equals(userId)){
            throw new TutorException(ErrorMessage.DISCUSSION_NOT_SUBMITTED_BY_REQUESTER, user.getId());
        }

        return discussionService.findDiscussionsByUserId(userId, courseId);
    }

    @GetMapping(value = "/discussions/question")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public List<DiscussionDto> getDiscussionsByQuestions(Principal principal, @Valid @RequestParam Integer questionId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        return discussionService.findDiscussionsByQuestionId(questionId);
    }
}
