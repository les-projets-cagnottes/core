package fr.thomah.valyou.controller;

import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.generator.UserGenerator;
import fr.thomah.valyou.model.*;
import fr.thomah.valyou.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class SlackController {

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private SlackUserRepository slackUserRepository;

    @Autowired
    private SlackTeamRepository slackTeamRepository;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/api/slack/{teamId}/member", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String teamJoin(@PathVariable String teamId, @RequestBody User user) {
        SlackTeam slackTeam = slackTeamRepository.findByTeamId(teamId);
        if(slackTeam == null) {
            throw new NotFoundException();
        } else {
            SlackUser slackUser = slackUserRepository.findBySlackUserId(user.getSlackUser().getSlackUserId());
            if(slackUser == null) {
                slackUser = slackUserRepository.save(user.getSlackUser());
            }

            User userInDb = userRepository.findBySlackUser_Id(slackUser.getId());
            if(userInDb == null) {
                userInDb = UserGenerator.newUser(user);
            }
            userInDb.setEmail(user.getEmail());
            userInDb.setFirstname(user.getFirstname());
            userInDb.setLastname(user.getLastname());
            userInDb.setAvatarUrl(user.getAvatarUrl());
            userInDb.setSlackUser(slackUser);
            userInDb.setPassword("");
            userInDb = userRepository.save(userInDb);

            slackUser.setUser(userInDb);
            slackUserRepository.save(slackUser);

            Organization organization = organizationRepository.findBySlackTeam_Id(slackTeam.getId());
            if(organization != null) {
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
