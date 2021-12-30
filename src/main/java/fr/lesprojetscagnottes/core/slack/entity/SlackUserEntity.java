package fr.lesprojetscagnottes.core.slack.entity;

import fr.lesprojetscagnottes.core.slack.model.SlackUserModel;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
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
    private UserEntity user = new UserEntity();

    @ManyToOne
    private SlackTeamEntity slackTeam;

}
