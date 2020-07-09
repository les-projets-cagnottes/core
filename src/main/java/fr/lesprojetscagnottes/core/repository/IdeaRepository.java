package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.Idea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdeaRepository extends JpaRepository<Idea, Long> {
    Page<Idea> findByOrganizationId(Long organizationId, Pageable pageable);
}