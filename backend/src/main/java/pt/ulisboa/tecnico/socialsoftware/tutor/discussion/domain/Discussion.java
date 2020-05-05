package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain;

import javax.persistence.*;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

@Entity
@Table(name = "discussions")
public class Discussion {
    @EmbeddedId
    private DiscussionId discussionId = new DiscussionId();

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "discussion", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Reply> replies = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private Question question;

    private LocalDateTime date;

    public Discussion() {
    }

    public Discussion(User user, Question question, DiscussionDto discussionDto) {
        checkConsistentDiscussion(discussionDto);
        this.content = discussionDto.getContent();
        this.question = question;
        this.user = user;
        this.discussionId.setQuestionId(question.getId());
        this.discussionId.setUserId(user.getId());
        this.question.addDiscussion(this);
        this.user.addDiscussion(this);
        this.setDate(DateHandler.toLocalDateTime(discussionDto.getDate()));
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
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

    public List<Reply> getReplies() {
        return this.replies;
    }

    public void addReply(Reply reply) {
        this.replies.add(reply);
    }

    public DiscussionId getId() {
        return discussionId;
    }

    public void setId(DiscussionId id) {
        discussionId = id;
    }

    private void checkConsistentDiscussion(DiscussionDto discussionDto) {
        if (discussionDto.getContent().trim().length() == 0 && discussionDto.getDate() != null) {
            throw new TutorException(DISCUSSION_MISSING_DATA);
        }
    }

    public boolean equals(Object o) {
        if (o instanceof Discussion) {
            return this.discussionId.equals(((Discussion) o).getId()) && this.content == ((Discussion) o).getContent();
        }

        return false;
    }

    public int hashCode() {
        return Objects.hash(discussionId, getContent(), getReplies(), getUser(), getQuestion());
    }
}
