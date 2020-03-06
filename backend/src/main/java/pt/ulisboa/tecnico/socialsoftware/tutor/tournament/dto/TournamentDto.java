package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;

import java.io.Serializable;
import java.util.*;
import java.time.LocalDateTime;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

public class TournamentDto implements Serializable {
    private Integer id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<TopicDto> topicsDto = new ArrayList<>();
    private Integer numberOfQuestions;
    private Tournament.Status state;

    public TournamentDto() {
    }

    public TournamentDto(Tournament tournament, List<TopicDto> topicsDto) {
        this.id = tournament.getId();
        this.startTime = tournament.getStartTime();
        this.endTime = tournament.getEndTime();
        this.topicsDto = topicsDto;
        this.numberOfQuestions = tournament.getNumberOfQuestions();
        this.state = tournament.getState();
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

    public List<TopicDto> getTopics() {
        return topicsDto;
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

    public void addTopics(List<TopicDto> topicsDto) {
        for (TopicDto topicDto : topicsDto) {
            addTopic(topicDto);
        }
    }

    public void addTopic(TopicDto topicDto) {
        if (topicsDto.contains(topicDto)) {
            throw new TutorException(DUPLICATE_TOURNAMENT_TOPIC, topicDto.getId());
        }
        this.topicsDto.add(topicDto);
    }

    public void removeTopic(TopicDto topicDto) {
        if (topicsDto.size() <= 1) {
            throw new TutorException(TOURNAMENT_HAS_ONLY_ONE_TOPIC);
        }
        this.topicsDto.remove(topicDto);
    }

    @Override
    public String toString() {
        return "TournamentDto{" +
                "id=" + id +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", numberOfQuestions='" + numberOfQuestions + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
