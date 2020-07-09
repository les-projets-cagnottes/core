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
import javax.validation.constraints.NotNull;
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

    @Column(name = "has_anonymous_creator")
    @NotNull
    protected Boolean hasAnonymousCreator = false;

    @Column(name = "has_leader_creator")
    @NotNull
    protected Boolean hasLeaderCreator = false;

    @Transient
    protected GenericModel organization;

    @Transient
    private Set<Long> tagsRef = new LinkedHashSet<>();

    public static IdeaModel fromEntity(Idea entity) {
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
        model.setShortDescription(entity.getShortDescription());
        model.setLongDescription(entity.getLongDescription());
        model.setOrganization(new GenericModel(entity.getOrganization()));
        entity.getTags().forEach(tag -> model.getTagsRef().add(tag.getId()));
        LOGGER.debug("Generated : " + model.toString());
        return model;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IdeaModel{");
        sb.append("shortDescription='").append(shortDescription).append('\'');
        sb.append(", longDescription='").append(longDescription).append('\'');
        sb.append(", hasAnonymousCreator=").append(hasAnonymousCreator);
        sb.append(", hasLeaderCreator=").append(hasLeaderCreator);
        sb.append(", organization=").append(organization);
        sb.append(", tagsRef=").append(tagsRef);
        sb.append(", id=").append(id);
        sb.append('}');
        return sb.toString();
    }
}
