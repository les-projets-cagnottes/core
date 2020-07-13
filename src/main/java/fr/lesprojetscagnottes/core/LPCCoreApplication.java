package fr.lesprojetscagnottes.core;

import fr.lesprojetscagnottes.core.entity.Authority;
import fr.lesprojetscagnottes.core.entity.AuthorityName;
import fr.lesprojetscagnottes.core.entity.Organization;
import fr.lesprojetscagnottes.core.entity.User;
import fr.lesprojetscagnottes.core.generator.StringGenerator;
import fr.lesprojetscagnottes.core.generator.UserGenerator;
import fr.lesprojetscagnottes.core.queue.DonationQueue;
import fr.lesprojetscagnottes.core.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Timer;

@SpringBootApplication
@EnableScheduling
public class LPCCoreApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(LPCCoreApplication.class);

	@Autowired
	private DonationQueue donationQueue;

	@Autowired
	private AuthorityRepository authorityRepository;

	@Autowired
	private UserGenerator userGenerator;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private OrganizationAuthorityRepository organizationAuthorityRepository;

	@Autowired
	private BudgetRepository budgetRepository;

	public static void main(String[] args) {
		SpringApplication.run(LPCCoreApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {

		// First launch of App
		if(authorityRepository.count() == 0) {

			// Creation of every roles in database
			for(AuthorityName authorityName : AuthorityName.values()) {
				authorityRepository.save(new Authority(authorityName));
			}

			userGenerator.init(); // Refresh authorities

			String email = "admin";
			String generatedPassword = StringGenerator.randomString();
			User admin = UserGenerator.newUser(email, generatedPassword);
			admin.setUsername("admin");
			admin.setFirstname("Administrator");
			admin.setAvatarUrl("https://eu.ui-avatars.com/api/?name=Administrator");
			admin.addAuthority(authorityRepository.findByName(AuthorityName.ROLE_ADMIN));
			userRepository.save(admin);

			// Creation of a default organization
			Organization organization = new Organization();
			organization.setName("Les Projets Cagnottes");
			organization.setLogoUrl("https://eu.ui-avatars.com/api/?name=Les+Projets+Cagnottes");
			organization.getMembers().add(admin);
			organizationRepository.save(organization);

			LOGGER.info("ONLY PRINTED ONCE - Default credentials are : admin / " + generatedPassword);
		}

		new Timer().schedule(donationQueue, 0, 500);
	}

}

