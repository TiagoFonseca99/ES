package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.util.List;

@Repository
@Transactional
public interface TournamentRepository extends JpaRepository<Tournament, Integer> {
    @Query(value = "SELECT * FROM tournaments t WHERE t.start_time < CURRENT_TIMESTAMP AND t.end_time > CURRENT_TIMESTAMP AND t.state = 'NOT_CANCELED'", nativeQuery = true)
    List<Tournament> getOpenedTournaments();

    @Query(value = "SELECT * FROM tournaments t WHERE t.user_id = :user_id", nativeQuery = true)
    List<Tournament> getUserTournaments(Integer user_id);
}
