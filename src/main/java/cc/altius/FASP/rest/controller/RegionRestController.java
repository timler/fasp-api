/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.rest.controller;

import cc.altius.FASP.model.DTO.PrgRegionDTO;
import cc.altius.FASP.model.CustomUserDetails;
import cc.altius.FASP.model.Region;
import cc.altius.FASP.model.ResponseCode;
import cc.altius.FASP.service.RegionService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:4202", "http://192.168.43.113:4202", "https://faspdeveloper.github.io","chrome-extension://fhbjgbiflinjbdggehcddcbncdddomop"})
public class RegionRestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    RegionService regionService;

    @PostMapping(value = "/region")
    public ResponseEntity addRegion(@RequestBody Region region, Authentication auth) {
        try {
            CustomUserDetails curUser = (CustomUserDetails) auth.getPrincipal();
            this.regionService.addRegion(region, curUser);
            return new ResponseEntity(new ResponseCode("static.message.addSuccess"), HttpStatus.OK);
        } catch (AccessDeniedException e) {
            logger.error("Error while trying to add Region", e);
            return new ResponseEntity(new ResponseCode("static.message.addFailed"), HttpStatus.UNAUTHORIZED);
        } catch (DuplicateKeyException e) {
            logger.error("Error while trying to add Region", e);
            return new ResponseEntity(new ResponseCode("static.message.addFailed"), HttpStatus.NOT_ACCEPTABLE);
        } catch (Exception e) {
            logger.error("Error while trying to add Region", e);
            return new ResponseEntity(new ResponseCode("static.message.addFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(path = "/region")
    public ResponseEntity putRegion(@RequestBody Region region, Authentication auth) {
        try {
            CustomUserDetails curUser = (CustomUserDetails) auth.getPrincipal();
            this.regionService.updateRegion(region, curUser);
            return new ResponseEntity(new ResponseCode("static.message.updateSuccess"), HttpStatus.OK);
        } catch (AccessDeniedException e) {
            logger.error("Error while trying to update Region", e);
            return new ResponseEntity(new ResponseCode("static.message.regoin.updateFailed"), HttpStatus.UNAUTHORIZED);
        } catch (DuplicateKeyException e) {
            logger.error("Error while trying to update Region", e);
            return new ResponseEntity(new ResponseCode("static.message.regoin.updateFailed"), HttpStatus.NOT_ACCEPTABLE);
        } catch (Exception e) {
            logger.error("Error while trying to update Region", e);
            return new ResponseEntity(new ResponseCode("static.message.regoin.updateFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/region")
    public ResponseEntity getRegion(Authentication auth) {
        try {
            CustomUserDetails curUser = (CustomUserDetails) auth.getPrincipal();
            return new ResponseEntity(this.regionService.getRegionList(curUser), HttpStatus.OK);
        } catch (DataAccessException e) {
            logger.error("Error while trying to list Region", e);
            return new ResponseEntity(new ResponseCode("static.message.regoin.listFailed"), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            logger.error("Error while trying to list Region", e);
            return new ResponseEntity(new ResponseCode("static.message.regoin.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/region/{regionId}")
    public ResponseEntity getRegion(@PathVariable("regionId") int regionId, Authentication auth) {
        try {
            CustomUserDetails curUser = (CustomUserDetails) auth.getPrincipal();
            return new ResponseEntity(this.regionService.getRegionById(regionId, curUser), HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            logger.error("Error while trying to list Region", e);
            return new ResponseEntity(new ResponseCode("static.message.regoin.listFailed"), HttpStatus.NOT_FOUND);
        } catch (DataAccessException e) {
            logger.error("Error while trying to list Region", e);
            return new ResponseEntity(new ResponseCode("static.message.regoin.listFailed"), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            logger.error("Error while trying to list Region", e);
            return new ResponseEntity(new ResponseCode("static.message.regoin.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/region/realmCountryId/{realmCountryId}")
    public ResponseEntity getRegionByRealmCountry(@PathVariable("realmCountryId") int realmCountryId, Authentication auth) {
        try {
            CustomUserDetails curUser = (CustomUserDetails) auth.getPrincipal();
            return new ResponseEntity(this.regionService.getRegionListByRealmCountryId(realmCountryId, curUser), HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            logger.error("Error while trying to list Region", e);
            return new ResponseEntity(new ResponseCode("static.message.regoin.listFailed"), HttpStatus.UNAUTHORIZED);
        } catch (DataAccessException e) {
            logger.error("Error while trying to list Region", e);
            return new ResponseEntity(new ResponseCode("static.message.regoin.listFailed"), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error while trying to list Region", e);
            return new ResponseEntity(new ResponseCode("static.message.regoin.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getRegionListForSync")
    public String getRegionListForSync(@RequestParam String lastSyncDate,int realmId) throws UnsupportedEncodingException {
        String json;
        List<PrgRegionDTO> regionList = this.regionService.getRegionListForSync(lastSyncDate,realmId);
        Gson gson = new Gson();
        Type typeList = new TypeToken<List>() {
        }.getType();
        json = gson.toJson(regionList, typeList);
        return json;
    }
    
}
