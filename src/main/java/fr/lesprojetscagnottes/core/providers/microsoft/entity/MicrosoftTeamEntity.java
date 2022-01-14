package fr.lesprojetscagnottes.core.providers.microsoft.entity;

import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.model.MicrosoftTeamModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "slack_team")
public class MicrosoftTeamEntity extends MicrosoftTeamModel {

    @OneToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private OrganizationEntity organization = new OrganizationEntity();

}
