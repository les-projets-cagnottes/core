package fr.lesprojetscagnottes.core.common.audit;

import fr.lesprojetscagnottes.core.common.GenericModel;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.util.Date;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@SuperBuilder
public class AuditEntity<U> extends GenericModel {

    @CreatedDate
    @Column(name = "created_at", updatable = false, columnDefinition = "timestamp default now()")
    private Date createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, columnDefinition = "varchar(255) default 'System'")
    private U createdBy;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "timestamp default now()")
    private Date updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", columnDefinition = "varchar(255) default 'System'")
    private U updatedBy;

    public AuditEntity(Date createdAt, U createdBy, Date updatedAt, U updatedBy) {
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }
}

