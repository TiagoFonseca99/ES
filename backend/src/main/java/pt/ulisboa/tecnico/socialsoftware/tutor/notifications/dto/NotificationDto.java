package pt.ulisboa.tecnico.socialsoftware.tutor.notifications.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NotificationDto implements Serializable {

    private Integer id;
    private List<Integer> usersId;
    private String title;
    private String content;
    private String creationDate;

    public NotificationDto() {}

    public NotificationDto(Notification notification) {
        this.id = notification.getId();
        setUsersId(notification.getUsers());
        this.title = notification.getTitle();
        this.content = notification.getContent();
        this.creationDate = DateHandler.toISOString(notification.getCreationDate());
    }

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public List<Integer> getUsersId() { return usersId; }

    public void setUsersId(List<User> users) {
        this.usersId = new ArrayList<>();
        for(User user: users) {
            this.usersId.add(user.getId());
        }
    }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public String getCreationDate() { return creationDate; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }
}