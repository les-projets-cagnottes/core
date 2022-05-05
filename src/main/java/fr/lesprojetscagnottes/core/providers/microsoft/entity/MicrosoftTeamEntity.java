package fr.lesprojetscagnottes.core.providers.microsoft.entity;

import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.model.MicrosoftTeamModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "ms_team")
public class MicrosoftTeamEntity extends MicrosoftTeamModel {

    @OneToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private OrganizationEntity organization = new OrganizationEntity();

    @OneToMany(mappedBy = "msTeam", cascade = CascadeType.REMOVE)
    private Set<MicrosoftUserEntity> msUsers = new LinkedHashSet<>();

}
