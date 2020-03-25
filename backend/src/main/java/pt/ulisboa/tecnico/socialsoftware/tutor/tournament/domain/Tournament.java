package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;

import javax.persistence.*;
import java.util.*;
import java.time.LocalDateTime;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;


@Entity
@Table(name = "tournaments")
public class Tournament {
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

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Topic> topics = new ArrayList<>();

    @Column(name = "number_of_questions")
    private Integer numberOfQuestions;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User creator;

    @Enumerated(EnumType.STRING)
    private Status state;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> participants = new ArrayList<>();

    public Tournament() {
    }

    public Tournament(User user, List<Topic> topics, TournamentDto tournamentDto) {
        this.startTime = tournamentDto.getStartTime();
        this.endTime = tournamentDto.getEndTime();
        this.numberOfQuestions = tournamentDto.getNumberOfQuestions();
        this.state = tournamentDto.getState();
        this.creator = user;
        this.topics = topics;
    }

    public Integer getId() {
        return id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public List<Topic> getTopics() {
        return topics;
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

    public List<User> getParticipants() {
        return participants;
    }

    public void addTopic(Topic topic) {
        if (topics.contains(topic)) {
            throw new TutorException(DUPLICATE_TOURNAMENT_TOPIC, topic.getId());
        }

        this.topics.add(topic);
    }

    public void removeTopic(Topic topic) {
        if (!this.topics.contains(topic)) {
            throw new TutorException(TOURNAMENT_TOPIC_MISMATCH, this.id, topic.getId());
        }

        if (topics.size() <= 1) {
            throw new TutorException(TOURNAMENT_HAS_ONLY_ONE_TOPIC);
        }

        this.topics.remove(topic);
    }

    public void addParticipant(User user) {

        this.participants.add(user);
    }

}