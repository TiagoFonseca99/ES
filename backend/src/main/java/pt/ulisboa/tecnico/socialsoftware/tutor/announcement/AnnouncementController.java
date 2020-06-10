package pt.ulisboa.tecnico.socialsoftware.tutor.announcement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.dto.AnnouncementDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.COURSE_EXECUTION_MISSING;

@RestController
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @PostMapping(value = "/management/announcements")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public AnnouncementDto createAnnouncement(Principal principal, @Valid @RequestBody AnnouncementDto announcementDto) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        announcementDto.setUserId(user.getId());
        return announcementService.createAnnouncement(announcementDto);
    }

    @GetMapping(value = "/management/announcements")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public List<AnnouncementDto> getAnnouncements(Principal principal, @Valid @RequestParam Integer executionId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        } else if (executionId == null) {
            throw new TutorException(COURSE_EXECUTION_MISSING);
        }

        return announcementService.getAnnouncements(user.getId(), executionId);
    }

    @PutMapping("/management/announcements/{announcementId}")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public AnnouncementDto updateAnnouncement(@PathVariable Integer announcementId, @Valid @RequestBody AnnouncementDto announcementDto) {
        return announcementService.updateAnnouncement(announcementId, announcementDto);
    }
}
