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

    @ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    private List<Topic> topics = new ArrayList<>();

    @Column(name = "number_of_questions")
    private Integer numberOfQuestions;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User creator;

    @Enumerated(EnumType.STRING)
    private Status state;

    public Tournament() {}

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

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public Integer getNumberOfQuestions() {
        return  numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Status getState() {
        return state;
    }

    public void setState(Status state) {
        this.state = state;
    }

    public void addTopic(Topic topic) {
        if (topics.contains(topic)) {
            throw new TutorException(DUPLICATE_TOURNAMENT_TOPIC, topic.getId());
        }
        this.topics.add(topic);
    }

    public void removeTopic(Topic topic) {
        if (topics.size() <= 1) {
            throw new TutorException(TOURNAMENT_HAS_ONLY_ONE_TOPIC);
        }
        this.topics.remove(topic);
    }


    @Override
    public String toString() {
        return "Tournament{" +
                "id=" + id +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", topics='" + topics + '\'' +
                ", numberOfQuestions='" + numberOfQuestions + '\'' +
                ", creator='" + creator + '\'' +
                '}';
    }
}
