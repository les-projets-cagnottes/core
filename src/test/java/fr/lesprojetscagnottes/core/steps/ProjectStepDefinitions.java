package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.component.AuthenticationHttpClient;
import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.component.ProjectHttpClient;
import fr.lesprojetscagnottes.core.project.ProjectEntity;
import fr.lesprojetscagnottes.core.authentication.model.AuthenticationResponseModel;
import fr.lesprojetscagnottes.core.project.ProjectModel;
import fr.lesprojetscagnottes.core.project.ProjectRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
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

        ProjectEntity project;
        for (Map<String, String> columns : rows) {

            // Create project
            project = new ProjectEntity();
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

        ProjectEntity project;
        for (Map<String, String> columns : rows) {

            // Create project
            project = new ProjectEntity();
            project.getOrganizations().add(context.getOrganizations().get(columns.get("organization")));
            project.setTitle(columns.get("title"));
            project.setShortDescription(columns.get("shortDescription"));
            project.setLongDescription(columns.get("longDescription"));
            project.setPeopleRequired(Integer.parseInt(columns.get("peopleRequired")));
            project.setLeader(context.getUsers().get(userFirstname));

            // Refresh Token
            authenticationHttpClient.setBearerAuth(context.getAuths().get(userFirstname).getToken());
            AuthenticationResponseModel response = authenticationHttpClient.refresh();
            context.getAuths().put(userFirstname, response);

            // Make donation
            projectHttpClient.setBearerAuth(response.getToken());
            projectHttpClient.post(ProjectModel.fromEntity(project));
        }
    }


    @Then("Following projects are registered")
    public void followingProjectsAreRegistered(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        List<ProjectEntity> projectsReturned = projectRepository.findAll();

        ProjectEntity project;
        for (Map<String, String> columns : rows) {

            // Create project from feature
            project = new ProjectEntity();
            project.getOrganizations().add(context.getOrganizations().get(columns.get("organization")));
            project.setTitle(columns.get("title"));
            project.setShortDescription(columns.get("shortDescription"));
            project.setLongDescription(columns.get("longDescription"));
            project.setPeopleRequired(Integer.parseInt(columns.get("peopleRequired")));
            final ProjectEntity projectFinal = project;

            projectsReturned.stream()
                    .filter(projectReturned -> projectFinal.getTitle().equals(projectReturned.getTitle()))
                    .filter(projectReturned -> projectFinal.getShortDescription().equals(projectReturned.getShortDescription()))
                    .filter(projectReturned -> projectFinal.getLongDescription().equals(projectReturned.getLongDescription()))
                    .filter(projectReturned -> projectFinal.getPeopleRequired().equals(projectReturned.getPeopleRequired()))
                    .findAny()
                    .ifPresentOrElse(
                            projectsReturned::remove,
                            Assert::fail);
        }

        Assert.assertEquals(0, projectsReturned.size());
    }

}
