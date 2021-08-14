package fr.lesprojetscagnottes.core.campaign;

import fr.lesprojetscagnottes.core.donation.entity.Donation;
import fr.lesprojetscagnottes.core.donation.queue.DonationOperationType;
import fr.lesprojetscagnottes.core.donation.repository.DonationRepository;
import fr.lesprojetscagnottes.core.donation.task.DonationProcessingTask;
import fr.lesprojetscagnottes.core.slack.SlackClientService;
import fr.lesprojetscagnottes.core.slack.entity.SlackTeamEntity;
import fr.lesprojetscagnottes.core.user.UserEntity;
import fr.lesprojetscagnottes.core.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

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
    private SpringTemplateEngine templateEngine;

    @Autowired
    private SlackClientService slackClientService;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${fr.lesprojetscagnottes.web.url}")
    private String webUrl;

    @Scheduled(cron = "0 0 2 * * *")
    public void processTotalDonations() {
        log.info("[processTotalDonations] Start total donations calculation");
        campaignRepository.updateTotalDonations();
        log.info("[processTotalDonations] End total donations calculation");
    }

    @Scheduled(cron = "0 0 3 * * *")
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
            log.info("[processCampaignFundingDeadlines][" + campaign.getId() + "] Campaign : " + campaign.getTitle());
            log.info("[processCampaignFundingDeadlines][" + campaign.getId() + "] Teammates : " + campaign.getProject().getPeopleGivingTime().size() + " / 3");
            log.info("[processCampaignFundingDeadlines][" + campaign.getId() + "] Donations : " + totalDonations + " € / " + campaign.getDonationsRequired() + " €");
            if (totalDonations >= campaign.getDonationsRequired()
                    && campaign.getPeopleGivingTime().size() >= 3) {
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

    @Scheduled(cron = "0 0 8 * * *")
    public void notifyCampaignsAlmostFinished() {
        log.info("[notifyCampaignsAlmostFinished] Start Notify Campaign Almost Finished");
        Set<CampaignEntity> campaigns = campaignRepository.findAllByStatus(CampaignStatus.IN_PROGRESS);
        log.info("[notifyCampaignsAlmostFinished] " + campaigns.size() + " campaign(s) found");
        campaigns.forEach(campaign -> {
            long diffInMillies = Math.abs(campaign.getFundingDeadline().getTime() - new Date().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;

            log.info("[notifyCampaignsAlmostFinished][" + campaign.getId() + "] Campaign : " + campaign.getTitle());
            log.info("[notifyCampaignsAlmostFinished][" + campaign.getId() + "] Days until deadline : " + diff);

            if(diff == 7 || diff == 1) {
                notifyCampaignStatus(campaign, diff);
            }
        });
        log.info("[notifyCampaignsAlmostFinished] End Notify Campaign Almost Finished");
    }

    public void notifyCampaignStatus(CampaignEntity campaign, long daysUntilDeadline) {

        int teamMatesMissing = campaign.getPeopleRequired() - campaign.getPeopleGivingTime().size();
        log.info("[notifyCampaignStatus][" + campaign.getId() + "] Teammates missing : " + teamMatesMissing);

        Set<Donation> donations = donationRepository.findAllByCampaignId(campaign.getId());
        float totalDonations = 0f;
        for (Donation donation : donations) {
            totalDonations += donation.getAmount();
        }
        float donationsMissing = campaign.getDonationsRequired() - totalDonations;
        log.info("[notifyCampaignsAlmostFinished][" + campaign.getId() + "] Donations : " + donationsMissing + " €");

        UserEntity leader = userRepository.findById(campaign.getLeader().getId()).orElse(null);
        if(leader == null) {
            log.error("Impossible to notify about campaign status : leader of campaign {} id null", campaign.getId());
        } else {
            if(teamMatesMissing > 0 || donationsMissing > 0) {

                Map<String, Object> model = new HashMap<>();
                model.put("URL", webUrl);
                model.put("campaign", campaign);
                model.put("daysUntilDeadline", daysUntilDeadline);
                model.put("teamMatesMissing", teamMatesMissing);
                model.put("donationsMissing", donationsMissing);
                model.put("donationsMissingFormatted", String.format("%.2f", donationsMissing));

                campaign.getOrganizations().forEach(organization -> {
                    if(organization.getSlackTeam() != null) {
                        SlackTeamEntity slackTeam = organization.getSlackTeam();
                        organization.getMembers().stream()
                                .filter(member -> member.getId().equals(leader.getId()))
                                .findAny()
                                .ifPresentOrElse(member -> slackTeam.getSlackUsers().stream()
                                    .filter(slackUser -> slackUser.getUser().getId().equals(leader.getId()))
                                    .findAny()
                                    .ifPresentOrElse(
                                        slackUser -> model.put("leader", "<@" + slackUser.getSlackId() + ">"),
                                        () -> model.put("leader", leader.getFullname())),
                                    () -> model.put("leader", leader.getFullname())
                                );

                        Context context = new Context();
                        context.setVariables(model);
                        String slackMessage = templateEngine.process("slack/fr/campaign-reminder", context);

                        log.info("[notifyCampaignStatus][" + campaign.getId() + "] Send Slack Message to " + slackTeam.getTeamId() + " / " + slackTeam.getPublicationChannelId() + " :\n" + slackMessage);
                        slackClientService.inviteBotInConversation(slackTeam);
                        slackClientService.postMessage(slackTeam, slackTeam.getPublicationChannelId(), slackMessage);
                        log.info("[notifyCampaignStatus][" + campaign.getId() + "] Slack Message Sent");
                    }
                });

            }
        }

    }
}
