package fr.lesprojetscagnottes.core.providers.microsoft.model;

import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.common.strings.StringsCommon;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class MicrosoftUserModel extends AuditEntity<String> {

    @Column
    private String msId = StringsCommon.EMPTY_STRING;

    @Column
    private String mail = StringsCommon.EMPTY_STRING;

    @Column
    private String givenName = StringsCommon.EMPTY_STRING;

    @Column
    private String surname = StringsCommon.EMPTY_STRING;

    @Column
    private String companyName = StringsCommon.EMPTY_STRING;

    @Transient
    protected GenericModel user;

    @Transient
    protected GenericModel msTeam;

    public static MicrosoftUserModel fromEntity(MicrosoftUserModel entity) {
        MicrosoftUserModel model = new MicrosoftUserModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setMsId(entity.getMsId());
        model.setMail(entity.getMail());
        model.setGivenName(entity.getGivenName());
        model.setSurname(entity.getSurname());
        model.setCompanyName(entity.getCompanyName());
        model.setUser(new GenericModel(entity.getUser()));
        model.setMsTeam(new GenericModel(entity.getMsTeam()));
        return model;
    }

}
