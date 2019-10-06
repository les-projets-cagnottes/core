package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, Long> {
    Page<Content> findAll(Pageable pageable);
    Page<Content> findAllByOrganizations_Id(Pageable pageable, Long organizationId);
}