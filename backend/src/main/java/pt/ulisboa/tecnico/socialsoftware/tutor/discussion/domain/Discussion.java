package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain;

import javax.persistence.*;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.Observable;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.Observer;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "discussions")
public class Discussion implements Observable {
    @EmbeddedId
    private DiscussionId discussionId = new DiscussionId();

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "discussion", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Reply> replies = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private Question question;

    private LocalDateTime date;

    @Column(columnDefinition = "boolean default false")
    private boolean available;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<User> observers = new HashSet<>();

    public Discussion() {
    }

    public Discussion(User user, Question question, Course course, DiscussionDto discussionDto) {
        checkConsistentDiscussion(discussionDto);
        this.content = discussionDto.getContent();
        this.question = question;
        this.user = user;
        this.course = course;
        this.discussionId.setQuestionId(question.getId());
        this.discussionId.setUserId(user.getId());
        this.question.addDiscussion(this);
        this.user.addDiscussion(this);
        this.setDate(DateHandler.toLocalDateTime(discussionDto.getDate()));
        this.available = discussionDto.isAvailable();
        this.observers.add(user);
        this.observers.addAll(user.getCourseExecutions().stream()
                .filter(execution -> execution.getCourseId() == course.getId() && execution.getUsers().contains(user))
                .map(execution -> {
                    return execution.getUsers().stream().filter(exUser -> exUser.isTeacher())
                            .collect(Collectors.toList());
                }).flatMap(List::stream).collect(Collectors.toList()));
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
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

    public boolean isAvailable() {
        return available;
    }

    public void setAvailability(boolean bool) {
        this.available = bool;
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

    public void remove() {
        this.user.getDiscussions().remove(this);
        this.question.getDiscussions().remove(this);

        for (Reply reply : replies) {
            reply.discussionRemove();
        }

        this.replies.clear();
    }

    private void checkConsistentDiscussion(DiscussionDto discussionDto) {
        if (discussionDto.getContent().trim().length() == 0 && discussionDto.getDate() != null) {
            throw new TutorException(DISCUSSION_MISSING_DATA);
        }
    }

    @Override
    public void Attach(Observer o) {
        this.observers.add((User) o);
    }

    @Override
    public void Dettach(Observer o) {
        this.observers.remove(o);
    }

    @Override
    public void Notify(Notification notification, User user) {
        for (Observer observer : observers) {
            if (((User) observer).getId() == user.getId()) {
                continue;
            }

            observer.update(this, notification);
        }
    }

    public boolean equals(Object o) {
        if (o instanceof Discussion) {
            return this.discussionId.equals(((Discussion) o).getId())
                    && this.content.equals(((Discussion) o).getContent());
        }

        return false;
    }

    public int hashCode() {
        return Objects.hash(discussionId, getContent());
    }
}
