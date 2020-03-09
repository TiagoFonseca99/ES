package pt.ulisboa.tecnico.socialsoftware.tutor.submission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.Submission;

import java.util.Optional;

@Repository
@Transactional
public interface SubmissionRepository extends JpaRepository<Submission, Integer> {

}