package fr.lesprojetscagnottes.core.authentication.service;

import fr.lesprojetscagnottes.core.authorization.entity.AuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.repository.AuthorityRepository;
import fr.lesprojetscagnottes.core.common.security.UserPrincipal;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import fr.lesprojetscagnottes.core.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service(value = "authService")
public class AuthService implements UserDetailsService {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("looking for user {}", username);
        UserEntity user = userRepository.findByUsername(username);
        log.debug("-> by username : {}", user);
        if (user == null) {
            user = userRepository.findByEmail(username);
            log.debug("-> by email : {}", user);
            if (user == null) {
                try {
                    Pattern pattern = Pattern.compile(".*id='(\\d+)'.*");
                    Matcher matcher = pattern.matcher(username);
                    if (matcher.find()) {
                        user = userRepository.findById(Long.parseLong(matcher.group(1))).orElse(null);
                    }
                    log.debug("-> by id : {}", user);
                } catch (NumberFormatException nfe) {
                    throw new UsernameNotFoundException("User not found");
                }
            }
        }
        if(user != null) {
            log.debug("user found : {}", user.getEmail());
            return new UserPrincipal(user.getEmail(), user.getPassword(), getAuthorities(user.getId()));
        } else {
            return null;
        }
    }

    public List<GrantedAuthority> getAuthorities(long userId) {
        Set<AuthorityEntity> userAuthorities = authorityRepository.findAllByUsers_Id(userId);
        log.debug("Authorities for user {} :", userId);
        return userAuthorities.stream().map(r -> {
            log.debug("{}", r.getName().name());
            return new SimpleGrantedAuthority(r.getAuthority());
        }).collect(Collectors.toList());
    }

}
