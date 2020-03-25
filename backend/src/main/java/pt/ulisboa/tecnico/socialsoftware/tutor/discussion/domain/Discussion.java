package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain;

import javax.persistence.*;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;

import java.io.Serializable;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@Table(name = "discussions")
public class Discussion implements Serializable {
    @EmbeddedId
    private DiscussionId discussionId = new DiscussionId();

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToOne
    @JoinColumn(name = "reply_id")
    private Reply reply;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="question_id", insertable = false, updatable = false)
    private Question question;

    public Discussion() {
    }

    public Discussion(User user, Question question, DiscussionDto discussionDto) {
        checkConsistentDiscussion(discussionDto);
        this.content = discussionDto.getContent();
        this.question = question;
        question.addDiscussion(this);
        this.user = user;
        user.addDiscussion(this);
        discussionId.setQuestionId(question.getId());
        discussionId.setUserId(user.getId());
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
        this.discussionId.setQuestionId(question.getId());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        this.discussionId.setUserId(user.getId());
    }

    public Reply getReply() {
        return reply;
    }

    public void setReply(Reply reply) {
        this.reply = reply;
    }

    public DiscussionId getId() {
        return discussionId;
    }

    public void setId(DiscussionId id) {
        discussionId = id;
    }

    private void checkConsistentDiscussion(DiscussionDto discussionDto){
        if(discussionDto.getContent().trim().length() == 0){
            throw new TutorException(DISCUSSION_MISSING_DATA);
        }
    }
}
