package pt.ulisboa.tecnico.socialsoftware.tutor.notifications;

import java.util.List;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.dto.NotificationDto;

public class NotificationsCreation {
    private NotificationsCreation(){}

    public static String createString(NotificationsMessage message, List<Object> arguments) {
        return String.format(message.getMessage(), arguments.toArray());
    }

    public static NotificationDto create(NotificationsMessage title, List<Object> titleArgs, NotificationsMessage content,
            List<Object> contentArgs, Notification.Type type) {
        NotificationDto notification = new NotificationDto();
        notification.setCreationDate(DateHandler.toISOString(DateHandler.now()));
        notification.setTitle(createString(title, titleArgs));
        notification.setContent(createString(content, contentArgs));
        notification.setType(type.name());

        return notification;
    }
}
