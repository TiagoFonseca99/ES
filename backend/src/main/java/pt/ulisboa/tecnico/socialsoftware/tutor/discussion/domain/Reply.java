package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.ReplyDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.REPLY_MISSING_DATA;

@Entity
@Table(name = "replies")
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private Discussion discussion;

    @NotNull
    @ManyToOne
    private User user;

    @NotNull
    @Column(name = "message")
    private String message;

    @Column(name = "date")
    private LocalDateTime date;

    public Reply() {
    }

    public Reply(User user, Discussion discussion, ReplyDto reply) {
        checkEmptyMessage(reply);
        this.user = user;
        user.addReply(this);
        this.date = DateHandler.toLocalDateTime(reply.getDate());
        this.message = reply.getMessage();
        this.discussion = discussion;
        discussion.addReply(this);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void getDate(LocalDateTime date) {
        this.date = date;
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

    public void remove() {
        this.discussion.getReplies().remove(this);
        this.user.getReplies().remove(this);
        this.discussion = null;
    }

    private void checkEmptyMessage(ReplyDto reply) {
        if(reply.getMessage().trim().length() == 0){
            throw new TutorException(REPLY_MISSING_DATA);
        }
    }
}
