package pt.ulisboa.tecnico.socialsoftware.tutor.announcement.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.dto.AnnouncementDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;


@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "course_execution_id")
    private CourseExecution courseExecution;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private boolean edited;

    public Announcement() {}

    public Announcement(User user, CourseExecution courseExecution, AnnouncementDto announcementDto){
        setTitle(announcementDto.getTitle());
        setContent(announcementDto.getContent());
        setCreationDate(DateHandler.toLocalDateTime(announcementDto.getCreationDate()));
        setUser(user);
        setCourseExecution(courseExecution);
        setEdited(announcementDto.isEdited());
        user.addAnnouncement(this);
        courseExecution.addAnnouncement(this);
    }

    public void update(AnnouncementDto announcementDto) {
        setTitle(announcementDto.getTitle());

        setContent(announcementDto.getContent());

        setEdited(true);
    }

    public void remove() {
        getCourseExecution().getAnnouncements().remove(this);
        courseExecution = null;

        getUser().getAnnouncements().remove(this);
        user = null;
    }

    public Integer getId() { return id; }

    public String getTitle() { return title; }

    public void setTitle(String title) {
        if (title == null || title.isBlank())
            throw new TutorException(INVALID_TITLE_FOR_ANNOUNCEMENT);
        this.title = title;
    }

    public String getContent() { return content; }

    public void setContent(String content) {
        if (content == null || content.isBlank())
            throw new TutorException(INVALID_CONTENT_FOR_ANNOUNCEMENT);
        this.content = content;
    }

    public LocalDateTime getCreationDate() { return creationDate; }

    public void setCreationDate(LocalDateTime creationDate) {
        if (this.creationDate == null) {
            this.creationDate = DateHandler.now();
        } else {
            this.creationDate = creationDate;
        }
    }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public CourseExecution getCourseExecution() { return courseExecution; }

    public void setCourseExecution(CourseExecution courseExecution) { this.courseExecution = courseExecution; }

    public boolean isEdited() { return edited; }

    public void setEdited(boolean edited) { this.edited = edited; }
}