package pt.ulisboa.tecnico.socialsoftware.tutor.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Date;

public class JwtTokenFilter extends GenericFilterBean {

    private JwtTokenProvider jwtTokenProvider;

    JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
            throws IOException, ServletException {

        String token = JwtTokenProvider.getTokenFromCookie((HttpServletRequest) req);
        if (!token.isEmpty()) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            if (JwtTokenProvider.getExpiry(token)
                    .before(new Date(new Date().getTime() + JwtTokenProvider.TOKEN_RENEW))) {
                String session = AuthService.getValueCookie("session", (HttpServletRequest) req);

                AuthService.setCookie(JwtTokenProvider.TOKEN_COOKIE_NAME,
                        JwtTokenProvider.generateToken(jwtTokenProvider.getUser(token)), (HttpServletResponse) res,
                        true, session != null && session.equals("true") ? null : AuthService.COOKIE_EXP_TIME);
            }
        }
        filterChain.doFilter(req, res);
    }
}
