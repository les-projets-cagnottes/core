package fr.lesprojetscagnottes.core.file.repository;

import fr.lesprojetscagnottes.core.file.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findByUrl(String url);
}
