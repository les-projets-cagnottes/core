package fr.lesprojetscagnottes.core;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(strict = true,
        features = "classpath:features",
        plugin = {"pretty", "json:target/cucumber/report.json"},
        extraGlue = "fr.lesprojetscagnottes.core.component")
public class RunCucumberTest {
}