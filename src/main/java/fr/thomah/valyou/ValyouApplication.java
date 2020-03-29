package fr.thomah.valyou;

import fr.thomah.valyou.generator.UserGenerator;
import fr.thomah.valyou.entity.*;
import fr.thomah.valyou.repository.*;
import fr.thomah.valyou.generator.StringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ValyouApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(ValyouApplication.class);

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
		SpringApplication.run(ValyouApplication.class, args);
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
			admin.addAuthority(authorityRepository.findByName(AuthorityName.ROLE_ADMIN));
			userRepository.save(admin);

			LOGGER.info("ONLY PRINTED ONCE - Default credentials are : admin / " + generatedPassword);
		}
	}

}

