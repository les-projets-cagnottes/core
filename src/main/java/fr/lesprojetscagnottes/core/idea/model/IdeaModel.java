package fr.lesprojetscagnottes.core.idea.model;

import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.common.strings.StringsCommon;
import fr.lesprojetscagnottes.core.idea.entity.IdeaEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class IdeaModel extends GenericModel {

    /*
    This class is not inherited from AuditEntity in order to make createdBy attribute anonymous if desired
     */

    private static final Logger LOGGER = LoggerFactory.getLogger(IdeaModel.class);

    @Column(name = "created_at", updatable = false, columnDefinition = "timestamp default now()")
    private Date createdAt;

    @Column(name = "created_by", updatable = false, columnDefinition = "varchar(255) default 'System'")
    private String createdBy;

    @Column(name = "updated_at", columnDefinition = "timestamp default now()")
    private Date updatedAt;

    @Column(name = "updated_by", columnDefinition = "varchar(255) default 'System'")
    private String updatedBy;

    @Column(name = "icon")
    protected String icon = StringsCommon.EMPTY_STRING;

    @Column(name = "short_description")
    protected String shortDescription = StringsCommon.EMPTY_STRING;

    @Column(name = "long_description", columnDefinition = "TEXT")
    protected String longDescription = StringsCommon.EMPTY_STRING;

    @Column(name = "has_anonymous_creator")
    @NotNull
    protected Boolean hasAnonymousCreator = false;

    @Column(name = "has_leader_creator")
    @NotNull
    protected Boolean hasLeaderCreator = false;

    @Column
    protected String workspace = StringsCommon.EMPTY_STRING;

    @Transient
    protected GenericModel submitter;

    @Transient
    protected GenericModel organization;

    public static IdeaModel fromEntity(IdeaEntity entity) {
        IdeaModel model = new IdeaModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        if (entity.getHasAnonymousCreator()) {
            model.setCreatedBy(StringsCommon.ANONYMOUS);
        } else {
            model.setCreatedBy(entity.getCreatedBy());
        }
        model.setIcon(entity.getIcon());
        model.setShortDescription(entity.getShortDescription());
        model.setLongDescription(entity.getLongDescription());
        model.setHasAnonymousCreator(entity.getHasAnonymousCreator());
        model.setHasLeaderCreator(entity.getHasLeaderCreator());
        model.setWorkspace(entity.getWorkspace());
        model.setSubmitter(new GenericModel(entity.getSubmitter()));
        model.setOrganization(new GenericModel(entity.getOrganization()));
        LOGGER.debug("Generated : " + model);
        return model;
    }

    @Override
    public String toString() {
        return "IdeaModel{" + "shortDescription='" + shortDescription + '\'' +
                ", longDescription='" + longDescription + '\'' +
                ", hasAnonymousCreator=" + hasAnonymousCreator +
                ", hasLeaderCreator=" + hasLeaderCreator +
                ", organization=" + organization +
                ", id=" + id +
                '}';
    }
}
