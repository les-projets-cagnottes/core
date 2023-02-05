package fr.lesprojetscagnottes.core.news.entity;

import fr.lesprojetscagnottes.core.news.model.NewsModel;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.project.entity.ProjectEntity;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "news")
public class NewsEntity extends NewsModel {

    @ManyToOne
    private UserEntity author = new UserEntity();

    @ManyToOne
    private OrganizationEntity organization = new OrganizationEntity();

    @ManyToOne
    private ProjectEntity project = new ProjectEntity();

}
