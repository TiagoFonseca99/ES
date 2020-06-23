package pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.dto.NotificationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@Table(name = "notifications")
public class Notification {
    public enum Type {
        BASIC, TOURNAMENT, ANNOUNCEMENT, SUBMISSION, REVIEW, TEACHER_SUBMISSION, DISCUSSION, QUESTION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private Type type = Type.BASIC;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    public Notification() {}

    public Notification(NotificationDto notificationDto){
        setTitle(notificationDto.getTitle());
        setContent(notificationDto.getContent());
        setCreationDate(DateHandler.toLocalDateTime(notificationDto.getCreationDate()));
        setType(notificationDto.getType());
    }

    public Integer getId() { return id; }

    public String getTitle() { return title; }

    public void setTitle(String title) {
        if (title == null || title.isBlank())
            throw new TutorException(INVALID_TITLE_FOR_NOTIFICATION);
        this.title = title;
    }

    public String getContent() { return content; }

    public void setContent(String content) {
        if (content == null || content.isBlank())
            throw new TutorException(INVALID_CONTENT_FOR_NOTIFICATION);
        this.content = content;
    }

    public LocalDateTime getCreationDate() { return creationDate; }

    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public List<User> getUsers() { return users; }

    public void addUser(User user) { this.users.add(user); }

    public void removeUser(User user) { this.users.remove(user); }

    public Type getType() { return type; }

    public void setType(Type type) { this.type = type; }

    public void setType(String type) { this.type = Type.valueOf(type); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return getId().equals(that.getId()) &&
                getTitle().equals(that.getTitle()) &&
                getContent().equals(that.getContent()) &&
                getType() == that.getType() &&
                Objects.equals(getCreationDate(), that.getCreationDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getContent(), getType(), getCreationDate());
    }
}
