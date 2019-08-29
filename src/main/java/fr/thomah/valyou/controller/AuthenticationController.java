package fr.thomah.valyou.controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.thomah.valyou.exception.BadRequestException;
import fr.thomah.valyou.exception.UnauthaurizedException;
import fr.thomah.valyou.generator.UserGenerator;
import fr.thomah.valyou.model.*;
import fr.thomah.valyou.repository.OrganizationRepository;
import fr.thomah.valyou.repository.UserRepository;
import fr.thomah.valyou.exception.AuthenticationException;
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
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import fr.thomah.valyou.security.JwtTokenUtil;

@RestController
public class AuthenticationController {

    private static final String HTTP_PROXY = System.getenv("HTTP_PROXY");
    private static final String SLACK_CLIENT_ID = System.getenv("VALYOU_SLACK_CLIENT_ID");
    private static final String SLACK_CLIENT_SECRET = System.getenv("VALYOU_SLACK_CLIENT_SECRET");

    private static final String TOKEN_HEADER = "Authorization";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private OrganizationRepository organizationRepository;

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

    @RequestMapping(value = "/api/auth/login/slack", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticationResponse slack(@RequestParam String code, @RequestParam String redirect_uri) throws AuthenticationException {
        HttpClient httpClient;
        if(HTTP_PROXY != null) {
            String[] proxy = HTTP_PROXY.replace("http://", "").replace("https://", "").split(":");
            httpClient = HttpClient.newBuilder()
                    .proxy(ProxySelector.of(new InetSocketAddress(proxy[0], Integer.parseInt(proxy[1]))))
                    .version(HttpClient.Version.HTTP_2)
                    .build();
        } else {
            httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .build();
        }
        String url = "https://slack.com/api/oauth.access?client_id=" + SLACK_CLIENT_ID + "&client_secret=" + SLACK_CLIENT_SECRET + "&code=" + code + "&redirect_uri=" + redirect_uri;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        HttpResponse response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body().toString(), JsonObject.class);
            if (json.get("user") != null && json.get("team") != null) {
                Organization organization = organizationRepository.findBySlackTeamId(json.get("team").getAsJsonObject().get("id").getAsString());
                if(organization != null) {
                    JsonObject jsonUser = json.get("user").getAsJsonObject();
                    User user = repository.findByEmail(jsonUser.get("email").getAsString());
                    if(user == null) {
                        user = new User();
                        user.setFirstname(jsonUser.get("name").getAsString());
                        user.setEmail(jsonUser.get("email").getAsString());
                        user.setAvatarUrl(jsonUser.get("image_192").getAsString());
                        user.setPassword(BCrypt.hashpw(jsonUser.get("email").getAsString(), BCrypt.gensalt()));
                        user = repository.save(UserGenerator.newUser(user));
                        organization.getMembers().add(user);
                        organizationRepository.save(organization);
                    }
                    return new AuthenticationResponse(jwtTokenUtil.generateToken(user));
                } else {
                    throw new UnauthaurizedException();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
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
