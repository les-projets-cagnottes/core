package fr.lesprojetscagnottes.core;

import com.google.gson.Gson;
import fr.lesprojetscagnottes.core.common.ScheduleParamsCommon;
import fr.lesprojetscagnottes.core.entity.*;
import fr.lesprojetscagnottes.core.generator.StringGenerator;
import fr.lesprojetscagnottes.core.generator.UserGenerator;
import fr.lesprojetscagnottes.core.repository.*;
import fr.lesprojetscagnottes.core.scheduler.MainScheduler;
import fr.lesprojetscagnottes.core.security.TokenProvider;
import fr.lesprojetscagnottes.core.service.UserService;
import fr.lesprojetscagnottes.core.task.DonationProcessingTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@SpringBootApplication
@EnableScheduling
public class LPCCoreApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(LPCCoreApplication.class);

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

	@Value("${fr.lesprojetscagnottes.adminPassword}")
	private String adminPassword;

	@Value("${fr.lesprojetscagnottes.core.storage}")
	private String storageFolder;

	@Value("${fr.lesprojetscagnottes.slack.enabled}")
	private boolean slackEnabled;

	public static void main(String[] args) {
		SpringApplication.run(LPCCoreApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {

		User admin = null;

		// First launch of App
		if (authorityRepository.count() == 0) {

			// Creation of every roles in database
			for (AuthorityName authorityName : AuthorityName.values()) {
				authorityRepository.save(new Authority(authorityName));
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
			Organization organization = new Organization();
			organization.setName("Les Projets Cagnottes");
			organization.setSocialName("Les Projets Cagnottes");
			organization.setLogoUrl("https://eu.ui-avatars.com/api/?name=Les+Projets+Cagnottes");
			organization.getMembers().add(admin);
			organization = organizationRepository.save(organization);

			// Create authorities
			for(OrganizationAuthorityName authorityName : OrganizationAuthorityName.values()) {
				organizationAuthorityRepository.save(new OrganizationAuthority(organization, authorityName));
			}

			// Creation of default reminders
			Map<String, String> params = new HashMap<>();
			params.put(ScheduleParamsCommon.SLACK_TEMPLATE, "slack/fr/idea-reminder");
			Schedule schedule = new Schedule();
			schedule.setType(ScheduleType.REMINDER);
			schedule.setPlanning("0 0 8 1W 1/1 *");
			schedule.setParams(gson.toJson(params));
			scheduleRepository.save(schedule);

			// If password was generated, we print it in the console
			if (adminPassword == null) {
				LOGGER.info("ONLY PRINTED ONCE - Default credentials are : admin / " + password);
			}

		}

		// If Slack is enabled, we create a dedicated user account
		if(slackEnabled) {
			if(admin == null) {
				admin = userRepository.findByEmail("admin");
			}
			List<AuthenticationResponse> apiTokens = apiTokenRepository.findAllByDescription("slack-events-catcher");
			AuthenticationResponse apiToken;
			if(apiTokens.size() == 1) {
				apiToken = apiTokens.get(0);
			} else if(apiTokens.size() == 0) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.YEAR, 1);
				Date nextYear = cal.getTime();
				Authentication authentication = new UsernamePasswordAuthenticationToken(admin, null, userService.getAuthorities(admin.getId()));
				apiToken = new AuthenticationResponse(jwtTokenUtil.generateToken(authentication, nextYear));
				apiToken.setDescription("slack-events-catcher");
				apiToken.setExpiration(nextYear);
				apiToken.setUser(admin);
			} else {
				LOGGER.error("Too many tokens registered for slack-events-catcher");
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
				LOGGER.debug("Cannot save slack-events-catcher token in {}", tokenFilePath);
			}
		}

		mainScheduler.schedule();
		new Timer().schedule(donationProcessingTask, 0, 500);
	}

	public void prepareDirectories(String directoryPath) {
		File directory = new File(storageFolder);
		if (!directory.exists()) {
			LOGGER.info("Creating path {}", directory.getPath());
			if(!directory.mkdirs()) {
				LOGGER.error("The path {} could not be created", directory.getPath());
			}
		}
		if (!directory.isDirectory()) {
			LOGGER.error("The path {} is not a directory", directory.getPath());
		}

		if (directoryPath != null && !directoryPath.isEmpty()) {
			directory = new File(storageFolder + File.separator + directoryPath.replaceAll("/", File.separator));
			LOGGER.debug("Prepare directory {}", directory.getAbsolutePath());
			if (!directory.exists()) {
				LOGGER.info("Creating path {}", directory.getPath());
				if(!directory.mkdirs()) {
					LOGGER.error("Cannot create directory {}", directory.getAbsolutePath());
				}
			}
			if (!directory.isDirectory()) {
				LOGGER.error("The path {} is not a directory", directory.getPath());
			}
		}
	}
}

