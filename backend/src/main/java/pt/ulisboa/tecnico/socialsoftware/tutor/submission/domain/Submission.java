package pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.Observable;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.Observer;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "submissions")
public class Submission implements Observable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String argument;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "course_execution_id")
    private CourseExecution courseExecution;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private boolean anonymous;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "submission", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Review> reviews = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<User> observers = new HashSet<>();

    public Submission() {
    }

    public Submission(CourseExecution courseExecution, Question question, User user) {
        this.courseExecution = courseExecution;
        this.question = question;
        this.user = user;
        user.addSubmission(this);
        courseExecution.addSubmission(this);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public int getStudentId() {
        return this.user.getId();
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    public CourseExecution getCourseExecution() {
        return courseExecution;
    }

    public Integer getCourseExecutionId() {
        return courseExecution.getId();
    }

    public Integer getCourseId() {
        return courseExecution.getCourseId();
    }

    public void setCourseExecution(CourseExecution courseExecution) {
        this.courseExecution = courseExecution;
    }

    @Override
    public Set<User> getObservers() {
        return observers;
    }

    @Override
    public String toString() {
        return "Submission{" + "id=" + id + ", question=" + question + ", user='" + user + '}';
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
        for (User observer : observers) {
            if (observer.getId().equals(user.getId())) {
                continue;
            }

            observer.update(this, notification);
        }
    }

}
