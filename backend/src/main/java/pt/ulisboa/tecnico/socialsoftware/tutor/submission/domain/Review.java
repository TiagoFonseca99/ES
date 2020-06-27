package pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.Observable;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.Observer;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.ReviewDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "reviews")

public class Review implements Observable {

    public enum Status {
        APPROVED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private Integer studentId;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Enumerated(EnumType.STRING)
    private Review.Status status = null;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "submission_id")
    private Submission submission;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "review")
    private Image image;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<User> observers = new HashSet<>();

    public Review() {
    }

    public Review(User user, Submission submission, ReviewDto reviewDto) {
        this.justification = reviewDto.getJustification();
        this.status = Status.valueOf(reviewDto.getStatus());
        this.user = user;
        this.submission = submission;
        this.studentId = submission.getStudentId();
        setCreationDate(DateHandler.toLocalDateTime(reviewDto.getCreationDate()));

        if (reviewDto.getImageDto() != null) {
            Image img = new Image(reviewDto.getImageDto());
            setImage(img);
            img.setReview(this);
        }

    }

    public int getTeacherId() {
        return this.user.getId();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public String getJustification() {
        return justification;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        if (this.creationDate == null) {
            this.creationDate = DateHandler.now();
        } else {
            this.creationDate = creationDate;
        }
    }
    
    public Set<User> getUsers() {
        return observers;
    }

    @Override
    public String toString() {
        return "Review{" + "id=" + id + ", justification='" + justification + '\'' + ", status=" + status
                + ", studentId=" + studentId + ", submission=" + submission.getQuestion() + ", image=" + image + '}';
    }

    @Override
    public void Attach(Observer o) {
        this.observers.add((User) o);
    }

    @Override
    public void Dettach(Observer o) {
        this.observers.remove(o);
    }

    @Override
    public void Notify(Notification notification, User user) {
        for (Observer observer : observers) {
            if (((User) observer).getId() == user.getId()) {
                continue;
            }

            observer.update(this, notification);
        }
    }
}
