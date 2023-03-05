package fr.lesprojetscagnottes.core.donation.repository;

import fr.lesprojetscagnottes.core.donation.entity.DonationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface DonationRepository extends JpaRepository<DonationEntity, Long> {

    Page<DonationEntity> findAll(Pageable pageable);

    Set<DonationEntity> findAllByCampaignId(long campaignId);

    Set<DonationEntity> findAllByAccountId(Long id);

    Set<DonationEntity> findAllByAccountIdInOrderByCreatedAtAsc(Set<Long> accountIds);

    Page<DonationEntity> findByCampaign_idOrderByIdAsc(long id, Pageable pageable);

    @Query(value = "SELECT create_donation(:_account_id, :_campaign_id, :_amount);", nativeQuery = true)
    boolean createDonation(@Param("_account_id") long accountId, @Param("_campaign_id") long campaignId, @Param("_amount") float amount);

    @Query(value = "SELECT delete_donation(:_donation_id);", nativeQuery = true)
    boolean deleteDonation(@Param("_donation_id") long donationId);

}