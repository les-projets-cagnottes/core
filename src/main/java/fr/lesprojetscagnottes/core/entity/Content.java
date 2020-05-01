package fr.lesprojetscagnottes.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.entity.model.ContentModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "contents")
public class Content extends ContentModel {

    @ManyToMany(mappedBy = "contents", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"members", "campaigns", "budgets", "contents"})
    private Set<Organization> organizations = new LinkedHashSet<>();

}
