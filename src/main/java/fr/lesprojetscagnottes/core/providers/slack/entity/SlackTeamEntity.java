package fr.lesprojetscagnottes.core.providers.slack.entity;

import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.providers.slack.model.SlackTeamModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "slack_team")
public class SlackTeamEntity extends SlackTeamModel {

    @OneToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private OrganizationEntity organization = new OrganizationEntity();

    @OneToMany(mappedBy = "slackTeam", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<SlackUserEntity> slackUsers = new LinkedHashSet<>();

}
