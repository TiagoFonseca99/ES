package pt.ulisboa.tecnico.socialsoftware.tutor.course;

import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.domain.Announcement;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.Observable;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.Observer;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@Table(name = "course_executions")
public class CourseExecution implements DomainEntity, Observable {
     public enum Status {ACTIVE, INACTIVE, HISTORIC}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private Course.Type type;

    private String acronym;
    private String academicTerm;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToMany(mappedBy = "courseExecutions")
    private final Set<User> users = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courseExecution", fetch=FetchType.LAZY, orphanRemoval=true)
    private final Set<Submission> submissions = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courseExecution", fetch=FetchType.LAZY, orphanRemoval=true)
    private final Set<Announcement> announcements = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courseExecution", fetch=FetchType.LAZY, orphanRemoval=true)
    private final Set<Quiz> quizzes = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courseExecution", fetch=FetchType.LAZY, orphanRemoval=true)
    private final Set<Assessment> assessments = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    private List<User> observers = new ArrayList<>();

    public CourseExecution() {
    }

    public CourseExecution(Course course, String acronym, String academicTerm, Course.Type type) {
        if (course.existsCourseExecution(acronym, academicTerm, type)) {
            throw new TutorException(DUPLICATE_COURSE_EXECUTION, acronym + academicTerm);
        }

        setType(type);
        setCourse(course);
        setAcronym(acronym);
        setAcademicTerm(academicTerm);
        setStatus(Status.ACTIVE);
        setObservers(new ArrayList<>(getUsers()));
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitCourseExecution(this);
    }

    public Integer getId() {
        return id;
    }

    public Course.Type getType() {
        return type;
    }

    public void setType(Course.Type type) {
        if (type == null)
            throw new TutorException(INVALID_TYPE_FOR_COURSE_EXECUTION);
        this.type = type;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        if (acronym == null || acronym.trim().isEmpty()) {
        throw new TutorException(INVALID_ACRONYM_FOR_COURSE_EXECUTION);
    }
        this.acronym = acronym;
    }

    public String getAcademicTerm() {
        return academicTerm;
    }

    public void setAcademicTerm(String academicTerm) {
        if (academicTerm == null || academicTerm.isBlank())
            throw new TutorException(INVALID_ACADEMIC_TERM_FOR_COURSE_EXECUTION);

        this.academicTerm = academicTerm;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Course getCourse() {
        return course;
    }

    public Integer getCourseId() { return course.getId(); }

    public void setCourse(Course course) {
        this.course = course;
        course.addCourseExecution(this);
    }

    public Set<User> getUsers() {
        return users;
    }

    public void addUser(User user) { users.add(user); }

    public Set<Quiz> getQuizzes() {
        return quizzes;
    }

    public void addQuiz(Quiz quiz) {
        quizzes.add(quiz);
    }

    public Set<Assessment> getAssessments() {
        return assessments;
    }

    public void addAssessment(Assessment assessment) {
        assessments.add(assessment);
    }

    public Set<Submission> getSubmissions() { return submissions; }

    public void addSubmission(Submission submission) {
        submissions.add(submission);
    }

    public void addAnnouncement(Announcement announcement) { announcements.add(announcement); }

    public Set<Announcement> getAnnouncements() { return announcements; }

    public List<User> getObservers() { return observers; }

    public void setObservers(List<User> observers) { this.observers = observers; }

    @Override
    public String toString() {
        return "CourseExecution{" +
                "id=" + id +
                ", type=" + type +
                ", acronym='" + acronym + '\'' +
                ", academicTerm='" + academicTerm + '\'' +
                ", status=" + status +
                ", users=" + users +
                ", quizzes=" + quizzes +
                ", assessments=" + assessments +
                ", submissions=" + submissions +
                ", announcements" + announcements +
                '}';
    }

    public void remove() {
        if (!getQuizzes().isEmpty() || !getAssessments().isEmpty()) {
            throw new TutorException(CANNOT_DELETE_COURSE_EXECUTION, acronym + academicTerm);
        }

        course.getCourseExecutions().remove(this);
        users.forEach(user -> user.getCourseExecutions().remove(this));
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
        for (Observer observer : users) {
            if (((User) observer).getId() == user.getId()) {
                continue;
            }

            observer.update(this, notification);
        }
    }
}
