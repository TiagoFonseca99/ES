package pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto.ReviewDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;

@Entity
@Table(name = "reviews")

public class Review {

    public enum Status {
        APPROVED, REJECTED, IN_REVIEW
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private Integer studentId;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Enumerated(EnumType.STRING)
    private Review.Status status = Review.Status.IN_REVIEW;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="submission_id")
    private Submission submission;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "review")
    private Image image;


    public Review() {}

    public Review(User user, Submission submission, ReviewDto reviewDto) {
        this.justification = reviewDto.getJustification();
        this.status = reviewDto.getStatus();
        this.user = user;
        this.submission = submission;
        this.studentId = submission.getStudentId();

        if (reviewDto.getImageDto() != null) {
            Image img = new Image(reviewDto.getImageDto());
            setImage(img);
            img.setReview(this);
        }

    }

    public int getTeacherId() { return this.user.getId(); }

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
}
