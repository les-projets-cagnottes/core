package fr.lesprojetscagnottes.core.content.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.content.model.ContentModel;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "contents")
public class ContentEntity extends ContentModel {

    @ManyToMany(mappedBy = "contents", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"members", "campaigns", "budgets", "contents"})
    private Set<OrganizationEntity> organizations = new LinkedHashSet<>();

}
