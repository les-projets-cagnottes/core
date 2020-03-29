package fr.thomah.valyou.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Main", description = "The Main API")
public class MainController {

    @Operation(summary = "Component Healcheck", description = "Verify if this component is alive", tags = { "Main" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "This component is alive",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class))))
    })
    @RequestMapping(value = "/health", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public void health() {}

}
