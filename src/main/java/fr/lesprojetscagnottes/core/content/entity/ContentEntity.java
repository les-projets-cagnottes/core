package fr.lesprojetscagnottes.core.content.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.content.model.ContentModel;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "contents")
public class ContentEntity extends ContentModel {

    @ManyToOne
    @JsonIgnoreProperties({"name", "members", "campaigns", "budgets", "contents", "organizationAuthorities", "slackTeam"})
    private OrganizationEntity organization = new OrganizationEntity();


}
