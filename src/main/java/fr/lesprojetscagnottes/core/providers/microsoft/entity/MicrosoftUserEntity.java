package fr.lesprojetscagnottes.core.providers.microsoft.entity;

import fr.lesprojetscagnottes.core.providers.microsoft.model.MicrosoftUserModel;
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
@Table(name = "ms_user")
public class MicrosoftUserEntity extends MicrosoftUserModel {

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user = null;

    @ManyToOne(fetch = FetchType.LAZY)
    private MicrosoftTeamEntity msTeam = null;

}
