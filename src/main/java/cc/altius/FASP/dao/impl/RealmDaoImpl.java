/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.dao.impl;

import cc.altius.FASP.dao.LabelDao;
import cc.altius.FASP.dao.RealmDao;
import cc.altius.FASP.model.CustomUserDetails;
import cc.altius.FASP.model.Realm;
import cc.altius.FASP.model.rowMapper.RealmRowMapper;
import cc.altius.utils.DateUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author altius
 */
@Repository
public class RealmDaoImpl implements RealmDao {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private DataSource dataSource;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Autowired
    private LabelDao labelDao;

    @Override
    public List<Realm> getRealmList(boolean active, CustomUserDetails curUser) {
        String sql = " SELECT r.REALM_ID, r.REALM_CODE, r.MONTHS_IN_PAST_FOR_AMC, r.MONTHS_IN_FUTURE_FOR_AMC, r.ORDER_FREQUENCY, r.DEFAULT_REALM, "
                + "rl.`LABEL_ID` ,rl.`LABEL_EN`, rl.`LABEL_FR`, rl.`LABEL_PR`, rl.`LABEL_SP`,"
                + "cb.USER_ID `CB_USER_ID`, cb.USERNAME `CB_USERNAME`, lmb.USER_ID `LMB_USER_ID`, lmb.USERNAME `LMB_USERNAME`, r.ACTIVE, r.CREATED_DATE, r.LAST_MODIFIED_DATE "
                + "FROM rm_realm r "
                + " LEFT JOIN ap_label rl ON r.`LABEL_ID`=rl.`LABEL_ID` "
                + " LEFT JOIN us_user cb ON r.CREATED_BY=cb.USER_ID "
                + " LEFT JOIN us_user lmb ON r.LAST_MODIFIED_BY=lmb.USER_ID "
                + "WHERE TRUE ";
        Map<String, Object> params = new HashMap<>();
        if (curUser.getRealm().getRealmId() != -1) {
            sql += " AND r.REALM_ID=:realmId";
            params.put("realmId", curUser.getRealm().getRealmId());
        }
        return this.namedParameterJdbcTemplate.query(sql, params, new RealmRowMapper());
    }

    @Override
    @Transactional
    public int addRealm(Realm r, CustomUserDetails curUser) {
        SimpleJdbcInsert si = new SimpleJdbcInsert(this.dataSource).withTableName("rm_realm").usingGeneratedKeyColumns("REALM_ID");
        Date curDate = DateUtils.getCurrentDateObject(DateUtils.EST);
        Map<String, Object> params = new HashMap<>();
        params.put("REALM_CODE", r.getRealmCode());
        int labelId = this.labelDao.addLabel(r.getLabel(), curUser.getUserId());
        params.put("LABEL_ID", labelId);
        params.put("MONTHS_IN_PAST_FOR_AMC", r.getMonthInPastForAmc());
        params.put("MONTHS_IN_FUTURE_FOR_AMC", r.getMonthInFutureForAmc());
        params.put("ORDER_FREQUENCY", r.getOrderFrequency());
        params.put("DEFAULT_REALM", r.isDefaultRealm());
        params.put("ACTIVE", true);
        params.put("CREATED_BY", curUser.getUserId());
        params.put("CREATED_DATE", curDate);
        params.put("LAST_MODIFIED_BY", curUser.getUserId());
        params.put("LAST_MODIFIED_DATE", curDate);
        int realmId = si.executeAndReturnKey(params).intValue();
        if (r.isDefaultRealm()) {
            params.clear();
            params.put("realmId", realmId);
            this.namedParameterJdbcTemplate.update("UPDATE rm_realm SET DEFAULT_REALM=0 WHERE REALM_ID!=:realmId", params);
        }
        return realmId;
    }

    @Override
    @Transactional
    public int updateRealm(Realm r, CustomUserDetails curUser) {
        Date curDate = DateUtils.getCurrentDateObject(DateUtils.EST);
        Map<String, Object> params = new HashMap<>();
        params.put("realmId", r.getRealmId());
        params.put("labelEn", r.getLabel().getLabel_en());
        params.put("realmCode", r.getRealmCode());
        params.put("monthInPastForAmc", r.getMonthInPastForAmc());
        params.put("monthInFutureForAmc", r.getMonthInFutureForAmc());
        params.put("orgerFrequency", r.getOrderFrequency());
        params.put("default", r.isDefaultRealm());
        params.put("active", r.isActive());
        params.put("curUser", curUser.getUserId());
        params.put("curDate", curDate);
        int rows = this.namedParameterJdbcTemplate.update("UPDATE rm_realm r LEFT JOIN ap_label rl ON r.LABEL_ID=rl.LABEL_ID SET "
                + "r.REALM_CODE=:realmCode, "
                + "r.MONTHS_IN_PAST_FOR_AMC=:monthInPastForAmc, "
                + "r.MONTHS_IN_FUTURE_FOR_AMC=:monthInFutureForAmc, "
                + "r.ORDER_FREQUENCY=:orgerFrequency, "
                + "r.DEFAULT_REALM=:default,"
                + "r.ACTIVE=:active, "
                + "r.LAST_MODIFIED_BY=IF("
                + "     r.REALM_CODE!=:realmCode OR "
                + "     r.MONTHS_IN_PAST_FOR_AMC=:monthInPastForAmc OR "
                + "     r.MONTHS_IN_FUTURE_FOR_AMC=:monthInFutureForAmc OR "
                + "     r.ORDER_FREQUENCY=:orgerFrequency OR "
                + "     r.ACTIVE=:active, :curUser, r.LAST_MODIFIED_BY), "
                + "r.LAST_MODIFIED_DATE=IF("
                + "     r.REALM_CODE!=:realmCode OR "
                + "     r.MONTHS_IN_PAST_FOR_AMC=:monthInPastForAmc OR "
                + "     r.MONTHS_IN_FUTURE_FOR_AMC=:monthInFutureForAmc OR "
                + "     r.ORDER_FREQUENCY=:orgerFrequency OR "
                + "     r.ACTIVE=:active, :curDate, r.LAST_MODIFIED_DATE), "
                + "rl.LABEL_EN=:labelEn, "
                + "rl.LAST_MODIFIED_BY=IF(rl.LABEL_EN!=:labelEn, :curUser, rl.LAST_MODIFIED_BY), "
                + "rl.LAST_MODIFIED_DATE=IF(rl.LABEL_EN!=:labelEn, :curDate, rl.LAST_MODIFIED_DATE) "
                + "WHERE r.REALM_ID=:realmId", params);
        if (r.isDefaultRealm()) {
            this.namedParameterJdbcTemplate.update("UPDATE rm_realm SET DEFAULT_REALM=0 WHERE REALM_ID!=:realmId", params);
        }
        return rows;
    }

    @Override
    public Realm getRealmById(int realmId, CustomUserDetails curUser) {
        String sqlString = " SELECT r.REALM_ID, r.REALM_CODE, r.MONTHS_IN_PAST_FOR_AMC, r.MONTHS_IN_FUTURE_FOR_AMC, r.ORDER_FREQUENCY, r.DEFAULT_REALM, "
                + "rl.`LABEL_ID` ,rl.`LABEL_EN`, rl.`LABEL_FR`, rl.`LABEL_PR`, rl.`LABEL_SP`,"
                + "cb.USER_ID `CB_USER_ID`, cb.USERNAME `CB_USERNAME`, lmb.USER_ID `LMB_USER_ID`, lmb.USERNAME `LMB_USERNAME`, r.ACTIVE, r.CREATED_DATE, r.LAST_MODIFIED_DATE "
                + "FROM rm_realm r "
                + " LEFT JOIN ap_label rl ON r.`LABEL_ID`=rl.`LABEL_ID` "
                + " LEFT JOIN us_user cb ON r.CREATED_BY=cb.USER_ID "
                + " LEFT JOIN us_user lmb ON r.LAST_MODIFIED_BY=lmb.USER_ID "
                + " WHERE r.REALM_ID=:realmId";
        Map<String, Object> params = new HashMap<>();
        params.put("realmId", realmId);
        return this.namedParameterJdbcTemplate.queryForObject(sqlString, params, new RealmRowMapper());
    }

    @Override
    public List<Realm> getRealmListForSync(String lastSyncDate, CustomUserDetails curUser) {
        String sql = " SELECT r.REALM_ID, r.REALM_CODE, r.MONTHS_IN_PAST_FOR_AMC, r.MONTHS_IN_FUTURE_FOR_AMC, r.ORDER_FREQUENCY, r.DEFAULT_REALM, "
                + "rl.`LABEL_ID` ,rl.`LABEL_EN`, rl.`LABEL_FR`, rl.`LABEL_PR`, rl.`LABEL_SP`,"
                + "cb.USER_ID `CB_USER_ID`, cb.USERNAME `CB_USERNAME`, lmb.USER_ID `LMB_USER_ID`, lmb.USERNAME `LMB_USERNAME`, r.ACTIVE, r.CREATED_DATE, r.LAST_MODIFIED_DATE "
                + "FROM rm_realm r "
                + " LEFT JOIN ap_label rl ON r.`LABEL_ID`=rl.`LABEL_ID` "
                + " LEFT JOIN us_user cb ON r.CREATED_BY=cb.USER_ID "
                + " LEFT JOIN us_user lmb ON r.LAST_MODIFIED_BY=lmb.USER_ID "
                + "WHERE r.LAST_MODIFIED_DATE>:lastSyncDate ";
        Map<String, Object> params = new HashMap<>();
        params.put("lastSyncDate", lastSyncDate);
        if (curUser.getRealm().getRealmId() != -1) {
            sql += " AND r.REALM_ID=:realmId";
            params.put("realmId", curUser.getRealm().getRealmId());
        }
        return this.namedParameterJdbcTemplate.query(sql, params, new RealmRowMapper());
    }

}
