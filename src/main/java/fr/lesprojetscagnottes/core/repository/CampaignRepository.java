package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.Campaign;
import fr.lesprojetscagnottes.core.entity.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Set;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Page<Campaign> findAll(Pageable pageable);

    Page<Campaign> findAllByStatusInOrderByStatusAscFundingDeadlineAsc(Set<CampaignStatus> status, Pageable pageable);

    Set<Campaign> findAllByLeaderId(Long memberId);

    Set<Campaign> findAllByPeopleGivingTime_Id(Long memberId);

    Set<Campaign> findAllByStatusAndFundingDeadlineLessThan(CampaignStatus inProgress, Date date);

    Page<Campaign> findByBudgets_idOrderByIdDesc(Long id, Pageable pageable);

    Set<Campaign> findAllByStatus(CampaignStatus campaignStatus);

    Set<Campaign> findAllByBudgets_Id(Long id);

    @Query(nativeQuery = true, value = "select p.* from projects p " +
            "inner join project_organizations on p.id = project_organizations.project_id " +
            "inner join organizations o on project_organizations.organization_id = o.id " +
            "inner join organizations_users on organizations_users.organization_id = o.id " +
            "inner join users u on u.id = organizations_users.user_id " +
            "where u.id = :user_id and p.id = :project_id")
    Set<Campaign> findAllProjectsByUserInOrganizations(@Param("user_id") Long userId, @Param("project_id") long projectId);
}