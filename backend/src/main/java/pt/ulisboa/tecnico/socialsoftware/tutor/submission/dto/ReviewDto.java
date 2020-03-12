package pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Review;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto;

import java.io.Serializable;

public class ReviewDto implements Serializable {
    private Integer id;
    private Integer key;
    private int studentId;
    private int teacherId;
    private int submissionId;
    private String justification;
    private ImageDto imageDto;
    private Review.Status status;


    public ReviewDto(){}

    public ReviewDto(Review review){
        this.id = review.getId();
        this.key = review.getKey();
        this.studentId = review.getStudentId();
        this.teacherId = review.getTeacherId();
        this.submissionId = review.getSubmission().getId();
        this.justification = review.getJustification();
        this.status = review.getStatus();

        if (review.getImage() != null)
            this.imageDto = new ImageDto(review.getImage());

    }

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public Integer getKey() { return key; }

    public void setKey(Integer key) { this.key = key; }

    public int getStudentId() { return studentId; }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getTeacherId() { return teacherId; }

    public int getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(int submissionId) {
        this.submissionId = submissionId;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public ImageDto getImageDto() {
        return imageDto;
    }

    public void setImageDto(ImageDto imageDto) {
        this.imageDto = imageDto;
    }

    public Review.Status getStatus() {
        return status;
    }

    public void setStatus(Review.Status status) {
        this.status = status;
    }


}
