package fr.lesprojetscagnottes.core.authentication.service;

import fr.lesprojetscagnottes.core.authorization.entity.AuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.repository.AuthorityRepository;
import fr.lesprojetscagnottes.core.common.security.UserPrincipal;
import fr.lesprojetscagnottes.core.user.UserEntity;
import fr.lesprojetscagnottes.core.user.UserRepository;
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
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            user = userRepository.findByEmail(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }
        }

        return new UserPrincipal(username, user.getPassword(), getAuthorities(user.getId()));
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
