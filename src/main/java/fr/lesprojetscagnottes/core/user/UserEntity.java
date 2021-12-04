package fr.lesprojetscagnottes.core.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.authentication.AuthenticationResponseEntity;
import fr.lesprojetscagnottes.core.authorization.entity.AuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.entity.OrganizationAuthorityEntity;
import fr.lesprojetscagnottes.core.budget.entity.AccountEntity;
import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.campaign.CampaignEntity;
import fr.lesprojetscagnottes.core.donation.entity.Donation;
import fr.lesprojetscagnottes.core.idea.IdeaEntity;
import fr.lesprojetscagnottes.core.news.entity.NewsEntity;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import fr.lesprojetscagnottes.core.project.ProjectEntity;
import fr.lesprojetscagnottes.core.slack.entity.SlackUserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serial;
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
public class UserEntity extends UserModel implements UserDetails {

    @Serial
    private static final long serialVersionUID = 6210782306288115135L;

    @ManyToMany
    @JoinTable(
            name = "user_authority",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"createdAt", "createdBy", "updatedAt", "updatedBy", "users"})
    private Set<AuthorityEntity> userAuthorities = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_authority_organizations",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "organization_authority_id", referencedColumnName = "id")})
    @JsonIgnoreProperties(value = {"organization", "users"})
    private Set<OrganizationAuthorityEntity> userOrganizationAuthorities = new LinkedHashSet<>();

    @Transient
    private Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();

    @OneToMany(mappedBy = "owner")
    @JsonIgnoreProperties(value = {"owner", "budget"})
    private Set<AccountEntity> accounts = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "members")
    @JsonIgnoreProperties({"name", "members", "campaigns", "budgets", "contents", "organizationAuthorities", "slackTeam"})
    private Set<OrganizationEntity> organizations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "sponsor")
    @JsonIgnoreProperties(value = {"organization", "campaigns", "sponsor", "donations", "accounts"})
    private Set<BudgetEntity> budgets = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "peopleGivingTime")
    @JsonIgnoreProperties(value = {"leader", "campaigns", "peopleGivingTime", "organizations", "news"})
    private Set<ProjectEntity> projects = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "peopleGivingTime")
    @JsonIgnoreProperties(value = {"leader", "budgets", "donations", "peopleGivingTime", "organizations"})
    private Set<CampaignEntity> campaigns = new LinkedHashSet<>();

    @OneToMany(mappedBy = "contributor")
    @JsonIgnoreProperties(value = {"contributor", "campaign", "budget", "account"})
    private Set<Donation> donations = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "submitter",
            orphanRemoval = true)
    @JsonIgnoreProperties({"submitter", "organization", "followers", "tags"})
    private Set<IdeaEntity> ideas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "followers")
    @JsonIgnoreProperties({"submitter", "organization", "followers", "tags"})
    private Set<IdeaEntity> followedIdeas = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "user")
    @JsonIgnoreProperties(value = {"organization", "slackTeam", "user"})
    private Set<SlackUserEntity> slackUsers = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "user",
            orphanRemoval = true)
    @JsonIgnoreProperties(value = {"user"})
    private Set<AuthenticationResponseEntity> apiTokens = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "organization",
            orphanRemoval = true)
    @JsonIgnoreProperties({"author", "organization", "project"})
    private Set<NewsEntity> news = new LinkedHashSet<>();

    public UserEntity() {
    }

    public UserEntity(String email, String password) {
        this.email = email;
        this.password = password;
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

    public void addAuthority(AuthorityEntity authority) {
        userAuthorities.add(authority);
    }

}
