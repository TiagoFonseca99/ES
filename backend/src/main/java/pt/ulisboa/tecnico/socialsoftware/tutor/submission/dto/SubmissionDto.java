package pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission;

import java.io.Serializable;

public class SubmissionDto implements Serializable {
    private Integer id;
    private Integer key;
    private Integer courseId;
    private QuestionDto questionDto;
    private Integer studentId;


    public SubmissionDto(){}

    public SubmissionDto(Submission submission){
        this.id = submission.getId();
        this.key = submission.getKey();
        this.courseId = submission.getQuestion().getCourse().getId();
        if(submission.getQuestion() != null)
            this.questionDto = new QuestionDto(submission.getQuestion());
        this.studentId = submission.getUser().getId();
    }

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public Integer getKey() { return key; }

    public void setKey(Integer key) { this.key = key; }

    public QuestionDto getQuestionDto() { return questionDto; }

    public void setQuestionDto(QuestionDto questionDto) { this.questionDto = questionDto; }

    public Integer getCourseId() { return courseId; }

    public Integer getStudentId() { return studentId; }

    public void setStudentId(Integer studentId) { this.studentId = studentId; }
}
