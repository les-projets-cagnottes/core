package fr.lesprojetscagnottes.core.repository;

import fr.lesprojetscagnottes.core.entity.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    Page<Donation> findAll(Pageable pageable);

    // TODO : Retrieve it by jointures with accounts
    Set<Donation> findAllByBudgetId(long budgetId);
    Set<Donation> findAllByCampaignId(long campaignId);
    Set<Donation> findAllByContributorIdOrderByBudgetIdAsc(long contributorId);
    Set<Donation> findAllByContributorIdAndBudgetId(long contributorId, long budgetId);
    void deleteByCampaignId(Long id);
    Page<Donation> findByCampaign_idOrderByIdAsc(long id, Pageable pageable);

}