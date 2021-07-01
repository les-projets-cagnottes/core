package fr.lesprojetscagnottes.core.user;

import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.common.strings.StringsCommon;
import fr.lesprojetscagnottes.core.common.strings.Constants;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class UserModel extends AuditEntity<String> {

    @Column(name = "username")
    protected String username;

    @Column(name = "password")
    @NotNull
    protected String password;

    @Column(name = "email", unique = true)
    @NotNull
    protected String email;

    @Column(name = "firstname")
    protected String firstname = StringsCommon.EMPTY_STRING;

    @Column(name = "lastname")
    protected String lastname = StringsCommon.EMPTY_STRING;

    @Column(name = "avatarUrl")
    protected String avatarUrl;

    @Column(name = "enabled")
    @NotNull
    protected Boolean enabled = false;

    @Column(name = "lastpasswordresetdate")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    protected Date lastPasswordResetDate = new Date();

    @Transient
    protected Set<Long> userAuthoritiesRef = new LinkedHashSet<>();

    @Transient
    protected Set<Long> userOrganizationAuthoritiesRef = new LinkedHashSet<>();

    @Transient
    protected Set<Long> accountsRef = new LinkedHashSet<>();

    @Transient
    protected Set<Long> organizationsRef = new LinkedHashSet<>();

    @Transient
    protected Set<Long> budgetsRef = new LinkedHashSet<>();

    @Transient
    protected Set<Long> projectsRef = new LinkedHashSet<>();

    @Transient
    protected Set<Long> donationsRef = new LinkedHashSet<>();

    @Transient
    protected Set<Long> ideasRef = new LinkedHashSet<>();

    @Transient
    protected Set<Long> followedIdeasRef = new LinkedHashSet<>();

    @Transient
    protected  Set<Long> slackUsersRef = new LinkedHashSet<>();

    @Transient
    protected  Set<Long> apiTokensRef = new LinkedHashSet<>();

    public static UserModel fromEntity(UserEntity entity) {
        UserModel model = new UserModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setUsername(entity.getUsername());
        model.setEmail(entity.getEmail());
        model.setFirstname(entity.getFirstname());
        model.setLastname(entity.getLastname());
        model.setAvatarUrl(entity.getAvatarUrl());
        model.setEnabled(entity.getEnabled());
        model.setLastPasswordResetDate(entity.getLastPasswordResetDate());
        entity.getUserAuthorities().forEach(item -> model.getUserAuthoritiesRef().add(item.getId()));
        entity.getUserOrganizationAuthorities().forEach(item -> model.getUserOrganizationAuthoritiesRef().add(item.getId()));
        entity.getAccounts().forEach(item -> model.getAccountsRef().add(item.getId()));
        entity.getOrganizations().forEach(item -> model.getAccountsRef().add(item.getId()));
        entity.getBudgets().forEach(item -> model.getBudgetsRef().add(item.getId()));
        entity.getProjects().forEach(item -> model.getProjectsRef().add(item.getId()));
        entity.getDonations().forEach(item -> model.getDonationsRef().add(item.getId()));
        entity.getIdeas().forEach(item -> model.getIdeasRef().add(item.getId()));
        entity.getFollowedIdeas().forEach(item -> model.getFollowedIdeasRef().add(item.getId()));
        entity.getSlackUsers().forEach(item -> model.getSlackUsersRef().add(item.getId()));
        entity.getApiTokens().forEach(item -> model.getApiTokensRef().add(item.getId()));
        return model;
    }

    public String getFullname() {
        return this.firstname + Constants.SPACE + this.lastname;
    }

    public void emptyPassword() {
        this.password = "***";
    }

}
