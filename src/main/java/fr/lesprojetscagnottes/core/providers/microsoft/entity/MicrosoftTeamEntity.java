package fr.lesprojetscagnottes.core.providers.microsoft.entity;

import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.model.MicrosoftTeamModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "ms_team")
public class MicrosoftTeamEntity extends MicrosoftTeamModel {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private OrganizationEntity organization = new OrganizationEntity();

    @OneToMany(mappedBy = "msTeam", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Set<MicrosoftUserEntity> msUsers = new LinkedHashSet<>();

}
