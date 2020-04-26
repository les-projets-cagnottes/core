package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.entity.Content;
import fr.lesprojetscagnottes.core.repository.ContentRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class ContentStepDefinitions {

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private CucumberContext context;

    @And("The following contents are saved")
    public void theFollowingContentsAreSaved(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        Content content;
        for (Map<String, String> columns : rows) {

            // Create content
            content = new Content();
            content.setName(columns.get("name"));
            content.setValue(columns.get("value"));
            content.getOrganizations().add(context.getOrganizations().get(columns.get("organization")));
            content = contentRepository.save(content);

            // Save in Test Map
            context.getContents().put(content.getName(), content);
        }
    }

}
