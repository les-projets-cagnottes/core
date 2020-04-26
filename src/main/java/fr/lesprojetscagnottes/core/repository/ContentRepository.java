package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface ContentRepository extends JpaRepository<Content, Long> {
    Page<Content> findAll(Pageable pageable);
    Page<Content> findAllByOrganizations_Id(Pageable pageable, Long organizationId);
    Set<Content> findAllByOrganizations_Id(Long organizationId);
}