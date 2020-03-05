package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.DiscussionId;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface DiscussionRepository extends JpaRepository<Discussion, DiscussionId> {
    @Query(value = "SELECT * FROM discussions d WHERE d.discussionId.question_id = :questionId", nativeQuery = true)
    List<Discussion> findByQuestionId(Integer questionId);

    @Query(value = "SELECT * FROM discussions d WHERE d.discussionId.user_id = :userId", nativeQuery = true)
    List<Discussion> findByUserId(Integer userId);

    @Query(value = "SELECT * FROM discussions d WHERE d.discussionId.user_id = :userId AND d.discussionId.question_id = :questionId", nativeQuery = true)
    Optional<Discussion> findByUserIdQuestionId(Integer userId, Integer questionId);
}
