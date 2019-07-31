package fr.thomah.valyou;

import fr.thomah.valyou.generator.UserGenerator;
import fr.thomah.valyou.model.Authority;
import fr.thomah.valyou.model.AuthorityName;
import fr.thomah.valyou.model.User;
import fr.thomah.valyou.repository.AuthorityRepository;
import fr.thomah.valyou.repository.UserRepository;
import fr.thomah.valyou.generator.StringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

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

			String email = "admin@valyou.fr";
			String generatedPassword = StringGenerator.randomString();
			admin = UserGenerator.newUser(email, generatedPassword);
			admin.setFirstname("Administrator");
			admin.addAuthority(authorityRepository.findByName(AuthorityName.ROLE_ADMIN));
			LOGGER.info("ONLY PRINTED ONCE - Default credentials are : admin@valyou.fr / " + generatedPassword);
			userRepository.save(admin);
		}
	}

}

