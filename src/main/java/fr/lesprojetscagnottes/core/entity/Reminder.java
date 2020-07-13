package fr.lesprojetscagnottes.core.entity;

import fr.lesprojetscagnottes.core.model.ReminderModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "ideas")
public class Reminder extends ReminderModel {

}
