package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto;

import java.io.Serializable;

import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;

public class DiscussionDto implements Serializable {
    private Integer userId;
    private Integer questionId;
    private QuestionDto question;
    private String content;

    public DiscussionDto() {
    }

    public DiscussionDto(Discussion discussion) {
        this.userId = discussion.getUser().getId();
        this.questionId = discussion.getQuestion().getId();
        this.content = discussion.getContent();
        this.question = new QuestionDto(discussion.getQuestion());
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setUserId(Integer id) {
        this.userId = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public QuestionDto getQuestion() {
        return question;
    }

    public void setQuestion(QuestionDto question) {
        this.question = question;
        this.questionId = question.getId();
    }
}
