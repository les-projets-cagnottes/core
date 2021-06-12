package fr.lesprojetscagnottes.core.schedule;

import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.common.strings.StringsCommon;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronSequenceGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class ScheduleModel extends AuditEntity<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleModel.class);

    @Column(name = "type")
    @NotNull
    @Enumerated(EnumType.STRING)
    protected ScheduleName type = ScheduleName.REMINDER;

    @Column(name = "params")
    protected String params = StringsCommon.EMPTY_STRING;

    @Column(name = "planning")
    @NotNull
    protected String planning = StringsCommon.EMPTY_STRING;

    @Column(name = "enabled")
    @NotNull
    protected Boolean enabled = false;

    @Transient
    protected Date nextExecution = new Date();

    public static ScheduleModel fromEntity(ScheduleEntity entity) {
        ScheduleModel model = new ScheduleModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setType(entity.getType());
        model.setPlanning(entity.getPlanning());
        model.setEnabled(entity.getEnabled());
        CronSequenceGenerator cronTrigger = new CronSequenceGenerator(entity.getPlanning());
        model.setNextExecution(cronTrigger.next(new Date()));
        LOGGER.debug("Generated : " + model.toString());
        return model;
    }

}
