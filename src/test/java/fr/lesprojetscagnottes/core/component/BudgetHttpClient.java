package fr.lesprojetscagnottes.core.component;

import fr.lesprojetscagnottes.core.budget.model.BudgetModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class BudgetHttpClient extends GenericHttpClient {

    public void getOrganizationBudgets(Long organizationId) {
        get("/api/organization/" + organizationId + "/budgets");
    }

    public void create(final BudgetModel budget) {
        post("/api/budget", context.getGson().toJson(budget));
    }

    public void save(final BudgetModel budget) {
        put("/api/budget", context.getGson().toJson(budget));
    }

}
