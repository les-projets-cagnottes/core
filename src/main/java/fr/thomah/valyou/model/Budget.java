package fr.thomah.valyou.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "budgets")
public class Budget extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "amount_per_member")
    @NotNull
    private Float amountPerMember;

    @ManyToOne(cascade = CascadeType.ALL)
    private Organization organization;

    @ManyToOne(cascade = CascadeType.ALL)
    private User sponsor;

}
