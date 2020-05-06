package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto;

import java.io.Serializable;

import java.time.LocalDateTime;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Reply;

public class ReplyDto implements Serializable {
    private Integer id;
    private Integer userId;
    private String userName;
    private String message;
    private String date;

    public ReplyDto() {
    }

	public ReplyDto(Reply reply) {
        this.setId(reply.getId());
        this.setUserId(reply.getUser().getId());
        this.setUserName(reply.getUser().getName());
        this.setMessage(reply.getMessage());
        this.setDate(DateHandler.toISOString(reply.getDate()));
    }

    public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDate(LocalDateTime date) {
        this.date = DateHandler.toISOString(date);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
