package pt.ulisboa.tecnico.socialsoftware.tutor.notifications.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification;

import java.util.List;

@Repository
@Transactional
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    @Query(value = "SELECT * FROM (SELECT notifications_id FROM notifications_users n WHERE n.users_id = :user_id) AS n1 INNER JOIN (SELECT * FROM notifications) AS n2 ON n1.notifications_id = n2.id", nativeQuery = true)
    List<Notification> getUserNotifications(Integer user_id);
}
