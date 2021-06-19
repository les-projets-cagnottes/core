package fr.lesprojetscagnottes.core.news.model;

import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.news.entity.NewsEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class NewsModel extends AuditEntity<String> {

    @Column
    protected String title;

    @Column(columnDefinition = "TEXT")
    protected String content;

    @Transient
    protected GenericModel author;

    @Transient
    protected GenericModel organization;

    @Transient
    protected GenericModel project;

    public static NewsModel fromEntity(NewsEntity entity) {
        NewsModel model = new NewsModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setTitle(entity.getTitle());
        model.setContent(entity.getContent());
        model.setAuthor(new GenericModel(entity.getAuthor()));
        model.setOrganization(new GenericModel(entity.getOrganization()));
        model.setProject(new GenericModel(entity.getProject()));
        return model;
    }

}
