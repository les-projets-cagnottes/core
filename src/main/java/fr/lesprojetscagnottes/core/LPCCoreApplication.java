package fr.lesprojetscagnottes.core;

import com.google.gson.Gson;
import fr.lesprojetscagnottes.core.authentication.ApiTokenRepository;
import fr.lesprojetscagnottes.core.authentication.AuthenticationResponseEntity;
import fr.lesprojetscagnottes.core.authorization.entity.AuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.entity.OrganizationAuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.name.AuthorityName;
import fr.lesprojetscagnottes.core.authorization.name.OrganizationAuthorityName;
import fr.lesprojetscagnottes.core.authorization.repository.AuthorityRepository;
import fr.lesprojetscagnottes.core.authorization.repository.OrganizationAuthorityRepository;
import fr.lesprojetscagnottes.core.budget.repository.BudgetRepository;
import fr.lesprojetscagnottes.core.common.scheduler.MainScheduler;
import fr.lesprojetscagnottes.core.common.security.TokenProvider;
import fr.lesprojetscagnottes.core.common.strings.ScheduleParamsCommon;
import fr.lesprojetscagnottes.core.common.strings.StringGenerator;
import fr.lesprojetscagnottes.core.donation.task.DonationProcessingTask;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.OrganizationRepository;
import fr.lesprojetscagnottes.core.schedule.ScheduleEntity;
import fr.lesprojetscagnottes.core.schedule.ScheduleName;
import fr.lesprojetscagnottes.core.schedule.ScheduleRepository;
import fr.lesprojetscagnottes.core.user.UserEntity;
import fr.lesprojetscagnottes.core.user.UserGenerator;
import fr.lesprojetscagnottes.core.user.UserRepository;
import fr.lesprojetscagnottes.core.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class LPCCoreApplication {

	@Autowired
	private DataSource datasource;

	@Autowired
	private Gson gson;

	@Autowired
	private MainScheduler mainScheduler;

	@Autowired
	private DonationProcessingTask donationProcessingTask;

	@Autowired
	private UserGenerator userGenerator;

	@Autowired
	private ApiTokenRepository apiTokenRepository;

	@Autowired
	private AuthorityRepository authorityRepository;

	@Autowired
	private BudgetRepository budgetRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private OrganizationAuthorityRepository organizationAuthorityRepository;

	@Autowired
	private ScheduleRepository scheduleRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TokenProvider jwtTokenUtil;

	@Autowired
	private UserService userService;

	@Value("${fr.lesprojetscagnottes.admin_password}")
	private String adminPassword;

	@Value("${fr.lesprojetscagnottes.core.storage}")
	private String storageFolder;

	@Value("${fr.lesprojetscagnottes.slack.enabled}")
	private boolean slackEnabled;

	@Value("${spring.datasource.driver-class-name}")
	private String datasourceDriverClassName;

	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		context = SpringApplication.run(LPCCoreApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {

		// Execute src/main/resources/create.sql file
		if (!datasourceDriverClassName.equals("org.postgresql.Driver")) {
			log.warn("File 'create.sql' will not be executed as the datasource is not configured for postgresql");
		} else {
			ClassLoader classLoader = getClass().getClassLoader();
			URL resource = classLoader.getResource("create.sql");
			if (resource == null) {
				String error = "File 'create.sql' not found";
				log.error(error);
				shutdown();
			} else {
				try {
					ScriptUtils.executeSqlScript(
							datasource.getConnection(),
							new EncodedResource(new FileSystemResource(resource.getFile()), "UTF-8"),
							false,
							false,
							ScriptUtils.DEFAULT_COMMENT_PREFIX,
							";;",
							ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER,
							ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);
				} catch (SQLException e) {
					String error = "Error while executing 'create.sql' file";
					log.error(error, e);
					shutdown();
				}
			}
		}

		UserEntity admin = null;

		// First launch of App
		if (authorityRepository.count() == 0) {

			// Creation of every roles in database
			for (AuthorityName authorityName : AuthorityName.values()) {
				authorityRepository.save(new AuthorityEntity(authorityName));
			}

			userGenerator.init(); // Refresh authorities

			String email = "admin";
			String password = Objects.requireNonNullElseGet(adminPassword, StringGenerator::randomString);
			admin = UserGenerator.newUser(email, password);
			admin.setUsername("admin");
			admin.setFirstname("Administrator");
			admin.setAvatarUrl("https://eu.ui-avatars.com/api/?name=Administrator");
			admin.setEnabled(true);
			admin.addAuthority(authorityRepository.findByName(AuthorityName.ROLE_ADMIN));
			admin = userRepository.save(admin);

			// Creation of a default organization
			OrganizationEntity organization = new OrganizationEntity();
			organization.setName("Les Projets Cagnottes");
			organization.setSocialName("Les Projets Cagnottes");
			organization.setLogoUrl("https://eu.ui-avatars.com/api/?name=Les+Projets+Cagnottes");
			organization.getMembers().add(admin);
			organization = organizationRepository.save(organization);

			// Create authorities
			for(OrganizationAuthorityName authorityName : OrganizationAuthorityName.values()) {
				organizationAuthorityRepository.save(new OrganizationAuthorityEntity(organization, authorityName));
			}

			// Creation of default reminders
			Map<String, String> params = new HashMap<>();
			params.put(ScheduleParamsCommon.SLACK_TEMPLATE, "slack/fr/idea-reminder");
			ScheduleEntity schedule = new ScheduleEntity();
			schedule.setType(ScheduleName.REMINDER);
			schedule.setPlanning("0 0 10 1 * *");
			schedule.setParams(gson.toJson(params));
			scheduleRepository.save(schedule);

			// If password was generated, we print it in the console
			if (adminPassword == null) {
				log.info("ONLY PRINTED ONCE - Default credentials are : admin / " + password);
			}

		}

		// If Slack is enabled, we create a dedicated user account
		if(slackEnabled) {
			if(admin == null) {
				admin = userRepository.findByEmail("admin");
			}
			List<AuthenticationResponseEntity> apiTokens = apiTokenRepository.findAllByDescription("slack-events-catcher");
			AuthenticationResponseEntity apiToken;
			if(apiTokens.size() == 1) {
				apiToken = apiTokens.get(0);
			} else if(apiTokens.size() == 0) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.YEAR, 1);
				Date nextYear = cal.getTime();
				Authentication authentication = new UsernamePasswordAuthenticationToken(admin, null, userService.getAuthorities(admin.getId()));
				apiToken = new AuthenticationResponseEntity(jwtTokenUtil.generateToken(authentication, nextYear));
				apiToken.setDescription("slack-events-catcher");
				apiToken.setExpiration(nextYear);
				apiToken.setUser(admin);
			} else {
				log.error("Too many tokens registered for slack-events-catcher");
				return;
			}
			prepareDirectories("config");
			String token = apiTokenRepository.save(apiToken).getToken();
			String tokenFilePath = storageFolder + File.separator + "config" + File.separator + "slack-events-catcher-token";
			FileWriter myWriter;
			try {
				myWriter = new FileWriter(tokenFilePath);
				myWriter.write(token);
				myWriter.close();
			} catch (IOException e) {
				log.debug("Cannot save slack-events-catcher token in {}", tokenFilePath);
			}
		}

		prepareDirectories("img");
		mainScheduler.schedule();
		new Timer().schedule(donationProcessingTask, 0, 500);
	}

	public void prepareDirectories(String directoryPath) {
		File directory = new File(storageFolder);
		if (!directory.exists()) {
			log.info("Creating path {}", directory.getPath());
			if(!directory.mkdirs()) {
				log.error("The path {} could not be created", directory.getPath());
			}
		}
		if (!directory.isDirectory()) {
			log.error("The path {} is not a directory", directory.getPath());
		}

		if (directoryPath != null && !directoryPath.isEmpty()) {
			directory = new File(storageFolder + File.separator + directoryPath.replaceAll("/", File.separator));
			log.debug("Prepare directory {}", directory.getAbsolutePath());
			if (!directory.exists()) {
				log.info("Creating path {}", directory.getPath());
				if(!directory.mkdirs()) {
					log.error("Cannot create directory {}", directory.getAbsolutePath());
				}
			}
			if (!directory.isDirectory()) {
				log.error("The path {} is not a directory", directory.getPath());
			}
		}
	}

	public void shutdown() {
		context.close();
	}
}

