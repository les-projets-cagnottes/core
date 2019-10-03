package fr.thomah.valyou.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class Content extends AuditEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    @ManyToMany(mappedBy = "contents", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"members", "projects", "budgets", "contents"})
    private Set<Organization> organizations = new LinkedHashSet<>();

}
