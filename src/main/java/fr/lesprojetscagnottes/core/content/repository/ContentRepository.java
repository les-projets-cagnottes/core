package fr.lesprojetscagnottes.core.content.repository;

import fr.lesprojetscagnottes.core.content.entity.ContentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface ContentRepository extends JpaRepository<ContentEntity, Long> {
    Page<ContentEntity> findAll(Pageable pageable);
    Page<ContentEntity> findAllByOrganizationId(Pageable pageable, Long organizationId);
    Set<ContentEntity> findAllByOrganizationId(Long organizationId);
}