package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.component.AuthenticationHttpClient;
import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.entity.*;
import fr.lesprojetscagnottes.core.model.AuthenticationResponseModel;
import fr.lesprojetscagnottes.core.repository.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static fr.lesprojetscagnottes.core.component.CucumberContext.generateId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class CommonStepDefinitions {

    @Autowired
    private AuthenticationHttpClient authenticationHttpClient;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CucumberContext context;

    @Given("Empty database")
    public void emptyDatabase() {
        budgetRepository.findAll().forEach(budget -> {
            budget.setCampaigns(new LinkedHashSet<>());
            budgetRepository.save(budget);
        });

        organizationRepository.findAll().forEach(organization -> {
            organization.setMembers(new LinkedHashSet<>());
            organization.setCampaigns(new LinkedHashSet<>());
            organization.setProjects(new LinkedHashSet<>());
            organizationRepository.save(organization);
        });

        campaignRepository.deleteAll();
        projectRepository.deleteAll();
        accountRepository.deleteAll();
        budgetRepository.deleteAll();
        contentRepository.deleteAll();
        ideaRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        context.reset();
    }

    @Given("{string} is logged in")
    public void userIsLoggedIn(String userFirstname) {

        User user = new User();
        user.setEmail(context.getUsers().get(userFirstname).getEmail());
        user.setPassword(context.getUsers().get(userFirstname).getPassword());
        AuthenticationResponseModel response = authenticationHttpClient.login(user.getEmail(), user.getPassword());

        assertNotNull(response);
        assertFalse(response.getToken().isEmpty());

        context.getAuths().put(userFirstname, response);
    }

    @Given("The following organizations are registered")
    public void theFollowingOrganizationsAreRegistered(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Organization organization;
        OrganizationAuthority organizationAuthority;
        for (Map<String, String> columns : rows) {

            // Create organization
            organization = new Organization();
            organization.setName(columns.get("name"));
            organization = organizationRepository.save(organization);

            // Create organization's authorities
            for (OrganizationAuthorityName authorityName : OrganizationAuthorityName.values()) {
                organizationAuthority = new OrganizationAuthority(organization, authorityName);
                organizationAuthority = organizationAuthorityRepository.save(organizationAuthority);
                organization.getOrganizationAuthorities().add(organizationAuthority);
                context.getOrganizationAuthorities().put(organization.getName() + authorityName.name(), organizationAuthority);
            }
            organization = organizationRepository.save(organization);

            // Save in Test Map
            context.getOrganizations().put(organization.getName(), organization);
        }
    }

    @Given("The following organizations are not registered")
    public void theFollowingOrganizationsAreNotRegistered(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Organization organization;
        OrganizationAuthority organizationAuthority;
        for (Map<String, String> columns : rows) {

            // Create organization
            organization = new Organization();
            organization.setId(generateId());
            organization.setName(columns.get("name"));

            // Create organization's authorities
            for (OrganizationAuthorityName authorityName : OrganizationAuthorityName.values()) {
                organizationAuthority = new OrganizationAuthority(organization, authorityName);
                organizationAuthority.setId(generateId());
                organization.getOrganizationAuthorities().add(organizationAuthority);
                context.getOrganizationAuthorities().put(organization.getName() + authorityName.name(), organizationAuthority);
            }

            // Save in Test Map
            context.getOrganizations().put(organization.getName(), organization);
        }
    }

}
