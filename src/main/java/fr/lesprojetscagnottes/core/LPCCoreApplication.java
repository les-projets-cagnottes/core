package fr.lesprojetscagnottes.core;

import com.google.gson.Gson;
import fr.lesprojetscagnottes.core.authentication.ApiTokenRepository;
import fr.lesprojetscagnottes.core.authorization.entity.AuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.entity.OrganizationAuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.name.AuthorityName;
import fr.lesprojetscagnottes.core.authorization.name.OrganizationAuthorityName;
import fr.lesprojetscagnottes.core.authorization.repository.AuthorityRepository;
import fr.lesprojetscagnottes.core.authorization.repository.OrganizationAuthorityRepository;
import fr.lesprojetscagnottes.core.budget.repository.BudgetRepository;
import fr.lesprojetscagnottes.core.common.strings.StringGenerator;
import fr.lesprojetscagnottes.core.donation.task.DonationProcessingTask;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.repository.OrganizationRepository;
import fr.lesprojetscagnottes.core.providers.slack.task.CatcherTokenProviderTask;
import fr.lesprojetscagnottes.core.user.UserGenerator;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import fr.lesprojetscagnottes.core.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.io.File;
import java.util.Timer;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class LPCCoreApplication implements WebMvcConfigurer {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private DataSource datasource;

	@Autowired
	private Gson gson;

	@Autowired
	private DonationProcessingTask donationProcessingTask;

	@Autowired
	private CatcherTokenProviderTask catcherTokenProviderTask;

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
	private UserRepository userRepository;

	@Value(("${fr.lesprojetscagnottes.core.demo}"))
	private boolean demoMode;

	@Value("${fr.lesprojetscagnottes.admin_password}")
	private String adminPassword;

	@Value("${fr.lesprojetscagnottes.core.storage.root}")
	private String rootStorageFolder;

	@Value("${fr.lesprojetscagnottes.core.storage.data}")
	private String dataStorageFolder;

	@Value("${fr.lesprojetscagnottes.slack.enabled}")
	private boolean slackEnabled;

	@Value("${springdoc.swagger-ui.path}")
	private String swaggerUrl;

	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		context = SpringApplication.run(LPCCoreApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {

		UserEntity admin;

		// First launch of App
		if (demoMode || authorityRepository.count() == 0) {

			if(authorityRepository.count() == 0) {

				// Creation of every role in database
				for (AuthorityName authorityName : AuthorityName.values()) {
					authorityRepository.save(new AuthorityEntity(authorityName));
				}

				userGenerator.init(); // Refresh authorities
			}

			if(demoMode) {
				log.info("DEMO MODE ENABLED");
				ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator(false, false, "UTF-8", new ClassPathResource("demo/dataset.sql"));
				resourceDatabasePopulator.execute(datasource);
			}

			String email = "admin";
			String password = adminPassword;
			if(adminPassword == null || adminPassword.isEmpty()) {
				password = StringGenerator.randomString();
			}
			admin = UserGenerator.newUser(email, passwordEncoder.encode(password));
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

			// If password was generated, we print it in the console
			if (adminPassword == null || adminPassword.isEmpty()) {
				log.info("ONLY PRINTED ONCE - Default credentials are : admin / {}", password);
			}
		}

		if(slackEnabled) {
			new Timer().schedule(catcherTokenProviderTask, 0, 1200000);
		}

		prepareRootDirectories(dataStorageFolder);
		new Timer().schedule(donationProcessingTask, 0, 500);
	}

	public void prepareRootDirectories(String directoryPath) {
		File directory = new File(rootStorageFolder);
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
			directory = new File(rootStorageFolder + File.separator + directoryPath.replaceAll("/", File.separator));
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

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addRedirectViewController("/", StringUtils.isNotEmpty(swaggerUrl) ? swaggerUrl : "/");
	}
}

