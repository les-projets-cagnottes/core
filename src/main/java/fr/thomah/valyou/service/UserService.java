package fr.thomah.valyou.service;

import fr.thomah.valyou.entity.Authority;
import fr.thomah.valyou.entity.User;
import fr.thomah.valyou.repository.AuthorityRepository;
import fr.thomah.valyou.repository.UserRepository;
import fr.thomah.valyou.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service(value = "userService")
public class UserService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    public User get(Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        UserPrincipal userPrincipal = (UserPrincipal) token.getPrincipal();
        User user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user == null) {
            user = userRepository.findByEmail(userPrincipal.getUsername());
        }
        return user;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            user = userRepository.findByEmail(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }
        }

        return new UserPrincipal(username, user.getPassword(), getAuthorities(user.getId()));
    }

    public List<GrantedAuthority> getAuthorities(long userId) {
        Set<Authority> userAuthorities = authorityRepository.findAllByUsers_Id(userId);
        LOGGER.debug("Authorities for user {} :", userId);
        return userAuthorities.stream().map(r -> {
            LOGGER.debug("{}", r.getName().name());
            return new SimpleGrantedAuthority(r.getAuthority());
        }).collect(Collectors.toList());
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        userRepository.findAll().iterator().forEachRemaining(list::add);
        return list;
    }

    public void delete(long id) {
        userRepository.deleteById(id);
    }

    public User findOne(String username) {
        return userRepository.findByUsername(username);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User save(User user) {
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        if (user.getPassword() != null && !user.getPassword().equals("")) {
            newUser.setPassword(bcryptEncoder.encode(user.getPassword()));
            newUser.setLastPasswordResetDate(new Date());
        }
        newUser.setEmail(user.getEmail());
        newUser.setFirstname(user.getFirstname());
        newUser.setLastname(user.getLastname());
        newUser.setAvatarUrl(user.getAvatarUrl());
        newUser.setEnabled(user.getEnabled());
        return userRepository.save(newUser);
    }
}