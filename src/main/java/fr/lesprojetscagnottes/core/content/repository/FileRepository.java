package fr.lesprojetscagnottes.core.content.repository;

import fr.lesprojetscagnottes.core.content.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

}
