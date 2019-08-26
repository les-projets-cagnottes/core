package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
@Entity
@Table(name = "users")
public class User extends AuditEntity<String> implements UserDetails {

    private static final long serialVersionUID = 6210782306288115135L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    @NotNull
    private String password;

    @Column(name = "email", unique = true)
    @NotNull
    private String email;

    @Column(name = "firstname")
    private String firstname = "";

    @Column(name = "lastname")
    private String lastname = "";

    @Column(name = "color")
    private String color = "";

    @Column(name = "avatarUrl")
    private String avatarUrl;

    @Column(name = "enabled")
    @NotNull
    private Boolean enabled = false;

    @Column(name = "lastpasswordresetdate")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date lastPasswordResetDate = new Date();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_authority",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_id", referencedColumnName = "id")})
    @JsonIgnoreProperties({"users"})
    private Set<Authority> userAuthorities = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_authority_organizations",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "organization_authority_id", referencedColumnName = "id")})
    @JsonIgnoreProperties({"organization", "userAuthorities"})
    private Set<OrganizationAuthority> userOrganizationAuthorities = new LinkedHashSet<>();

    @Transient
    private Collection<GrantedAuthority> authorities = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    private Set<Organization> organizations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "sponsor")
    @JsonIgnoreProperties({"organization", "sponsor", "donations"})
    private Set<Budget> budgets = new LinkedHashSet<>();

    @OneToMany(mappedBy = "leader")
    @JsonIgnoreProperties({"leader", "donations", "peopleGivingTime", "organizations"})
    private Set<Project> projects = new LinkedHashSet<>();

    @OneToMany(mappedBy = "contributor")
    @JsonIgnoreProperties({"budget"})
    private Set<Donation> donations = new LinkedHashSet<>();

    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public User(Long id,
                String username,
                @NotNull String password,
                @NotNull String email,
                String firstname,
                String lastname,
                String color,
                String avatarUrl,
                @NotNull Boolean enabled,
                @NotNull Date lastPasswordResetDate,
                Collection<GrantedAuthority> authorities,
                Set<Organization> organizations,
                Set<Budget> budgets,
                Set<Project> projects,
                Set<Donation> donations) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.color = color;
        this.avatarUrl = avatarUrl;
        this.enabled = enabled;
        this.lastPasswordResetDate = lastPasswordResetDate;
        this.authorities = authorities;
        this.organizations = organizations;
        this.budgets = budgets;
        this.projects = projects;
        this.donations = donations;
    }

    public String getAvatarUrl() {
        if (avatarUrl == null || avatarUrl.equals("")) {
            return "https://ui-avatars.com/api/?name=" + firstname + "+" + lastname + "&background=" + color;
        }
        return avatarUrl;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", enabled=" + enabled +
                ", lastPasswordResetDate=" + lastPasswordResetDate +
                '}';
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void addAuthority(Authority authority) {
        userAuthorities.add(authority);
    }

    public void addOrganization(Organization organization) {
        organizations.add(organization);
    };

    public void addOrganizationAuthority(OrganizationAuthority authority) {
        userOrganizationAuthorities.add(authority);
    }
    public void generateColor() {
        this.color = "6CBFBB";
    }

}
