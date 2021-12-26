package fr.lesprojetscagnottes.core.donation.task;

import fr.lesprojetscagnottes.core.donation.repository.DonationRepository;
import fr.lesprojetscagnottes.core.donation.entity.Donation;
import fr.lesprojetscagnottes.core.donation.queue.DonationOperation;
import fr.lesprojetscagnottes.core.donation.queue.DonationOperationType;
import fr.lesprojetscagnottes.core.account.repository.AccountRepository;
import fr.lesprojetscagnottes.core.budget.repository.BudgetRepository;
import fr.lesprojetscagnottes.core.campaign.repository.CampaignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;
import java.util.TimerTask;

@Component
public class DonationProcessingTask extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonationProcessingTask.class);

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
                case CREATION -> {
                    LOGGER.info("Create donation : {}", donation);
                    createDonation(donation);
                }
                case DELETION -> {
                    LOGGER.info("Delete donation : {}", donation);
                    deleteDonation(donation);
                }
            }
        }
    }

    private void createDonation(Donation donation) {
        try {
            donationRepository.createDonation(donation.getAccount().getId(), donation.getCampaign().getId(), donation.getAmount());
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
