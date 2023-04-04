/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.rest.controller;

import cc.altius.FASP.model.CustomUserDetails;
import cc.altius.FASP.model.IntegrationProgram;
import cc.altius.FASP.model.ManualIntegration;
import cc.altius.FASP.model.ResponseCode;
import cc.altius.FASP.model.Views;
import cc.altius.FASP.service.IntegrationProgramService;
import cc.altius.FASP.service.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author altius
 */
@RestController
@RequestMapping("/api/integrationProgram")
public class IntegrationProgramRestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    IntegrationProgramService integrationProgramService;
    @Autowired
    private UserService userService;

    /**
     * API used to get the complete Integration Program list.
     *
     * @param auth
     * @return returns the complete list of Integration Programs
     */
    @GetMapping("")
    @Operation(description = "API used to get the complete Integration Program list.", summary = "Get Integration Program list", tags = ("integrationProgram"))
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "200", description = "Returns the Integration Program list")
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "500", description = "Internal error that prevented the retreival of Integration Program list")
    public ResponseEntity getIntegrationProgram(Authentication auth) {
        try {
            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
            return new ResponseEntity(this.integrationProgramService.getIntegrationProgramList(curUser), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while trying to list Integration Programs", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * API used to get the complete Integration Program list for a ProgramId.
     *
     * @param auth
     * @return returns the complete list of Integration Programs for a ProgramId
     */
    @GetMapping("/program/{programId}")
    @Operation(description = "API used to get the complete Integration Program list for a ProgramId.", summary = "Get Integration Program list for a ProgramId", tags = ("integrationProgram"))
    @Parameters(
            @Parameter(name = "programId", description = "programId that you want to the Integration Program list for"))
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "200", description = "Returns the Integration Program list for a ProgramId")
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "500", description = "Internal error that prevented the retreival of Integration Program list")
    public ResponseEntity getIntegrationProgramForProgramId(@PathVariable("programId") int programId, Authentication auth) {
        try {
            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
            return new ResponseEntity(this.integrationProgramService.getIntegrationProgramListForProgramId(programId, curUser), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while trying to list Integration Programs", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * API used to get the Integration Program for a specific IntegrationProgramId
     *
     * @param integrationProgramId IntegrationProgramId that you want the Integration Program for
     * Object for
     * @param auth
     * @return returns the list the Integration Program object based on
     * IntegrationProgramId specified
     */
    @GetMapping(value = "/{integrationProgramId}")
    @Operation(description = "API used to get the Integration Programs for a specific IntegrationProgramId", summary = "Get Integration Programs for an IntegrationProgramId", tags = ("integrationProgram"))
    @Parameters(
            @Parameter(name = "integrationProgramId", description = "IntegrationProgramId that you want to the Integration Program for"))
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "200", description = "Returns the Integration Program")
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "403", description = "Returns a HttpStatus.FORBIDDEN if the User does not have access")
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "404", description = "Returns a HttpStatus.NOT_FOUND if the IntegrationId specified does not exist")
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "500", description = "Internal error that prevented the retreival of Integration Program")
    public ResponseEntity getIntegrationProgram(@PathVariable("integrationProgramId") int integrationProgramId, Authentication auth) {
        try {
            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
            return new ResponseEntity(this.integrationProgramService.getIntegrationProgramById(integrationProgramId, curUser), HttpStatus.OK);
        } catch (EmptyResultDataAccessException ae) {
            logger.error("Error while trying to get IntegrationProgram Id=" + integrationProgramId, ae);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException ae) {
            logger.error("Error while trying to get IntegrationProgram Id=" + integrationProgramId, ae);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error while trying to get Integration Program Id=" + integrationProgramId, e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    /**
//     * API used to add an Integration Program
//     *
//     * @param integrationProgram IntegrationProgram object that you want to add
//     * @param auth
//     * @return returns a Success code if the operation was successful
//     */
//    @PostMapping(value = "")
//    @Operation(description = "API used to add an IntegrationProgram", summary = "Add IntegrationProgram", tags = ("integrationProgram"))
//    @Parameters(
//            @Parameter(name = "integrationProgram", description = "The Integration Program object that you want to add"))
//    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "200", description = "Returns a Success code if the operation was successful")
//    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "403", description = "Returns a HttpStatus.FORBIDDEN if the User does not have access")
//    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "406", description = "Returns a HttpStatus.NOT_ACCEPTABLE if the data supplied is not unique")
//    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "500", description = "Returns a HttpStatus.INTERNAL_SERVER_ERROR if there was some other error that did not allow the operation to complete")
//    public ResponseEntity addIntegration(@RequestBody IntegrationProgram integrationProgram, Authentication auth) {
//        try {
//            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
//            this.integrationProgramService.addIntegrationProgram(integrationProgram, curUser);
//            return new ResponseEntity(new ResponseCode("static.message.addSuccess"), HttpStatus.OK);
//        } catch (AccessDeniedException ae) {
//            logger.error("Error while trying to add IntegrationProgram", ae);
//            return new ResponseEntity(new ResponseCode("static.message.addFailed"), HttpStatus.FORBIDDEN);
//        } catch (DuplicateKeyException d) {
//            logger.error("Error while trying to add IntegrationProgram", d);
//            return new ResponseEntity(new ResponseCode("static.message.alreadExists"), HttpStatus.NOT_ACCEPTABLE);
//        } catch (Exception e) {
//            logger.error("Error while trying to add IntegrationProgram", e);
//            return new ResponseEntity(new ResponseCode("static.message.addFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    /**
     * API used to update an IntegrationProgram
     *
     * @param integrationPrograms Array of IntegrationProgram that you want to update
     * @param auth
     * @return returns a Success code if the operation was successful
     */
    @PutMapping(path = "")
    @Operation(description = "API used to update an IntegrationProgram", summary = "Update IntegrationProgram", tags = ("integrationProgram"))
    @Parameters(
            @Parameter(name = "integrationPrograms", description = "An array of Integration Program objects that you want to update"))
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "200", description = "Returns a Success code if the operation was successful")
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "403", description = "Returns a HttpStatus.FORBIDDEN if the User does not have access")
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "404", description = "Returns a HttpStatus.NOT_FOUND if the IntegrationId supplied does not exist")
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "406", description = "Returns a HttpStatus.NOT_ACCEPTABLE if the data supplied is not unique")
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "500", description = "Returns a HttpStatus.INTERNAL_SERVER_ERROR if there was some other error that did not allow the operation to complete")
    public ResponseEntity updateIntegrationProgram(@RequestBody IntegrationProgram integrationPrograms[], Authentication auth) {
        try {
            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
            this.integrationProgramService.updateIntegrationProgram(integrationPrograms, curUser);
            return new ResponseEntity(new ResponseCode("static.message.updateSuccess"), HttpStatus.OK);
        } catch (EmptyResultDataAccessException ae) {
            logger.error("Error while trying to update IntegrationProgram", ae);
            return new ResponseEntity(new ResponseCode("static.message.updateFailed"), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException ae) {
            logger.error("Error while trying to update IntegrationProgram", ae);
            return new ResponseEntity(new ResponseCode("static.message.updateFailed"), HttpStatus.FORBIDDEN);
        } catch (DuplicateKeyException d) {
            logger.error("Error while trying to update IntegrationProgram", d);
            return new ResponseEntity(new ResponseCode("static.message.alreadExists"), HttpStatus.NOT_ACCEPTABLE);
        } catch (Exception e) {
            logger.error("Error while trying to update IntegrationProgram", e);
            return new ResponseEntity(new ResponseCode("static.message.updateFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * API used to add a Manual JSON push
     *
     * @param integrationPrograms Array of IntegrationProgram that you want to
     * update
     * @param auth
     * @return returns a Success code if the operation was successful
     */
    @PostMapping(path = "/manualJson")
    @Operation(description = "API used to add a manual JSON push", summary = "Add manual JSON push", tags = ("integrationProgram"))
    @Parameters(
            @Parameter(name = "manualIntegrations", description = "An array of Manual Integration requests that you want to add"))
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "200", description = "Returns a Success code if the operation was successful")
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "403", description = "Returns a HttpStatus.FORBIDDEN if the User does not have access")
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "404", description = "Returns a HttpStatus.NOT_FOUND if the IntegrationId supplied does not exist")
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "500", description = "Returns a HttpStatus.INTERNAL_SERVER_ERROR if there was some other error that did not allow the operation to complete")
    public ResponseEntity addManualJsonPush(@RequestBody ManualIntegration manualIntegrations[], Authentication auth) {
        try {
            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
            this.integrationProgramService.addManualJsonPush(manualIntegrations, curUser);
            return new ResponseEntity(new ResponseCode("static.message.updateSuccess"), HttpStatus.OK);
        } catch (EmptyResultDataAccessException ae) {
            logger.error("Error while trying to add manual Json push", ae);
            return new ResponseEntity(new ResponseCode("static.message.updateFailed"), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException ae) {
            logger.error("Error while trying to add manual Json push", ae);
            return new ResponseEntity(new ResponseCode("static.message.updateFailed"), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error while trying to add manual Json push", e);
            return new ResponseEntity(new ResponseCode("static.message.updateFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * API used to get the report for Manual Json push
     *
     * @param startDate Start date that you want to report for
     * @param stopDate Stop date that you want to report for
     * @param auth
     * @return returns the list the Manual Json push based on the date variables
     */
    @JsonView(Views.ReportView.class)
    @GetMapping(value = "/manualJson/{startDate}/{stopDate}")
    @Operation(description = "API used to get the report for Manual Json push", summary = "API used to get the report for Manual Json push", tags = ("integrationProgram"))
    @Parameters(
            {
                @Parameter(name = "startDate", description = "Start date that you want the report for"),
                @Parameter(name = "stopDate", description = "Start date that you want the report for")
            }
    )
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "200", description = "Returns the report")
    @ApiResponse(content = @Content(mediaType = "text/json"), responseCode = "500", description = "Internal error that prevented the retreival of Integration Program")
    public ResponseEntity getManualJsonReport(@PathVariable(value = "startDate", required = true) String startDate, @PathVariable(value = "stopDate", required = true) String stopDate, Authentication auth) {
        try {
            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
            return new ResponseEntity(this.integrationProgramService.getManualJsonPushReport(startDate, stopDate, curUser), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while trying to get report", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
