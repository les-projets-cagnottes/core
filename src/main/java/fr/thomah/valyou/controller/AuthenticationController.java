package fr.thomah.valyou.controller;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

import fr.thomah.valyou.exceptions.BadRequestException;
import fr.thomah.valyou.model.User;
import fr.thomah.valyou.repository.UserRepository;
import fr.thomah.valyou.exceptions.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import fr.thomah.valyou.security.JwtTokenUtil;
import fr.thomah.valyou.model.AuthenticationResponse;

@RestController
public class AuthenticationController {

    private static final String TOKEN_HEADER = "Authorization";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserRepository repository;

    @Autowired
    @Qualifier("jwtUserDetailsService")
    private UserDetailsService userDetailsService;

    @RequestMapping(value = "${jwt.route.authentication.path}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticationResponse login(@RequestBody User user) throws AuthenticationException {
        authenticate(user.getEmail(), user.getPassword());
        return new AuthenticationResponse(jwtTokenUtil.generateToken(user));
    }

    @RequestMapping(value = "${jwt.route.authentication.refresh}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticationResponse refresh(HttpServletRequest request) {
        String authToken = request.getHeader(TOKEN_HEADER);
        final String token = authToken.substring(7);
        String email = jwtTokenUtil.getEmailFromToken(token);
        User user = repository.findByEmail(email);
        if (jwtTokenUtil.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
            String refreshedToken = jwtTokenUtil.refreshToken(token);
            return new AuthenticationResponse(refreshedToken);
        } else {
            throw new BadRequestException();
        }
    }

    @RequestMapping(value = "/api/whoami", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public User getAuthenticatedUser(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER).substring(7);
        String email = jwtTokenUtil.getEmailFromToken(token);
        return repository.findByEmail(email);
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    /**
     * Authenticates the user. If something is wrong, an {@link AuthenticationException} will be thrown
     */
    private void authenticate(String email, String password) {
        Objects.requireNonNull(email);
        Objects.requireNonNull(password);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException e) {
            throw new AuthenticationException("User is disabled!", e);
        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Bad credentials!", e);
        }
    }
}
