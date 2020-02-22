package fr.thomah.valyou.controller;

import fr.thomah.valyou.exception.ForbiddenException;
import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.generator.UserGenerator;
import fr.thomah.valyou.model.*;
import fr.thomah.valyou.repository.*;
import fr.thomah.valyou.service.SlackClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

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

    public void hello(SlackTeam slackTeam) {
        User orgAdminUser = userRepository.findByEmail(slackTeam.getUpdatedBy());

        StringBuilder helloMessage = new StringBuilder("Bonjour à tous :wave:\n")
                .append("Merci de tester *Les Projets Cagnottes* dans votre organisation. Vous vous demandez de quoi il s'agit ?\n")
                .append("Eh bien c'est très simple : avec ces projets, chacun est libre de :\n")
                .append(":rocket: Lancer un projet\n")
                .append(":handshake: Rejoindre un projet\n")
                .append(":moneybag: Financer un projet avec sa part du budget de l'organisation\n\n")
                .append("Découvrez vite les projets en cours ou lancez le vôtre à l'adresse suivante :\n")
                .append(WEB_URL)
                .append("\n\n")
                .append("Pour toute question, vous pouvez contacter : ");

        if(orgAdminUser.getSlackUser() != null) {
            helloMessage.append("<@")
                    .append(orgAdminUser.getSlackUser().getSlackUserId())
                    .append(">");
        } else {
            String orgAdminFullname = new StringBuilder("*")
                    .append(orgAdminUser.getFirstname())
                    .append(" ")
                    .append(orgAdminUser.getLastname())
                    .append("*")
                    .toString();
            helloMessage.append(orgAdminFullname);
        }

        String channelId = slackClientService.joinChannel(slackTeam);
        slackClientService.inviteInChannel(slackTeam, channelId);
        slackClientService.postMessage(slackTeam, channelId, helloMessage.toString());
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/slack/{teamId}/hello", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void hello(Principal principal, @PathVariable String teamId) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        final User userLoggedIn = userRepository.findByEmail(((User) token.getPrincipal()).getEmail());

        SlackTeam slackTeam = slackTeamRepository.findByTeamId(teamId);
        if(slackTeam != null) {
            Organization organization = organizationRepository.findBySlackTeam_Id(slackTeam.getId());
            if(organization.getMembers().contains(userLoggedIn)) {
                hello(slackTeam);
            } else {
                throw new ForbiddenException();
            }
        } else {
            throw new NotFoundException();
        }
    }

    @RequestMapping(value = "/api/slack/{teamId}/member", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String teamJoin(@PathVariable String teamId, @RequestBody User user) {
        SlackTeam slackTeam = slackTeamRepository.findByTeamId(teamId);
        if(slackTeam != null) {
            Organization organization = organizationRepository.findBySlackTeam_Id(slackTeam.getId());
            if (organization == null) {
                throw new NotFoundException();
            } else {
                SlackUser slackUser = slackUserRepository.findBySlackUserId(user.getSlackUser().getSlackUserId());
                if (slackUser == null) {
                    slackUser = user.getSlackUser();
                    slackUser.setOrganization(organization);
                    slackUser.setSlackTeam(slackTeam);
                    slackUser = slackUserRepository.save(slackUser);
                    organization = slackUser.getOrganization();
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
                final SlackUser slackUserInDb = slackUser = slackUserRepository.save(slackUser);

                slackTeam.getSlackUsers().stream().filter(slackTeamUser -> slackTeamUser.getId().equals(slackUserInDb.getId()))
                        .findAny()
                        .ifPresentOrElse(
                                slackTeamUser -> slackTeamUser = slackUserInDb,
                                () -> slackTeam.getSlackUsers().add(slackUserInDb));
                slackTeamRepository.save(slackTeam);


                String welcomeMessage = new StringBuilder("Bienvenue sur le Slack ")
                        .append(slackTeam.getOrganization().getName())
                        .append(" ! :tada:\n\nVotre organisation a mis en place *les projets cagnottes*. Vous ignorez sans doute de quoi il s'agit ?\n")
                        .append("Eh bien c'est très simple : avec les projets cagnottes, chacun est libre de :\n")
                        .append("- Lancer un projet\n")
                        .append("- Rejoindre un projet\n")
                        .append("- Financer un projet avec sa part du budget de l'organisation\n\n")
                        .append("Découvrez vite les projets en cours ou lancez le vôtre à l'adresse suivante :\n")
                        .append(WEB_URL)
                        .toString();

                slackClientService.postMessage(slackTeam, slackUser.getImId(), welcomeMessage);

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
