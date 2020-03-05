package pt.ulisboa.tecnico.socialsoftware.tutor.question.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;

import java.util.List;

@Repository
@Transactional
public interface TopicRepository extends JpaRepository<Topic, Integer> {
    @Query(value = "SELECT * FROM topics t, courses c WHERE t.course_id = c.id AND c.id = :courseId", nativeQuery = true)
    List<Topic> findTopics(int courseId);

    @Query(value = "SELECT * FROM topics t, courses c WHERE t.course_id = c.id AND c.id = :courseId AND t.name = :name", nativeQuery = true)
    Topic findTopicByName(int courseId, String name);
}
