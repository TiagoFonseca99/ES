package pt.ulisboa.tecnico.socialsoftware.tutor.user.dto;

import java.io.Serializable;
import java.util.List;

import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

public class DashboardDto implements Serializable {
    private Integer numDiscussions;
    private Integer numPublicDiscussions;
    private Integer numSubmissions;
    private Integer numApprovedSubmissions;
    private List<TournamentDto> joinedTournaments;

    public DashboardDto() {
    }

    public DashboardDto(User user) {
        this.numDiscussions = user.getDiscussions().size();
    }

    public List<TournamentDto> getJoinedTournaments() {
        return joinedTournaments;
    }

    public void setJoinedTournaments(List<TournamentDto> joinedTournaments) {
        this.joinedTournaments = joinedTournaments;
    }

    public Integer getNumApprovedSubmissions() {
        return numApprovedSubmissions;
    }

    public void setNumApprovedSubmissions(Integer numApprovedSubmissions) {
        this.numApprovedSubmissions = numApprovedSubmissions;
    }

    public Integer getNumSubmissions() {
        return numSubmissions;
    }

    public void setNumSubmissions(Integer numSubmissions) {
        this.numSubmissions = numSubmissions;
    }

    public Integer getNumPublicDiscussions() {
        return numPublicDiscussions;
    }

    public void setNumPublicDiscussions(Integer numPublicDiscussions) {
        this.numPublicDiscussions = numPublicDiscussions;
    }

    public Integer getNumDiscussions() {
        return numDiscussions;
    }

    public void setNumDiscussions(Integer numDiscussions) {
        this.numDiscussions = numDiscussions;
    }
}
