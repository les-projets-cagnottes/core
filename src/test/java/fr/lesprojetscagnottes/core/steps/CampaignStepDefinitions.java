package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.entity.Budget;
import fr.lesprojetscagnottes.core.entity.Campaign;
import fr.lesprojetscagnottes.core.entity.Organization;
import fr.lesprojetscagnottes.core.model.CampaignStatus;
import fr.lesprojetscagnottes.core.repository.BudgetRepository;
import fr.lesprojetscagnottes.core.repository.CampaignRepository;
import fr.lesprojetscagnottes.core.repository.OrganizationRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class CampaignStepDefinitions {

    @Autowired
    private CucumberContext context;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @And("The following campaigns are running")
    public void theFollowingCampaignsAreRunning(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        // Get funding deadline
        LocalDate now = LocalDate.now();
        LocalDate fundingDeadline = now.plusMonths(1);

        Campaign campaign;
        for (Map<String, String> columns : rows) {

            // Create campaign
            campaign = new Campaign();
            campaign.setTitle(columns.get("title"));
            campaign.setLeader(context.getUsers().get(columns.get("leader")));
            campaign.setProject(context.getProjects().get(columns.get("project")));
            campaign.setStatus(CampaignStatus.valueOf(columns.get("status")));
            campaign.setPeopleRequired(Integer.valueOf(columns.get("peopleRequired")));
            campaign.setDonationsRequired(Float.valueOf(columns.get("donationsRequired")));
            campaign.setFundingDeadline(Date.valueOf(fundingDeadline));
            campaign = campaignRepository.save(campaign);

            // Save in Test Map
            context.getCampaigns().put(campaign.getTitle(), campaign);
        }
    }

    @And("The following campaigns have a deadline reached")
    public void theFollowingCampaignsHaveADeadlineReached(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        // Get funding deadline
        LocalDate now = LocalDate.now();
        LocalDate fundingDeadline = now.minusMonths(1);

        Campaign campaign;
        for (Map<String, String> columns : rows) {

            // Create campaign
            campaign = new Campaign();
            campaign.setTitle(columns.get("title"));
            campaign.setProject(context.getProjects().get(columns.get("project")));
            campaign.setLeader(context.getUsers().get(columns.get("leader")));
            campaign.setStatus(CampaignStatus.valueOf(columns.get("status")));
            campaign.setPeopleRequired(Integer.valueOf(columns.get("peopleRequired")));
            campaign.setDonationsRequired(Float.valueOf(columns.get("donationsRequired")));
            campaign.setFundingDeadline(Date.valueOf(fundingDeadline));
            campaign = campaignRepository.save(campaign);

            // Save in Test Map
            context.getCampaigns().put(campaign.getTitle(), campaign);
        }
    }

    @When("The following campaigns are associated to organizations")
    public void theFollowingCampaignsAreAssociatedToOrganizations(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Organization organization;
        Campaign campaign;
        for (Map<String, String> columns : rows) {

            // Get campaign
            campaign = context.getCampaigns().get(columns.get("campaign"));
            final Campaign campaignFinal = campaign;

            // Get organization
            organization = organizationRepository.findById(context.getOrganizations().get(columns.get("organization")).getId()).orElse(null);
            final Organization organizationFinal = organization;
            assertNotNull(organizationFinal);

            // Associate campaign to the organization
            organizationFinal.getCampaigns().stream()
                    .filter(organizationCampaign -> organizationCampaign.getId().equals(campaignFinal.getId()))
                    .findAny()
                    .ifPresentOrElse(
                            campaignPresent -> {},
                            () -> organizationFinal.getCampaigns().add(campaignFinal)
                    );

            // Save
            organization = organizationRepository.save(organizationFinal);
            context.getOrganizations().put(organization.getName(), organization);
        }
    }

    @When("The following campaigns uses budgets")
    public void theFollowingCampaignsUsesBudgets(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        Budget budget;
        for (Map<String, String> columns : rows) {
            budget = context.getBudgets().get(columns.get("budget"));
            budget.getCampaigns().add(context.getCampaigns().get(columns.get("campaign")));
            budgetRepository.save(budget);
            context.getBudgets().put(budget.getName(), budget);
        }
    }

}
