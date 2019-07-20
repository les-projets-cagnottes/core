package fr.thomah.valyou;

import fr.thomah.valyou.entity.User;
import fr.thomah.valyou.repository.UserRepository;
import org.apache.commons.codec.digest.DigestUtils;
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

	private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final Logger LOGGER = LoggerFactory.getLogger(ValyouApplication.class);

	@Autowired
	private UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(ValyouApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		User admin = userRepository.findByUsername("admin");
		if(admin == null) {
			String generatedPassword = randomString();
			LOGGER.info("ONLY PRINTED ONCE - Default credentials are : admin / " + generatedPassword);
			userRepository.save(new User("admin", DigestUtils.sha1Hex(generatedPassword)));
		}
	}

	private String randomString() {
		int count = 12;
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}
}

