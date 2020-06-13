package pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Review;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

public class ReviewDto implements Serializable {
    private Integer id;
    private Integer studentId;
    private Integer teacherId;
    private Integer submissionId;
    private String justification;
    private ImageDto imageDto;
    private String status;
    private String creationDate;
    private String studentUsername;
    private String teacherUsername;


    public ReviewDto(){}

    public ReviewDto(Review review){
        this.id = review.getId();
        this.studentId = review.getStudentId();
        this.studentUsername = review.getSubmission().getUser().getUsername();
        this.teacherId = review.getTeacherId();
        this.teacherUsername = review.getUser().getUsername();
        this.submissionId = review.getSubmission().getId();
        this.justification = review.getJustification();
        this.status = review.getStatus().name();

        if (review.getImage() != null)
            this.imageDto = new ImageDto(review.getImage());

        if (review.getCreationDate() != null)
            this.creationDate = DateHandler.toISOString(review.getCreationDate());
    }
    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public Integer getStudentId() { return studentId; }

    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getTeacherId() { return teacherId; }

    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }

    public Integer getSubmissionId() { return submissionId; }

    public void setSubmissionId(Integer submissionId) { this.submissionId = submissionId; }

    public String getJustification() { return justification; }

    public void setJustification(String justification) { this.justification = justification; }

    public ImageDto getImageDto() { return imageDto; }

    public void setImageDto(ImageDto imageDto) { this.imageDto = imageDto; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getCreationDate() { return creationDate; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    public String getStudentUsername() { return studentUsername; }

    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }

    public String getTeacherUsername() { return teacherUsername; }

    public void setTeacherUsername(String teacherUsername) { this.teacherUsername = teacherUsername; }
}
