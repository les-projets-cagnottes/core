package fr.lesprojetscagnottes.core.providers.microsoft.service;

import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.service.OrganizationService;
import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftTeamEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.model.MicrosoftTeamModel;
import fr.lesprojetscagnottes.core.providers.microsoft.repository.MicrosoftTeamRepository;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import fr.lesprojetscagnottes.core.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Slf4j
@Service
public class MicrosoftTeamService {

    @Autowired
    private MicrosoftGraphService microsoftGraphService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;

    @Autowired
    private MicrosoftTeamRepository microsoftTeamRepository;

    public MicrosoftTeamEntity findById(Long id) {
        return microsoftTeamRepository.findById(id).orElse(null);
    }

    public MicrosoftTeamModel findById(Principal principal, Long id) {

        // Verify that ID is correct
        if(id <= 0) {
            log.error("Impossible to get MS Teams by ID : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that entity exists
        MicrosoftTeamEntity entity = findById(id);
        if(entity == null) {
            log.error("Impossible to get MS Teams by ID : news not found");
            throw new NotFoundException();
        }

        // If the news is in an organization, verify that principal is in this organization
        if(entity.getOrganization() != null) {
            Long userLoggedInId = userService.get(principal).getId();
            if(!userService.isMemberOfOrganization(userLoggedInId, entity.getOrganization().getId()) && userService.isNotAdmin(userLoggedInId)) {
                log.error("Impossible to get MS Teams by ID : principal has not enough privileges");
                throw new ForbiddenException();
            }
        }

        // Transform and return organization
        return MicrosoftTeamModel.fromEntity(entity);
    }

    public MicrosoftTeamEntity findByOrganizationId(Long id) {
        return microsoftTeamRepository.findByOrganizationId(id);
    }

    public MicrosoftTeamEntity findByTenantId(String tenantId) {
        return microsoftTeamRepository.findByTenantId(tenantId);
    }

    public MicrosoftTeamEntity save(MicrosoftTeamModel msTeam) {

        // Retrieve full referenced objects
        OrganizationEntity organization = organizationService.findById(msTeam.getOrganization().getId());

        // Fails if any of references are null
        if(organization == null) {
            log.error("Impossible to save ms team {} : one or more reference(s) doesn't exist", msTeam);
            throw new NotFoundException();
        }

        // Retrieve MS Team if ID is provided
        MicrosoftTeamEntity msTeamToSave;
        if(msTeam.getId() > 0)  {
            msTeamToSave = findById(msTeam.getId());
            if(msTeamToSave == null) {
                log.error("Impossible to save ms team {} : it does not exist", msTeam);
                throw new NotFoundException();
            }
        } else {
            msTeamToSave = findByOrganizationId(msTeam.getOrganization().getId());
            if(msTeamToSave == null) {
                msTeamToSave = new MicrosoftTeamEntity();
            }
        }

        // Retrieve data from Microsoft Graph
        String token = microsoftGraphService.token(msTeam.getTenantId(), "https://graph.microsoft.com/.default", null, null);
        MicrosoftTeamEntity msTeamFromMsGraph = microsoftGraphService.getOrganization(token, msTeam.getTenantId());

        // Pass new values
        msTeamToSave.setAccessToken(token);
        msTeamToSave.setDisplayName(msTeamFromMsGraph.getDisplayName());
        msTeamToSave.setTenantId(msTeam.getTenantId());
        msTeamToSave.setOrganization(organization);

        // Save
        return microsoftTeamRepository.save(msTeamToSave);
    }

    public MicrosoftTeamModel save(Principal principal, MicrosoftTeamModel msTeam) {

        // Fails if any of references are null
        if(msTeam == null || msTeam.getOrganization() == null || msTeam.getTenantId() == null
                || msTeam.getOrganization().getId() == null || msTeam.getTenantId().length() <= 0) {
            if(msTeam != null ) {
                log.error("Impossible to save ms team {} : some references are missing", msTeam);
            } else {
                log.error("Impossible to save a null ms team");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        OrganizationEntity organization = organizationService.findById(msTeam.getOrganization().getId());

        // Fails if any of references are null
        if(organization == null) {
            log.error("Impossible to save ms team {} : one or more reference(s) doesn't exist", msTeam);
            throw new NotFoundException();
        }

        // Test that user logged in has correct rights
        UserEntity userLoggedIn = userService.get(principal);
        if(!userService.isOwnerOfOrganization(userLoggedIn.getId(), organization.getId()) && userService.isNotAdmin(userLoggedIn.getId())) {
            log.error("Impossible to save ms team {} : principal {} has not enough privileges", msTeam, userLoggedIn.getId());
            throw new ForbiddenException();
        }

        return MicrosoftTeamModel.fromEntity(this.save(msTeam));
    }

    public void delete(Principal principal, Long id) {

        // Verify that parameters are correct
        if(id <= 0) {
            log.error("Impossible disconnect MS Team from organization : ID is incorrect");
            throw new BadRequestException();
        }

        // Verify that MS Team exists
        MicrosoftTeamEntity msTeam = microsoftTeamRepository.findById(id).orElse(null);
        if(msTeam == null) {
            log.error("Impossible disconnect MS Team from organization : MS Team {} not found", id);
            throw new NotFoundException();
        }

        // Verify if principal has correct privileges
        if(msTeam.getOrganization() != null && msTeam.getOrganization().getId() > 0) {
            Long userLoggedInId = userService.get(principal).getId();
            if(!userService.isOwnerOfOrganization(userLoggedInId, msTeam.getOrganization().getId()) && userService.isNotAdmin(userLoggedInId)) {
                log.error("Impossible disconnect MS Team from organization : principal is not owner of organization {}", msTeam.getOrganization().getId());
                throw new ForbiddenException();
            }
        }

        // Delete MS Team
        microsoftTeamRepository.deleteById(id);
    }

}
