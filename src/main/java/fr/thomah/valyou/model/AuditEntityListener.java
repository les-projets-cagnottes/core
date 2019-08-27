package fr.thomah.valyou.model;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.Optional;

public class AuditEntityListener implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Object object = SecurityContextHolder.getContext().getAuthentication();
        if(object == null) {
            return Optional.of("System");
        } else {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return Optional.of(user.getEmail());
        }
    }

}
