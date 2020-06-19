package pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain;

import org.springframework.beans.factory.annotation.Autowired;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.dto.NotificationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    public Notification() {}

    public Notification(NotificationDto notificationDto){
        setTitle(notificationDto.getTitle());
        setContent(notificationDto.getContent());
        setCreationDate(DateHandler.toLocalDateTime(notificationDto.getCreationDate()));
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
}
