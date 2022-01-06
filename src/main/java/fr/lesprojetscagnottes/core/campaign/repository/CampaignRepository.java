package fr.lesprojetscagnottes.core.campaign.repository;

import fr.lesprojetscagnottes.core.campaign.entity.CampaignEntity;
import fr.lesprojetscagnottes.core.campaign.model.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Set;

public interface CampaignRepository extends JpaRepository<CampaignEntity, Long> {

    Set<CampaignEntity> findAllByStatusAndFundingDeadlineLessThan(CampaignStatus inProgress, Date date);

    Page<CampaignEntity> findByBudgetId(Long id, Pageable pageable);

    Set<CampaignEntity> findAllByStatus(CampaignStatus status);

}