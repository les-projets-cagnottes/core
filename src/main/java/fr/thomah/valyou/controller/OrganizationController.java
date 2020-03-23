package fr.thomah.valyou.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.thomah.valyou.exception.AuthenticationException;
import fr.thomah.valyou.exception.BadRequestException;
import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.generator.OrganizationGenerator;
import fr.thomah.valyou.generator.StringGenerator;
import fr.thomah.valyou.generator.UserGenerator;
import fr.thomah.valyou.model.*;
import fr.thomah.valyou.repository.*;
import fr.thomah.valyou.security.UserPrincipal;
import fr.thomah.valyou.service.HttpClientService;
import fr.thomah.valyou.service.SlackClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Principal;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
public class OrganizationController {

    private static final String SLACK_CLIENT_ID = System.getenv("VALYOU_SLACK_CLIENT_ID");
    private static final String SLACK_CLIENT_SECRET = System.getenv("VALYOU_SLACK_CLIENT_SECRET");

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationController.class);

    @Autowired
    private SlackController slackController;

    @Autowired
    private HttpClientService httpClientService;

    @Autowired
    private SlackClientService slackClientService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private OrganizationRepository repository;

    @Autowired
    private SlackUserRepository slackUserRepository;

    @Autowired
    private SlackTeamRepository slackTeamRepository;

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
    public Set<Organization> getUserOrganizations(@RequestParam("member_id") Long memberId) {
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
    public Organization create(@RequestBody Organization org) {
        org = repository.save(OrganizationGenerator.newOrganization(org));
        for(OrganizationAuthorityName authorityName : OrganizationAuthorityName.values()) {
            organizationAuthorityRepository.save(new OrganizationAuthority(org, authorityName));
        }
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
        Pageable pageable = PageRequest.of(offset, limit, Sort.by("firstname").ascending().and(Sort.by("lastname").ascending()));
        Page<User> members = userRepository.findByOrganizations_id(id, pageable);
        for(User member : members) {
            member.getUserOrganizationAuthorities().addAll(organizationAuthorityRepository.findAllByUsers_Id(member.getId()));
        }
        return members;
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}/members", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void addMember(@PathVariable long id, @RequestBody long userId) {
        Organization org = repository.findById(id).orElse(null);
        User newMember = userRepository.findById(userId).orElse(null);
        if(org == null || newMember == null) {
            throw new NotFoundException();
        } else {
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
    @RequestMapping(value = "/api/organization/{id}/authorities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<OrganizationAuthority> getOrganizationAuthorities(@PathVariable("id") long id) {

        Organization organization = repository.findById(id).orElse(null);
        if(organization == null) {
            throw new BadRequestException();
        }

        return organizationAuthorityRepository.findAllByOrganizationId(organization.getId());
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

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}/slack", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String slack(@PathVariable long id, @RequestParam String code, @RequestParam String redirect_uri) throws AuthenticationException {
        String url = "https://slack.com/api/oauth.access?client_id=" + SLACK_CLIENT_ID + "&client_secret=" + SLACK_CLIENT_SECRET + "&code=" + code + "&redirect_uri=" + redirect_uri;
        String body = "{\"code\":\"" + code + "\", \"redirect_uri\":\"" + redirect_uri + "\"}";
        LOGGER.debug("POST " + url);
        LOGGER.debug("body : " + body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .header("Authorization", basicAuth(SLACK_CLIENT_ID, SLACK_CLIENT_SECRET))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse response;
        try {
            response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("response : " + response.body().toString());
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body().toString(), JsonObject.class);
            if (json.get("ok") != null && json.get("ok").getAsBoolean()) {
                Organization organization = repository.findById(id).orElse(null);
                if(organization != null) {
                    SlackTeam slackTeam;
                    if(organization.getSlackTeam() != null) {
                        slackTeam = organization.getSlackTeam();
                    } else {
                        slackTeam = new SlackTeam();
                    }
                    JsonObject jsonBot = json.get("bot").getAsJsonObject();
                    slackTeam.setAccessToken(json.get("access_token").getAsString());
                    slackTeam.setTeamId(json.get("team_id").getAsString());
                    slackTeam.setBotAccessToken(jsonBot.get("bot_access_token").getAsString());
                    slackTeam.setBotUserId(jsonBot.get("bot_user_id").getAsString());
                    slackTeam.setOrganization(organization);
                    slackTeamRepository.save(slackTeam);
                } else {
                    throw new NotFoundException();
                }
            }
            return response.body().toString();
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}/slack/sync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String slackSync(@PathVariable long id) throws InterruptedException {
        Organization organization = this.repository.findById(id).orElse(null);
        if(organization == null) {
            throw new NotFoundException();
        } else {
            SlackTeam slackTeam = organization.getSlackTeam();
            List<SlackUser> slackUsers = slackClientService.listUsers(organization.getSlackTeam());
            User user;
            long delay = 0;
            long tsAfterOpenIm = (new Timestamp(System.currentTimeMillis())).getTime();
            for(SlackUser slackUser : slackUsers) {
                user = userRepository.findByEmail(slackUser.getEmail());
                SlackUser slackUserEditted = slackUserRepository.findBySlackId(slackUser.getSlackId());
                if(slackUserEditted != null) {
                    slackUserEditted.setName(slackUser.getName());
                    slackUserEditted.setImage_192(slackUser.getImage_192());
                    slackUserEditted.setEmail(slackUser.getEmail());
                } else {
                    slackUserEditted = slackUser;
                }

                delay = (new Timestamp(System.currentTimeMillis())).getTime() - tsAfterOpenIm;
                if(delay > 600) {
                    delay = 600;
                }
                Thread.sleep(600 - delay);
                slackUserEditted.setImId(slackClientService.openDirectMessageChannel(slackTeam, slackUserEditted.getSlackId()));
                tsAfterOpenIm = (new Timestamp(System.currentTimeMillis())).getTime();

                if(user == null) {
                    user = new User();
                    user.setFirstname(slackUserEditted.getName());
                    user.setUsername(slackUserEditted.getEmail());
                    user.setEmail(slackUserEditted.getEmail());
                    user.setAvatarUrl(slackUserEditted.getImage_192());
                    user.setPassword(BCrypt.hashpw(StringGenerator.randomString(), BCrypt.gensalt()));
                    user = userRepository.save(UserGenerator.newUser(user));
                }
                user.setEnabled(!slackUser.getDeleted());
                final User userInDb = user;

                slackUserEditted.setSlackTeam(slackTeam);
                slackUserEditted.setUser(user);
                final SlackUser slackUserInDb = slackUserRepository.save(slackUserEditted);

                slackTeam.getSlackUsers().stream().filter(slackTeamUser -> slackTeamUser.getId().equals(slackUserInDb.getId()))
                        .findAny()
                        .ifPresentOrElse(
                                slackTeamUser -> slackTeamUser = slackUserInDb,
                                () -> slackTeam.getSlackUsers().add(slackUserInDb));
                slackTeamRepository.save(slackTeam);

                if(user.getEnabled()) {
                    organization.getMembers().stream().filter(member -> member.getId().equals(userInDb.getId()))
                            .findAny()
                            .ifPresentOrElse(
                                    member -> member = userInDb,
                                    () -> organization.getMembers().add(userInDb)
                            );
                    repository.save(organization);
                } else {
                    organization.getMembers().stream().filter(member -> member.getId().equals(userInDb.getId()))
                            .findAny()
                            .ifPresent(member -> organization.getMembers().remove(member));
                }
            }
        }
        return null;
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}/slack/{slackTeamId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void slackDisconnect(@PathVariable long id, @PathVariable long slackTeamId) {
        Organization org = repository.findById(id).orElse(null);
        SlackTeam slackTeam = slackTeamRepository.findById(slackTeamId).orElse(null);
        if(org == null || slackTeam == null) {
            throw new NotFoundException();
        } else {
            if(slackTeam.getOrganization().getId() != id) {
                throw new BadRequestException();
            } else {
                slackTeamRepository.deleteById(slackTeamId);
            }
        }
    }

    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

}
