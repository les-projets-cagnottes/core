package fr.lesprojetscagnottes.core.entity;

import fr.lesprojetscagnottes.core.model.ScheduleModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "schedules")
public class Schedule extends ScheduleModel {

}
