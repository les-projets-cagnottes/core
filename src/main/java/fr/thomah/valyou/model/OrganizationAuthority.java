package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organizations_authorities")
public class OrganizationAuthority extends AuditEntity {

    private static final long serialVersionUID = -5098047340982969186L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @JsonIgnore
    private Organization organization;

    @ManyToMany(mappedBy = "userAuthorities", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> users;

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

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public OrganizationAuthorityName getName() {
        return name;
    }

    public void setName(OrganizationAuthorityName name) {
        this.name = name;
    }
}
