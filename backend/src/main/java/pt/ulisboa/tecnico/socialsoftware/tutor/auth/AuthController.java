package pt.ulisboa.tecnico.socialsoftware.tutor.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.AuthUserDto;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

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

    @GetMapping("/auth/check")
    public ResponseEntity<AuthUserDto> checkToken(@RequestHeader(value = "Authorization", required = false) String token, HttpServletResponse response) {
        if (token == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return this.authService.checkToken(token, response);
    }

    @GetMapping("/auth/fenix")
    public ResponseEntity<AuthUserDto> fenixAuth(@RequestParam String code, HttpServletResponse response) {
        FenixEduInterface fenix = new FenixEduInterface(baseUrl, oauthConsumerKey, oauthConsumerSecret, callbackUrl);
        fenix.authenticate(code);
        return this.authService.fenixAuth(fenix, response);
    }

    @GetMapping("/auth/demo/student")
    public ResponseEntity<AuthUserDto> demoStudentAuth(HttpServletResponse response) {
        return this.authService.demoStudentAuth(response);
    }

    @GetMapping("/auth/demo/teacher")
    public ResponseEntity<AuthUserDto> demoTeacherAuth(HttpServletResponse response) {
        return this.authService.demoTeacherAuth(response);
    }

    @GetMapping("/auth/demo/admin")
    public ResponseEntity<AuthUserDto> demoAdminAuth(HttpServletResponse response) {
        return this.authService.demoAdminAuth(response);
    }
}
