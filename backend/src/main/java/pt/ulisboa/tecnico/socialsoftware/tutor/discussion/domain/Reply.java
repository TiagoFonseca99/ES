package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.ReplyDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

@Entity
@Table(name = "replies")
public class Reply implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    private Discussion discussion;

    @NotNull
    @ManyToOne
    private User teacher;

    @NotNull
    @Column(name="message")
    private String message;

    @Column(name="date")
    private LocalTime date;

    public Reply() {
    }

    public Reply(User teacher, Discussion discussion, ReplyDto reply) {
        this.teacher = teacher;
        teacher.addReply(this);
        this.date = reply.getDate();
        this.message = reply.getMessage();
        this.discussion = discussion;
        discussion.setReply(this);
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalTime getDate() {
        return date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Discussion getDiscussion() {
        return discussion;
    }

    public void setDiscussion(Discussion discussion) {
        this.discussion = discussion;
    }


}