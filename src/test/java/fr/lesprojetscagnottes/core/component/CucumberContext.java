package fr.lesprojetscagnottes.core.component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.lesprojetscagnottes.core.authorization.entity.OrganizationAuthorityEntity;
import fr.lesprojetscagnottes.core.budget.entity.AccountEntity;
import fr.lesprojetscagnottes.core.budget.entity.BudgetEntity;
import fr.lesprojetscagnottes.core.campaign.CampaignEntity;
import fr.lesprojetscagnottes.core.common.strings.StringsCommon;
import fr.lesprojetscagnottes.core.content.entity.ContentEntity;
import fr.lesprojetscagnottes.core.authentication.model.AuthenticationResponseModel;
import fr.lesprojetscagnottes.core.idea.IdeaEntity;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import fr.lesprojetscagnottes.core.project.entity.ProjectEntity;
import fr.lesprojetscagnottes.core.user.UserEntity;
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

    private Map<String, AccountEntity> accounts = new HashMap<>();
    private Map<String, AuthenticationResponseModel> auths = new HashMap<>();
    private Map<String, OrganizationAuthorityEntity> organizationAuthorities = new HashMap<>();
    private Map<String, OrganizationEntity> organizations = new HashMap<>();
    private Map<String, UserEntity> users = new HashMap<>();
    private Map<String, ContentEntity> contents = new HashMap<>();
    private Map<String, BudgetEntity> budgets = new HashMap<>();
    private Map<String, ProjectEntity> projects = new HashMap<>();
    private Map<String, CampaignEntity> campaigns = new HashMap<>();
    private Map<String, IdeaEntity> ideas = new HashMap<>();

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
