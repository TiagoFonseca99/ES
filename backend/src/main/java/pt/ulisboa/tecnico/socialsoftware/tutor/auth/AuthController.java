package pt.ulisboa.tecnico.socialsoftware.tutor.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

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
    public AuthDto checkToken(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return this.authService.checkToken(token);
    }

    @GetMapping("/auth/fenix")
    public AuthDto fenixAuth(@RequestParam String code) {
        FenixEduInterface fenix = new FenixEduInterface(baseUrl, oauthConsumerKey, oauthConsumerSecret, callbackUrl);
        fenix.authenticate(code);
        return this.authService.fenixAuth(fenix);
    }

    @GetMapping("/auth/demo/student")
    public AuthDto demoStudentAuth() {
        return this.authService.demoStudentAuth();
    }

    @GetMapping("/auth/demo/teacher")
    public AuthDto demoTeacherAuth() {
        return this.authService.demoTeacherAuth();
    }

    @GetMapping("/auth/demo/admin")
    public AuthDto demoAdminAuth() {
        return this.authService.demoAdminAuth();
    }
}
