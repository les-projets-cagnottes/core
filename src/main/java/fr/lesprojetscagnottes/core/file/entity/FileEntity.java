package fr.lesprojetscagnottes.core.file.entity;

import fr.lesprojetscagnottes.core.file.model.FileModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "files")
public class FileEntity extends FileModel {

}
