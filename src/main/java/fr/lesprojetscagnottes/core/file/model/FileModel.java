package fr.lesprojetscagnottes.core.file.model;

import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.file.entity.FileEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@MappedSuperclass
public class FileModel extends AuditEntity<String> {

    @NotNull
    protected String name = "";

    @NotNull
    protected String directory = "";

    @NotNull
    protected String format = "";

    @NotNull
    protected String url = "";

    public static FileModel fromEntity(FileEntity entity) {
        FileModel model = new FileModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setDirectory(entity.getDirectory());
        model.setFormat(entity.getFormat());
        model.setUrl(entity.getUrl());
        return model;
    }

    public String getFullname() {
        return name + "." + format;
    }

}
