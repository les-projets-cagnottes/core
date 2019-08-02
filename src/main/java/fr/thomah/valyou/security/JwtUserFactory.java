package fr.thomah.valyou.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import fr.thomah.valyou.model.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class JwtUserFactory {

    private JwtUserFactory() {
    }

    public static User create(User user) {
        return new User(user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getFirstname(),
                user.getLastname(),
                user.getColor(),
                user.getAvatarUrl(),
                user.getEnabled(),
                user.getLastPasswordResetDate(),
                mapToGrantedAuthorities(user.getUserAuthorities()),
                user.getOrganizations(),
                user.getBudgets(),
                user.getProjects(),
                user.getDonations());
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(Collection<Authority> authorities) {
        return authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName().name()))
                .collect(Collectors.toList());
    }
}
