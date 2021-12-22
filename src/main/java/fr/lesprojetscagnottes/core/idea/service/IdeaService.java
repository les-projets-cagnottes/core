package fr.lesprojetscagnottes.core.idea.service;

import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.ForbiddenException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.common.strings.StringsCommon;
import fr.lesprojetscagnottes.core.idea.entity.IdeaEntity;
import fr.lesprojetscagnottes.core.idea.model.IdeaModel;
import fr.lesprojetscagnottes.core.idea.repository.IdeaRepository;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.repository.OrganizationRepository;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import fr.lesprojetscagnottes.core.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;
import java.util.Date;

@Slf4j
@Service
public class IdeaService {

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserService userService;

    public IdeaModel create(Principal principal, IdeaModel model) {

        // Fails if any of references are null
        if(model == null || model.getShortDescription() == null || model.getShortDescription().isEmpty()) {
            if(model != null ) {
                log.error("Impossible to create idea : some references are missing");
            } else {
                log.error("Impossible to create a null idea");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        OrganizationEntity organization = organizationRepository.findById(model.getOrganization().getId()).orElse(null);

        // Fails if any of references are null
        if(organization == null) {
            log.error("Impossible to create idea : one or more reference(s) doesn't exist");
            throw new NotFoundException();
        }

        // Verify that principal is member of organizations
        UserEntity userLoggedIn = userService.get(principal);
        Long userLoggedInId = userLoggedIn.getId();
        if(!userService.isMemberOfOrganization(userLoggedInId, model.getOrganization().getId()) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to create idea : principal {} is not member of organization {}", userLoggedInId, model.getOrganization().getId());
            throw new ForbiddenException();
        }

        // Save idea
        IdeaEntity idea = new IdeaEntity();
        idea.setIcon(model.getIcon());
        idea.setShortDescription(model.getShortDescription());
        idea.setLongDescription(model.getLongDescription());
        idea.setHasAnonymousCreator(model.getHasAnonymousCreator());
        idea.setHasLeaderCreator(model.getHasLeaderCreator());
        idea.setWorkspace(model.getWorkspace());
        idea.setOrganization(organization);

        if(idea.getHasAnonymousCreator()) {
            idea.setCreatedBy(StringsCommon.ANONYMOUS);
            idea.setUpdatedBy(StringsCommon.ANONYMOUS);
        } else {
            idea.setCreatedBy(userLoggedIn.getEmail());
            idea.setUpdatedBy(userLoggedIn.getEmail());
            idea.setSubmitter(userLoggedIn);
        }
        idea.setCreatedAt(new Date());
        idea.setUpdatedAt(idea.getCreatedAt());

        return IdeaModel.fromEntity(ideaRepository.save(idea));
    }

    public IdeaModel update(Principal principal, @RequestBody IdeaModel model) {

        // Fails if any of references are null
        if(model == null || model.getId() <= 0 || model.getShortDescription() == null || model.getShortDescription().isEmpty()) {
            if(model != null ) {
                log.error("Impossible to update idea : some references are missing");
            } else {
                log.error("Impossible to update a null idea");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        IdeaEntity idea = ideaRepository.findById(model.getId()).orElse(null);
        OrganizationEntity organization = organizationRepository.findById(model.getOrganization().getId()).orElse(null);

        // Fails if any of references are null
        if(idea == null || organization == null) {
            log.error("Impossible to update idea : one or more reference(s) doesn't exist");
            throw new NotFoundException();
        }

        // Verify that principal is the author
        UserEntity userLoggedIn = userService.get(principal);
        Long userLoggedInId = userLoggedIn.getId();
        if((idea.getSubmitter() == null || !userLoggedInId.equals(idea.getSubmitter().getId())) && userService.isNotAdmin(userLoggedInId)) {
            log.error("Impossible to update idea : principal {} is not the author of the idea {}", userLoggedInId, model.getOrganization().getId());
            throw new ForbiddenException();
        }

        // Save idea
        idea.setIcon(model.getIcon());
        idea.setShortDescription(model.getShortDescription());
        idea.setLongDescription(model.getLongDescription());
        idea.setHasLeaderCreator(model.getHasLeaderCreator());
        idea.setWorkspace(model.getWorkspace());
        idea.setOrganization(organization);
        idea.setUpdatedAt(idea.getCreatedAt());

        if(idea.getHasAnonymousCreator()) {
            idea.setCreatedBy(StringsCommon.ANONYMOUS);
            idea.setUpdatedBy(StringsCommon.ANONYMOUS);
        } else {
            idea.setCreatedBy(userLoggedIn.getEmail());
            idea.setUpdatedBy(userLoggedIn.getEmail());
            idea.setSubmitter(userLoggedIn);
        }

        return IdeaModel.fromEntity(ideaRepository.save(idea));
    }
    
}
