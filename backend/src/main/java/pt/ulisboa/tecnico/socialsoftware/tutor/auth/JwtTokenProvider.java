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

import javax.servlet.http.HttpServletRequest;

import java.security.Key;
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
    public static final String TOKEN_COOKIE_NAME = "auth";
    public static final int TOKEN_EXPIRATION = 1000 * 60 * 60 * 24;
    public static final int TOKEN_RENEW = 1000 * 60 * 60 * 6;

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

    public static String generateToken(String audience, User user, Key key) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(user.getId())).setAudience(audience);
        claims.put("role", user.getRole());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + TOKEN_EXPIRATION);

        return Jwts.builder().setClaims(claims).setIssuedAt(new Date()).setExpiration(expiryDate).signWith(key)
                .compact();
    }

    static String generateToken(User user) {
        if (publicKey == null) {
            generateKeys();
        }

        return generateToken("", user, privateKey);
    }

    static String getToken(String token) {
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
        return getToken(AuthService.getValueCookie(TOKEN_COOKIE_NAME, req));
    }

    static Date getExpiry(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).getBody()
                    .getExpiration();
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

    static int getUserId(String token) {
        try {
            return Integer.parseInt(
                    Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).getBody().getSubject());
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

    User getUser(String token) {
        return this.userRepository.findById(getUserId(token))
                .orElseThrow(() -> new TutorException(USER_NOT_FOUND, getUserId(token)));
    }

    Authentication getAuthentication(String token) {
        User user = this.getUser(token);
        return new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
    }
}
