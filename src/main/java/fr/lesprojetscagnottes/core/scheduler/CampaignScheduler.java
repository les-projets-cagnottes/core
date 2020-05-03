package fr.lesprojetscagnottes.core.scheduler;

import fr.lesprojetscagnottes.core.entity.Campaign;
import fr.lesprojetscagnottes.core.entity.CampaignStatus;
import fr.lesprojetscagnottes.core.entity.Donation;
import fr.lesprojetscagnottes.core.entity.User;
import fr.lesprojetscagnottes.core.repository.CampaignRepository;
import fr.lesprojetscagnottes.core.repository.DonationRepository;
import fr.lesprojetscagnottes.core.repository.UserRepository;
import fr.lesprojetscagnottes.core.service.SlackClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class CampaignScheduler {

    private static final String WEB_URL = System.getenv("LPC_WEB_URL");

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignScheduler.class);

    @Autowired
    private SlackClientService slackClientService;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "0 0 2 * * *")
    public void processTotalDonations() {
        LOGGER.info("[processTotalDonations] Start total donations calculation");
        campaignRepository.updateTotalDonations();
        LOGGER.info("[processTotalDonations] End total donations calculation");
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void processCampaignFundingDeadlines() {
        LOGGER.info("[processCampaignFundingDeadlines] Start Campaign Funding Deadlines Processing");
        Set<Campaign> campaigns = campaignRepository.findAllByStatusAndFundingDeadlineLessThan(CampaignStatus.A_IN_PROGRESS, new Date());
        LOGGER.info("[processCampaignFundingDeadlines] " + campaigns.size() + " campaign(s) found");
        campaigns.forEach(campaign -> {
            Set<Donation> donations = donationRepository.findAllByCampaignId(campaign.getId());
            float totalDonations = 0f;
            for (Donation donation : donations) {
                totalDonations += donation.getAmount();
            }
            LOGGER.info("[processCampaignFundingDeadlines][" + campaign.getId() + "] Campaign : " + campaign.getTitle());
            LOGGER.info("[processCampaignFundingDeadlines][" + campaign.getId() + "] Teammates : " + campaign.getPeopleGivingTime().size() + " / " + campaign.getPeopleRequired());
            LOGGER.info("[processCampaignFundingDeadlines][" + campaign.getId() + "] Donations : " + totalDonations + " € / " + campaign.getDonationsRequired() + " €");
            if (totalDonations >= campaign.getDonationsRequired()
                    && campaign.getPeopleGivingTime().size() >= campaign.getPeopleRequired()) {
                campaign.setStatus(CampaignStatus.B_READY);
                LOGGER.info("[processCampaignFundingDeadlines][" + campaign.getId() + "] Status => B_READY");
            } else {
                campaign.setStatus(CampaignStatus.C_AVORTED);
                LOGGER.info("[processCampaignFundingDeadlines][" + campaign.getId() + "] Status => C_AVORTED");
                donationRepository.deleteByCampaignId(campaign.getId());
                LOGGER.info("[processCampaignFundingDeadlines][" + campaign.getId() + "] Donations deleted");
            }
        });
        LOGGER.info("[processCampaignFundingDeadlines] End Campaign Funding Deadlines Processing");
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void notifyCampaignsAlmostFinished() {
        LOGGER.info("[notifyCampaignsAlmostFinished] Start Notify Campaign Almost Finished");
        Set<Campaign> campaigns = campaignRepository.findAllByStatus(CampaignStatus.A_IN_PROGRESS);
        LOGGER.info("[notifyCampaignsAlmostFinished] " + campaigns.size() + " campaign(s) found");
        campaigns.forEach(campaign -> {
            long diffInMillies = Math.abs(campaign.getFundingDeadline().getTime() - new Date().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;

            LOGGER.info("[notifyCampaignsAlmostFinished][" + campaign.getId() + "] Campaign : " + campaign.getTitle());
            LOGGER.info("[notifyCampaignsAlmostFinished][" + campaign.getId() + "] Days until deadline : " + diff);

            if(diff == 7 || diff == 1) {
                notifyCampaignStatus(campaign, diff);
            }
        });
        LOGGER.info("[notifyCampaignsAlmostFinished] End Notify Campaign Almost Finished");
    }

    public void notifyCampaignStatus(Campaign campaign, long daysUntilDeadline) {

        int teamMatesMissing = campaign.getPeopleRequired() - campaign.getPeopleGivingTime().size();
        LOGGER.info("[notifyCampaignStatus][" + campaign.getId() + "] Teammates missing : " + teamMatesMissing);

        Set<Donation> donations = donationRepository.findAllByCampaignId(campaign.getId());
        float totalDonations = 0f;
        for (Donation donation : donations) {
            totalDonations += donation.getAmount();
        }
        float donationsMissing = campaign.getDonationsRequired() - totalDonations;
        LOGGER.info("[notifyCampaignsAlmostFinished][" + campaign.getId() + "] Donations : " + donationsMissing + " €");

        User leader = userRepository.findById(campaign.getLeader().getId()).orElse(null);
        if(leader == null) {
            LOGGER.error("Impossible to notify about campaign status : leader of campaign {} id null", campaign.getId());
        } else {
            if(teamMatesMissing > 0 || donationsMissing > 0) {

                String defaultUser = new StringBuilder("*")
                        .append(leader.getFirstname())
                        .append(" ")
                        .append(leader.getLastname())
                        .append("*")
                        .toString();

                StringBuilder endMessage = new StringBuilder(" a besoin de vous !\n")
                        .append("Il reste *")
                        .append(daysUntilDeadline)
                        .append(" jour(s)* pour compléter la campagne du projet *")
                        .append(campaign.getTitle())
                        .append("* !\n\nA ce jour, il manque :\n");

                if(teamMatesMissing > 0) {
                    endMessage.append(" - ")
                            .append(teamMatesMissing)
                            .append(" personne(s) dans l'équipe\n");
                }
                if(donationsMissing > 0) {
                    endMessage.append(" - ")
                            .append(String.format("%.2f", donationsMissing))
                            .append(" € de budget\n");
                }

                campaign.getOrganizations().forEach(organization -> {
                    if(organization.getSlackTeam() != null) {
                        StringBuilder stringBuilderUser = new StringBuilder(":timer_clock: ");
                        organization.getMembers().stream()
                                .filter(member -> member.getId() == leader.getId())
                                .findAny()
                                .ifPresentOrElse(member -> {
                                    organization.getSlackTeam().getSlackUsers().stream()
                                            .filter(slackUser -> slackUser.getUser().getId() == leader.getId())
                                            .findAny()
                                            .ifPresentOrElse(slackUser -> {
                                                stringBuilderUser.append("<@")
                                                        .append(slackUser.getSlackId())
                                                        .append(">");
                                            }, () -> {
                                                stringBuilderUser.append(defaultUser);
                                            });

                                    stringBuilderUser.append(endMessage.toString())
                                            .append("\nRendez-vous à l'adresse suivante pour participer : \n")
                                            .append(WEB_URL)
                                            .append("/projects/")
                                            .append(campaign.getId());
                                },() -> {
                                    stringBuilderUser.append(defaultUser)
                                            .append(endMessage.toString())
                                            .append("\nRendez-vous à l'adresse suivante pour participer : \n")
                                            .append(WEB_URL)
                                            .append("/projects/")
                                            .append(campaign.getId());
                                });
                        LOGGER.info("[notifyCampaignStatus][" + campaign.getId() + "] Send Slack Message to " + organization.getSlackTeam().getTeamId() + " / " + organization.getSlackTeam().getPublicationChannel() + " :\n" + stringBuilderUser.toString());
                        String channelId = slackClientService.joinChannel(organization.getSlackTeam());
                        slackClientService.inviteInChannel(organization.getSlackTeam(), channelId);
                        slackClientService.postMessage(organization.getSlackTeam(), channelId, stringBuilderUser.toString());
                        LOGGER.info("[notifyCampaignStatus][" + campaign.getId() + "] Slack Message Sent");
                    }
                });

            }
        }

    }
}
