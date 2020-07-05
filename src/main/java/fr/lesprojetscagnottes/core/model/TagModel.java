package fr.lesprojetscagnottes.core.model;

import fr.lesprojetscagnottes.core.audit.AuditEntity;
import fr.lesprojetscagnottes.core.common.StringsCommon;
import fr.lesprojetscagnottes.core.entity.Tag;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class TagModel extends AuditEntity<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TagModel.class);

    @Column(name = "name")
    protected String name = StringsCommon.EMPTY_STRING;

    @Transient
    protected GenericModel organization;

    public static TagModel fromEntity(Tag entity) {
        TagModel model = new TagModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setOrganization(new GenericModel(entity.getOrganization()));
        LOGGER.debug("Generated : " + model.toString());
        return model;
    }

}
