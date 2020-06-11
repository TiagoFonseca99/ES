package pt.ulisboa.tecnico.socialsoftware.tutor.notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.dto.NotificationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService;

import javax.validation.Valid;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping(value = "/notifications")
    public List<NotificationDto> getNotifications(@Valid @RequestParam String username) {
        User user = userService.findByUsername(username);

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return notificationService.getUserNotifications(user.getId());
    }
}
