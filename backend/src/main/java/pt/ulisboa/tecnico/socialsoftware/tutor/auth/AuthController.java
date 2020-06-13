package pt.ulisboa.tecnico.socialsoftware.tutor.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.AuthUserDto;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    @Value("${base.url}")
    private String baseUrl;

    @Value("${oauth.consumer.key}")
    private String oauthConsumerKey;

    @Value("${oauth.consumer.secret}")
    private String oauthConsumerSecret;

    @Value("${callback.url}")
    private String callbackUrl;

    @PostMapping("/auth/logout")
    public boolean logout(@CookieValue(value = JwtTokenProvider.TOKEN_COOKIE_NAME, required = false) String token,
            HttpServletRequest request, HttpServletResponse response) {
        if (token == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        this.authService.removeCookie(JwtTokenProvider.TOKEN_COOKIE_NAME, request, response);
        return true;
    }

    @GetMapping("/auth/check")
    public AuthUserDto checkToken(
            @CookieValue(value = JwtTokenProvider.TOKEN_COOKIE_NAME, required = false) String token,
            HttpServletRequest request, HttpServletResponse response) {
        if (token == null) {
            this.authService.removeCookie(JwtTokenProvider.TOKEN_COOKIE_NAME, request, response);
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return this.authService.checkToken(token, request, response);
    }

    @GetMapping("/auth/fenix")
    public AuthUserDto fenixAuth(@CookieValue(value = "session", required = false) Boolean session,
            @RequestParam String code, HttpServletResponse response) {
        FenixEduInterface fenix = new FenixEduInterface(baseUrl, oauthConsumerKey, oauthConsumerSecret, callbackUrl);
        fenix.authenticate(code);

        return this.authService.fenixAuth(session, fenix, response);
    }

    @GetMapping("/auth/demo/student")
    public AuthUserDto demoStudentAuth(@CookieValue(value = "session", required = false) Boolean session,
            HttpServletResponse response) {
        return this.authService.demoStudentAuth(session, response);
    }

    @GetMapping("/auth/demo/teacher")
    public AuthUserDto demoTeacherAuth(@CookieValue(value = "session", required = false) Boolean session,
            HttpServletResponse response) {
        return this.authService.demoTeacherAuth(session, response);
    }

    @GetMapping("/auth/demo/admin")
    public AuthUserDto demoAdminAuth(@CookieValue(value = "session", required = false) Boolean session,
            HttpServletResponse response) {
        return this.authService.demoAdminAuth(session, response);
    }
}
