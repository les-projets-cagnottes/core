package fr.thomah.valyou.controller;

import fr.thomah.valyou.model.Donation;
import fr.thomah.valyou.model.Project;
import fr.thomah.valyou.model.User;
import fr.thomah.valyou.repository.DonationRepository;
import fr.thomah.valyou.repository.ProjectRepository;
import fr.thomah.valyou.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
public class DonationController {

    @Autowired
    private DonationRepository repository;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/api/donation", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public void create(@RequestBody Donation donation, Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        User userPrincipal = (User) token.getPrincipal();
        userPrincipal = userRepository.findByEmail(userPrincipal.getEmail());
        donation.setContributor(userPrincipal);
        repository.save(donation);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/donation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"projectId"})
    public List<Donation> getByProjectId(@RequestParam("projectId") long projectId) {
        return repository.findAllByProjectId(projectId);
    }



}
