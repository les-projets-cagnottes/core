package fr.thomah.valyou;

import fr.thomah.valyou.generator.UserGenerator;
import fr.thomah.valyou.model.*;
import fr.thomah.valyou.repository.*;
import fr.thomah.valyou.generator.StringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Month;

@SpringBootApplication
@EnableJpaAuditing
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
		User admin = userRepository.findByEmail("admin@valyou.fr");

		// First launch of App
		if(admin == null) {

			// Creation of every roles in database
			for(AuthorityName authorityName : AuthorityName.values()) {
				authorityRepository.save(new Authority(authorityName));
			}
			userGenerator.init(); // Refresh authorities

			Organization organization = new Organization();
			organization.setName("Valyou");
			organization = organizationRepository.save(organization);

			for(OrganizationAuthorityName authorityName : OrganizationAuthorityName.values()) {
				organizationAuthorityRepository.save(new OrganizationAuthority(organization, authorityName));
			}

			String email = "admin@valyou.fr";
			String generatedPassword = StringGenerator.randomString();
			admin = UserGenerator.newUser(email, generatedPassword);
			admin.setFirstname("Administrator");
			admin.addAuthority(authorityRepository.findByName(AuthorityName.ROLE_ADMIN));
			admin.addOrganizationAuthority(organizationAuthorityRepository.findByOrganizationAndName(organization, OrganizationAuthorityName.ROLE_MEMBER));
			admin.addOrganizationAuthority(organizationAuthorityRepository.findByOrganizationAndName(organization, OrganizationAuthorityName.ROLE_OWNER));
			admin.addOrganization(organization);
			userRepository.save(admin);

			organization.addMember(admin);
			organization = organizationRepository.save(organization);

			Budget budget = new Budget();
			budget.setName("My budget");
			budget.setAmountPerMember(250f);
			budget.setStartDate(Date.valueOf(LocalDate.of(2019, Month.JANUARY, 1)));
			budget.setEndDate(Date.valueOf(LocalDate.of(2019, Month.DECEMBER, 31)));
			budget.setOrganization(organization);
			budget.setSponsor(admin);
			budgetRepository.save(budget);

			LOGGER.info("ONLY PRINTED ONCE - Default credentials are : admin@valyou.fr / " + generatedPassword);
		}
	}

}

