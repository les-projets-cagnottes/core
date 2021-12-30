package fr.lesprojetscagnottes.core.project.entity;

import fr.lesprojetscagnottes.core.campaign.entity.CampaignEntity;
import fr.lesprojetscagnottes.core.news.entity.NewsEntity;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.project.model.ProjectModel;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "projects")
public class ProjectEntity extends ProjectModel {

    @ManyToOne
    private UserEntity leader = new UserEntity();

    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE)
    private Set<CampaignEntity> campaigns = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "projects_members",
            joinColumns = {@JoinColumn(name = "project_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    private Set<UserEntity> peopleGivingTime = new LinkedHashSet<>();

    @ManyToOne
    private OrganizationEntity organization = new OrganizationEntity();

    @OneToMany(
            mappedBy = "project",
            orphanRemoval = true)
    private Set<NewsEntity> news = new LinkedHashSet<>();

}
