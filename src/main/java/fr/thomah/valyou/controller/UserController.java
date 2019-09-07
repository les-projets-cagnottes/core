package fr.thomah.valyou.controller;

import fr.thomah.valyou.generator.UserGenerator;
import fr.thomah.valyou.model.Organization;
import fr.thomah.valyou.model.Project;
import fr.thomah.valyou.model.User;
import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.repository.AuthorityRepository;
import fr.thomah.valyou.repository.OrganizationRepository;
import fr.thomah.valyou.repository.ProjectRepository;
import fr.thomah.valyou.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
public class UserController {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository repository;

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/api/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit"})
    public Page<User> list(@RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
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
    public Set<User> getByOrganizationId(@RequestParam("organizationId") long organizationId, @RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        return repository.findByOrganizations_idOrderByIdAsc(organizationId);
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
            userInDb.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            userInDb.setEmail(user.getEmail());
            userInDb.setFirstname(user.getFirstname());
            userInDb.setLastname(user.getLastname());
            userInDb.setAvatarUrl(user.getAvatarUrl());
            userInDb.setEnabled(user.getEnabled());
            userInDb.setLastPasswordResetDate(user.getLastPasswordResetDate());
            repository.save(userInDb);
        }
    }

    @RequestMapping(value = "/api/user/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable("id") long id) {
        User user = repository.findById(id).orElse(null);
        if(user == null) {
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
