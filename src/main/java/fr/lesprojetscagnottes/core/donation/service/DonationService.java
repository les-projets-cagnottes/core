package fr.lesprojetscagnottes.core.donation.service;

import fr.lesprojetscagnottes.core.donation.entity.DonationEntity;
import fr.lesprojetscagnottes.core.donation.repository.DonationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class DonationService {

    private final DonationRepository donationRepository;

    @Autowired
    public DonationService(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
    }

    public Page<DonationEntity> findByCampaign_idOrderByIdAsc(long campaignId, PageRequest id) {
        return donationRepository.findByCampaign_idOrderByIdAsc(campaignId, id);
    }
}
