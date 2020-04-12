package fr.thomah.valyou.steps;

import fr.thomah.valyou.component.AuthenticationHttpClient;
import fr.thomah.valyou.component.CucumberContext;
import fr.thomah.valyou.component.DonationHttpClient;
import fr.thomah.valyou.entity.*;
import fr.thomah.valyou.entity.model.DonationModel;
import fr.thomah.valyou.pagination.DataPage;
import fr.thomah.valyou.repository.BudgetRepository;
import fr.thomah.valyou.repository.DonationRepository;
import fr.thomah.valyou.repository.OrganizationRepository;
import fr.thomah.valyou.repository.ProjectRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DonationStepDefinitions {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonationStepDefinitions.class);

    @Autowired
    private AuthenticationHttpClient authenticationHttpClient;

    @Autowired
    private DonationHttpClient donationHttpClient;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CucumberContext context;

    @And("The following campaigns are running")
    public void theFollowingCampaignsAreRunning(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        // Get funding deadline
        LocalDate now = LocalDate.now();
        LocalDate fundingDeadline = now.plusMonths(1);

        Project campaign;
        for (Map<String, String> columns : rows) {

            // Create campaign
            campaign = new Project();
            campaign.setTitle(columns.get("title"));
            campaign.setLeader(context.getUsers().get(columns.get("leader")));
            campaign.setStatus(ProjectStatus.valueOf(columns.get("status")));
            campaign.setPeopleRequired(Integer.valueOf(columns.get("peopleRequired")));
            campaign.setDonationsRequired(Float.valueOf(columns.get("donationsRequired")));
            campaign.setFundingDeadline(Date.valueOf(fundingDeadline));
            campaign = projectRepository.save(campaign);

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

        Project campaign;
        for (Map<String, String> columns : rows) {

            // Create campaign
            campaign = new Project();
            campaign.setTitle(columns.get("title"));
            campaign.setLeader(context.getUsers().get(columns.get("leader")));
            campaign.setStatus(ProjectStatus.valueOf(columns.get("status")));
            campaign.setPeopleRequired(Integer.valueOf(columns.get("peopleRequired")));
            campaign.setDonationsRequired(Float.valueOf(columns.get("donationsRequired")));
            campaign.setFundingDeadline(Date.valueOf(fundingDeadline));
            campaign = projectRepository.save(campaign);

            // Save in Test Map
            context.getCampaigns().put(campaign.getTitle(), campaign);
        }
    }

    @When("The following campaigns are associated to organizations")
    public void theFollowingCampaignsAreAssociatedToOrganizations(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Organization organization;
        Project campaign;
        for (Map<String, String> columns : rows) {

            // Get campaign
            campaign = context.getCampaigns().get(columns.get("campaign"));
            final Project campaignFinal = campaign;

            // Get organization
            organization = organizationRepository.findById(context.getOrganizations().get(columns.get("organization")).getId()).orElse(null);
            final Organization organizationFinal = organization;
            assertNotNull(organizationFinal);

            // Associate project to the organization
            organizationFinal.getProjects().stream()
                    .filter(project -> project.getId().equals(campaignFinal.getId()))
                    .findAny()
                    .ifPresentOrElse(
                            campaignPresent -> {},
                            () -> organizationFinal.getProjects().add(campaignFinal)
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
            budget.getProjects().add(context.getCampaigns().get(columns.get("campaign")));
            budgetRepository.save(budget);
            context.getBudgets().put(budget.getName(), budget);
        }
    }

    @Given("The following donations are made")
    public void theFollowingDonationsAreMade(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        Donation donation;
        for (Map<String, String> columns : rows) {
            donation = new Donation();
            donation.setAmount(Float.parseFloat(columns.get("amount")));
            donation.setBudget(context.getBudgets().get(columns.get("budget")));
            donation.setProject(context.getCampaigns().get(columns.get("campaign")));
            donation.setContributor(context.getUsers().get(columns.get("contributor")));
            donationRepository.save(donation);
        }
    }

    @When("{string} submit the following donations")
    public void submitTheFollowingDonations(String userFirstname, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Donation donation;
        for (Map<String, String> columns : rows) {

            // Create donation
            donation = new Donation();
            donation.setAmount(Float.parseFloat(columns.get("amount")));
            donation.setBudget(context.getBudgets().get(columns.get("budget")));
            donation.setProject(context.getCampaigns().get(columns.get("campaign")));
            donation.setContributor(context.getUsers().get(columns.get("contributor")));

            // Refresh Token
            authenticationHttpClient.setBearerAuth(context.getAuths().get(userFirstname).getToken());
            AuthenticationResponse response = authenticationHttpClient.refresh();
            context.getAuths().put(userFirstname, response);

            // Make donation
            donationHttpClient.setBearerAuth(response.getToken());
            donationHttpClient.post(donation);
        }
    }

    @Then("{string} has {string} donation on the {string} budget")
    public void hasDonationOnTheBudget(String userFirstname, String numberOfDonations, String budgetName) {
        Set<Donation> donations = donationRepository.findAllByContributorIdAndBudgetId(context.getUsers().get(userFirstname).getId(), context.getBudgets().get(budgetName).getId());
        assertEquals(numberOfDonations, String.valueOf(donations.size()));
    }

    @Then("Following users have corresponding amount left on budgets")
    public void followingUsersHaveCorrespondingAmountLeftOnBudgets(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Budget budget;
        Set<Donation> donations;
        float totalAmount = 0f;
        for (Map<String, String> columns : rows) {
            budget = context.getBudgets().get(columns.get("budget"));
            donations = donationRepository.findAllByContributorIdAndBudgetId(context.getUsers().get(columns.get("user")).getId(), budget.getId());
            for(Donation donation : donations) {
                totalAmount+= donation.getAmount();
            }

            assertEquals(Float.parseFloat(columns.get("amount")), budget.getAmountPerMember() - totalAmount, 0);
        }
    }

    @Then("It returns following donations")
    public void itReturnsFollowingDonations(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        DataPage<DonationModel> body = (DataPage<DonationModel>) donationHttpClient.getLastResponse().getBody();
        Assert.assertNotNull(body);
        List<DonationModel> donationsReturned = body.getContent();
        Assert.assertNotNull(donationsReturned);

        Donation donation;
        for (Map<String, String> columns : rows) {

            // Create budget from feature
            donation = new Donation();
            donation.setAmount(Float.parseFloat(columns.get("amount")));
            donation.setBudget(context.getBudgets().get(columns.get("budget")));
            donation.setProject(context.getCampaigns().get(columns.get("campaign")));
            donation.setContributor(context.getUsers().get(columns.get("contributor")));
            final Donation donationFinal = donation;
            LOGGER.debug("HERE");
            LOGGER.debug(String.valueOf(donationsReturned.size()));
            donationsReturned.stream()
                    .filter(donationReturned -> donationFinal.getAmount() == donationReturned.getAmount())
                    .filter(donationReturned -> donationFinal.getBudget().getId().equals(donationReturned.getBudget().getId()))
                    .filter(donationReturned -> donationFinal.getProject().getId().equals(donationReturned.getProject().getId()))
                    .filter(donationReturned -> donationFinal.getContributor().getId().equals(donationReturned.getContributor().getId()))
                    .findAny()
                    .ifPresentOrElse(
                            donationsReturned::remove,
                            Assert::fail);
        }

        Assert.assertEquals(0, donationsReturned.size());
    }

    @When("{string} gets donations of the {string} campaign")
    public void getsDonationsOfTheCampaign(String userFirstname, String campaign) {

        // Refresh Token
        authenticationHttpClient.setBearerAuth(context.getAuths().get(userFirstname).getToken());
        AuthenticationResponse response = authenticationHttpClient.refresh();
        context.getAuths().put(userFirstname, response);

        // Get donations
        donationHttpClient.setBearerAuth(response.getToken());
        donationHttpClient.getByProjectId(context.getCampaigns().get(campaign).getId());
    }

}
