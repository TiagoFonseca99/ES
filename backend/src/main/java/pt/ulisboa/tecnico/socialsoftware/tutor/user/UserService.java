package pt.ulisboa.tecnico.socialsoftware.tutor.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.Demo;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.UsersXmlExport;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.UsersXmlImport;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.domain.Review;
import pt.ulisboa.tecnico.socialsoftware.tutor.submission.repository.ReviewRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.DashboardDto;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public User findByUsername(String username) {
        return this.userRepository.findByUsername(username);
    }

    public User findByKey(Integer key) {
        return this.userRepository.findByKey(key);
    }

    public Integer getMaxUserNumber() {
        Integer result = userRepository.getMaxUserNumber();
        return result != null ? result : 0;
    }

    public User createUser(String name, String username, User.Role role) {

        if (findByUsername(username) != null) {
            throw new TutorException(DUPLICATE_USER, username);
        }

        User user = new User(name, username, getMaxUserNumber() + 1, role);
        userRepository.save(user);
        return user;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public String getEnrolledCoursesAcronyms(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        return user.getEnrolledCoursesAcronyms();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<CourseDto> getCourseExecutions(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        return user.getCourseExecutions().stream().map(CourseDto::new).collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void setNumberOfReviewedSubmissions(User user) {
        List<Review> studentApprovedSubmissions = reviewRepository.getApprovedSubmissions(user.getId());
        List<Review> studentRejectedSubmissions = reviewRepository.getRejectedSubmissions(user.getId());

        user.setNumberOfApprovedSubmissions(studentApprovedSubmissions.size());
        user.setNumberOfRejectedSubmissions(studentRejectedSubmissions.size());
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void addCourseExecution(int userId, int executionId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        CourseExecution courseExecution = courseExecutionRepository.findById(executionId)
                .orElseThrow(() -> new TutorException(COURSE_EXECUTION_NOT_FOUND, executionId));

        user.addCourse(courseExecution);
        courseExecution.addUser(user);
    }

    public String exportUsers() {
        UsersXmlExport xmlExporter = new UsersXmlExport();

        return xmlExporter.export(userRepository.findAll());
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public DashboardDto getDashboardInfo(Integer requesterId) {
        User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, requesterId));

        checkStudent(user);
        setNumberOfReviewedSubmissions(user);

        return user.getDashboardInfo();
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public DashboardDto toggleDiscussionStatsVisibility(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        checkStudent(user);
        user.toggleDiscussionStatsVisibility();

        return user.getDashboardInfo();
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public DashboardDto toggleSubmissionStatsVisibility(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        checkStudent(user);
        user.toggleSubmissionStatsVisibility();

        return user.getDashboardInfo();
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void importUsers(String usersXML) {
        UsersXmlImport xmlImporter = new UsersXmlImport();

        xmlImporter.importUsers(usersXML, this);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public User getDemoTeacher() {
        User user = this.userRepository.findByUsername(Demo.TEACHER_USERNAME);
        if (user == null)
            return createUser("Demo Teacher", Demo.TEACHER_USERNAME, User.Role.TEACHER);
        return user;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public User getDemoStudent() {
        User user = this.userRepository.findByUsername(Demo.STUDENT_USERNAME);
        if (user == null)
            return createUser("Demo Student", Demo.STUDENT_USERNAME, User.Role.STUDENT);
        return user;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public User getDemoAdmin() {
        User user = this.userRepository.findByUsername(Demo.ADMIN_USERNAME);
        if (user == null)
            return createUser("Demo Admin", Demo.ADMIN_USERNAME, User.Role.DEMO_ADMIN);
        return user;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void switchTournamentNamePermission(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        if (user.getTournamentNamePermission()) {
            user.setTournamentNamePermission(false);
            user.setTournamentScorePermission(false);
        }
        else {
            user.setTournamentNamePermission(true);
        }
    }


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void switchTournamentScorePermission(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        if (!user.getTournamentNamePermission()) {
            throw new TutorException(USER_TOURNAMENT_PERMISSIONS_NOT_CONSISTENT, user.getUsername());
        }

        user.setTournamentScorePermission(!user.getTournamentScorePermission());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean getTournamentNamePermission(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        return user.getTournamentNamePermission();
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean getTournamentScorePermission(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        return user.getTournamentScorePermission();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public User createDemoStudent() {
        String birthDate = DateHandler.now().toString();
        User newDemoUser = createUser("Demo-Student-" + birthDate, "Demo-Student-" + birthDate, User.Role.STUDENT);

        User demoUser = this.userRepository.findByUsername(Demo.STUDENT_USERNAME);

        CourseExecution courseExecution = demoUser.getCourseExecutions().stream().findAny().orElse(null);

        if (courseExecution != null) {
            courseExecution.addUser(newDemoUser);
            newDemoUser.addCourse(courseExecution);
        }

        return newDemoUser;
    }

    private void checkStudent(User user) {
        if (user.getRole() != User.Role.STUDENT) {
            throw new TutorException(USER_NOT_STUDENT, user.getId());
        }
    }
}
