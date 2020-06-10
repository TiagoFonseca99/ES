package pt.ulisboa.tecnico.socialsoftware.tutor.announcement.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.domain.Announcement;

import java.util.List;


@Repository
@Transactional
public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {
    @Query(value = "select * from announcements a where a.user_id = :userId and a.course_execution_id = :courseExecutionId", nativeQuery = true)
    List<Announcement> getAnnouncements(Integer userId, Integer courseExecutionId);


}


