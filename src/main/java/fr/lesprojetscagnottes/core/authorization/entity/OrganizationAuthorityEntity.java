package fr.lesprojetscagnottes.core.authorization.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.authorization.name.OrganizationAuthorityName;
import fr.lesprojetscagnottes.core.authorization.model.OrganizationAuthorityModel;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "organizations_authorities")
public class OrganizationAuthorityEntity extends OrganizationAuthorityModel {

    private static final long serialVersionUID = -5098047340982969186L;

    @ManyToOne
    @JsonIgnoreProperties({"name", "members", "campaigns", "budgets", "contents", "organizationAuthorities", "slackTeam"})
    private OrganizationEntity organization = new OrganizationEntity();

    @ManyToMany(mappedBy = "userOrganizationAuthorities")
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "campaigns", "donations", "slackUsers", "apiTokens", "accounts"})
    private Set<UserEntity> users = new LinkedHashSet<>();

    public OrganizationAuthorityEntity() {
    }

    public OrganizationAuthorityEntity(OrganizationEntity organization, OrganizationAuthorityName name) {
        super();
        this.organization = organization;
        this.name = name;
    }

    @Override
    public String toString() {
        return "OrganizationAuthority{" +
                "id=" + id +
                ", organization=" + organization.getName() +
                ", name=" + name +
                '}';
    }
}
