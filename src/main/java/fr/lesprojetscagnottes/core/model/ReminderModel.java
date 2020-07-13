package fr.lesprojetscagnottes.core.model;

import fr.lesprojetscagnottes.core.audit.AuditEntity;
import fr.lesprojetscagnottes.core.common.StringsCommon;
import fr.lesprojetscagnottes.core.entity.Reminder;
import fr.lesprojetscagnottes.core.entity.ReminderName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class ReminderModel extends AuditEntity<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderModel.class);

    @Column(name = "name")
    protected ReminderName name = ReminderName.IDEA;

    @Column(name = "planning")
    protected String planning = StringsCommon.EMPTY_STRING;

    @Column(name = "enabled")
    @NotNull
    protected Boolean enabled = false;

    public static ReminderModel fromEntity(Reminder entity) {
        ReminderModel model = new ReminderModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setPlanning(entity.getPlanning());
        model.setEnabled(entity.getEnabled());
        LOGGER.debug("Generated : " + model.toString());
        return model;
    }

}
