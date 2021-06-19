package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.authorization.entity.OrganizationAuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.name.OrganizationAuthorityName;
import fr.lesprojetscagnottes.core.authorization.repository.OrganizationAuthorityRepository;
import fr.lesprojetscagnottes.core.budget.repository.AccountRepository;
import fr.lesprojetscagnottes.core.budget.repository.BudgetRepository;
import fr.lesprojetscagnottes.core.campaign.CampaignRepository;
import fr.lesprojetscagnottes.core.component.AuthenticationHttpClient;
import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.authentication.model.AuthenticationResponseModel;
import fr.lesprojetscagnottes.core.content.repository.ContentRepository;
import fr.lesprojetscagnottes.core.idea.IdeaRepository;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.OrganizationRepository;
import fr.lesprojetscagnottes.core.project.ProjectRepository;
import fr.lesprojetscagnottes.core.user.UserEntity;
import fr.lesprojetscagnottes.core.user.UserRepository;
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

        UserEntity user = new UserEntity();
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

        OrganizationEntity organization;
        OrganizationAuthorityEntity organizationAuthority;
        for (Map<String, String> columns : rows) {

            // Create organization
            organization = new OrganizationEntity();
            organization.setName(columns.get("name"));
            organization = organizationRepository.save(organization);

            // Create organization's authorities
            for (OrganizationAuthorityName authorityName : OrganizationAuthorityName.values()) {
                organizationAuthority = new OrganizationAuthorityEntity(organization, authorityName);
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

        OrganizationEntity organization;
        OrganizationAuthorityEntity organizationAuthority;
        for (Map<String, String> columns : rows) {

            // Create organization
            organization = new OrganizationEntity();
            organization.setId(generateId());
            organization.setName(columns.get("name"));

            // Create organization's authorities
            for (OrganizationAuthorityName authorityName : OrganizationAuthorityName.values()) {
                organizationAuthority = new OrganizationAuthorityEntity(organization, authorityName);
                organizationAuthority.setId(generateId());
                organization.getOrganizationAuthorities().add(organizationAuthority);
                context.getOrganizationAuthorities().put(organization.getName() + authorityName.name(), organizationAuthority);
            }

            // Save in Test Map
            context.getOrganizations().put(organization.getName(), organization);
        }
    }

}
