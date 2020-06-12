package pt.ulisboa.tecnico.socialsoftware.tutor.notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.dto.NotificationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.repository.NotificationRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public NotificationDto createNotification(NotificationDto notificationDto) {
        if (notificationDto.getCreationDate() == null) {
            notificationDto.setCreationDate(DateHandler.toISOString(DateHandler.now()));
        }

        Notification notification = new Notification(notificationDto);

        entityManager.persist(notification);
        return new NotificationDto(notification);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Notification getNotificationById(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new TutorException(NOTIFICATION_NOT_FOUND, notificationId));

        return notification;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<NotificationDto> getUserNotifications(Integer userId) {
        return notificationRepository.getUserNotifications(userId).stream().map(NotificationDto::new).collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void removeNotification(Integer userId, Integer notificationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new TutorException(NOTIFICATION_NOT_FOUND, notificationId));

        notification.removeUser(user);
        //if (notification.getUsers().isEmpty())
            //notificationRepository.delete(notification); TODO Makes sense ??
    }
}
