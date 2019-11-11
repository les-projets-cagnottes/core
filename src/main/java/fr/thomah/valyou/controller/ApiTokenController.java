package fr.thomah.valyou.controller;

import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.model.AuthenticationResponse;
import fr.thomah.valyou.model.User;
import fr.thomah.valyou.repository.ApiTokenRepository;
import fr.thomah.valyou.repository.UserRepository;
import fr.thomah.valyou.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
public class ApiTokenController {

    @Autowired
    private ApiTokenRepository apiTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuthenticationResponse> list(Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        User user = (User) token.getPrincipal();
        user = userRepository.findByEmail(user.getEmail());
        List<AuthenticationResponse> apiTokens = apiTokenRepository.findAllByUserId(user.getId());
        apiTokens.forEach(apiToken -> {
            apiToken.setToken("");
        });
        return apiTokens;
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/token", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticationResponse generateApiToken(Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        User user = (User) token.getPrincipal();
        user = userRepository.findByEmail(user.getEmail());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        Date nextYear = cal.getTime();

        AuthenticationResponse authenticationResponse = new AuthenticationResponse(jwtTokenUtil.generateApiToken(user, nextYear));
        authenticationResponse.setExpiration(nextYear);
        authenticationResponse.setUser(user);

        return apiTokenRepository.save(authenticationResponse);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/token/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(Principal principal, @PathVariable("id") long id) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        User user = (User) token.getPrincipal();
        user = userRepository.findByEmail(user.getEmail());
        AuthenticationResponse apiToken = apiTokenRepository.findByIdAndUserId(id, user.getId());
        if(apiToken == null) {
            throw new NotFoundException();
        } else {
            apiTokenRepository.deleteById(apiToken.getId());
        }
    }
}
