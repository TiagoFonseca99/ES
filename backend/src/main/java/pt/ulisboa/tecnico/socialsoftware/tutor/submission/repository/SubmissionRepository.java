package pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Submission;

import java.util.List;
import java.util.Optional;


@Repository
@Transactional
public interface SubmissionRepository extends JpaRepository<Submission, Integer> {
    @Query(value = "select * from submissions s where s.user_id = :userId", nativeQuery = true)
    List<Submission> getSubmissions(Integer userId);

    @Query(value = "select * from submissions s where s.question_id = :questionId", nativeQuery = true)
    Optional<Submission> findByQuestionId(Integer questionId);
}

