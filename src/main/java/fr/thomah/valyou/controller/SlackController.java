package fr.thomah.valyou.controller;

import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.generator.UserGenerator;
import fr.thomah.valyou.model.*;
import fr.thomah.valyou.repository.*;
import fr.thomah.valyou.service.SlackClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class SlackController {

    private static final String WEB_URL = System.getenv("VALYOU_WEB_URL");

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private SlackUserRepository slackUserRepository;

    @Autowired
    private SlackTeamRepository slackTeamRepository;

    @Autowired
    private SlackClientService slackClientService;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/api/slack/{teamId}/member", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String teamJoin(@PathVariable String teamId, @RequestBody User user) {
        SlackTeam slackTeam = slackTeamRepository.findByTeamId(teamId);
        if (slackTeam == null) {
            throw new NotFoundException();
        } else {
            SlackUser slackUser = slackUserRepository.findBySlackUserId(user.getSlackUser().getSlackUserId());
            if (slackUser == null) {
                slackUser = slackUserRepository.save(user.getSlackUser());
            }

            User userInDb = userRepository.findBySlackUser_Id(slackUser.getId());
            if (userInDb == null) {
                userInDb = UserGenerator.newUser(user);
            }
            userInDb.setEmail(user.getEmail());
            userInDb.setFirstname(user.getFirstname());
            userInDb.setLastname(user.getLastname());
            userInDb.setAvatarUrl(user.getAvatarUrl());
            userInDb.setSlackUser(slackUser);
            userInDb.setPassword("");
            userInDb = userRepository.save(userInDb);

            slackUser.setImId(slackClientService.openDirectMessageChannel(slackTeam, slackUser.getSlackUserId()));
            slackUser.setUser(userInDb);
            slackUserRepository.save(slackUser);

            String welcomeMessage = new StringBuilder("Bienvenue sur le Slack ")
                    .append(slackTeam.getOrganization().getName())
                    .append(" ! :tada:\n\nVotre organisation a mis en place *les projets cagnottes*. Vous ignorez sans doute de quoi il s'agit ?\n")
                    .append("Eh bien c'est très simple : avec les projets cagnottes, chacun est libre de :\n")
                    .append("- Lancer un projet\n")
                    .append("- Rejoindre un projet\n")
                    .append("- Financer un projet avec le budget de l'organisation\n\n")
                    .append("Découvrez vite les projets en cours ou lancez le vôtre à l'adresse suivante :\n")
                    .append(WEB_URL)
                    .toString();

            slackClientService.postMessage(slackTeam, slackUser.getImId(), welcomeMessage);

            Organization organization = organizationRepository.findBySlackTeam_Id(slackTeam.getId());
            if (organization != null) {
                OrganizationAuthority memberOrganizationAuthority = organizationAuthorityRepository.findByOrganizationAndName(organization, OrganizationAuthorityName.ROLE_MEMBER);
                userInDb.addOrganizationAuthority(memberOrganizationAuthority);
                organization.getMembers().add(userInDb);

                organizationRepository.save(organization);
                userRepository.save(userInDb);
            }
        }
        return null;
    }


}
