package fr.thomah.valyou.generator;

import fr.thomah.valyou.entity.Authority;
import fr.thomah.valyou.entity.AuthorityName;
import fr.thomah.valyou.entity.User;
import fr.thomah.valyou.repository.AuthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class UserGenerator {

    @Autowired
    private AuthorityRepository authorityRepository;

    private static List<Authority> authorities;

    @PostConstruct
    public void init() {
        authorities = authorityRepository.findAll();
    }

    public static User newUser(String email, String password) {
        User user = new User(email, password);
        return newUser(user);
    }

    public static User newUser(User user) {
        user.setEnabled(true);
        user.addAuthority(authorities.stream().filter(authority -> authority.getName().equals(AuthorityName.ROLE_USER)).findFirst().orElse(null));
        return user;
    }

}
