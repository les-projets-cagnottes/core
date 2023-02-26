package fr.lesprojetscagnottes.core.common.audit;

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
            System.out.println(principal.toString());
            if(!principal.toString().equals("anonymousUser") && principal.getClass().equals(String.class)) {
                return Optional.of(principal.toString());
            } else {
                return Optional.of("System");
            }
        }
    }

}
