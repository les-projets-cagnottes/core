package fr.lesprojetscagnottes.core.providers.microsoft.entity;

import fr.lesprojetscagnottes.core.providers.microsoft.model.MicrosoftUserModel;
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
@Table(name = "ms_user")
public class MicrosoftUserEntity extends MicrosoftUserModel {

    @ManyToOne
    private UserEntity user = new UserEntity();

    @ManyToOne
    private MicrosoftTeamEntity msTeam;

}
