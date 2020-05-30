package pt.ulisboa.tecnico.socialsoftware.tutor.auth;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private UserRepository userRepository;
    private static PublicKey publicKey;
    private static PrivateKey privateKey;
    public static final String tokenCookieName = "auth";
    public static final int expiration = 1000*60*60*24;

    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static void generateKeys() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            logger.error("Unable to generate keys");
        }
    }

    static String generateToken(User user) {
        if (publicKey == null) {
            generateKeys();
        }

        Claims claims = Jwts.claims().setSubject(String.valueOf(user.getId()));
        claims.put("role", user.getRole());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(privateKey)
                .compact();
    }

    static String getToken(String token){
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        } else if (token != null && token.startsWith("AUTH")) {
            return token.substring(4);
        } else if (token != null) {
            return token;
        }

        return "";
    }

    static String getTokenFromHeader(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");

        return getToken(authHeader);
    }

    static String getTokenFromCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();

        String token = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(tokenCookieName)) {
                token = cookie.getValue();
                break;
            }
        }

        return getToken(token);
    }

    static int getUserId(String token) {
        try {
            verifyToken(token);
            return Integer.parseInt(Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).getBody().getSubject());
        } catch (MalformedJwtException ex) {
            logger.error("Invalkey JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        throw new TutorException(AUTHENTICATION_ERROR);
    }

    static void verifyToken(String token) {
        
    }

    Authentication getAuthentication(String token) {
        User user = this.userRepository.findById(getUserId(token)).orElseThrow(() -> new TutorException(USER_NOT_FOUND, getUserId(token)));
        return new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
    }
}
