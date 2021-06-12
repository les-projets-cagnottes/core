package fr.lesprojetscagnottes.core.common.audit;

import fr.lesprojetscagnottes.core.common.security.UserPrincipal;
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
                UserPrincipal user = (UserPrincipal) principal;
                return Optional.of(user.getUsername());
            } else {
                return Optional.of("System");
            }
        }
    }

}
