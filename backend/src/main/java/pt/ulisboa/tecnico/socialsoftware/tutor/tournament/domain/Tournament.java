package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.Observable;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.Observer;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;

import javax.persistence.*;
import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;


@Entity
@Table(name = "tournaments")
public class Tournament implements Observable {
    @SuppressWarnings("unused")
    public enum Status {
        NOT_CANCELED, CANCELED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Topic> topics = new ArrayList<>();

    @Column(name = "number_of_questions")
    private Integer numberOfQuestions;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User creator;

    @Enumerated(EnumType.STRING)
    private Status state;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<User> participants = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "course_execution_id")
    private CourseExecution courseExecution;

    @Column(name = "quizID")
    private Integer quizId;

    @Column(name = "privateTournament")
    private boolean privateTournament;

    @Column(name = "password")
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<User> observers = new HashSet<>();

    public Tournament() {
    }

    public Tournament(User user, List<Topic> topics, TournamentDto tournamentDto) {
        setStartTime(DateHandler.toLocalDateTime(tournamentDto.getStartTime()));
        setEndTime(DateHandler.toLocalDateTime(tournamentDto.getEndTime()));
        setNumberOfQuestions(tournamentDto.getNumberOfQuestions());
        this.state = tournamentDto.getState();
        this.creator = user;
        setCourseExecution(user);
        updateTopics(topics);
        setPassword(tournamentDto.getPassword());
        setPrivateTournament(tournamentDto.isPrivateTournament());
    }

    public Integer getId() {
        return id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        if (startTime == null) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "startTime");
        }
        // Added 1 minute as a buffer to take latency into consideration
        if (this.endTime != null && this.endTime.isBefore(startTime) ||
                startTime.plusMinutes(1).isBefore(DateHandler.now())) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "startTime");
        }

        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        if (endTime == null) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "endTime");
        }
        if (this.startTime != null && endTime.isBefore(this.startTime)) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "endTime");
        }

        this.endTime = endTime;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        if (numberOfQuestions <= 0) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "number of questions");
        }
        this.numberOfQuestions = numberOfQuestions;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public User getCreator() {
        return creator;
    }

    public Status getState() {
        return state;
    }

    public void setState(Status status) { this.state = status; }

    public List<User> getParticipants() {
        return participants;
    }

    public void setCourseExecution(User user) {
        this.courseExecution = user.getCourseExecutions().iterator().next();
    }

    public CourseExecution getCourseExecution() {
        return courseExecution;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public void setQuizId(Integer quizId) {
        this.quizId = quizId;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void updateTopics(List<Topic> newTopics) {
        if (newTopics.isEmpty()) throw new TutorException(TOURNAMENT_MUST_HAVE_ONE_TOPIC);

        for (Topic topic : newTopics) {
            checkTopicCourse(topic);
        }

        List<Topic> toRemove = this.topics.stream().filter(topic -> !newTopics.contains(topic)).collect(Collectors.toList());

        toRemove.forEach(topic -> {
            this.topics.remove(topic);
            topic.removeTournament(this);
        });

        newTopics.stream().filter(topic -> !this.topics.contains(topic)).forEach(topic -> {
            this.topics.add(topic);
            topic.addTournament(this);
        });
    }

    public void checkTopicCourse(Topic topic) {
        if (topic.getCourse() != courseExecution.getCourse()) {
            throw new TutorException(TOURNAMENT_TOPIC_COURSE);
        }
    }

    public void addParticipant(User user) {
        this.participants.add(user);
        user.addTournament(this);
        this.Attach(user);
        user.addObserver(this);
    }

    public void removeParticipant(User user) {
        this.participants.remove(user);
        this.Dettach(user);
        user.removeTournament(this);
        user.removeObserver(this);
    }

    public boolean hasQuiz() {
        return this.getQuizId() != null;
    }

    public void remove() {
        creator = null;
        courseExecution = null;

        getTopics().forEach(topic -> topic.getTournaments().remove(this));
        getTopics().clear();

        getParticipants().forEach(participant -> participant.getTournaments().remove(this));
        getParticipants().clear();
    }

    public boolean isPrivateTournament() { return privateTournament; }

    public void setPrivateTournament(boolean privateTournament) {
        this.privateTournament = privateTournament;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Set<User> getObservers() {
        return this.observers;
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
}
