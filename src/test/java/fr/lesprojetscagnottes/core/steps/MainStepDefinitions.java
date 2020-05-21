package fr.lesprojetscagnottes.core.steps;

import fr.lesprojetscagnottes.core.component.CucumberContext;
import fr.lesprojetscagnottes.core.component.MainHttpClient;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class MainStepDefinitions {

    @Autowired
    private CucumberContext context;

    @Autowired
    MainHttpClient mainHttpClient;

    @When("Anyone checks the health of the app")
    public void anyoneChecksTheHealthOfTheApp() {
        context.setLastHttpCode(mainHttpClient.health());
    }

    @Then("Last HTTP code was {string}")
    public void lastHTTPCodeWas(String code) {
        assertEquals(code, String.valueOf(context.getLastHttpCode()));
    }
}
