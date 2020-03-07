package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;


import java.io.Serializable;
import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class TournamentDto implements Serializable {
    private Integer id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer numberOfQuestions;
    private Tournament.Status state;
    private List<TopicDto> topics = new ArrayList<>();
    private List<UserDto> participants = new ArrayList<>();

    public TournamentDto() {
    }

    public TournamentDto(Tournament tournament) {
        this.id = tournament.getId();
        this.startTime = tournament.getStartTime();
        this.endTime = tournament.getEndTime();
        this.numberOfQuestions = tournament.getNumberOfQuestions();
        this.state = tournament.getState();
        this.topics = tournament.getTopics().stream().map(TopicDto::new).collect(Collectors.toList());
        this.participants = tournament.getParticipants().stream().map(UserDto::new).collect(Collectors.toList());
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

    // TODO Verificar
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public Tournament.Status getState() {
        return state;
    }

    public void setState(Tournament.Status state) {
        this.state = state;
    }

    public List<TopicDto> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicDto> topics) {
        this.topics = topics;
    }

    public List<UserDto> getParticipants() {
        return participants;
    }

    public void setParticipants(List<UserDto> participants) {
        this.participants = participants;
    }



    @Override
    public String toString() {
        return "TournamentDto{" +
                "id=" + id +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", numberOfQuestions='" + numberOfQuestions + '\'' +
                ", state='" + state + '\'' +
                ", topics='" + topics + '\'' +
                ", participants='" + participants + '\'' +
                '}';
    }
}
