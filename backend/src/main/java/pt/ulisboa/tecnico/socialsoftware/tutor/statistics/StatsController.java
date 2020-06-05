package pt.ulisboa.tecnico.socialsoftware.tutor.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService;

import java.security.Principal;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USERNAME_NOT_FOUND;

@RestController
public class StatsController {

    @Autowired
    private StatsService statsService;

    @Autowired
    private UserService userService;

    @GetMapping("/executions/{executionId}/stats/{username}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#executionId, 'EXECUTION.ACCESS') or hasRole('ROLE_TEACHER')")
    public StatsDto getStats(Principal principal, @PathVariable int executionId, @PathVariable String username) {
        User user = userService.findByUsername(username);

        if (user == null) {
            throw new TutorException(USERNAME_NOT_FOUND, username);
        }
        
        return statsService.getStats(user.getId(), executionId);
    }
}