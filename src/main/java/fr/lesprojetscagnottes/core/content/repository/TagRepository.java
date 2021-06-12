package fr.lesprojetscagnottes.core.content.repository;

import fr.lesprojetscagnottes.core.content.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface TagRepository extends JpaRepository<TagEntity, Long> {

    Set<TagEntity> findAllByIdIn(Set<Long> id);
}