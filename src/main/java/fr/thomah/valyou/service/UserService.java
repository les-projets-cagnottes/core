package fr.thomah.valyou.service;

import fr.thomah.valyou.model.User;
import fr.thomah.valyou.repository.UserRepository;
import fr.thomah.valyou.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service(value = "userService")
public class UserService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if(user == null){
            user = userRepository.findByEmail(username);
            if(user == null) {
                throw new UsernameNotFoundException("Invalid username or password.");
            }
        }
        return new UserPrincipal(username, user.getPassword(), getAuthority(user));
    }

    private Set<SimpleGrantedAuthority> getAuthority(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        user.getUserAuthorities().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName().name()));
        });
        return authorities;
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
        return userRepository.findById(id).get();
    }

    public User save(User user) {
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        if(user.getPassword() != null && !user.getPassword().equals("")) {
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