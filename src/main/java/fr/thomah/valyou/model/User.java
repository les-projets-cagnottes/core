package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends AuditEntity implements UserDetails {

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
    private List<Authority> userAuthorities = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_authority_organizations",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "organization_authority_id", referencedColumnName = "id")})
    @JsonIgnoreProperties({"organization", "userAuthorities"})
    private List<OrganizationAuthority> userOrganizationAuthorities = new ArrayList<>();

    @Transient
    private Collection<GrantedAuthority> authorities = new ArrayList<>();

    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    private List<Organization> organizations = new ArrayList<>();

    @OneToMany(mappedBy = "sponsor")
    @JsonIgnoreProperties({"organization", "sponsor", "donations"})
    private List<Budget> budgets = new ArrayList<>();

    @OneToMany(mappedBy = "leader")
    @JsonIgnoreProperties({"leader", "donations", "peopleGivingTime", "organizations"})
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "contributor")
    @JsonIgnoreProperties({"budget"})
    private List<Donation> donations = new ArrayList<>();

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
                List<Organization> organizations,
                List<Budget> budgets,
                List<Project> projects,
                List<Donation> donations) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getAvatarUrl() {
        if (avatarUrl == null || avatarUrl.equals("")) {
            return "https://ui-avatars.com/api/?name=" + firstname + "+" + lastname + "&background=" + color;
        }
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Date getLastPasswordResetDate() {
        return lastPasswordResetDate;
    }

    public void setLastPasswordResetDate(Date lastPasswordResetDate) {
        this.lastPasswordResetDate = lastPasswordResetDate;
    }

    public List<Authority> getUserAuthorities() {
        return userAuthorities;
    }

    public void setUserAuthorities(List<Authority> userAuthorities) {
        this.userAuthorities = userAuthorities;
    }

    public List<OrganizationAuthority> getUserOrganizationAuthorities() {
        return userOrganizationAuthorities;
    }

    public void setUserOrganizationAuthorities(List<OrganizationAuthority> userOrganizationAuthorities) {
        this.userOrganizationAuthorities = userOrganizationAuthorities;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Collection<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public List<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<Organization> organizations) {
        this.organizations = organizations;
    }

    public List<Budget> getBudgets() {
        return budgets;
    }

    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<Donation> getDonations() {
        return donations;
    }

    public void setDonations(List<Donation> donations) {
        this.donations = donations;
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
