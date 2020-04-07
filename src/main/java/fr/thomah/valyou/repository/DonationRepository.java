package fr.thomah.valyou.repository;

import fr.thomah.valyou.entity.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    Page<Donation> findAll(Pageable pageable);
    Set<Donation> findAllByBudgetId(long budgetId);
    Set<Donation> findAllByProjectId(long projectId);
    Set<Donation> findAllByContributorIdOrderByBudgetIdAsc(long contributorId);
    Set<Donation> findAllByContributorIdAndBudgetId(long contributorId, long budgetId);
    void deleteByProjectId(Long id);
    Page<Donation> findByProject_idOrderByIdAsc(long id, Pageable pageable);

    @Query(value = "select d.* from donations d inner join projects p on p.id = d.project_id " +
            "inner join project_organizations on p.id = project_organizations.project_id " +
            "inner join organizations o on project_organizations.organization_id = o.id " +
            "inner join organizations_users on organizations_users.organization_id = o.id " +
            "inner join users u on u.id = organizations_users.user_id  " +
            "where u.id = :user_id and p.id = :project_id group by d.id",
            nativeQuery = true)
    Set<Donation> getDonationsByUserIdAndProjectId(@Param("user_id") long userId, @Param("project_id") long projectId);

}