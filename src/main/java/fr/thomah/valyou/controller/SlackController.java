package fr.thomah.valyou.controller;

import fr.thomah.valyou.exception.ForbiddenException;
import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.generator.UserGenerator;
import fr.thomah.valyou.model.*;
import fr.thomah.valyou.repository.*;
import fr.thomah.valyou.security.UserPrincipal;
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

        StringBuilder helloMessage = new StringBuilder("Bonjour à tous :wave:\n\n")
                .append("Connaissez-vous *Les Projets Cagnottes* ?\n")
                .append("Avec ces projets, chacun est libre de :\n")
                .append(":rocket: Lancer un projet\n")
                .append(":handshake: Rejoindre un projet\n")
                .append(":moneybag: Financer un projet avec sa part du budget de l'organisation\n\n")
                .append("Découvrez vite les projets en cours ou lancez le vôtre à l'adresse suivante :\n")
                .append(WEB_URL)
                .append("\n\n")
                .append("Pour toute question, vous pouvez contacter : ");

        slackTeam.getSlackUsers().stream()
                .filter(slackUser -> slackUser.getId() == orgAdminUser.getId())
                .findAny()
                .ifPresentOrElse(slackUser -> {
                    helloMessage.append("<@")
                            .append(slackUser.getSlackId())
                            .append(">");
                },() -> {
                    helloMessage.append("*")
                            .append(orgAdminUser.getFirstname())
                            .append(" ")
                            .append(orgAdminUser.getLastname())
                            .append("*");
                });

        String channelId = slackClientService.joinChannel(slackTeam);
        slackClientService.inviteInChannel(slackTeam, channelId);
        slackClientService.postMessage(slackTeam, channelId, helloMessage.toString());
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/slack/{teamId}/hello", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void hello(Principal principal, @PathVariable String teamId) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        UserPrincipal userPrincipal = (UserPrincipal) token.getPrincipal();
        final User userLoggedIn = userRepository.findByUsername(userPrincipal.getUsername());

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
                user.getSlackUsers().stream()
                        .findFirst()
                        .ifPresent(slackUser -> {
                            SlackUser slackUserInDb = slackUserRepository.findBySlackId(slackUser.getSlackId());
                            slackUserInDb.setEmail(slackUser.getEmail());
                            slackUserInDb.setSlackTeam(slackTeam);
                            slackUser.setImId(slackClientService.openDirectMessageChannel(slackTeam, slackUser.getSlackId()));
                            final SlackUser slackUserFinal = slackUserRepository.save(slackUserInDb);

                            // Create User if not exists in DB
                            User userInDb = userRepository.findBySlackUsers_Id(slackUserInDb.getId());
                            if (userInDb == null) {
                                userInDb = UserGenerator.newUser(user);
                            }
                            userInDb.setEmail(user.getEmail());
                            userInDb.setFirstname(user.getFirstname());
                            userInDb.setLastname(user.getLastname());
                            userInDb.setAvatarUrl(user.getAvatarUrl());
                            userInDb.setPassword("");
                            final User userInDbFinal = userInDb;

                            // If the User doesnt have the SlackUser -> Add it
                            // Else -> replace by the new one
                            userInDbFinal.getSlackUsers().stream().filter(userSlackUser -> userSlackUser.getUser().getId().equals(userInDbFinal.getId()))
                                    .findAny()
                                    .ifPresentOrElse(
                                            userSlackUser -> userSlackUser = slackUserFinal,
                                            () -> userInDbFinal.getSlackUsers().add(slackUserFinal));

                            userInDb = userRepository.save(userInDbFinal);

                            // Complete SlackUser with user saved
                            slackUserInDb.setUser(userInDb);
                            final SlackUser slackUserFinal2 = slackUserRepository.save(slackUserInDb);

                            // If the SlackTeam doesnt have the SlackUser -> Add it
                            // Else - replace by the new one
                            slackTeam.getSlackUsers().stream().filter(slackTeamUser -> slackTeamUser.getId().equals(slackUserFinal2.getId()))
                                    .findAny()
                                    .ifPresentOrElse(
                                            slackTeamUser -> slackTeamUser = slackUserFinal2,
                                            () -> slackTeam.getSlackUsers().add(slackUserFinal2));
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
                        });
            }
        }
        return null;
    }

    @RequestMapping(value = "/api/slack/{teamId}/member", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateUser(@PathVariable String teamId, @RequestBody User user) {
        SlackTeam slackTeam = slackTeamRepository.findByTeamId(teamId);
        if(slackTeam != null) {
            final Organization organization = organizationRepository.findBySlackTeam_Id(slackTeam.getId());
            if (organization == null) {
                throw new NotFoundException();
            } else {
                user.getSlackUsers().stream()
                        .findFirst()
                        .ifPresent(slackUser -> {
                            SlackUser slackUserInDb = slackUserRepository.findBySlackId(slackUser.getSlackId());

                            User userEditted = userRepository.findBySlackUsers_Id(slackUserInDb.getId());
                            userEditted.setEnabled(user.getEnabled());
                            final User userInDb = userRepository.save(userEditted);

                            slackUserInDb.setSlackTeam(slackTeam);
                            slackUserInDb.setUser(user);
                            final SlackUser slackUserFinal = slackUserRepository.save(slackUserInDb);

                            if(user.getEnabled()) {
                                slackTeam.getSlackUsers().stream().filter(slackTeamUser -> slackTeamUser.getId().equals(slackUserFinal.getId()))
                                        .findAny()
                                        .ifPresentOrElse(
                                                slackTeamUser -> slackTeamUser = slackUserFinal,
                                                () -> slackTeam.getSlackUsers().add(slackUserFinal));
                                slackTeamRepository.save(slackTeam);

                                organization.getMembers().stream().filter(member -> member.getId().equals(userInDb.getId()))
                                        .findAny()
                                        .ifPresentOrElse(
                                                member -> member = userInDb,
                                                () -> organization.getMembers().add(userInDb)
                                        );
                            } else {
                                organization.getMembers().stream().filter(member -> member.getId().equals(userInDb.getId()))
                                        .findAny()
                                        .ifPresent(member -> organization.getMembers().remove(member));
                            }
                            organizationRepository.save(organization);
                        });
            }
        }
        return null;
    }

}
