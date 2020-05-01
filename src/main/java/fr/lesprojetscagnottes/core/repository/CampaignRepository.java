package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.Campaign;
import fr.lesprojetscagnottes.core.entity.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Set;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Page<Campaign> findAll(Pageable pageable);

    Set<Campaign> findAllByLeaderId(Long memberId);

    Set<Campaign> findAllByPeopleGivingTime_Id(Long memberId);

    Set<Campaign> findAllByStatusAndFundingDeadlineLessThan(CampaignStatus inProgress, Date date);

    Page<Campaign> findByBudgets_id(Long id, PageRequest pageable);

    Set<Campaign> findAllByStatus(CampaignStatus status);

    Page<Campaign> findAllByStatusIn(Set<CampaignStatus> status, Pageable pageable);

    @Query(nativeQuery = true,
            value = "select c.* from campaigns c WHERE c.status IN (:status) " +
                    "inner join campaigns_organizations on c.id = campaigns_organizations.campaign_id " +
                    "inner join organizations o on campaigns_organizations.organization_id = o.id " +
                    "inner join organizations_users on organizations_users.organization_id = o.id " +
                    "inner join users u on u.id = organizations_users.user_id " +
                    "where u.id = :user_id #pageable",
            countQuery = "select count(*) from campaigns c WHERE c.status IN (:status) " +
                    "inner join campaigns_organizations on c.id = campaigns_organizations.campaign_id " +
                    "inner join organizations o on campaigns_organizations.organization_id = o.id " +
                    "inner join organizations_users on organizations_users.organization_id = o.id " +
                    "inner join users u on u.id = organizations_users.user_id " +
                    "where u.id = :user_id")
    Page<Campaign> findAllByUserAndStatus(@Param("user_id") Long userId, @Param("status") Set<CampaignStatus> status, Pageable pageable);

    @Query(nativeQuery = true, value = "select c.* from campaigns c " +
            "inner join campaigns_organizations on c.id = campaigns_organizations.campaign_id " +
            "inner join organizations o on campaigns_organizations.organization_id = o.id " +
            "inner join organizations_users on organizations_users.organization_id = o.id " +
            "inner join users u on u.id = organizations_users.user_id " +
            "where u.id = :user_id and c.id = :campaign_id")
    Set<Campaign> findByUserAndId(@Param("user_id") Long userId, @Param("campaign_id") long campaignId);

}