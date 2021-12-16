package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.content.entity.ContentEntity;
import fr.lesprojetscagnottes.core.content.repository.ContentRepository;
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

        ContentEntity content;
        for (Map<String, String> columns : rows) {

            // Create content
            content = new ContentEntity();
            content.setName(columns.get("name"));
            content.setValue(columns.get("value"));
            content.setOrganization(context.getOrganizations().get(columns.get("organization")));
            content = contentRepository.save(content);

            // Save in Test Map
            context.getContents().put(content.getName(), content);
        }
    }

}
