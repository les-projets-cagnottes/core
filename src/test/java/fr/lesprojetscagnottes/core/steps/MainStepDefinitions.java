package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.component.CucumberContext;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class MainStepDefinitions {

    @Autowired
    private CucumberContext context;

    @Then("Last HTTP code was {string}")
    public void lastHTTPCodeWas(String code) {
        assertEquals(code, String.valueOf(context.getLastHttpCode()));
    }
}
