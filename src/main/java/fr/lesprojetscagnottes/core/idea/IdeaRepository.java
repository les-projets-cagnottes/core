package fr.lesprojetscagnottes.core.idea;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdeaRepository extends JpaRepository<IdeaEntity, Long> {
    Page<IdeaEntity> findByOrganizationId(Long organizationId, Pageable pageable);
}