package fr.thomah.valyou.controller;

import fr.thomah.valyou.exception.AuthenticationException;
import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.generator.OrganizationGenerator;
import fr.thomah.valyou.model.*;
import fr.thomah.valyou.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Principal;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;

@RestController
public class OrganizationController {

    private static final String HTTP_PROXY = System.getenv("HTTP_PROXY");
    private static final String SLACK_CLIENT_ID = System.getenv("VALYOU_SLACK_CLIENT_ID");
    private static final String SLACK_CLIENT_SECRET = System.getenv("VALYOU_SLACK_CLIENT_SECRET");

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private OrganizationRepository repository;

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit"})
    public Page<Organization> list(@RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return repository.findAll(pageable);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Organization getById(@PathVariable("id") Long id) {
        Organization org = repository.findById(id).orElse(null);
        if(org == null) {
            throw new NotFoundException();
        } else {
            return org;
        }
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"member_id"})
    public Set<Organization> getUserOrganizations(Principal authUserToken, @RequestParam("member_id") Long memberId) {
        Set<Organization> organizations = repository.findByMembers_Id(memberId);
        for(Organization org : organizations) {
            for(Budget b : org.getBudgets()) {
                b.setTotalDonations(budgetRepository.getTotalDonations(b.getId()));
            }
        }
        return organizations;
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Organization create(@RequestBody Organization org, Principal owner) {
        org = repository.save(OrganizationGenerator.newOrganization(org));
        for(OrganizationAuthorityName authorityName : OrganizationAuthorityName.values()) {
            organizationAuthorityRepository.save(new OrganizationAuthority(org, authorityName));
        }
        OrganizationAuthority memberOrganizationAuthority = organizationAuthorityRepository.findByOrganizationAndName(org, OrganizationAuthorityName.ROLE_MEMBER);
        for(User member : org.getMembers()) {
            member = userRepository.findById(member.getId()).orElse(null);
            if(member != null) {
                member.addOrganizationAuthority(memberOrganizationAuthority);
                userRepository.save(member);
            }
        }
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) owner;
        User userOwner = (User) token.getPrincipal();
        userOwner = userRepository.findByEmail(userOwner.getEmail());
        userOwner.addOrganizationAuthority(organizationAuthorityRepository.findByOrganizationAndName(org, OrganizationAuthorityName.ROLE_OWNER));
        userRepository.save(userOwner);

        return org;
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public void save(@PathVariable("id") String id, @RequestBody Organization org) {
        Organization orgInDb = repository.findById(Long.valueOf(id)).orElse(null);
        if (orgInDb == null) {
            throw new NotFoundException();
        } else {
            orgInDb.setName(org.getName());
            orgInDb.setSlackTeamId(org.getSlackTeamId());
            repository.save(orgInDb);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void delete(@PathVariable("id") String id) {
        repository.deleteById(Long.valueOf(id));
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}/members", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit"})
    public Page<User> getMembers(@PathVariable("id") long id, @RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return userRepository.findByOrganizations_idOrderByIdAsc(id, pageable);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}/members", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void addMember(@PathVariable long id, @RequestBody long userId) {
        Organization org = repository.findById(id).orElse(null);
        User newMember = userRepository.findById(userId).orElse(null);
        if(org == null || newMember == null) {
            throw new NotFoundException();
        } else {
            OrganizationAuthority memberOrganizationAuthority = organizationAuthorityRepository.findByOrganizationAndName(org, OrganizationAuthorityName.ROLE_MEMBER);
            newMember.addOrganizationAuthority(memberOrganizationAuthority);
            org.getMembers().add(newMember);

            repository.save(org);
            userRepository.save(newMember);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}/members", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void removeMember(@PathVariable long id, @RequestParam long userId) {
        Organization org = repository.findById(id).orElse(null);
        User member = userRepository.findById(userId).orElse(null);
        if(org == null || member == null) {
            throw new NotFoundException();
        } else {
            Set<OrganizationAuthority> organizationAuthorities = new LinkedHashSet<>();
            member.getUserOrganizationAuthorities().stream().filter(organizationAuthority -> organizationAuthority.getOrganization().getId() == id).forEach(organizationAuthorities::add);
            member.getUserOrganizationAuthorities().removeAll(organizationAuthorities);
            userRepository.save(member);

            org.getMembers().remove(member);
            repository.save(org);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}/contents", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void removeContent(@PathVariable long id, @RequestParam long contentId) {
        Organization org = repository.findById(id).orElse(null);
        Content content = contentRepository.findById(contentId).orElse(null);
        if(org == null || content == null) {
            throw new NotFoundException();
        } else {
            org.getContents().remove(content);
            repository.save(org);
            if(content.getOrganizations().size() == 1) {
                contentRepository.deleteById(contentId);
            }
        }
    }

    @RequestMapping(value = "/api/organization/{id}/slack", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String slack(@RequestParam String code, @RequestParam String redirect_uri) throws AuthenticationException {
        HttpClient httpClient;
        if(HTTP_PROXY != null) {
            String[] proxy = HTTP_PROXY.replace("http://", "").replace("https://", "").split(":");
            httpClient = HttpClient.newBuilder()
                    .proxy(ProxySelector.of(new InetSocketAddress(proxy[0], Integer.parseInt(proxy[1]))))
                    .version(HttpClient.Version.HTTP_2)
                    .build();
        } else {
            httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .build();
        }

        String url = "https://slack.com/api/oauth.access?client_id=" + SLACK_CLIENT_ID + "&client_secret=" + SLACK_CLIENT_SECRET + "&code=" + code + "&redirect_uri=" + redirect_uri;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .header("Authorization", basicAuth(SLACK_CLIENT_ID, SLACK_CLIENT_SECRET))
                .POST(HttpRequest.BodyPublishers.ofString("{\"code\":\"" + code + "\", \"redirect_uri\":\"" + redirect_uri + "\"}"))
                .build();
        HttpResponse response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug(response.body().toString());
            return response.body().toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

}
