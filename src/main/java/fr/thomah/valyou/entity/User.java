package fr.thomah.valyou.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.thomah.valyou.entity.model.UserModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

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
public class User extends UserModel implements UserDetails {

    private static final long serialVersionUID = 6210782306288115135L;

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

}
