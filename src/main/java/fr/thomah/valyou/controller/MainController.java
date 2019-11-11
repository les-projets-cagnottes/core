package fr.thomah.valyou.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class MainController {

    @RequestMapping(value = "/api/health", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public void list() {
    }

}
