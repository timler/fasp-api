/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.rest.controller;

import cc.altius.FASP.model.CustomUserDetails;
import cc.altius.FASP.model.DTO.PrgDataSourceTypeDTO;
import cc.altius.FASP.model.DataSourceType;
import cc.altius.FASP.model.ResponseCode;
import cc.altius.FASP.service.DataSourceTypeService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author palash
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:4202", "https://faspdeveloper.github.io", "chrome-extension://fhbjgbiflinjbdggehcddcbncdddomop"})
public class DataSourceTypeRestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DataSourceTypeService dataSourceTypeService;

    @PostMapping(value = "/dataSourceType")
    public ResponseEntity addDataSourceType(@RequestBody DataSourceType dataSourceType, Authentication auth) {
        try {
            CustomUserDetails curUser = (CustomUserDetails) auth.getPrincipal();
            this.dataSourceTypeService.addDataSourceType(dataSourceType, curUser);
            return new ResponseEntity("static.message.dataSourceType.addSuccess", HttpStatus.OK);
        } catch (AccessDeniedException e) {
            logger.error("Error while trying to add DataSourceType", e);
            return new ResponseEntity("static.message.dataSourceType.addFailed", HttpStatus.UNAUTHORIZED);
        } catch (DuplicateKeyException e) {
            logger.error("Error while trying to add DataSourceType", e);
            return new ResponseEntity("static.message.dataSourceType.addFailed", HttpStatus.NOT_ACCEPTABLE);
        } catch (Exception e) {
            logger.error("Error while trying to add DataSourceType", e);
            return new ResponseEntity("static.message.dataSourceType.addFailed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/dataSourceType")
    public ResponseEntity getDataSourceTypeList(Authentication auth) {
        try {
            CustomUserDetails curUser = (CustomUserDetails) auth.getPrincipal();
            return new ResponseEntity(this.dataSourceTypeService.getDataSourceTypeList(true, curUser), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while trying to get DataSourceType list", e);
            return new ResponseEntity(new ResponseCode("static.message.dataSourceType.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/dataSourceType/all")
    public ResponseEntity getDataSourceTypeListAll(Authentication auth) {
        try {
            CustomUserDetails curUser = (CustomUserDetails) auth.getPrincipal();
            return new ResponseEntity(this.dataSourceTypeService.getDataSourceTypeList(false, curUser), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while trying to get DataSourceType list", e);
            return new ResponseEntity(new ResponseCode("static.message.dataSourceType.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping(value = "/dataSourceType/{dataSourceTypeId}")
    public ResponseEntity getDataSourceTypeById(@PathVariable("dataSourceTypeId") int dataSourceTypeId, Authentication auth) {
        try {
            CustomUserDetails curUser = (CustomUserDetails) auth.getPrincipal();
            return new ResponseEntity(this.dataSourceTypeService.getDataSourceTypeById(dataSourceTypeId, curUser), HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            logger.error("Error while trying to get DataSourceType list", e);
            return new ResponseEntity(new ResponseCode("static.message.dataSourceType.listFailed"), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            logger.error("Error while trying to get DataSourceType list", e);
            return new ResponseEntity(new ResponseCode("static.message.dataSourceType.listFailed"), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            logger.error("Error while trying to get DataSourceType list", e);
            return new ResponseEntity(new ResponseCode("static.message.dataSourceType.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/dataSourceType/realmId/{realmId}")
    public ResponseEntity getDataSourceTypeListForRealmId(@PathVariable("realmId") int realmId, Authentication auth) {
        try {
            CustomUserDetails curUser = (CustomUserDetails) auth.getPrincipal();
            return new ResponseEntity(this.dataSourceTypeService.getDataSourceTypeForRealm(realmId, true, curUser), HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            logger.error("Error while trying to get DataSourceType list", e);
            return new ResponseEntity(new ResponseCode("static.message.dataSourceType.listFailed"), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            logger.error("Error while trying to get DataSourceType list", e);
            return new ResponseEntity(new ResponseCode("static.message.dataSourceType.listFailed"), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            logger.error("Error while trying to get DataSourceType list", e);
            return new ResponseEntity(new ResponseCode("static.message.dataSourceType.listFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = "/dataSourceType")
    public ResponseEntity editDataSourceType(@RequestBody DataSourceType dataSourceType, Authentication auth) {
        try {
            CustomUserDetails curUser = (CustomUserDetails) auth.getPrincipal();
            this.dataSourceTypeService.updateDataSourceType(dataSourceType, curUser);
            return new ResponseEntity(new ResponseCode("static.message.DataSourceType.updateSuccess"), HttpStatus.OK);
        } catch (AccessDeniedException e) {
            logger.error("Error while trying to update DataSourceType", e);
            return new ResponseEntity(new ResponseCode("static.message.dataSourceType.updateFailed"), HttpStatus.UNAUTHORIZED);
        } catch (EmptyResultDataAccessException e) {
            logger.error("Error while trying to update DataSourceType", e);
            return new ResponseEntity(new ResponseCode("static.message.dataSourceType.updateFailed"), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error while trying to update DataSourceType", e);
            return new ResponseEntity(new ResponseCode("static.message.dataSourceType.updateFailed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getDataSourceTypeListForSync")
    public String getDataSourceTypeListForSync(@RequestParam String lastSyncDate) throws UnsupportedEncodingException {
        String json;
        List<PrgDataSourceTypeDTO> dataSourceTypeTypeList = this.dataSourceTypeService.getDataSourceTypeListForSync(lastSyncDate);
        Gson gson = new Gson();
        Type typeList = new TypeToken<List>() {
        }.getType();
        json = gson.toJson(dataSourceTypeTypeList, typeList);
        return json;
    }

}
