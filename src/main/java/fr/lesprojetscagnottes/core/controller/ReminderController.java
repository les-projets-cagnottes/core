package fr.lesprojetscagnottes.core.controller;

import fr.lesprojetscagnottes.core.entity.Reminder;
import fr.lesprojetscagnottes.core.exception.BadRequestException;
import fr.lesprojetscagnottes.core.exception.NotFoundException;
import fr.lesprojetscagnottes.core.model.ReminderModel;
import fr.lesprojetscagnottes.core.repository.ReminderRepository;
import fr.lesprojetscagnottes.core.scheduler.ReminderScheduler;
import fr.lesprojetscagnottes.core.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Ideas", description = "The Ideas API")
public class ReminderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderController.class);

    @Autowired
    private ReminderScheduler reminderScheduler;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Get statuses by a list of IDs", description = "Get statuses by a list of IDs", tags = { "Reminders" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the contents", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Map.class)))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/reminder/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"ids"})
    public Map<Long, Boolean> getByIds(Principal principal, @RequestParam("ids") Set<Long> ids) {
        return reminderScheduler.statuses(ids);
    }

    @Operation(summary = "Update a reminder", description = "Update a reminder", tags = { "Reminders" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reminder updated", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/reminder", method = RequestMethod.PUT, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void update(Principal principal, @RequestBody ReminderModel model) {

        // Fails if any of references are null
        if(model == null || model.getId() <= 0 || model.getPlanning().isEmpty()) {
            if(model != null ) {
                LOGGER.error("Impossible to update reminder : some references are missing");
            } else {
                LOGGER.error("Impossible to update a null reminder");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        Reminder reminder = reminderRepository.getOne(model.getId());

        // Fails if any of references are null
        if(reminder == null) {
            LOGGER.error("Impossible to update reminder : one or more reference(s) doesn't exist");
            throw new NotFoundException();
        }

        boolean needToStart = model.getEnabled() && !reminder.getEnabled();
        boolean needToStop = !model.getEnabled() && reminder.getEnabled();

        // Save idea
        reminder.setType(model.getType());
        reminder.setEnabled(model.getEnabled());
        reminder.setPlanning(model.getPlanning());
        reminderRepository.save(reminder);

        if(needToStart) {
            reminderScheduler.start(reminder);
        }

        if(needToStop) {
            reminderScheduler.stop(reminder);
        }

    }

}
