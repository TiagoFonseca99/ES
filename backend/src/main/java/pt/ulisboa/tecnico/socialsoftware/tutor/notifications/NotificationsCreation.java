package pt.ulisboa.tecnico.socialsoftware.tutor.notifications;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.dto.NotificationDto;

public class NotificationsCreation {

    private NotificationDto notificationDto;

    public NotificationsCreation(String title, String content) {
        this.notificationDto = new NotificationDto();
        this.notificationDto.setTitle(title);
        this.notificationDto.setContent(content);
        this.notificationDto.setCreationDate(DateHandler.toISOString(DateHandler.now()));
    }

    public NotificationDto getNotificationDto() {
        return notificationDto;
    }

    public static String createTitle(NotificationsMessage message, Integer value) {
        return String.format(message.getMessage(), value);
    }

    public static String createContent(NotificationsMessage message, Integer value) {
        return String.format(message.getMessage(), value);
    }

    public static String createContent(NotificationsMessage message, Integer value1, Integer value2) {
        return String.format(message.getMessage(), value1, value2);
    }

    public static String createContent(NotificationsMessage message, Integer value1, String value2, String value3) {
        return String.format(message.getMessage(), value1, value2, value3);
    }

    public static String createContent(NotificationsMessage message, Integer value1, Integer value2, Integer value3) {
        return String.format(message.getMessage(), value1, value2, value3);
    }
}


