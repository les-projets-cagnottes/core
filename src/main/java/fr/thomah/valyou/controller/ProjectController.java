package fr.thomah.valyou.controller;

import fr.thomah.valyou.exception.BadRequestException;
import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.generator.ProjectGenerator;
import fr.thomah.valyou.model.Project;
import fr.thomah.valyou.model.User;
import fr.thomah.valyou.repository.ProjectRepository;
import fr.thomah.valyou.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
public class ProjectController {

    @Autowired
    private ProjectRepository repository;

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/project", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit"})
    public Page<Project> list(@RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return repository.findAll(pageable);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/project/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Project findById(@PathVariable("id") Long id) {
        return repository.findById(id).orElse(null);
    }

    @RequestMapping(value = "/api/project", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public void create(@RequestBody Project project) {
        repository.save(ProjectGenerator.newProject(project));
    }

    @RequestMapping(value = "/api/project/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public void save(@PathVariable("id") String id, @RequestBody Project project) {
        Project projectInDb = repository.findById(Long.valueOf(id)).orElse(null);
        if (projectInDb == null) {
            throw new NotFoundException();
        } else {
            repository.save(project);
        }
    }

    @RequestMapping(value = "/api/project/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable("id") String id) {
        repository.deleteById(Long.valueOf(id));
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/project/{id}/join", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Project join(@PathVariable("id") Long id, Principal user) {
        Project projectInDb = repository.findById(id).orElse(null);
        if (projectInDb == null) {
            throw new NotFoundException();
        } else {
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) user;
            final User userLoggedIn = userRepository.findByEmail(((User) token.getPrincipal()).getEmail());
            User userInPeopleGivingTime = projectInDb.getPeopleGivingTime().stream().filter(userGivingTime -> userLoggedIn.getId().equals(userGivingTime.getId())).findFirst().orElse(null);
            if(userInPeopleGivingTime == null) {
                projectInDb.addPeopleGivingTime(userLoggedIn);
            } else {
                projectInDb.getPeopleGivingTime().remove(userInPeopleGivingTime);
            }
            return repository.save(projectInDb);
        }
    }

}
