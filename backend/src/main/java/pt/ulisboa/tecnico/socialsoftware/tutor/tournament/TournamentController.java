package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;
import java.security.Principal;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @PostMapping(value = "/tournaments")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public TournamentDto createTournament(Principal principal, @Valid @RequestBody TournamentDto tournamentDto, @RequestParam List<Integer> topicsId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        formatDates(tournamentDto);
        return tournamentService.createTournament(user.getId(), topicsId, tournamentDto);
    }
    
    @GetMapping(value = "/tournaments/getAllTournaments")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public List<TournamentDto> getAllTournaments(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return tournamentService.getAllTournaments(user);
    }

    @GetMapping(value = "/tournaments/getOpenTournaments")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public List<TournamentDto> getOpenTournaments(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return tournamentService.getOpenedTournaments(user);
    }

    @GetMapping(value = "/tournaments/getClosedTournaments")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public List<TournamentDto> getClosedTournaments(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return tournamentService.getClosedTournaments(user);
    }

    @GetMapping(value = "/tournaments/getUserTournaments")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public List<TournamentDto> getUserTournaments(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return tournamentService.getUserTournaments(user);
    }

    @PutMapping(value = "/tournaments/joinTournament")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public void joinTournament(Principal principal, @Valid @RequestBody TournamentDto tournamentDto, @RequestParam String password) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        tournamentService.joinTournament(user.getId(), tournamentDto, password);
    }


    @PutMapping(value = "/tournaments/leaveTournament")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public void leaveTournament(Principal principal, @Valid @RequestBody TournamentDto tournamentDto) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        tournamentService.leaveTournament(user.getId(), tournamentDto);
    }

    @PutMapping(value = "/tournaments/cancelTournament")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public void cancelTournament(Principal principal, @Valid @RequestBody TournamentDto tournamentDto) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        tournamentService.cancelTournament(user.getId(), tournamentDto);
    }

    @PutMapping(value = "/tournaments/editStartTime")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public void editStartTime(Principal principal, @Valid @RequestBody TournamentDto tournamentDto) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        tournamentService.editStartTime(user.getId(), tournamentDto);
    }

    @PutMapping(value = "/tournaments/editEndTime")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public void editEndTime(Principal principal, @Valid @RequestBody TournamentDto tournamentDto) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        tournamentService.editEndTime(user.getId(), tournamentDto);
    }

    @PutMapping(value = "/tournaments/editNumberOfQuestions")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public void editNumberOfQuestions(Principal principal, @Valid @RequestBody TournamentDto tournamentDto, @RequestParam Integer numberOfQuestions) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        tournamentService.editNumberOfQuestions(user.getId(), tournamentDto, numberOfQuestions);
    }

    @PutMapping(value = "/tournaments/updateTopics")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public void updateTopics(Principal principal, @Valid @RequestBody TournamentDto tournamentDto, @RequestParam List<Integer> topicsId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        tournamentService.updateTopics(user.getId(), tournamentDto, topicsId);
    }

    @DeleteMapping(value = "/tournaments/removeTournament/{tournamentId}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public void removeTournament(Principal principal, @PathVariable Integer tournamentId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        tournamentService.removeTournament(user.getId(), tournamentId);
    }

    @GetMapping(value = "/tournaments/getTournamentParticipants")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public List<UserDto> getTournamentParticipants(Principal principal, @Valid @RequestBody TournamentDto tournamentDto) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        return tournamentService.getTournamentParticipants(tournamentDto);
    }

    @PutMapping(value = "/tournaments/solveQuiz")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public StatementQuizDto solveQuiz(Principal principal, @Valid @RequestBody TournamentDto tournamentDto) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return tournamentService.solveQuiz(user.getId(), tournamentDto);
    }

    private void formatDates(TournamentDto tournamentDto) {
        if (tournamentDto.getStartTime() != null && !DateHandler.isValidDateFormat(tournamentDto.getStartTime()))
            tournamentDto.setStartTime(DateHandler.toISOString(DateHandler.toLocalDateTime(tournamentDto.getStartTime())));

        if (tournamentDto.getEndTime() !=null && !DateHandler.isValidDateFormat(tournamentDto.getEndTime()))
            tournamentDto.setEndTime(DateHandler.toISOString(DateHandler.toLocalDateTime(tournamentDto.getEndTime())));
    }
}
