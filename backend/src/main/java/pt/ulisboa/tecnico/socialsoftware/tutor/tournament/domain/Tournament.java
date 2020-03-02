package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "tournaments")
public class Tournament {
    @SuppressWarnings("unused")
    public enum Status {
        SCHEDULED, CANCELED, OPEN, CLOSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String startTime;

    private String endTime;

    @ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    private List<Topic> topics = new ArrayList<>();

    private Integer numberOfQuestions;

    private User creator;

    private Enum state;

    public Tournament() {}

    public Tournament(User user, List<Topic> topics, TournamentDto tournamentDto) {
        this.startTime = tournamentDto.getStartTime();
        this.endTime = tournamentDto.getEndTime();
        this.topics = topics;
        this.numberOfQuestions = tournamentDto.getNumberOfQuestions();
        this.creator = user;
        this.state = tournamentDto.getState();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void addTopic(Topic topic) {
        if (topics.contains(topic)) {
            // THRWO EXCEPTION TODO
        }
        this.topics.add(topic);
    }

    public void removeTopic(Topic topic) {
        if (topics.size() <= 1) {
            // ELSE THROW EXCEPTION TODO
        }
        this.topics.remove(topic);
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

    public Enum getState() {
        return state;
    }

    public void setState(Enum state) {
        this.state = state;
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
