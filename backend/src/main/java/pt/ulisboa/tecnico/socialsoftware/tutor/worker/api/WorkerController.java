// File is based on https://golb.hplar.ch/2019/08/webpush-java.html

package pt.ulisboa.tecnico.socialsoftware.tutor.worker.api;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.ServerKeys;
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.WorkerService;
import pt.ulisboa.tecnico.socialsoftware.tutor.worker.dto.SubscriptionDto;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@RestController
public class WorkerController {
    @Autowired
    private ServerKeys serverKeys;

    @Autowired
    private WorkerService workerService;

    @GetMapping("/worker/publicKey")
    public String publicKey() {
        return serverKeys.getBase64();
    }

    @PostMapping("/worker/subscribe")
    public void subscribe(Principal principal, @RequestBody SubscriptionDto subscription) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        workerService.subscribe(user.getId(), subscription);
    }

    @PostMapping("/worker/isSubscribed")
    public void isSubscribed(Principal principal, @RequestBody SubscriptionDto subscription) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        workerService.isSubscribed(user.getId(), subscription);
    }

    @DeleteMapping("/worker/unsubscribe")
    public void unsubscribe(Principal principal, @RequestBody SubscriptionDto subscription) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        workerService.unsubscribe(user.getId(), subscription);
    }
}
