/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.rest.controller;

import cc.altius.FASP.model.CustomUserDetails;
import cc.altius.FASP.model.ProgramData;
import cc.altius.FASP.model.ProgramIdAndVersionId;
import cc.altius.FASP.model.ResponseCode;
import cc.altius.FASP.model.UpdateProgramVersion;
import cc.altius.FASP.model.Views;
import cc.altius.FASP.model.report.ActualConsumptionDataInput;
import cc.altius.FASP.model.report.LoadProgramInput;
import cc.altius.FASP.service.ProgramDataService;
import cc.altius.FASP.service.ProgramService;
import cc.altius.FASP.service.UserService;
import static cc.altius.FASP.utils.CompressUtils.compress;
import static cc.altius.FASP.utils.CompressUtils.isCompress;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @author akil
 */
@RestController
@RequestMapping("/api")
public class ProgramDataRestController {

    private final Logger logger = LoggerFactory.getLogger(ProgramDataRestController.class);

    @Autowired
    private ProgramDataService programDataService;
    @Autowired
    private ProgramService programService;
    @Autowired
    private UserService userService;

    @JsonView(Views.InternalView.class)
    @GetMapping("/programData/programId/{programId}/versionId/{versionId}")
    public ResponseEntity getProgramData(@PathVariable(value = "programId", required = true) int programId, @PathVariable(value = "versionId", required = true) int versionId, Authentication auth) {
        try {
            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
            return new ResponseEntity(this.programDataService.getProgramData(programId, versionId, curUser, false, false), HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            logger.error("Error while trying to get ProgramData", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            logger.error("Error while trying to get ProgramData", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error while trying to get ProgramData", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @JsonView(Views.InternalView.class)
    @PostMapping("/programData")
    public ResponseEntity getLoadProgramData(@RequestBody List<LoadProgramInput> loadProgramInputList, Authentication auth) {
        try {
            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
            List<ProgramData> masters = this.programDataService.getProgramData(loadProgramInputList, curUser);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(masters);
            if (isCompress(jsonString)) {
                return new ResponseEntity(compress(jsonString), HttpStatus.OK);
            } else {
                return new ResponseEntity(masters, HttpStatus.OK);
            }
        } catch (EmptyResultDataAccessException e) {
            logger.error("Error while trying to get ProgramData", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            logger.error("Error while trying to get ProgramData", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error while trying to get ProgramData", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @JsonView(Views.ArtmisView.class)
//    @GetMapping("/programData/artmis/programId/{programId}/versionId/{versionId}")
//    public ResponseEntity getProgramDataArtmis(@PathVariable(value = "programId", required = true) int programId, @PathVariable(value = "versionId", required = true) int versionId, Authentication auth) {
//        try {
//            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
//            return new ResponseEntity(this.programDataService.getProgramData(programId, versionId, curUser, true, false), HttpStatus.OK);
//        } catch (EmptyResultDataAccessException e) {
//            logger.error("Error while trying to get ProgramData", e);
//            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.NOT_FOUND);
//        } catch (AccessDeniedException e) {
//            logger.error("Error while trying to get ProgramData", e);
//            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.FORBIDDEN);
//        } catch (Exception e) {
//            logger.error("Error while trying to get ProgramData", e);
//            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @JsonView(Views.GfpVanView.class)
//    @GetMapping("/programData/gfpvan/programId/{programId}/versionId/{versionId}")
//    public ResponseEntity getProgramDataGfpVan(@PathVariable(value = "programId", required = true) int programId, @PathVariable(value = "versionId", required = true) int versionId, Authentication auth) {
//        try {
//            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
//            return new ResponseEntity(this.programDataService.getProgramData(programId, versionId, curUser, true, false), HttpStatus.OK);
//        } catch (EmptyResultDataAccessException e) {
//            logger.error("Error while trying to get ProgramData", e);
//            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.NOT_FOUND);
//        } catch (AccessDeniedException e) {
//            logger.error("Error while trying to get ProgramData", e);
//            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.FORBIDDEN);
//        } catch (Exception e) {
//            logger.error("Error while trying to get ProgramData", e);
//            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
    @GetMapping("/versionType")
    public ResponseEntity getVersionType(Authentication auth) {
        try {
            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
            return new ResponseEntity(this.programDataService.getVersionTypeList(), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while trying to get Version Type", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/versionStatus")
    public ResponseEntity getVersionStatus(Authentication auth) {
        try {
            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
            return new ResponseEntity(this.programDataService.getVersionStatusList(), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while trying to get Version Status", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/programVersion/programId/{programId}/versionId/{versionId}/realmCountryId/{realmCountryId}/healthAreaId/{healthAreaId}/organisationId/{organisationId}/versionTypeId/{versionTypeId}/versionStatusId/{versionStatusId}/dates/{startDate}/{stopDate}")
    public ResponseEntity getProgramVersionList(
            @PathVariable(value = "programId", required = true) int programId,
            @PathVariable(value = "versionId", required = true) int versionId,
            @PathVariable(value = "realmCountryId", required = true) int realmCountryId,
            @PathVariable(value = "healthAreaId", required = true) int healthAreaId,
            @PathVariable(value = "organisationId", required = true) int organisationId,
            @PathVariable(value = "versionTypeId", required = true) int versionTypeId,
            @PathVariable(value = "versionStatusId", required = true) int versionStatusId,
            @PathVariable(value = "startDate", required = true) String startDate,
            @PathVariable(value = "stopDate", required = true) String stopDate,
            Authentication auth) {
        try {
            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
            return new ResponseEntity(this.programDataService.getProgramVersionList(programId, versionId, realmCountryId, healthAreaId, organisationId, versionTypeId, versionStatusId, startDate, stopDate, curUser), HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            logger.error("Error while trying to get ProgramData", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            logger.error("Error while trying to get ProgramData", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error while trying to get ProgramData", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/programVersion/programId/{programId}/versionId/{versionId}/versionStatusId/{versionStatusId}")
    public ResponseEntity updateProgramVersion(@RequestBody UpdateProgramVersion updateProgramVersion, @PathVariable(value = "programId", required = true) int programId, @PathVariable(value = "versionId", required = true) int versionId, @PathVariable(value = "versionStatusId", required = true) int versionStatusId, Authentication auth) {
        try {
            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
            return new ResponseEntity(this.programDataService.updateProgramVersion(programId, versionId, versionStatusId, updateProgramVersion, curUser), HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            logger.error("Error while trying to update ProgramVersion", e);
            return new ResponseEntity(new ResponseCode("static.message.updateFailed"), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            logger.error("Error while trying to update ProgramVersion", e);
            return new ResponseEntity(new ResponseCode("static.message.updateFailed"), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error while trying to update ProgramVersion", e);
            return new ResponseEntity(new ResponseCode("static.message.updateFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/programVersion/resetProblem")
    public ResponseEntity resetProblemForProgramIds(@RequestBody int[] programIds, Authentication auth) {
        try {
            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
            this.programDataService.resetProblemListForPrograms(programIds, curUser);
            return new ResponseEntity(HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            logger.error("Error while trying to update ProgramVersion", e);
            return new ResponseEntity(new ResponseCode("static.message.updateFailed"), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            logger.error("Error while trying to update ProgramVersion", e);
            return new ResponseEntity(new ResponseCode("static.message.updateFailed"), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error while trying to update ProgramVersion", e);
            return new ResponseEntity(new ResponseCode("static.message.updateFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
//    @GetMapping("/programData/checkErpOrder/orderNo/{orderNo}/primeLineNo/{primeLineNo}/realmCountryId/{realmCountryId}/planningUnitId/{planningUnitId}")
//    public ResponseEntity checkErpOrder(
//            @PathVariable(value = "orderNo", required = true) String orderNo,
//            @PathVariable(value = "primeLineNo", required = true) String primeLineNo,
//            @PathVariable(value = "realmCountryId", required = true) int realmCountryId,
//            @PathVariable(value = "planningUnitId", required = true) int planningUnitId,
//            Authentication auth) {
//        try {
//            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
//            return new ResponseEntity(this.programDataService.checkErpOrder(orderNo, primeLineNo, realmCountryId, planningUnitId), HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error("Error while trying to update ProgramVersion", e);
//            return new ResponseEntity(new ResponseCode("static.message.updateFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @GetMapping("/programData/shipmentSync/programId/{programId}/versionId/{versionId}/userId/{userId}/lastSyncDate/{lastSyncDate}")
    public ResponseEntity shipmentSync(@PathVariable(value = "programId", required = true) int programId, @PathVariable(value = "versionId", required = true) int versionId, @PathVariable(value = "userId", required = true) int userId, @PathVariable("lastSyncDate") String lastSyncDate, Authentication auth) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.parse(lastSyncDate);
            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
            return new ResponseEntity(this.programDataService.getShipmentListForSync(programId, versionId, userId, lastSyncDate, curUser), HttpStatus.OK);
        } catch (ParseException p) {
            logger.error("Error while getting Sync list for Shipments", p);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.PRECONDITION_FAILED);
        } catch (Exception e) {
            logger.error("Error while getting Sync list for Shipments", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Sample JSON
     * [{"programId":2535,"versionId":3},{"programId":2001,"versionId":5}]
     *
     * @param programVersionList
     * @param auth
     * @return
     */
    @PostMapping("/programData/checkNewerVersions")
    public ResponseEntity checkNewerVersions(@RequestBody List<ProgramIdAndVersionId> programVersionList, Authentication auth) {
        try {
            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
            return new ResponseEntity(this.programDataService.checkNewerVersions(programVersionList, curUser), HttpStatus.OK);
        } catch (AccessDeniedException e) {
            logger.error("Error while trying to check ProgramVersion", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error while trying to check ProgramVersion", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/programData/getLatestVersionForProgram/{programId}")
    public ResponseEntity getLatestVersionForProgram(@PathVariable(value = "programId", required = true) int programId) {
        try {
            return new ResponseEntity(this.programService.getLatestVersionForPrograms("" + programId).get(0).getVersionId(), HttpStatus.OK);
        } catch (AccessDeniedException e) {
            logger.error("Error while trying to get latest version for program", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error while trying to get latest version for program", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/programData/getLastModifiedDateForProgram/{programId}/{versionId}")
    public ResponseEntity getLastModifiedDateForProgram(@PathVariable(value = "programId", required = true) int programId, @PathVariable(value = "versionId", required = true) int versionId) {
        try {
            return new ResponseEntity(this.programDataService.getLastModifiedDateForProgram(programId, versionId), HttpStatus.OK);
        } catch (AccessDeniedException e) {
            logger.error("Error while trying to get last modified date for program", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error while trying to get last modified date for program", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Used in Forecasting Unit import data from Supply Plan
    /**
     * <pre>
     * Sample JSON {"programVersionList": [{"programId": "2476", "versionId": "100"},{"programId": "2486", "versionId": "115"}], "planningUnitIds": ["1150", "1477", "1483"], "startDate": "2018-01-01", "stopDate":"2021-12-01", "regionIds":["70", "73", "74"]}
     * -- Program Id must be a valid Supply Plan Program Id, cannot be -1 (Any)      *
     * -- versionId must be a valid VersionId of that Program
     * -- forecastingUnitIds must be a list of ForecastingUnits whose PlanningUnits you want the Consumption data for
     * -- startDate and stopDate are required fields and indicate the start and stop dates that you want the consumption from
     * -- regionIdList is the list of regionIds that you want the data from
     * -- Return the list of Actual Consumption data for the given filters
     * </pre>
     *
     * @param ActualConsumptionDataInput
     * @param auth Authentication object from JWT
     * @return ProgramProductCatalogOutput
     */
    @JsonView(Views.ReportView.class)
    @PostMapping(value = "/program/actualConsumptionReport")
    public ResponseEntity getProgramProductCatalog(@RequestBody ActualConsumptionDataInput acd, Authentication auth) {
        try {
            CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
            return new ResponseEntity(this.programDataService.getActualConsumptionDataInput(acd, curUser), HttpStatus.OK);
        } catch (AccessDeniedException ade) {
            logger.error("/api/program/actualConsumptionReport", ade);
            return new ResponseEntity(new ResponseCode("static.label.listFailed"), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("/api/program/actualConsumptionReport", e);
            return new ResponseEntity(new ResponseCode("static.label.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/programData/getLatestVersionForPrograms")
    public ResponseEntity getLatestVersionForProgram(@RequestBody String[] programIds) {
        try {
            String programIdsString = getProgramIds(programIds);
            return new ResponseEntity(this.programService.getLatestVersionForPrograms(programIdsString), HttpStatus.OK);
        } catch (AccessDeniedException e) {
            logger.error("Error while trying to get latest version for program", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error while trying to get latest version for program", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @JsonView(Views.ReportView.class)
    @GetMapping({"/program/data/version/trans/programId/{programId}", "/program/data/version/trans/programId/{programId}/versionId/{versionId}"})
    public ResponseEntity getProgramVersionTrans(@PathVariable(value = "programId", required = true) int programId, @PathVariable(value = "versionId", required = false) int versionId, Authentication auth) {
        CustomUserDetails curUser = this.userService.getCustomUserByUserId(((CustomUserDetails) auth.getPrincipal()).getUserId());
        try {
            if (versionId == 0) {
                versionId = -1;
            }
            return new ResponseEntity(this.programDataService.getProgramVersionTrans(programId, versionId, curUser), HttpStatus.OK);
        } catch (AccessDeniedException e) {
            logger.error("Error while trying to get latest version for program", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error while trying to get latest version for program", e);
            return new ResponseEntity(new ResponseCode("static.message.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getProgramIds(String[] programIds) {
        if (programIds == null) {
            return "";
        } else {
            String opt = String.join("','", programIds);
            if (programIds.length > 0) {
                return "'" + opt + "'";
            } else {
                return opt;
            }
        }
    }
}
