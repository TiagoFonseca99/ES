package pt.ulisboa.tecnico.socialsoftware.tutor.auth;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.AuthUserDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_ENROLLED;

@Service
public class AuthService {
    private static final int COOKIE_EXP_TIME = JwtTokenProvider.TOKEN_EXPIRATION / 1000;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AuthUserDto checkToken(String token, HttpServletRequest request, HttpServletResponse response) {
        token = JwtTokenProvider.getToken(token);

        try {
            User user = this.userService.findById(JwtTokenProvider.getUserId(token));
            return new AuthUserDto(user);
        } catch (TutorException e) {
            removeCookie(JwtTokenProvider.TOKEN_COOKIE_NAME, request, response);
            throw e;
        }
    }

    @Retryable(value = { SQLException.class }, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AuthUserDto fenixAuth(Boolean session, FenixEduInterface fenix, HttpServletResponse response) {
        String username = fenix.getPersonUsername();
        List<CourseDto> fenixAttendingCourses = fenix.getPersonAttendingCourses();
        List<CourseDto> fenixTeachingCourses = fenix.getPersonTeachingCourses();

        List<CourseExecution> activeAttendingCourses = getActiveTecnicoCourses(fenixAttendingCourses);
        List<CourseExecution> activeTeachingCourses = getActiveTecnicoCourses(fenixTeachingCourses);

        User user = this.userService.findByUsername(username);

        // If user is student and is not in db
        if (user == null && !activeAttendingCourses.isEmpty()) {
            user = this.userService.createUser(fenix.getPersonName(), username, User.Role.STUDENT);
        }

        // If user is teacher and is not in db
        if (user == null && !fenixTeachingCourses.isEmpty()) {
            user = this.userService.createUser(fenix.getPersonName(), username, User.Role.TEACHER);
        }

        if (user == null) {
            throw new TutorException(USER_NOT_ENROLLED, username);
        }

        user.setLastAccess(DateHandler.now());

        if (user.getRole() == User.Role.ADMIN) {
            List<CourseDto> allCoursesInDb = courseExecutionRepository.findAll().stream().map(CourseDto::new)
                    .collect(Collectors.toList());

            if (!fenixTeachingCourses.isEmpty()) {
                User finalUser = user;
                activeTeachingCourses.stream()
                        .filter(courseExecution -> !finalUser.getCourseExecutions().contains(courseExecution))
                        .forEach(user::addCourse);

                allCoursesInDb.addAll(fenixTeachingCourses);

                String ids = fenixTeachingCourses.stream()
                        .map(courseDto -> courseDto.getAcronym() + courseDto.getAcademicTerm())
                        .collect(Collectors.joining(","));

                user.setEnrolledCoursesAcronyms(ids);
            }

            setCookie(JwtTokenProvider.TOKEN_COOKIE_NAME, JwtTokenProvider.generateToken(user), response, true,
                    session != null && session ? null : COOKIE_EXP_TIME);
            return new AuthUserDto(user, allCoursesInDb);
        }

        // Update student courses
        if (!activeAttendingCourses.isEmpty() && user.getRole() == User.Role.STUDENT) {
            User student = user;
            activeAttendingCourses.stream()
                    .filter(courseExecution -> !student.getCourseExecutions().contains(courseExecution))
                    .forEach(user::addCourse);

            setCookie(JwtTokenProvider.TOKEN_COOKIE_NAME, JwtTokenProvider.generateToken(user), response, true,
                    session != null && session ? null : COOKIE_EXP_TIME);
            return new AuthUserDto(user);
        }

        // Update teacher courses
        if (!fenixTeachingCourses.isEmpty() && user.getRole() == User.Role.TEACHER) {
            User teacher = user;
            activeTeachingCourses.stream()
                    .filter(courseExecution -> !teacher.getCourseExecutions().contains(courseExecution))
                    .forEach(user::addCourse);

            String ids = fenixTeachingCourses.stream()
                    .map(courseDto -> courseDto.getAcronym() + courseDto.getAcademicTerm())
                    .collect(Collectors.joining(","));

            user.setEnrolledCoursesAcronyms(ids);

            setCookie(JwtTokenProvider.TOKEN_COOKIE_NAME, JwtTokenProvider.generateToken(user), response, true,
                    session != null && session ? null : COOKIE_EXP_TIME);
            return new AuthUserDto(user, fenixTeachingCourses);
        }

        // Previous teacher without active courses
        if (user.getRole() == User.Role.TEACHER) {
            setCookie(JwtTokenProvider.TOKEN_COOKIE_NAME, JwtTokenProvider.generateToken(user), response, true,
                    session != null && session ? null : COOKIE_EXP_TIME);
            return new AuthUserDto(user);
        }

        throw new TutorException(USER_NOT_ENROLLED, username);
    }

    private List<CourseExecution> getActiveTecnicoCourses(List<CourseDto> courses) {
        return courses.stream().map(courseDto -> {
            Course course = courseRepository.findByNameType(courseDto.getName(), Course.Type.TECNICO.name())
                    .orElse(null);
            if (course == null) {
                return null;
            }
            return course.getCourseExecution(courseDto.getAcronym(), courseDto.getAcademicTerm(), Course.Type.TECNICO)
                    .orElse(null);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Retryable(value = { SQLException.class }, maxAttempts = 2, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AuthUserDto demoStudentAuth(Boolean session, HttpServletResponse response) {
        User user;
        // if (activeProfile.equals("dev")) {
        // user = this.userService.createDemoStudent();
        // } else {
        user = this.userService.getDemoStudent();
        // }

        setCookie(JwtTokenProvider.TOKEN_COOKIE_NAME, JwtTokenProvider.generateToken(user), response, true,
                session != null && session ? null : COOKIE_EXP_TIME);
        return new AuthUserDto(user);
    }

    @Retryable(value = { SQLException.class }, maxAttempts = 2, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AuthUserDto demoTeacherAuth(Boolean session, HttpServletResponse response) {
        User user = this.userService.getDemoTeacher();

        setCookie(JwtTokenProvider.TOKEN_COOKIE_NAME, JwtTokenProvider.generateToken(user), response, true,
                session != null && session ? null : COOKIE_EXP_TIME);
        return new AuthUserDto(user);
    }

    @Retryable(value = { SQLException.class }, maxAttempts = 2, backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AuthUserDto demoAdminAuth(Boolean session, HttpServletResponse response) {
        User user = this.userService.getDemoAdmin();

        setCookie(JwtTokenProvider.TOKEN_COOKIE_NAME, JwtTokenProvider.generateToken(user), response, true,
                session != null && session ? null : COOKIE_EXP_TIME);
        return new AuthUserDto(user);
    }

    public void setCookie(String name, String value, HttpServletResponse response, Boolean http, Integer age) {
        String header = name + "=" + value + "; Path=/; SameSite=Lax;";
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(http != null ? http : false);

        if (http != null) {
            header += "; HttpOnly";
        }
        // Allow session cookies
        if (age != null) {
            header += "; Max-Age=" + age;
        }

        if (activeProfile.equals("prod")) {
            header += "; Secure";
        }

        response.setHeader("Set-Cookie", header);
    }

    public void removeCookie(String name, HttpServletRequest request, HttpServletResponse response) {
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);

                return;
            }
        }

    }
}
