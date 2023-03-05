package fr.lesprojetscagnottes.core.campaign.controller;

import fr.lesprojetscagnottes.core.campaign.model.CampaignModel;
import fr.lesprojetscagnottes.core.campaign.service.CampaignService;
import fr.lesprojetscagnottes.core.common.pagination.DataPage;
import fr.lesprojetscagnottes.core.donation.model.DonationModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Campaigns", description = "The Campaigns API")
public class CampaignController {

    private final CampaignService campaignService;

    @Autowired
    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @Operation(summary = "Find a campaign by its ID", description = "Find a campaign by its ID", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the campaign", content = @Content(schema = @Schema(implementation = CampaignModel.class))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Campaign not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/campaign/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public CampaignModel findById(Principal principal, @PathVariable("id") Long id) {
        return campaignService.findById(principal, id);
    }

    @Operation(summary = "Get list of campaigns by a list of IDs", description = "Find a list of campaigns by a list of IDs", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the campaigns", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CampaignModel.class)))),
            @ApiResponse(responseCode = "400", description = "ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/campaign", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"ids"})
    public List<CampaignModel> getByIds(Principal principal, @RequestParam("ids") Set<Long> ids) {
        return campaignService.getByIds(principal, ids);
    }

    @Operation(summary = "Get paginated donations made on a campaign", description = "Get paginated donations made on a campaign", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns corresponding paginated donations", content = @Content(schema = @Schema(implementation = DataPage.class))),
            @ApiResponse(responseCode = "400", description = "Campaign ID is incorrect", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "User is not member of concerned organizations", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Campaign not found", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/campaign/{id}/donations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataPage<DonationModel> getDonations(Principal principal, @PathVariable("id") long campaignId, @RequestParam(name = "offset", defaultValue = "0") int offset, @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return campaignService.getDonations(principal, campaignId, offset, limit);
    }

    @Operation(summary = "Create a campaign", description = "Create a campaign", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Campaign created", content = @Content(schema = @Schema(implementation = CampaignModel.class))),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/campaign", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignModel create(Principal principal, @RequestBody CampaignModel campaign) {
        return campaignService.create(principal, campaign);
    }

    @Operation(summary = "Update a campaign", description = "Update a campaign", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campaign updated", content = @Content(schema = @Schema(implementation = CampaignModel.class))),
            @ApiResponse(responseCode = "400", description = "Some references are missing", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Some references doesn't exist", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Principal has not enough privileges", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/campaign", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public CampaignModel update(Principal principal, @RequestBody CampaignModel campaignModel) {
        return campaignService.update(principal, campaignModel);
    }

    @Operation(summary = "Execute campaign validation", description = "Execute campaign validation without waiting for the CRON", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation executed with success", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/campaign/validate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public void validate() {
        campaignService.validate();
    }

    @Operation(summary = "Execute campaign notification", description = "Execute campaign notification without waiting for the CRON", tags = { "Campaigns" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification executed with success", content = @Content(schema = @Schema()))
    })
    @RequestMapping(value = "/campaign/notify", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public void notifyCampaignsAlmostFinished() {
        campaignService.notifyCampaignsAlmostFinished();
    }

    @RequestMapping(value = "/campaign/{id}/notify", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public void notifyCampaignStatus(@PathVariable("id") long id) {
        campaignService.notifyCampaignStatus(id);
    }

}
