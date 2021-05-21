package fr.lesprojetscagnottes.core;

import com.google.gson.Gson;
import fr.lesprojetscagnottes.core.common.ScheduleParamsCommon;
import fr.lesprojetscagnottes.core.entity.*;
import fr.lesprojetscagnottes.core.generator.StringGenerator;
import fr.lesprojetscagnottes.core.generator.UserGenerator;
import fr.lesprojetscagnottes.core.repository.*;
import fr.lesprojetscagnottes.core.scheduler.MainScheduler;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

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

	@Value("${fr.lesprojetscagnottes.adminPassword}")
	private String adminPassword;

	public static void main(String[] args) {
		SpringApplication.run(LPCCoreApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {

		// First launch of App
		if (authorityRepository.count() == 0) {

			// Creation of every roles in database
			for (AuthorityName authorityName : AuthorityName.values()) {
				authorityRepository.save(new Authority(authorityName));
			}

			userGenerator.init(); // Refresh authorities

			String email = "admin";
			String password;
			if (adminPassword != null) {
				password = adminPassword;
			} else {
				password = StringGenerator.randomString();
			}
			User admin = UserGenerator.newUser(email, password);
			admin.setUsername("admin");
			admin.setFirstname("Administrator");
			admin.setAvatarUrl("https://eu.ui-avatars.com/api/?name=Administrator");
			admin.setEnabled(true);
			admin.addAuthority(authorityRepository.findByName(AuthorityName.ROLE_ADMIN));
			userRepository.save(admin);

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

		mainScheduler.schedule();
		new Timer().schedule(donationProcessingTask, 0, 500);
	}

}

