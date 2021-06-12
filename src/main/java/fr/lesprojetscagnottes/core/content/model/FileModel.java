package fr.lesprojetscagnottes.core.content.model;

import fr.lesprojetscagnottes.core.common.audit.AuditEntity;
import fr.lesprojetscagnottes.core.content.entity.FileEntity;
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
    protected String filename;

    @NotNull
    protected String directory = "";

    @NotNull
    protected String originalName = "";

    @NotNull
    protected String extension = "";

    @NotNull
    protected String path = "";

    public static FileModel fromEntity(FileEntity entity) {
        FileModel model = new FileModel();
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setId(entity.getId());
        model.setFilename(entity.getFilename());
        model.setDirectory(entity.getDirectory());
        model.setOriginalName(entity.getOriginalName());
        model.setExtension(entity.getExtension());
        model.setPath(entity.getPath());
        return model;
    }

}
