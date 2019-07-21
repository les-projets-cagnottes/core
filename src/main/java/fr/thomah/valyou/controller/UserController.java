package fr.thomah.valyou.controller;

import fr.thomah.valyou.entity.User;
import fr.thomah.valyou.exceptions.NotFoundException;
import fr.thomah.valyou.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private
    UserRepository repository;

    @RequestMapping(value = "/api/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit"})
    public Page<User> list(@RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return repository.findAll(pageable);
    }

    @RequestMapping(value = "/api/user", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public User create(@RequestBody User user) {
        return repository.save(user);
    }

    @RequestMapping(value = "/api/user/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public void save(@PathVariable("id") String id, @RequestBody User user) {
        User userInDb = repository.findById(Long.valueOf(id)).orElse(null);
        if (userInDb == null) {
            throw new NotFoundException();
        } else {
            repository.save(user);
        }
    }

    @RequestMapping(value = "/api/user/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void delete(@PathVariable("id") String id) {
        repository.deleteById(Long.valueOf(id));
    }

}
