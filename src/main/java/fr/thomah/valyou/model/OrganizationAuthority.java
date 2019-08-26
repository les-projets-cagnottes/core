package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "organizations_authorities")
public class OrganizationAuthority extends AuditEntity<String>{

    private static final long serialVersionUID = -5098047340982969186L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JsonIgnoreProperties({"members", "projects", "budgets"})
    private Organization organization;

    @ManyToMany(mappedBy = "userAuthorities", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "donations"})
    private Set<User> users;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public OrganizationAuthorityName getName() {
        return name;
    }

    public void setName(OrganizationAuthorityName name) {
        this.name = name;
    }
}
