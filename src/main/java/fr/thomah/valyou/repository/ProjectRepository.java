package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findAll(Pageable pageable);
    Page<Project> findByOrganizations_Id(Pageable pageable, Long orgId);
}