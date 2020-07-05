package fr.lesprojetscagnottes.core.model;

import fr.lesprojetscagnottes.core.audit.AuditEntity;
import fr.lesprojetscagnottes.core.common.StringsCommon;
import fr.lesprojetscagnottes.core.entity.Idea;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class IdeaModel extends AuditEntity<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdeaModel.class);

    @Column(name = "short_description")
    protected String shortDescription = StringsCommon.EMPTY_STRING;

    @Column(name = "long_description", columnDefinition = "TEXT")
    protected String longDescription = StringsCommon.EMPTY_STRING;

    @Transient
    protected GenericModel organization;

    @Transient
    private Set<Long> tagsRef = new LinkedHashSet<>();

    public static IdeaModel fromEntity(Idea entity) {
        IdeaModel model = new IdeaModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setShortDescription(entity.getShortDescription());
        model.setLongDescription(entity.getLongDescription());
        model.setOrganization(new GenericModel(entity.getOrganization()));
        entity.getTags().forEach(tag -> model.getTagsRef().add(tag.getId()));
        LOGGER.debug("Generated : " + model.toString());
        return model;
    }

}
