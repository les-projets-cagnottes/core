package fr.lesprojetscagnottes.core.component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.lesprojetscagnottes.core.common.StringsCommon;
import fr.lesprojetscagnottes.core.entity.*;
import fr.lesprojetscagnottes.core.model.AuthenticationResponseModel;
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

    private String lastBody = StringsCommon.EMPTY_STRING;
    private int lastHttpCode = 0;

    private Map<String, Account> accounts = new HashMap<>();
    private Map<String, AuthenticationResponseModel> auths = new HashMap<>();
    private Map<String, OrganizationAuthority> organizationAuthorities = new HashMap<>();
    private Map<String, Organization> organizations = new HashMap<>();
    private Map<String, User> users = new HashMap<>();
    private Map<String, Content> contents = new HashMap<>();
    private Map<String, Budget> budgets = new HashMap<>();
    private Map<String, Project> projects = new HashMap<>();
    private Map<String, Campaign> campaigns = new HashMap<>();
    private Map<String, Idea> ideas = new HashMap<>();

    private Gson gson;

    public CucumberContext() {
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .create();
    }

    public <T> T deserialize(String jsonString, Class<T> clazz) {
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat("MM/dd/yy HH:mm:ss");

        Gson gson = builder.create();
        return gson.fromJson(jsonString, clazz);
    }

    public void reset() {
        accounts = new HashMap<>();
        campaigns = new HashMap<>();
        projects = new HashMap<>();
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
