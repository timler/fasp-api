/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.service;

import cc.altius.FASP.model.CustomUserDetails;
import java.util.Map;

/**
 *
 * @author akil
 */
public interface AclService {

    public boolean checkAccessForUser(CustomUserDetails curUser, int realmId, int realmCountryId, int healthAreaId, int organisationId, int programId);

    public boolean checkRealmAccessForUser(CustomUserDetails curUser, int realmId);

    public boolean checkHealthAreaAccessForUser(CustomUserDetails curUser, int healthAreaId);

    public boolean checkOrganisationAccessForUser(CustomUserDetails curUser, int organisationId);

    public boolean checkProgramAccessForUser(CustomUserDetails curUser, int programId);

    public String addUserAclForRealm(String sqlString, Map<String, Object> params, String realmAlias, int realmId, CustomUserDetails curUser);

    public String addUserAclForRealm(String sqlString, Map<String, Object> params, String realmAlias, CustomUserDetails curUser);
    
    public void addUserAclForRealm(StringBuilder sb, Map<String, Object> params, String realmAlias, int realmId, CustomUserDetails curUser);

    public void addUserAclForRealm(StringBuilder sb, Map<String, Object> params, String realmAlias, CustomUserDetails curUser);

    public void addFullAclForProgram(StringBuilder sb, Map<String, Object> params, String programAlias, CustomUserDetails curUser);
}
