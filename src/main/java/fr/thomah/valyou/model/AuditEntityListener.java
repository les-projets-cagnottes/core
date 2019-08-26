package fr.thomah.valyou.model;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditEntityListener implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of("System");
    }
}
