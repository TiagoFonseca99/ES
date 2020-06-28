package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Reply;


@Repository
@Transactional
public interface ReplyRepository extends JpaRepository<Reply, Integer> {
    @Query(value = "SELECT * FROM replies r WHERE r.discussion_question_id = :questionId AND r.discussion_user_id = :userId", nativeQuery = true)
    List<Reply> findByDiscussionId(Integer userId, Integer questionId);
}
