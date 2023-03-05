package fr.lesprojetscagnottes.core.steps;

import com.google.gson.reflect.TypeToken;
import fr.lesprojetscagnottes.core.account.entity.AccountEntity;
import fr.lesprojetscagnottes.core.authentication.model.AuthenticationResponseModel;
import fr.lesprojetscagnottes.core.common.pagination.DataPage;
import fr.lesprojetscagnottes.core.component.AuthenticationHttpClient;
import fr.lesprojetscagnottes.core.component.CampaignHttpClient;
import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.component.DonationHttpClient;
import fr.lesprojetscagnottes.core.donation.entity.DonationEntity;
import fr.lesprojetscagnottes.core.donation.model.DonationModel;
import fr.lesprojetscagnottes.core.donation.repository.DonationRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DonationStepDefinitions {

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
        DonationEntity donation;
        for (Map<String, String> columns : rows) {
            donation = new DonationEntity();
            donation.setAmount(Float.parseFloat(columns.get("amount")));
            donation.setAccount(context.getAccounts().get(columns.get("budget") + "-" + columns.get("contributor")));
            donation.setCampaign(context.getCampaigns().get(columns.get("campaign")));
            donationRepository.save(donation);
        }
    }

    @When("{string} submit the following donations")
    public void submitTheFollowingDonations(String userFirstname, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        DonationEntity donation;
        for (Map<String, String> columns : rows) {

            // Create donation
            donation = new DonationEntity();
            donation.setAmount(Float.parseFloat(columns.get("amount")));
            donation.setAccount(context.getAccounts().get(columns.get("budget") + "-" + userFirstname));
            donation.setCampaign(context.getCampaigns().get(columns.get("campaign")));

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

        DonationEntity donation;
        for (Map<String, String> columns : rows) {

            // Create budget from feature
            donation = new DonationEntity();
            donation.setAmount(Float.parseFloat(columns.get("amount")));
            donation.setAccount(context.getAccounts().get(columns.get("budget") + "-" + columns.get("contributor")));
            donation.setCampaign(context.getCampaigns().get(columns.get("campaign")));
            final DonationEntity donationFinal = donation;
            donationsReturned.stream()
                    .filter(donationReturned -> donationFinal.getAmount() == donationReturned.getAmount())
                    .filter(donationReturned -> donationFinal.getAccount().getId().equals(donationReturned.getAccount().getId()))
                    .filter(donationReturned -> donationFinal.getCampaign().getId().equals(donationReturned.getCampaign().getId()))
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
        Set<DonationEntity> donations = donationRepository.findAllByAccountId(account.getId());
        Assert.assertEquals(Integer.valueOf(nbDonations), Integer.valueOf(donations.size()));
    }
}
