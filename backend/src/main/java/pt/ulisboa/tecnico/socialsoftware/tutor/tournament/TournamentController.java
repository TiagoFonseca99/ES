package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.security.Principal;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @PostMapping(value = "/tournaments")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public TournamentDto createTournament(Principal principal, @RequestParam List<Integer> topicsId, @Valid @RequestBody TournamentDto tournamentDto) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        formatDates(tournamentDto);
        return tournamentService.createTournament(user.getId(), topicsId, tournamentDto);
    }
    
    @GetMapping(value = "/tournaments/getTournaments")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public List<TournamentDto> getTournaments(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return tournamentService.getTournaments();
    }

    @GetMapping(value = "/tournaments/getOpenTournaments")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public List<TournamentDto> getOpenTournaments(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return tournamentService.getOpenedTournaments();
    }

    @PostMapping(value = "/tournaments/joinTournament")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public void joinTournament(Principal principal, @Valid @RequestBody TournamentDto tournamentDto) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        tournamentService.joinTournament(user.getId(), tournamentDto);
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

    @GetMapping(value = "/tournaments/getQuiz")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    public QuizDto getQuiz(Principal principal, @Valid @RequestBody TournamentDto tournamentDto) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }
        return tournamentService.getQuiz(tournamentDto);
    }

    private void formatDates(TournamentDto tournamentDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (tournamentDto.getStartTime() != null && !tournamentDto.getStartTime().matches("(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2})")){
            tournamentDto.setStartTime(LocalDateTime.parse(tournamentDto.getStartTime().replaceAll(".$", ""), DateTimeFormatter.ISO_DATE_TIME).format(formatter));
        }
        if (tournamentDto.getEndTime() !=null && !tournamentDto.getEndTime().matches("(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2})"))
            tournamentDto.setEndTime(LocalDateTime.parse(tournamentDto.getEndTime().replaceAll(".$", ""), DateTimeFormatter.ISO_DATE_TIME).format(formatter));
    }

}
