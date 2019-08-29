package fr.thomah.valyou.model;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditEntityListener implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Object object = SecurityContextHolder.getContext().getAuthentication();
        if(object == null) {
            return Optional.of("System");
        } else {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(!principal.toString().equals("anonymousUser")) {
                User user = (User) principal;
                return Optional.of(user.getEmail());
            } else {
                return Optional.of("System");
            }
        }
    }

}
