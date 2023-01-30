package fr.lesprojetscagnottes.core.content.model;

import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.content.entity.ContentEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class ContentModel extends AuditEntity<String> {

    @Column(name = "name")
    protected String name;

    @Column(name = "value", columnDefinition = "TEXT")
    protected String value;

    @Transient
    private GenericModel organization = new GenericModel();

    public static ContentModel fromEntity(ContentEntity entity) {
        ContentModel model = new ContentModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setValue(entity.getValue());
        model.setOrganization(new GenericModel(entity.getOrganization()));
        return model;
    }

}
