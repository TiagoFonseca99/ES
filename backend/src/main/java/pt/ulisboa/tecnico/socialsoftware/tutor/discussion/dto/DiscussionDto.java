package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto;

import java.io.Serializable;

import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;

public class DiscussionDto implements Serializable {
    private Integer id;
    private Integer key;
    private String content;
    private Question question;
    private User user;

    public DiscussionDto() {
    }

    public DiscussionDto(Discussion discussion) {
        this.id = discussion.getId();
        this.key = discussion.getKey();
        this.content = discussion.getContent();
        this.question = discussion.getQuestion();
        this.user = discussion.getUser();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "DiscussionDto{" +
            "id=" + id +
            ", content='" + content + '\'' +
                "}";
    }
}
