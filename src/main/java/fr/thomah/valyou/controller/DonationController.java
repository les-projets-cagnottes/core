package fr.thomah.valyou.controller;

import com.google.gson.Gson;
import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.model.Donation;
import fr.thomah.valyou.model.Project;
import fr.thomah.valyou.model.ProjectStatus;
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
import java.util.Set;

@RestController
public class DonationController {

    @Autowired
    private Gson gson;

    @Autowired
    private DonationRepository repository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/api/donation", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public void create(@RequestBody String donationStr, Principal principal) {
        Donation donation = gson.fromJson(donationStr, Donation.class);
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        User userPrincipal = (User) token.getPrincipal();
        userPrincipal = userRepository.findByEmail(userPrincipal.getEmail());
        donation.setContributor(userPrincipal);
        donation.setProject(projectRepository.findById(donation.getProject().getId()).orElse(null));
        repository.save(donation);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/donation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"projectId"})
    public Set<Donation> getByProjectId(@RequestParam("projectId") long projectId) {
        return repository.findAllByProjectId(projectId);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/donation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"contributorId"})
    public Set<Donation> getByContributorId(@RequestParam("contributorId") long contributorId) {
        return repository.findAllByContributorIdOrderByBudgetIdAsc(contributorId);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/donation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"contributorId", "budgetId"})
    public Set<Donation> getByContributorIdAndBudgetId(@RequestParam("contributorId") long contributorId, @RequestParam("budgetId") long budgetId) {
        return repository.findAllByContributorIdAndBudgetId(contributorId, budgetId);
    }

    @RequestMapping(value = "/api/donation/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable("id") Long id) {
        Donation donation = repository.findById(id).orElse(null);
        if(donation == null) {
            throw new NotFoundException();
        } else if(donation.getProject().getStatus() == ProjectStatus.IN_PROGRESS) {
            repository.deleteById(id);
        }
    }


}
