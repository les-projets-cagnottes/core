package fr.lesprojetscagnottes.core.queue;

import fr.lesprojetscagnottes.core.entity.Donation;
import fr.lesprojetscagnottes.core.repository.AccountRepository;
import fr.lesprojetscagnottes.core.repository.BudgetRepository;
import fr.lesprojetscagnottes.core.repository.CampaignRepository;
import fr.lesprojetscagnottes.core.repository.DonationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;
import java.util.TimerTask;

@Component
public class DonationQueue extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonationQueue.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private DonationRepository donationRepository;

    Queue<DonationOperation> queue = new LinkedList<>();

    public void insert(Donation donation, DonationOperationType type) {
        queue.add(new DonationOperation(donation, type));
    }

    @Override
    public void run() {
        DonationOperation operation = queue.poll();
        if(operation != null) {
            Donation donation = operation.getDonation();
            switch (operation.getType()) {
                case CREATION:
                    LOGGER.info("Create donation : {}", donation);
                    createDonation(donation);
                    break;
                case DELETION:
                    LOGGER.info("Delete donation : {}", donation);
                    deleteDonation(donation);
                    break;
            }
        }
    }

    private void createDonation(Donation donation) {
        try {
            donationRepository.createDonation(donation.getAccount().getId(), donation.getCampaign().getId(), donation.getBudget().getId(), donation.getAmount());
        } catch(Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void deleteDonation(Donation donation) {
        try {
            donationRepository.deleteDonation(donation.getId());
        } catch(Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

}
