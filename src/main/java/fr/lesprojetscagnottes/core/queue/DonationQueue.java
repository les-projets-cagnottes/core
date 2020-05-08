package fr.lesprojetscagnottes.core.queue;

import fr.lesprojetscagnottes.core.entity.Account;
import fr.lesprojetscagnottes.core.entity.Budget;
import fr.lesprojetscagnottes.core.entity.Campaign;
import fr.lesprojetscagnottes.core.entity.Donation;
import fr.lesprojetscagnottes.core.exception.InternalServerException;
import fr.lesprojetscagnottes.core.repository.AccountRepository;
import fr.lesprojetscagnottes.core.repository.BudgetRepository;
import fr.lesprojetscagnottes.core.repository.CampaignRepository;
import fr.lesprojetscagnottes.core.repository.DonationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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
        LOGGER.debug("Trigger donation queue");
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

    private void createDonation(Donation donationToSave) {

        donationToSave = donationRepository.save(donationToSave);
        float amount = donationToSave.getAmount();

        // Update account
        Long accountId = donationToSave.getAccount().getId();
        Account account = accountRepository.findById(accountId).orElse(null);
        if(account == null) {
            LOGGER.error("Error while updating account {} after donation {} creation", accountId, donationToSave.getId());
            throw new InternalServerException();
        }
        account.setAmount(account.getAmount() - amount);
        accountRepository.save(account);

        // Update campaign
        Long campaignId = donationToSave.getCampaign().getId();
        Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
        if(campaign == null) {
            LOGGER.error("Error while updating campaign {} after donation {} creation", campaignId, donationToSave.getId());
            throw new InternalServerException();
        }
        campaign.setTotalDonations(campaign.getTotalDonations() + amount);
        campaignRepository.save(campaign);

        // Update budget
        Long budgetId = donationToSave.getBudget().getId();
        Budget budget = budgetRepository.findById(budgetId).orElse(null);
        if(budget == null) {
            LOGGER.error("Error while updating budget {} after donation {} creation", budgetId, donationToSave.getId());
            throw new InternalServerException();
        }
        budget.setTotalDonations(budget.getTotalDonations() + amount);
        budgetRepository.save(budget);

    }

    private void deleteDonation(Donation donation) {

        Long id = donation.getId();
        try {
            donationRepository.deleteById(id);
        } catch(EmptyResultDataAccessException e) {
            LOGGER.error("Error while deleting donation {} : donation not found", id);
        }
        float amount = donation.getAmount();

        // Update account
        Long accountId = donation.getAccount().getId();
        Account account = accountRepository.findById(accountId).orElse(null);
        if(account == null) {
            LOGGER.error("Error while updating account {} after donation {} of \"{}\"€ deletion", accountId, id, amount);
            throw new InternalServerException();
        }
        account.setAmount(account.getAmount() + amount);
        accountRepository.save(account);

        // Update campaign
        Long campaignId = donation.getCampaign().getId();
        Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
        if(campaign == null) {
            LOGGER.error("Error while updating campaign {} after donation {} of \"{}\"€ deletion", campaignId, id, amount);
            throw new InternalServerException();
        }
        campaign.setTotalDonations(campaign.getTotalDonations() - amount);
        campaignRepository.save(campaign);

        // Update budget
        Long budgetId = donation.getBudget().getId();
        Budget budget = budgetRepository.findById(budgetId).orElse(null);
        if(budget == null) {
            LOGGER.error("Error while updating budget {} after donation {} of \"{}\"€ deletion", budgetId, id, amount);
            throw new InternalServerException();
        }
        budget.setTotalDonations(budget.getTotalDonations() - amount);
        budgetRepository.save(budget);

    }

}
