package pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Review;


import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    @Query(value = "SELECT * FROM reviews r WHERE r.student_id = :studentId", nativeQuery = true)
    List<Review> getSubmissionStatus(int studentId);
}