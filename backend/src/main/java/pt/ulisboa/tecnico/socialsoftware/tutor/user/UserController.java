package pt.ulisboa.tecnico.socialsoftware.tutor.user;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.DashboardDto;

import javax.validation.Valid;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USERNAME_NOT_FOUND;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping(value = "/dashboard")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_TEACHER')")
    public DashboardDto getDashboardInfo(@Valid @RequestParam String username) {
        User user = userService.findByUsername(username);
        
        if (user == null) {
            throw new TutorException(USERNAME_NOT_FOUND, username);
        }

        return userService.getDashboardInfo(user.getId());
    }

    @PutMapping(value = "/dashboard/discussions")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public DashboardDto toggleDiscussionStats(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return userService.toggleDiscussionStatsVisibility(user.getId());
    }

    @PutMapping(value = "/dashboard/tournaments")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public DashboardDto toggleTournamentStats(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return userService.toggleTournamentStatsVisibility(user.getId());
    }

    @PutMapping(value = "/dashboard/submissions")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public DashboardDto toggleSubmissionStats(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return userService.toggleSubmissionStatsVisibility(user.getId());
    }

    @PutMapping(value = "/dashboard/stats")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public DashboardDto toggleUserStats(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return userService.toggleUserStatsVisibility(user.getId());
    }
}
