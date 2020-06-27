package pt.ulisboa.tecnico.socialsoftware.tutor.notifications;

import java.util.Set;

import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

public interface Observable {
    void Attach(Observer o);
    void Dettach(Observer o);
    void Notify(Notification n, User user);
    Set<User> getObservers();
}

