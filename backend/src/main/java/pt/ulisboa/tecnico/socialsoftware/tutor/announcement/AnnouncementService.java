package pt.ulisboa.tecnico.socialsoftware.tutor.announcement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.domain.Announcement;
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.dto.AnnouncementDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.repository.AnnouncementRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationService;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationsCreation;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.domain.Notification;
import pt.ulisboa.tecnico.socialsoftware.tutor.notifications.dto.NotificationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.notifications.NotificationsMessage.*;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class AnnouncementService {

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private NotificationService notificationService;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AnnouncementDto createAnnouncement(AnnouncementDto announcementDto) {

        checkIfConsistentAnnouncement(announcementDto);

        User user = getTeacher(announcementDto.getUserId());

        CourseExecution courseExecution = getCourseExecution(announcementDto.getCourseExecutionId());

        if (announcementDto.getCreationDate() == null) {
            announcementDto
                    .setCreationDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }

        Announcement announcement = new Announcement(user, courseExecution, announcementDto);
        entityManager.persist(announcement);

        NotificationDto notification = NotificationsCreation.create(ADD_ANNOUNCEMENT_TITLE,
                List.of(announcement.getUser().getName()), ADD_ANNOUNCEMENT_CONTENT,
                List.of(announcement.getTitle(), user.getName()), Notification.Type.ANNOUNCEMENT);
        courseExecution.Notify(notificationService.createNotification(notification), user);

        return new AnnouncementDto(announcement);
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<AnnouncementDto> getAnnouncements(Integer teacherId, Integer courseExecutionId) {
        if (teacherId == null)
            throw new TutorException(TEACHER_MISSING);
        else if (courseExecutionId == null)
            throw new TutorException(COURSE_EXECUTION_MISSING);

        return announcementRepository.getAnnouncements(teacherId, courseExecutionId).stream().map(AnnouncementDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AnnouncementDto updateAnnouncement(Integer announcementId, AnnouncementDto announcementDto) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new TutorException(ANNOUNCEMENT_NOT_FOUND, announcementId));
        announcement.update(announcementDto);
        return new AnnouncementDto(announcement);
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean removeAnnouncement(Integer announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new TutorException(ANNOUNCEMENT_NOT_FOUND, announcementId));
        announcement.remove();
        announcementRepository.delete(announcement);
        return true;
    }

    private void checkIfConsistentAnnouncement(AnnouncementDto announcementDto) {
        if (announcementDto.getUserId() == null)
            throw new TutorException(TEACHER_MISSING);
        else if (announcementDto.getCourseExecutionId() == null)
            throw new TutorException(COURSE_EXECUTION_MISSING);
    }

    private CourseExecution getCourseExecution(Integer executionId) {
        return courseExecutionRepository.findById(executionId)
                .orElseThrow(() -> new TutorException(COURSE_EXECUTION_NOT_FOUND, executionId));
    }

    private User getTeacher(Integer teacherId) {
        User user = userRepository.findById(teacherId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, teacherId));
        if (!user.isTeacher())
            throw new TutorException(USER_NOT_TEACHER, user.getUsername());
        return user;
    }
}
