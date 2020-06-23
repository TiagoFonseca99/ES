package pt.ulisboa.tecnico.socialsoftware.tutor.announcement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.dto.AnnouncementDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.validation.Valid;
import java.security.Principal;
import java.sql.SQLException;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

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

    @DeleteMapping("/management/announcements/{announcementId}")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean removeAnnouncement(@Valid @PathVariable Integer announcementId) {
        return announcementService.removeAnnouncement(announcementId);
    }
}
