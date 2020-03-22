package fr.thomah.valyou.controller;

import fr.thomah.valyou.exception.BadRequestException;
import fr.thomah.valyou.exception.ForbiddenException;
import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.model.*;
import fr.thomah.valyou.repository.BudgetRepository;
import fr.thomah.valyou.repository.DonationRepository;
import fr.thomah.valyou.repository.ProjectRepository;
import fr.thomah.valyou.repository.UserRepository;
import fr.thomah.valyou.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.Set;

@RestController
public class DonationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonationController.class);

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/api/donation", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public void create(Principal principal, @RequestBody Donation donation) {

        // Fails if any of references are null
        if(donation == null || donation.getProject() == null || donation.getContributor() == null || donation.getBudget() == null) {
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        Project project = projectRepository.findById(donation.getProject().getId()).orElse(null);
        Budget budget = budgetRepository.findById(donation.getBudget().getId()).orElse(null);
        User contributor = userRepository.findById(donation.getContributor().getId()).orElse(null);

        // Fails if any of references are null
        if(project == null || budget == null || contributor == null) {
            throw new NotFoundException();
        }

        // Fails if contributor != principal
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        UserPrincipal userPrincipal = (UserPrincipal) token.getPrincipal();
        User userLoggedIn = userRepository.findByUsername(userPrincipal.getUsername());
        if(!userLoggedIn.getId().equals(contributor.getId())) {
            throw new ForbiddenException();
        }

        // Fails if funding deadline of project has been reached
        if(!project.getStatus().equals(ProjectStatus.A_IN_PROGRESS)) {
            throw new BadRequestException();
        }

        // Fails if funding deadline of project has been reached
        Date now = new Date();
        if(now.compareTo(project.getFundingDeadline()) > 0) {
            throw new BadRequestException();
        }

        // Fails if donation budgets is not associated with the campaign
        if(project.getBudgets().stream().noneMatch(projectBudget -> projectBudget.getId().equals(budget.getId()))) {
            throw new BadRequestException();
        }

        // Fails if contributor has enough amount on the budget
        Set<Donation> contributorDonations = donationRepository.findAllByContributorIdAndBudgetId(contributor.getId(), budget.getId());
        float totalAmount = 0;
        for(Donation contributorDonation : contributorDonations) {
            totalAmount+= contributorDonation.getAmount();
        }
        if(totalAmount + donation.getAmount() > budget.getAmountPerMember()) {
            throw new BadRequestException();
        }

        // Otherwise save donation
        Donation donationToSave = new Donation();
        donationToSave.setProject(project);
        donationToSave.setBudget(budget);
        donationToSave.setContributor(contributor);
        donationToSave.setAmount(donation.getAmount());
        donationRepository.save(donation);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/donation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"projectId"})
    public Set<Donation> getByProjectId(@RequestParam("projectId") long projectId) {
        return donationRepository.findAllByProjectId(projectId);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/donation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"contributorId"})
    public Set<Donation> getByContributorId(@RequestParam("contributorId") long contributorId) {
        return donationRepository.findAllByContributorIdOrderByBudgetIdAsc(contributorId);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/donation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"contributorId", "budgetId"})
    public Set<Donation> getByContributorIdAndBudgetId(@RequestParam("contributorId") long contributorId, @RequestParam("budgetId") long budgetId) {
        return donationRepository.findAllByContributorIdAndBudgetId(contributorId, budgetId);
    }

    @RequestMapping(value = "/api/donation/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable("id") Long id) {
        Donation donation = donationRepository.findById(id).orElse(null);
        if(donation == null) {
            throw new NotFoundException();
        } else if(donation.getProject().getStatus() == ProjectStatus.A_IN_PROGRESS) {
            donationRepository.deleteById(id);
        }
    }


}
