package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;

@Service
public class TournamentService {
    @Autowired
    private TournamentRepository tournamentRepository;

    @Retryable
    public TournamentDto createTournament(TournamentDto tournamentDto) {

        Tournament tournament = new Tournament(tournamentDto);
        return new TournamentDto(tournament);
    }
}
