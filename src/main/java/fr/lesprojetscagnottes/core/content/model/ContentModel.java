package fr.lesprojetscagnottes.core.content.model;

import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.content.entity.ContentEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class ContentModel extends AuditEntity<String> {

    @Column(name = "name")
    protected String name;

    @Column(name = "value", columnDefinition = "TEXT")
    protected String value;

    public static ContentModel fromEntity(ContentEntity entity) {
        ContentModel model = new ContentModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setValue(entity.getValue());
        return model;
    }

}
