package pt.ulisboa.tecnico.socialsoftware.tutor.notifications;

import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification;

public interface Observer {
    void update(Object o, Notification n);
}
