package fr.thomah.valyou.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "donations")
public class Donation extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "amount")
    @NotNull
    private Float amount;

    @ManyToOne
    private User contributor;

    @ManyToOne
    private Project project;

}
