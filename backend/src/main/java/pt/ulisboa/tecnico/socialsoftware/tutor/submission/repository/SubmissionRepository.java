package pt.ulisboa.tecnico.socialsoftware.tutor.submission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface SubmissionRepository extends JpaRepository<Submission, Integer> {

}