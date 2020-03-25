package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.api;

import java.security.Principal;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.ReplyDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

@RestController
public class DiscussionController {
    private static Logger logger = LoggerFactory.getLogger(DiscussionController.class);

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

        return discussionService.createDiscussion(discussion);
    }

    @GetMapping(value = "/discussions/reply")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ReplyDto getReply(Principal principal, @Valid @RequestBody DiscussionDto discussion){
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        return discussionService.getReply(user.getId(), discussion);
    }

    @PostMapping(value = "/discussions/replies")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ReplyDto createReply(Principal principal, @Valid @RequestParam String message, @Valid @RequestBody DiscussionDto discussion){
        User user = (User) ((Authentication) principal).getPrincipal();

        ReplyDto reply = new ReplyDto();

        reply.setMessage(message);

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        reply.setTeacherId(user.getId());

        return discussionService.giveReply(reply, discussion);
    }
}
