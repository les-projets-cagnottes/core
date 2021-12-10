package fr.lesprojetscagnottes.core.file.repository;

import fr.lesprojetscagnottes.core.file.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

}
