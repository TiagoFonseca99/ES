package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import java.io.Serializable;

public class TournamentDto implements Serializable {
    public TournamentDto() {}

    public TournamentDto(Tournament tournament) {}

    void setStartTime(String startTime) {}
    void setEndTime(String endTime) {}
    void setTopicName(String topicName) {}
    void setNumberOfQuestions(Integer numberOfQuestions) {}

    Integer getID() {return 1;}
    String getStartTime() {return "";}
    String getEndTime() {return  "";}
    String getTopicName() {return  "";}
    Integer getNumberOfQuestions() {return  1;}
    String getState() {return "";}
    String getCreator() {return "";}
}
