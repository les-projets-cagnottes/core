package fr.lesprojetscagnottes.core.campaign;

import fr.lesprojetscagnottes.core.campaign.entity.CampaignEntity;
import fr.lesprojetscagnottes.core.campaign.model.CampaignStatus;
import fr.lesprojetscagnottes.core.campaign.repository.CampaignRepository;
import fr.lesprojetscagnottes.core.donation.entity.Donation;
import fr.lesprojetscagnottes.core.donation.queue.DonationOperationType;
import fr.lesprojetscagnottes.core.donation.repository.DonationRepository;
import fr.lesprojetscagnottes.core.donation.task.DonationProcessingTask;
import fr.lesprojetscagnottes.core.notification.model.NotificationName;
import fr.lesprojetscagnottes.core.notification.service.NotificationService;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import fr.lesprojetscagnottes.core.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CampaignScheduler {

    @Autowired
    private DonationProcessingTask donationProcessingTask;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Value("${fr.lesprojetscagnottes.web.url}")
    private String webUrl;

    @Scheduled(cron = "${fr.lesprojetscagnottes.core.schedule.campaignfunding}")
    @Transactional
    public void processCampaignFundingDeadlines() {
        log.info("[processCampaignFundingDeadlines] Start Campaign Funding Deadlines Processing");
        Set<CampaignEntity> campaigns = campaignRepository.findAllByStatusAndFundingDeadlineLessThan(CampaignStatus.IN_PROGRESS, new Date());
        log.info("[processCampaignFundingDeadlines] " + campaigns.size() + " campaign(s) found");
        campaigns.forEach(campaign -> {
            Set<Donation> donations = donationRepository.findAllByCampaignId(campaign.getId());
            float totalDonations = 0f;
            for (Donation donation : donations) {
                totalDonations += donation.getAmount();
            }
            log.info("[processCampaignFundingDeadlines][" + campaign.getId() + "] Campaign : " + campaign.getId());
            log.info("[processCampaignFundingDeadlines][" + campaign.getId() + "] Donations : " + totalDonations + " € / " + campaign.getDonationsRequired() + " €");
            if (totalDonations >= campaign.getDonationsRequired()) {
                campaign.setStatus(CampaignStatus.SUCCESSFUL);
                log.info("[processCampaignFundingDeadlines][" + campaign.getId() + "] Status => SUCCESSFUL");
            } else {
                campaign.setStatus(CampaignStatus.FAILED);
                log.info("[processCampaignFundingDeadlines][" + campaign.getId() + "] Status => FAILED");
                for (Donation donation : donations) {
                    donationProcessingTask.insert(donation, DonationOperationType.DELETION);
                }
                log.info("[processCampaignFundingDeadlines][" + campaign.getId() + "] Donations deleted");
            }
        });
        log.info("[processCampaignFundingDeadlines] End Campaign Funding Deadlines Processing");
    }

    @Scheduled(cron = "${fr.lesprojetscagnottes.core.schedule.campaignalmostfinished}")
    public void notifyCampaignsAlmostFinished() {
        log.info("[notifyCampaignsAlmostFinished] Start Notify Campaign Almost Finished");
        Set<CampaignEntity> campaigns = campaignRepository.findAllByStatus(CampaignStatus.IN_PROGRESS);
        log.info("[notifyCampaignsAlmostFinished] " + campaigns.size() + " campaign(s) found");
        campaigns.forEach(campaign -> {
            long diffInMillies = Math.abs(campaign.getFundingDeadline().getTime() - new Date().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;

            log.info("[notifyCampaignsAlmostFinished][" + campaign.getId() + "] Campaign : " + campaign.getId());
            log.info("[notifyCampaignsAlmostFinished][" + campaign.getId() + "] Days until deadline : " + diff);

            if(diff == 7 || diff == 1) {
                notifyCampaignStatus(campaign, diff);
            }
        });
        log.info("[notifyCampaignsAlmostFinished] End Notify Campaign Almost Finished");
    }

    public void notifyCampaignStatus(CampaignEntity campaign, long daysUntilDeadline) {

        Set<Donation> donations = donationRepository.findAllByCampaignId(campaign.getId());
        float totalDonations = 0f;
        for (Donation donation : donations) {
            totalDonations += donation.getAmount();
        }
        float donationsMissing = campaign.getDonationsRequired() - totalDonations;
        log.info("[notifyCampaignsAlmostFinished][" + campaign.getId() + "] Donations : " + donationsMissing + " €");

        UserEntity leader = userRepository.findById(campaign.getProject().getLeader().getId()).orElse(null);
        if(leader == null) {
            log.error("Impossible to notify about campaign status : leader of campaign {} id null", campaign.getId());
        } else {
            if(donationsMissing > 0) {
                Map<String, Object> model = new HashMap<>();
                model.put("days_until_deadline", daysUntilDeadline);
                model.put("donation_missing_formatted", String.format("%.2f", donationsMissing));
                model.put("project_title", campaign.getProject().getTitle());
                model.put("project_url", webUrl + "/projects/" + campaign.getProject().getId());
                model.put("profile_url", webUrl + "/profile");
                notificationService.create(NotificationName.CAMPAIGN_REMINDER, model, campaign.getProject().getOrganization().getId());
            }
        }

    }
}
