package fr.lesprojetscagnottes.core.providers.slack.entity;

import fr.lesprojetscagnottes.core.providers.slack.model.SlackUserModel;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import jakarta.persistence.FetchType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "slack_user")
public class SlackUserEntity extends SlackUserModel {

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user = new UserEntity();

    @ManyToOne(fetch = FetchType.LAZY)
    private SlackTeamEntity slackTeam;

}
