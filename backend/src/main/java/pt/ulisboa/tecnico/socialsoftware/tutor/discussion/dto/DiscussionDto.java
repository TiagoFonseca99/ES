package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto;

import java.io.Serializable;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;

public class DiscussionDto implements Serializable {
    private Integer userId;
    private QuestionDto question;
    private String content;
    private ReplyDto replyDto;

    public DiscussionDto() {
    }

    public DiscussionDto(Discussion discussion) {
        this.userId = discussion.getId().getUserId();
        this.content = discussion.getContent();
        this.question = new QuestionDto(discussion.getQuestion());
        if(discussion.getReply() != null) {
            this.replyDto = new ReplyDto(discussion.getReply());
        }
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getQuestionId() {
        return question.getId();
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
    }

    public ReplyDto getReplyDto() {
        return replyDto;
    }

    public void setReplyDto(ReplyDto replyDto) {
        this.replyDto = replyDto;
    }
}
