package fr.lesprojetscagnottes.core.authorization.entity;

import fr.lesprojetscagnottes.core.authorization.model.AuthorityModel;
import fr.lesprojetscagnottes.core.authorization.name.AuthorityName;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.io.Serial;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "authorities")
public class AuthorityEntity extends AuthorityModel implements GrantedAuthority {

    @Serial
    private static final long serialVersionUID = -8193848589240726612L;

    @ManyToMany(mappedBy = "userAuthorities", fetch = FetchType.LAZY)
    private Set<UserEntity> users = new LinkedHashSet<>();

    public AuthorityEntity() {
    }

    public AuthorityEntity(AuthorityName authorityName) {
        this.name = authorityName;
    }

    @Override
    public String getAuthority() {
        return name.name();
    }
}