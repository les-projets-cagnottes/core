package fr.lesprojetscagnottes.core.slack.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.slack.model.SlackTeamModel;
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
    @JsonIgnoreProperties(value = {"leader", "peopleGivingTime", "organizations", "news"})
    private OrganizationEntity organization = new OrganizationEntity();

    @OneToMany(mappedBy = "slackTeam", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnoreProperties(value = {"organization", "slackTeam", "user"})
    private Set<SlackUserEntity> slackUsers = new LinkedHashSet<>();

}
