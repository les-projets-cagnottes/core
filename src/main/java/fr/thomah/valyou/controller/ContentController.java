package fr.thomah.valyou.controller;

import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.entity.Content;
import fr.thomah.valyou.entity.Organization;
import fr.thomah.valyou.repository.ContentRepository;
import fr.thomah.valyou.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

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

    @RequestMapping(value = "/api/content", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public Content update(@RequestBody Content content) {
        Content contentInDb = contentRepository.findById(content.getId()).orElse(null);
        if (contentInDb == null) {
            throw new NotFoundException();
        } else {
            Set<Organization> organizations = content.getOrganizations();
            organizations.forEach(organization -> {
                Organization organizationInDb = organizationRepository.findById(organization.getId()).orElse(null);
                if (organizationInDb == null) {
                    throw new NotFoundException();
                } else {
                    if (organizationInDb.getContents().stream().noneMatch(prj -> contentInDb.getId().equals(prj.getId()))) {
                        organizationInDb.getContents().add(content);
                    }
                    organization = organizationInDb;
                }
            });
            contentInDb.setOrganizations(organizations);
            contentInDb.setName(content.getName());
            contentInDb.setValue(content.getValue());
            return contentRepository.save(contentInDb);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/content", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit", "organizationId"})
    public Page<Content> getByOrganizationId(@RequestParam("offset") int offset, @RequestParam("limit") int limit, @RequestParam("organizationId") Long organizationId) {
        if(offset < 0) {
            offset = 0;
        }
        Pageable pageable = PageRequest.of(offset, limit);
        return contentRepository.findAllByOrganizations_Id(pageable, organizationId);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/content", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"organizationId"})
    public Set<Content> getByOrganizationId(@RequestParam("organizationId") Long organizationId) {
        return contentRepository.findAllByOrganizations_Id(organizationId);
    }

}
