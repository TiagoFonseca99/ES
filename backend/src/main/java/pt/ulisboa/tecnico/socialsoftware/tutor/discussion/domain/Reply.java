package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;

import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

@Embeddable
public class Reply implements Serializable {

    @NotNull
    @Column(name="reply_id")
    private int id;

    @NotNull
    @OneToOne
    private Discussion discussion;

    @NotNull
    @ManyToOne
    @Column(name="teacher")
    private User teacher;

    @NotNull
    @Column(name="message")
    private String message;

    @Column(name="date")
    private LocalTime date = LocalTime.now();

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