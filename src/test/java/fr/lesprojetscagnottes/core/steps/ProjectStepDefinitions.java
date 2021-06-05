package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.component.AuthenticationHttpClient;
import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.component.ProjectHttpClient;
import fr.lesprojetscagnottes.core.entity.AuthenticationResponse;
import fr.lesprojetscagnottes.core.entity.Project;
import fr.lesprojetscagnottes.core.repository.ProjectRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class ProjectStepDefinitions {

    @Autowired
    private AuthenticationHttpClient authenticationHttpClient;

    @Autowired
    private ProjectHttpClient projectHttpClient;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CucumberContext context;

    @Given("The following projects are created")
    public void theFollowingProjectsAreCreated(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Project project;
        for (Map<String, String> columns : rows) {

            // Create project
            project = new Project();
            project.setLeader(context.getUsers().get(columns.get("leader")));
            project.setTitle(columns.get("title"));
            project.setShortDescription(columns.get("shortDescription"));
            project.setLongDescription(columns.get("longDescription"));
            project.setPeopleRequired(Integer.parseInt(columns.get("peopleRequired")));
            project = projectRepository.save(project);

            // Save in Test Map
            context.getProjects().put(project.getTitle(), project);
        }
    }

    @When("{string} creates the following projects")
    public void createsTheFollowingProjects(String userFirstname, DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Project project;
        for (Map<String, String> columns : rows) {

            // Create project
            project = new Project();
            project.getOrganizations().add(context.getOrganizations().get(columns.get("organization")));
            project.setTitle(columns.get("title"));
            project.setShortDescription(columns.get("shortDescription"));
            project.setLongDescription(columns.get("longDescription"));
            project.setPeopleRequired(Integer.parseInt(columns.get("peopleRequired")));
            project.setLeader(context.getUsers().get(userFirstname));

            // Refresh Token
            authenticationHttpClient.setBearerAuth(context.getAuths().get(userFirstname).getToken());
            AuthenticationResponse response = authenticationHttpClient.refresh();
            context.getAuths().put(userFirstname, response);

            // Make donation
            projectHttpClient.setBearerAuth(response.getToken());
            projectHttpClient.post(project);
        }
    }


}
