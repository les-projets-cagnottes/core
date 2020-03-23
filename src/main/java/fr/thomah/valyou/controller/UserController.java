package fr.thomah.valyou.controller;

import fr.thomah.valyou.exception.BadRequestException;
import fr.thomah.valyou.exception.ForbiddenException;
import fr.thomah.valyou.generator.UserGenerator;
import fr.thomah.valyou.model.*;
import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.repository.*;
import fr.thomah.valyou.security.UserPrincipal;
import fr.thomah.valyou.service.UserService;
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
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class UserController {

    @Autowired
    private ApiTokenRepository apiTokenRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository repository;

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/api/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit"})
    public Page<User> list(@RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        Pageable pageable = PageRequest.of(offset, limit, Sort.by("firstname").and(Sort.by("lastname")));
        return repository.findAll(pageable);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"email"})
    public User findByEmail(@RequestParam("email") String email) {
        User user = repository.findByEmail(email);
        user.setPassword("");
        return user;
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"budgetId", "offset", "limit"})
    public Page<User> getByBudgetId(@RequestParam("budgetId") long budgetId, @RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        Page<User> users = repository.findByBudgetIdWithPagination(budgetId, pageable);
        Page<Float> totalBudgetDonations = repository.sumTotalBudgetDonationsByBudgetIdWithPagination(budgetId, pageable);
        final List<Float> totalBudgetDonationsArray = totalBudgetDonations.get().collect(Collectors.toList());
        List<User> userList = users.get().collect(Collectors.toList());
        IntStream
                .range(0, (int) users.get().count())
                .forEach(index -> {
                    userList.get(index).setTotalBudgetDonations(totalBudgetDonationsArray.get(index));
                });
        return users;
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/user", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void create(@RequestBody User user) {
        user = UserGenerator.newUser(user);
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        repository.save(user);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/user/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public void save(@PathVariable("id") String id, @RequestBody User user) {
        User userInDb = repository.findById(Long.valueOf(id)).orElse(null);
        if (userInDb == null) {
            throw new NotFoundException();
        } else {
            userInDb.setUsername(user.getUsername());
            if(user.getPassword() != null && !user.getPassword().equals("")) {
                userInDb.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
                userInDb.setLastPasswordResetDate(new Date());
            }
            userInDb.setEmail(user.getEmail());
            userInDb.setFirstname(user.getFirstname());
            userInDb.setLastname(user.getLastname());
            userInDb.setAvatarUrl(user.getAvatarUrl());
            userInDb.setEnabled(user.getEnabled());
            repository.save(userInDb);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/user/profile", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateProfile(Principal principal, @RequestBody User user) {
        User userLoggedIn = userService.get(principal);
        if(!userLoggedIn.getUsername().equals(user.getEmail())) {
            throw new ForbiddenException();
        } else {
            User userInDb = repository.findById(user.getId()).orElse(null);
            if (userInDb == null) {
                throw new NotFoundException();
            } else {
                userInDb.setUsername(user.getEmail());
                if(user.getPassword() != null && !user.getPassword().equals("")) {
                    userInDb.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
                    userInDb.setLastPasswordResetDate(new Date());
                }
                userInDb.setEmail(user.getEmail());
                userInDb.setFirstname(user.getFirstname());
                userInDb.setLastname(user.getLastname());
                userInDb.setAvatarUrl(user.getAvatarUrl());
                repository.save(userInDb);
            }
        }
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/user/{id}/roles", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void grant(@PathVariable long id, @RequestBody OrganizationAuthority organizationAuthority) {

        if(id <= 0 || organizationAuthority == null || organizationAuthority.getId() <= 0) {
            throw new BadRequestException();
        }

        User userInDb = repository.findById(id).orElse(null);
        OrganizationAuthority organizationAuthorityInDb = organizationAuthorityRepository.findById(organizationAuthority.getId()).orElse(null);

        if(userInDb == null || organizationAuthorityInDb == null) {
            throw new NotFoundException();
        }

        userInDb.getUserOrganizationAuthorities().stream().filter(authority -> authority.getId().equals(organizationAuthorityInDb.getId()))
                .findAny()
                .ifPresentOrElse(
                        authority -> userInDb.getUserOrganizationAuthorities().remove(authority),
                        () -> userInDb.getUserOrganizationAuthorities().add(organizationAuthorityInDb)
                );
        repository.save(userInDb);
    }

    @RequestMapping(value = "/api/user/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable("id") long id) {
        User user = repository.findById(id).orElse(null);
        if (user == null) {
            throw new NotFoundException();
        } else {
            Set<Organization> organizations = organizationRepository.findByMembers_Id(id);
            organizations.forEach(organization -> {
                organization.getMembers().remove(user);
                organizationRepository.save(organization);
            });
            Set<Project> projects = projectRepository.findAllByPeopleGivingTime_Id(id);
            projects.forEach(project -> {
                project.getPeopleGivingTime().remove(user);
                projectRepository.save(project);
            });
            repository.deleteById(id);
        }
    }

}
