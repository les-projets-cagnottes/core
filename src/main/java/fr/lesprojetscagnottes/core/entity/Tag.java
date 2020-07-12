package fr.lesprojetscagnottes.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lesprojetscagnottes.core.model.TagModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "donations")
public class Tag extends TagModel {

    @ManyToOne
    @JsonIgnoreProperties({"name", "members", "campaigns", "budgets", "contents", "organizationAuthorities", "slackTeam"})
    private Organization organization = new Organization();

    @ManyToMany(mappedBy = "tags")
    @JsonIgnoreProperties({"submitter", "organization", "followers", "tags"})
    private Set<Idea> ideas = new LinkedHashSet<>();

}
