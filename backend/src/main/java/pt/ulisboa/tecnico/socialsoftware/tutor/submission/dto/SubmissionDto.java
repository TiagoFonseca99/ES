package pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission;

import java.io.Serializable;

public class SubmissionDto implements Serializable {
    private Integer id;
    private Integer key;
    private Integer questionId;
    private Integer studentId;


    public SubmissionDto(){}

    public SubmissionDto(Submission submission){
        this.id = submission.getId();
        this.key = submission.getKey();
        this.questionId = submission.getQuestion().getId();
        this.studentId = submission.getUser().getId();
    }

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public Integer getKey() { return key; }

    public void setKey(Integer key) { this.key = key; }

    public void setQuestionId(Integer questionId) { this.questionId = questionId; }

    public Integer getStudentId() { return studentId; }

    public void setStudentId(Integer studentId) { this.studentId = studentId; }
}
