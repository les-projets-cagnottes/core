package fr.lesprojetscagnottes.core.slack.model;

import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.slack.entity.SlackUserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class SlackUserModel extends AuditEntity<String> {

    @Column
    private String slackId;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private String imId;

    @Column
    private String image_192;

    @Column
    private Boolean deleted;

    @Column(name = "is_restricted")
    private Boolean isRestricted;

    @Transient
    protected GenericModel user;

    @Transient
    protected GenericModel slackTeam;

    public static SlackUserModel fromEntity(SlackUserEntity entity) {
        SlackUserModel model = new SlackUserModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setSlackId(entity.getSlackId());
        model.setName(entity.getName());
        model.setEmail(entity.getEmail());
        model.setImId(entity.getImId());
        model.setImage_192(entity.getImage_192());
        model.setDeleted(entity.getDeleted());
        model.setIsRestricted(entity.getIsRestricted());
        model.setUser(new GenericModel(entity.getUser()));
        model.setSlackTeam(new GenericModel(entity.getSlackTeam()));
        return model;
    }

}
