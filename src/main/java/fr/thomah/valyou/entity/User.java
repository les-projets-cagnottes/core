package fr.thomah.valyou.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.thomah.valyou.audit.AuditEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
@Entity
@NamedEntityGraph(name = "User.withAuthorities",
        attributeNodes = {
            @NamedAttributeNode("userAuthorities"),
            @NamedAttributeNode("userOrganizationAuthorities")
        }
)
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

    @Column(name = "avatarUrl")
    private String avatarUrl;

    @Column(name = "enabled")
    @NotNull
    private Boolean enabled = false;

    @Column(name = "lastpasswordresetdate")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date lastPasswordResetDate = new Date();

    @ManyToMany
    @JoinTable(
            name = "user_authority",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"users"})
    private Set<Authority> userAuthorities = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_authority_organizations",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "organization_authority_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"organization", "users"})
    private Set<OrganizationAuthority> userOrganizationAuthorities = new LinkedHashSet<>();

    @Transient
    private Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "members")
    @JsonIgnoreProperties({"members", "projects", "budgets", "contents", "organizationAuthorities", "slackTeam"})
    private Set<Organization> organizations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "sponsor")
    @JsonIgnoreProperties(value = {"organization", "projects", "sponsor", "donations"})
    private Set<Budget> budgets = new LinkedHashSet<>();

    @OneToMany(mappedBy = "leader")
    @JsonIgnoreProperties(value = {"leader", "budgets", "donations", "peopleGivingTime", "organizations"})
    private Set<Project> projects = new LinkedHashSet<>();

    @OneToMany(mappedBy = "contributor")
    @JsonIgnoreProperties(value = {"budget"})
    private Set<Donation> donations = new LinkedHashSet<>();

    private Float totalBudgetDonations;

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties(value = {"organization", "slackTeam", "user"})
    private Set<SlackUser> slackUsers = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "user",
            orphanRemoval = true)
    @JsonIgnoreProperties(value = {"user"})
    private Set<AuthenticationResponse> apiTokens = new LinkedHashSet<>();

    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
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

}
