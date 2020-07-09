package fr.lesprojetscagnottes.core.component;

import fr.lesprojetscagnottes.core.entity.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Component
public class CucumberContext {

    private int lastHttpCode = 0;

    private Map<String, Account> accounts = new HashMap<>();
    private Map<String, AuthenticationResponse> auths = new HashMap<>();
    private Map<String, OrganizationAuthority> organizationAuthorities = new HashMap<>();
    private Map<String, Organization> organizations = new HashMap<>();
    private Map<String, User> users = new HashMap<>();
    private Map<String, Content> contents = new HashMap<>();
    private Map<String, Budget> budgets = new HashMap<>();
    private Map<String, Campaign> campaigns = new HashMap<>();
    private Map<String, Idea> ideas = new HashMap<>();

    public void reset() {
        accounts = new HashMap<>();
        campaigns = new HashMap<>();
        budgets = new HashMap<>();
        contents = new HashMap<>();
        ideas = new HashMap<>();
        users = new HashMap<>();
        organizationAuthorities = new HashMap<>();
        organizations = new HashMap<>();
    }

    public static Long generateId() {
        return ThreadLocalRandom.current().nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
    }

}
