package fr.thomah.valyou.controller;

import fr.thomah.valyou.model.Content;
import fr.thomah.valyou.model.Organization;
import fr.thomah.valyou.repository.ContentRepository;
import fr.thomah.valyou.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContentController {

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @RequestMapping(value = "/api/content", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public void create(@RequestBody Content content) {
        content = contentRepository.save(content);
        for(Organization org : content.getOrganizations()) {
            Organization organization = organizationRepository.findById(org.getId()).orElse(null);
            if(organization != null) {
                organization.getContents().add(content);
                organizationRepository.save(organization);
            }
        }
    }

}
