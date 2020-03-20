package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.thomah.valyou.audit.AuditEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "organizations_authorities")
public class OrganizationAuthority extends AuditEntity<String> {

    private static final long serialVersionUID = -5098047340982969186L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JsonIgnoreProperties({"members", "projects", "budgets", "slackTeam"})
    private Organization organization = new Organization();

    @ManyToMany(mappedBy = "userAuthorities", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations", "slackUsers", "apiTokens"})
    private Set<User> users = new LinkedHashSet<>();

    @Column(length = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    private OrganizationAuthorityName name;

    public OrganizationAuthority() {
    }

    public OrganizationAuthority(Organization organization, OrganizationAuthorityName name) {
        this.organization = organization;
        this.name = name;
    }

}
