package pt.ulisboa.tecnico.socialsoftware.tutor.submission.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission;

import java.io.Serializable;

public class SubmissionDto implements Serializable {
    private Integer id;
    private Integer courseId;
    private QuestionDto questionDto;
    private Integer studentId;
    private String username;
    private String argument;
    private boolean anonymous;

    public SubmissionDto(){}

    public SubmissionDto(Submission submission){
        this.id = submission.getId();
        this.courseId = submission.getQuestion().getCourse().getId();
        if(submission.getQuestion() != null)
            this.questionDto = new QuestionDto(submission.getQuestion());
        this.studentId = submission.getUser().getId();
        this.username = submission.getUser().getUsername();
        this.argument = submission.getArgument();
        this.anonymous = submission.isAnonymous();
    }

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public QuestionDto getQuestionDto() { return questionDto; }

    public void setQuestionDto(QuestionDto questionDto) { this.questionDto = questionDto; }

    public Integer getCourseId() { return courseId; }

    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public Integer getStudentId() { return studentId; }

    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public boolean isAnonymous() { return anonymous; }

    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public String getArgument() { return argument; }

    public void setArgument(String argument) { this.argument = argument; }
}
