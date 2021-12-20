package fr.lesprojetscagnottes.core.user;

import fr.lesprojetscagnottes.core.authorization.entity.AuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.name.AuthorityName;
import fr.lesprojetscagnottes.core.authorization.repository.AuthorityRepository;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class UserGenerator {

    @Autowired
    private AuthorityRepository authorityRepository;

    private static List<AuthorityEntity> authorities;

    @PostConstruct
    public void init() {
        authorities = authorityRepository.findAll();
    }

    public static UserEntity newUser(String email, String password) {
        UserEntity user = new UserEntity(email, password);
        return newUser(user);
    }

    public static UserEntity newUser(UserEntity user) {
        user.setEnabled(true);
        user.addAuthority(authorities.stream().filter(authority -> authority.getName().equals(AuthorityName.ROLE_USER)).findFirst().orElse(null));
        return user;
    }

}
