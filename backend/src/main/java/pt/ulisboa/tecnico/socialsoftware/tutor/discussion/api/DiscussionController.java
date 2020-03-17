package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.api;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;

@RestController
public class DiscussionController {
    private static Logger logger = LoggerFactory.getLogger(DiscussionController.class);

    @Autowired
    private DiscussionService discussionService;

    @PostMapping(value = "/discussions")
    @PreAuthorize("hasROLE('ROLE_STUDENT')")
    public DiscussionDto createDiscussion(@Valid @RequestBody DiscussionDto discussion){
        return discussionService.createDiscussion(discussion);
    }
}
