package pt.ulisboa.tecnico.socialsoftware.tutor.announcement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.dto.AnnouncementDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.validation.Valid;
import java.security.Principal;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @PostMapping(value = "/management/announcements")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public AnnouncementDto createSubmission(Principal principal, @Valid @RequestBody AnnouncementDto announcementDto) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        announcementDto.setUserId(user.getId());
        return announcementService.createAnnouncement(announcementDto);
    }
}
