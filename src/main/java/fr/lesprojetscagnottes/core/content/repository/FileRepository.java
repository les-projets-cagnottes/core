package fr.lesprojetscagnottes.core.content.repository;

import fr.lesprojetscagnottes.core.content.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findByFilename(String id);
}
