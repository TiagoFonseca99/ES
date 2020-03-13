package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto;

import java.io.Serializable;

import java.time.LocalTime;

import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Reply;

public class ReplyDto implements Serializable {
    private Integer id;
    private Integer teacherId;
    private String message;
    private LocalTime date;

    public ReplyDto() {
    }

    public ReplyDto(Reply reply) {
        this.setId(reply.getId());
        this.setTeacherId(reply.getTeacher().getId());
        this.setMessage(reply.getMessage());
        this.setDate(reply.getDate());
    }

    public LocalTime getDate() {
        return date;
    }

    public void setDate(LocalTime date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacher_id) {
        this.teacherId = teacher_id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
}