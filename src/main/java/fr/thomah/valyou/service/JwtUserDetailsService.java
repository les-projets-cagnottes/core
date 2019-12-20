package fr.thomah.valyou.service;

import fr.thomah.valyou.model.User;
import fr.thomah.valyou.exception.EmailNotFoundException;
import fr.thomah.valyou.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import fr.thomah.valyou.security.JwtUserFactory;

import java.security.Principal;

@Service("jwtUserDetailsService")
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);

        if (user == null) {
            throw new EmailNotFoundException(String.format("No user found with email '%s'.", username));
        } else {
            return JwtUserFactory.create(user);
        }
    }

    public User getUserFromPrincipal(Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        User userLoggedIn = (User) token.getPrincipal();
        return userRepository.findByEmail(userLoggedIn.getEmail());
    }
}
