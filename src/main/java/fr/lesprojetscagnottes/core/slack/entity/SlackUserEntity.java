package fr.lesprojetscagnottes.core.slack.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.user.UserEntity;
import fr.lesprojetscagnottes.core.slack.model.SlackUserModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "slack_user")
public class SlackUserEntity extends SlackUserModel {

    @ManyToOne
    @JsonIgnoreProperties(value = {"username", "password", "lastPasswordResetDate", "userAuthorities", "userOrganizationAuthorities", "authorities", "organizations", "budgets", "projects", "campaigns", "donations", "slackUsers", "apiTokens", "accounts"})
    private UserEntity user = new UserEntity();

    @ManyToOne
    @JsonIgnoreProperties({"organization", "slackUsers"})
    private SlackTeamEntity slackTeam;

}
