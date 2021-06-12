package fr.lesprojetscagnottes.core.campaign;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;

public interface CampaignRepository extends JpaRepository<CampaignEntity, Long> {

    Page<CampaignEntity> findAll(Pageable pageable);

    Set<CampaignEntity> findAllByLeaderId(Long memberId);

    Set<CampaignEntity> findAllByPeopleGivingTime_Id(Long memberId);

    Set<CampaignEntity> findAllByStatusAndFundingDeadlineLessThan(CampaignStatus inProgress, Date date);

    Page<CampaignEntity> findByBudgets_id(Long id, Pageable pageable);

    Set<CampaignEntity> findAllByStatus(CampaignStatus status);

    Page<CampaignEntity> findAllByStatusIn(Set<CampaignStatus> status, Pageable pageable);

    @Transactional
    @Procedure(procedureName = "update_campaigns_total_donations")
    void updateTotalDonations();

    @Query(nativeQuery = true,
            value = "select c.* from campaigns c " +
                    "inner join campaigns_organizations on c.id = campaigns_organizations.campaign_id " +
                    "inner join organizations o on campaigns_organizations.organization_id = o.id " +
                    "inner join organizations_users on organizations_users.organization_id = o.id " +
                    "inner join users u on u.id = organizations_users.user_id " +
                    "where u.id = ?1 and c.status IN (?2) --#pageable\n",
            countQuery = "select count(*) from campaigns c " +
                    "inner join campaigns_organizations on c.id = campaigns_organizations.campaign_id " +
                    "inner join organizations o on campaigns_organizations.organization_id = o.id " +
                    "inner join organizations_users on organizations_users.organization_id = o.id " +
                    "inner join users u on u.id = organizations_users.user_id " +
                    "where u.id = ?1 c.status IN (?2)")
    Page<CampaignEntity> findAllByUserAndStatus(Long userId, Set<CampaignStatus> status, Pageable pageable);
    Page<CampaignEntity> findAllByOrganizations_IdAndStatusIn(Long id, Set<CampaignStatus> status, Pageable pageable);

    @Query(nativeQuery = true, value = "select c.* from campaigns c " +
            "inner join campaigns_organizations on c.id = campaigns_organizations.campaign_id " +
            "inner join organizations o on campaigns_organizations.organization_id = o.id " +
            "inner join organizations_users on organizations_users.organization_id = o.id " +
            "inner join users u on u.id = organizations_users.user_id " +
            "where u.id = :user_id and c.id = :campaign_id")
    Set<CampaignEntity> findByUserAndId(@Param("user_id") Long userId, @Param("campaign_id") long campaignId);

}