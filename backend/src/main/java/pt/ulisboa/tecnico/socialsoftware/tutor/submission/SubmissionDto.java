package pt.ulisboa.tecnico.socialsoftware.tutor.submission;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;

import java.io.Serializable;

public class SubmissionDto implements Serializable {
    private Integer id;
    private Integer key;
    private int questionId;
    private int studentId;


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

    public int getQuestionId() { return questionId; }

    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public int getStudentId() { return studentId; }

    public void setStudentId(int studentId) { this.studentId = studentId; }
}
