package fr.thomah.valyou.repository;

import fr.thomah.valyou.model.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    @EntityGraph(value = "Organization.withMembers")
    List<Organization> findAll();

    Page<Organization> findAll(Pageable pageable);

    @EntityGraph(value = "Organization.withMembers")
    Optional<Organization> findById(Long id);

    Set<Organization> findByMembers_Id(Long userId);

    @EntityGraph(value = "Organization.withMembers")
    Optional<Organization> findByIdAndMembers_Id(Long id, Long userId);

    Organization findBySlackTeam_Id(Long slackTeamId);

    Organization findByBudgets_id(long budgetId);
}
