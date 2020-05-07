package pt.ulisboa.tecnico.socialsoftware.tutor.user.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

public class DashboardDto implements Serializable {
    private String name;
    private String username;
    private boolean discussionStatsPublic;
    private boolean submissionStatsPublic;
    private boolean tournamentStatsPublic;
    private Integer numDiscussions;
    private Integer numPublicDiscussions;
    private Integer numSubmissions;
    private Integer numApprovedSubmissions;
    private Integer numRejectedSubmissions;
    private List<TournamentDto> joinedTournaments;
    private Boolean tournamentNamePermission;
    private Boolean tournamentScorePermission;

    public DashboardDto() {
    }

    public DashboardDto(User user) {
        this.name = user.getName();
        this.username = user.getUsername();
        this.discussionStatsPublic = user.isDiscussionStatsPublic();
        this.submissionStatsPublic = user.isSubmissionStatsPublic();
        this.tournamentStatsPublic = user.isTournamentStatsPublic();
        this.numDiscussions = user.getDiscussions().size();
        this.numSubmissions = user.getSubmissions().size();
        this.numApprovedSubmissions = user.getNumberOfApprovedSubmissions();
        this.numRejectedSubmissions = user.getNumberOfRejectedSubmissions();
        this.joinedTournaments = user.getTournaments().stream().map(TournamentDto::new).collect(Collectors.toList());
        this.tournamentNamePermission = user.getTournamentNamePermissionB();
        this.tournamentScorePermission = user.getTournamentScorePermissionB();
    }

    public DashboardDto(Integer numDiscussions, Integer numPublicDiscussions, Integer numSubmissions, Integer numApprovedSubmissions, Integer numRejectedSubmissions, List<TournamentDto> joinedTournaments) {
        this.numDiscussions = numDiscussions;
        this.numPublicDiscussions = numPublicDiscussions;
        this.numSubmissions = numSubmissions;
        this.numApprovedSubmissions = numApprovedSubmissions;
        this.numRejectedSubmissions = numRejectedSubmissions;
        this.joinedTournaments = joinedTournaments;
    }

    public boolean isDiscussionStatsPublic() {
        return discussionStatsPublic;
    }

    public boolean isSubmissionStatsPublic() {
        return submissionStatsPublic;
    }

    public boolean isTournamentStatsPublic() {
        return tournamentStatsPublic;
    }
    
    public void setDiscussionStatsPublic(boolean discussionStatsPublic) {
        this.discussionStatsPublic = discussionStatsPublic;
    }

    public void setSubmissionStatsPublic(boolean submissionStatsPublic) {
        this.submissionStatsPublic = submissionStatsPublic;
    }


    public void setTournamentStatsPublic(boolean tournamentStatsPublic) {
        this.tournamentStatsPublic = tournamentStatsPublic;
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

    public void setJoinedTournaments(List<TournamentDto> joinedTournaments) { this.joinedTournaments = joinedTournaments; }

    public Integer getNumApprovedSubmissions() {
        return numApprovedSubmissions;
    }

    public void setNumApprovedSubmissions(Integer numApprovedSubmissions) { this.numApprovedSubmissions = numApprovedSubmissions; }

    public Integer getNumRejectedSubmissions() { return numRejectedSubmissions; }

    public void setNumRejectedSubmissions(Integer numRejectedSubmissions) { this.numRejectedSubmissions = numRejectedSubmissions; }

    public Integer getNumSubmissions() {
        return numSubmissions;
    }

    public void setNumSubmissions(Integer numSubmissions) {
        this.numSubmissions = numSubmissions;
    }

    public Integer getNumPublicDiscussions() {
        return numPublicDiscussions;
    }

    public void setNumPublicDiscussions(Integer numPublicDiscussions) { this.numPublicDiscussions = numPublicDiscussions; }

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
