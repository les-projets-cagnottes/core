package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.budget.repository.BudgetRepository;
import fr.lesprojetscagnottes.core.campaign.entity.CampaignEntity;
import fr.lesprojetscagnottes.core.campaign.model.CampaignStatus;
import fr.lesprojetscagnottes.core.campaign.repository.CampaignRepository;
import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.organization.repository.OrganizationRepository;
import fr.lesprojetscagnottes.core.project.repository.ProjectRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class CampaignStepDefinitions {

    @Autowired
    private CucumberContext context;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @And("The following campaigns are running")
    public void theFollowingCampaignsAreRunning(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        // Get funding deadline
        LocalDate now = LocalDate.now();
        LocalDate fundingDeadline = now.plusMonths(1);

        CampaignEntity campaign;
        for (Map<String, String> columns : rows) {

            // Create campaign
            campaign = new CampaignEntity();
            campaign.setProject(context.getProjects().get(columns.get("project")));
            campaign.setBudget(context.getBudgets().get(columns.get("budget")));
            campaign.setTitle(StringUtils.isNotEmpty(columns.get("title")) ? columns.get("title") : "Awesome title");
            campaign.setStatus(CampaignStatus.valueOf(columns.get("status")));
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

        CampaignEntity campaign;
        for (Map<String, String> columns : rows) {

            // Create campaign
            campaign = new CampaignEntity();
            campaign.setProject(context.getProjects().get(columns.get("project")));
            campaign.setBudget(context.getBudgets().get(columns.get("budget")));
            campaign.setTitle(columns.get("title"));
            campaign.setStatus(CampaignStatus.valueOf(columns.get("status")));
            campaign.setDonationsRequired(Float.valueOf(columns.get("donationsRequired")));
            campaign.setFundingDeadline(Date.valueOf(fundingDeadline));
            campaign = campaignRepository.save(campaign);

            // Save in Test Map
            context.getCampaigns().put(campaign.getTitle(), campaign);
        }
    }


}
