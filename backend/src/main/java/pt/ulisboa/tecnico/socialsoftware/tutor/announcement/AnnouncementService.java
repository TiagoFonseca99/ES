package pt.ulisboa.tecnico.socialsoftware.tutor.announcement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.domain.Announcement;
import pt.ulisboa.tecnico.socialsoftware.tutor.announcement.dto.AnnouncementDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;


@Service
public class AnnouncementService {

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    EntityManager entityManager;


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AnnouncementDto createAnnouncement(AnnouncementDto announcementDto){

        checkIfConsistentAnnouncement(announcementDto);

        User user = getTeacher(announcementDto.getUserId());

        CourseExecution courseExecution = getCourseExecution(announcementDto.getCourseExecutionId());

        if (announcementDto.getCreationDate() == null) {
            announcementDto.setCreationDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }

        Announcement announcement = new Announcement(user, courseExecution, announcementDto);

        entityManager.persist(announcement);
        return new AnnouncementDto(announcement);
    }

    private void checkIfConsistentAnnouncement(AnnouncementDto announcementDto) {
        if(announcementDto.getUserId() == null)
            throw new TutorException(TEACHER_MISSING);
        else if(announcementDto.getCourseExecutionId() == null)
            throw new TutorException(COURSE_EXECUTION_MISSING);
    }


    private CourseExecution getCourseExecution(Integer executionId) {
        return courseExecutionRepository.findById(executionId).orElseThrow(() -> new TutorException(COURSE_EXECUTION_NOT_FOUND, executionId));
    }

    private User getTeacher(Integer teacherId) {
        User user = userRepository.findById(teacherId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, teacherId));
        if (!user.isTeacher())
            throw new TutorException(USER_NOT_TEACHER, user.getUsername());
        return user;
    }
}
