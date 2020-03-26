package fr.thomah.valyou.steps;

import fr.thomah.valyou.component.CucumberContext;
import fr.thomah.valyou.controller.UserController;
import fr.thomah.valyou.repository.*;
import io.cucumber.java.en.Given;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashSet;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommonStepDefinitions {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonStepDefinitions.class);

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CucumberContext context;

    @Given("Empty database")
    public void emptyDatabase() {
        budgetRepository.findAll().forEach(budget -> {
            budget.setProjects(new LinkedHashSet<>());
            budgetRepository.save(budget);
        });

        projectRepository.deleteAll();
        budgetRepository.deleteAll();
        contentRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        context.reset();
    }

}
