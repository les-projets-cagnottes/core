package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Set<Tag> findAllByIdIn(Set<Long> id);
}