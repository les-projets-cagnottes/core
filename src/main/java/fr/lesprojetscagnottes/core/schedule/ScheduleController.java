package fr.lesprojetscagnottes.core.schedule;

import fr.lesprojetscagnottes.core.common.exception.BadRequestException;
import fr.lesprojetscagnottes.core.common.exception.NotFoundException;
import fr.lesprojetscagnottes.core.common.scheduler.MainScheduler;
import fr.lesprojetscagnottes.core.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
@Tag(name = "Schedules", description = "The Schedules API")
public class ScheduleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleController.class);

    @Autowired
    private MainScheduler mainScheduler;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Get statuses by a list of IDs", description = "Get statuses by a list of IDs", tags = { "Schedules" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the statuses", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Map.class))))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/schedule/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"ids"})
    public Map<Long, Boolean> status(Principal principal, @RequestParam("ids") Set<Long> ids) {
        return mainScheduler.statuses(ids);
    }

    @Operation(summary = "Get all schedules", description = "Get all schedules", tags = { "Schedules" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get all schedules", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ScheduleEntity.class)))),
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/schedule", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<ScheduleModel> getAll(Principal principal) {
        List<ScheduleEntity> schedules = scheduleRepository.findAll();
        Set<ScheduleModel> models = new LinkedHashSet<>();
        schedules.forEach(schedule -> models.add(ScheduleModel.fromEntity(schedule)));
        return models;
    }

    @Operation(summary = "Update a schedule", description = "Update a schedule", tags = { "Schedules" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Schedule updated", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/schedule", method = RequestMethod.PUT, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void update(Principal principal, @RequestBody ScheduleModel model) {

        // Fails if any of references are null
        if(model == null || model.getId() <= 0 || model.getPlanning().isEmpty()) {
            if(model != null ) {
                LOGGER.error("Impossible to update schedule : some references are missing");
            } else {
                LOGGER.error("Impossible to update a null schedule");
            }
            throw new BadRequestException();
        }

        // Retrieve full referenced objects
        ScheduleEntity schedule = scheduleRepository.getOne(model.getId());

        // Fails if any of references are null
        if(schedule == null) {
            LOGGER.error("Impossible to update schedule : one or more reference(s) doesn't exist");
            throw new NotFoundException();
        }

        Set<Long> ids = new LinkedHashSet<>();
        ids.add(schedule.getId());
        Map<Long, Boolean> statuses = mainScheduler.statuses(ids);

        boolean isScheduled;
        if(statuses.get(schedule.getId()) == null) {
            isScheduled = false;
        } else {
            isScheduled = statuses.get(schedule.getId());
        }
        boolean needToStart = model.getEnabled() && !isScheduled;
        boolean needToStop = !model.getEnabled() && isScheduled;

        // Save idea
        schedule.setType(model.getType());
        schedule.setEnabled(model.getEnabled());
        schedule.setPlanning(model.getPlanning());
        scheduleRepository.save(schedule);

        if(needToStart) {
            mainScheduler.start(schedule);
        }

        if(needToStop) {
            mainScheduler.stop(schedule);
        }

    }

}
