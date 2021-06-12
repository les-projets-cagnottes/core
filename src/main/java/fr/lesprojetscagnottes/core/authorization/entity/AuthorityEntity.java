package fr.lesprojetscagnottes.core.authorization.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.authorization.name.AuthorityName;
import fr.lesprojetscagnottes.core.authorization.model.AuthorityModel;
import fr.lesprojetscagnottes.core.user.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "authorities")
public class AuthorityEntity extends AuthorityModel implements GrantedAuthority {

    private static final long serialVersionUID = -8193848589240726612L;

    @ManyToMany(mappedBy = "userAuthorities", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "campaigns", "donations", "slackUsers", "apiTokens", "accounts"})
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