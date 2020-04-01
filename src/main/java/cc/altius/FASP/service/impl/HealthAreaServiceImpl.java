/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.service.impl;

import cc.altius.FASP.dao.HealthAreaDao;
import cc.altius.FASP.dao.RealmDao;
import cc.altius.FASP.model.CustomUserDetails;
import cc.altius.FASP.model.DTO.PrgHealthAreaDTO;
import cc.altius.FASP.model.HealthArea;
import cc.altius.FASP.model.Realm;
import cc.altius.FASP.service.AclService;
import cc.altius.FASP.service.HealthAreaService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 *
 * @author akil
 */
@Service
public class HealthAreaServiceImpl implements HealthAreaService {

    @Autowired
    private HealthAreaDao healthAreaDao;
    @Autowired
    private RealmDao realmDao;
    @Autowired
    private AclService aclService;

    @Override
    public int addHealthArea(HealthArea h, CustomUserDetails curUser) {
        if (this.aclService.checkRealmAccessForUser(curUser, h.getRealm().getRealmId())) {
            return this.healthAreaDao.addHealthArea(h, curUser);
        } else {
            throw new AccessDeniedException("Access denied");
        }
    }

    @Override
    public int updateHealthArea(HealthArea h, CustomUserDetails curUser) {
        HealthArea ha = this.getHealthAreaById(h.getHealthAreaId(), curUser);
        if (this.aclService.checkRealmAccessForUser(curUser, ha.getRealm().getRealmId())) {
            return this.healthAreaDao.updateHealthArea(h, curUser);
        } else {
            throw new AccessDeniedException("Access denied");
        }
    }

    @Override
    public List<HealthArea> getHealthAreaList(CustomUserDetails curUser) {
        return this.healthAreaDao.getHealthAreaList(curUser);
    }

    @Override
    public List<HealthArea> getHealthAreaListByRealmId(int realmId, CustomUserDetails curUser) {
        Realm r = this.realmDao.getRealmById(realmId, curUser);
        if (r == null) {
            throw new EmptyResultDataAccessException(1);
        }
        if (this.aclService.checkRealmAccessForUser(curUser, realmId)) {
            return this.healthAreaDao.getHealthAreaListByRealmId(realmId, curUser);
        } else {
            throw new AccessDeniedException("Access denied");
        }
    }

    @Override
    public HealthArea getHealthAreaById(int healthAreaId, CustomUserDetails curUser) {
        HealthArea ha = this.healthAreaDao.getHealthAreaById(healthAreaId, curUser);
        if (ha != null) {
            return ha;
        } else {
            throw new EmptyResultDataAccessException(1);
        }
    }

    @Override
    public List<HealthArea> getHealthAreaListForSync(String lastSyncDate, CustomUserDetails curUser) {
        return this.healthAreaDao.getHealthAreaListForSync(lastSyncDate, curUser);
    }

}
