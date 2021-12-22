package fr.lesprojetscagnottes.core.steps;

import com.google.gson.reflect.TypeToken;
import fr.lesprojetscagnottes.core.authentication.model.AuthenticationResponseModel;
import fr.lesprojetscagnottes.core.account.entity.AccountEntity;
import fr.lesprojetscagnottes.core.common.pagination.DataPage;
import fr.lesprojetscagnottes.core.component.AuthenticationHttpClient;
import fr.lesprojetscagnottes.core.component.CampaignHttpClient;
import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.component.DonationHttpClient;
import fr.lesprojetscagnottes.core.donation.entity.Donation;
import fr.lesprojetscagnottes.core.donation.model.DonationModel;
import fr.lesprojetscagnottes.core.donation.repository.DonationRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DonationStepDefinitions {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonationStepDefinitions.class);

    @Autowired
    private AuthenticationHttpClient authenticationHttpClient;

    @Autowired
    private DonationHttpClient donationHttpClient;

    @Autowired
    private CampaignHttpClient campaignHttpClient;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CucumberContext context;

    @Given("The following donations are made")
    public void theFollowingDonationsAreMade(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        Donation donation;
        for (Map<String, String> columns : rows) {
            donation = new Donation();
            donation.setAmount(Float.parseFloat(columns.get("amount")));
            donation.setAccount(context.getAccounts().get(columns.get("budget") + "-" + columns.get("contributor")));
            donation.setCampaign(context.getCampaigns().get(columns.get("campaign")));
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
            donation.setAccount(context.getAccounts().get(columns.get("budget") + "-" + columns.get("contributor")));
            donation.setCampaign(context.getCampaigns().get(columns.get("campaign")));
            donation.setContributor(context.getUsers().get(columns.get("contributor")));

            // Refresh Token
            authenticationHttpClient.setBearerAuth(context.getAuths().get(userFirstname).getToken());
            AuthenticationResponseModel response = authenticationHttpClient.refresh();
            context.getAuths().put(userFirstname, response);

            // Make donation
            donationHttpClient.setBearerAuth(response.getToken());
            donationHttpClient.create(DonationModel.fromEntity(donation));
        }
    }

    @Then("It returns following donations")
    public void itReturnsFollowingDonations(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Type dataPageType = new TypeToken<DataPage<DonationModel>>(){}.getType();
        DataPage<DonationModel> body = context.getGson().fromJson(context.getLastBody(), dataPageType);
        Assert.assertNotNull(body);
        List<DonationModel> donationsReturned = body.getContent();
        Assert.assertNotNull(donationsReturned);

        Donation donation;
        for (Map<String, String> columns : rows) {

            // Create budget from feature
            donation = new Donation();
            donation.setAmount(Float.parseFloat(columns.get("amount")));
            donation.setAccount(context.getAccounts().get(columns.get("budget") + "-" + columns.get("contributor")));
            donation.setCampaign(context.getCampaigns().get(columns.get("campaign")));
            donation.setContributor(context.getUsers().get(columns.get("contributor")));
            final Donation donationFinal = donation;
            donationsReturned.stream()
                    .filter(donationReturned -> donationFinal.getAmount() == donationReturned.getAmount())
                    .filter(donationReturned -> donationFinal.getAccount().getId().equals(donationReturned.getAccount().getId()))
                    .filter(donationReturned -> donationFinal.getCampaign().getId().equals(donationReturned.getCampaign().getId()))
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
        AuthenticationResponseModel response = authenticationHttpClient.refresh();
        context.getAuths().put(userFirstname, response);

        // Get donations
        campaignHttpClient.setBearerAuth(response.getToken());
        campaignHttpClient.getDonations(context.getCampaigns().get(campaign).getId());
    }

    @Then("{string} has {string} donation on the {string} account")
    public void hasDonationOnTheAccount(String userFirstname, String nbDonations, String budgetName) {
        AccountEntity account = context.getAccounts().get(budgetName + "-" + userFirstname);
        Set<Donation> donations = donationRepository.findAllByAccountId(account.getId());
        Assert.assertEquals(Integer.valueOf(nbDonations), Integer.valueOf(donations.size()));
    }
}
