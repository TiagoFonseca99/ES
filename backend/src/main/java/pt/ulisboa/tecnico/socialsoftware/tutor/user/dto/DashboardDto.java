package pt.ulisboa.tecnico.socialsoftware.tutor.user.dto;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

public class DashboardDto implements Serializable {
    private String name;
    private String username;
    private Integer numDiscussions;
    private Integer numPublicDiscussions;
    private Integer numSubmissions;
    private Integer numApprovedSubmissions;
    private List<TournamentDto> joinedTournaments;
    private Boolean tournamentNamePermission;
    private Boolean tournamentScorePermission;

    public DashboardDto() {
    }

	public DashboardDto(User user) {
        this.name = user.getName();
        this.username = user.getUsername();
        this.numDiscussions = user.getDiscussions().size();
        this.joinedTournaments = user.getTournaments().stream().map(TournamentDto::new).collect(Collectors.toList());
        this.tournamentNamePermission = user.getTournamentNamePermissionB();
        this.tournamentScorePermission = user.getTournamentScorePermissionB();

    }

    public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

    public Boolean getTournamentNamePermission() {
        return tournamentNamePermission;
    }

    public void setTournamentNamePermission(Boolean tournamentNamePermission) {
        this.tournamentNamePermission = tournamentNamePermission;
    }

    public Boolean getTournamentScorePermission() {
        return tournamentScorePermission;
    }

    public void setTournamentScorePermission(Boolean tournamentScorePermission) {
        this.tournamentScorePermission = tournamentScorePermission;
    }


    public Integer getNumDiscussions() {
        return numDiscussions;
    }

    public void setNumDiscussions(Integer numDiscussions) {
        this.numDiscussions = numDiscussions;
    }
}
