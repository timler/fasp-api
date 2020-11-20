/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.dao.impl;

import cc.altius.FASP.dao.LabelDao;
import cc.altius.FASP.dao.ProgramDao;
import cc.altius.FASP.model.CustomUserDetails;
import cc.altius.FASP.model.DTO.ManualTaggingDTO;
import cc.altius.FASP.model.DTO.ManualTaggingOrderDTO;
import cc.altius.FASP.model.DTO.ProgramDTO;
import cc.altius.FASP.model.DTO.rowMapper.ManualTaggingDTORowMapper;
import cc.altius.FASP.model.DTO.rowMapper.ManualTaggingOrderDTORowMapper;
import cc.altius.FASP.model.DTO.rowMapper.ProgramDTORowMapper;
import cc.altius.FASP.model.LabelConstants;
import cc.altius.FASP.model.LoadProgram;
import cc.altius.FASP.model.Program;
import cc.altius.FASP.model.ProgramPlanningUnit;
import cc.altius.FASP.model.SimpleObject;
import cc.altius.FASP.model.UserAcl;
import cc.altius.FASP.model.Version;
import cc.altius.FASP.model.rowMapper.LoadProgramRowMapper;
import cc.altius.FASP.model.rowMapper.LoadProgramVersionRowMapper;
import cc.altius.FASP.model.rowMapper.ProgramListResultSetExtractor;
import cc.altius.FASP.model.rowMapper.ProgramPlanningUnitRowMapper;
import cc.altius.FASP.model.rowMapper.ProgramResultSetExtractor;
import cc.altius.FASP.model.rowMapper.SimpleObjectRowMapper;
import cc.altius.FASP.model.rowMapper.VersionRowMapper;
import cc.altius.FASP.service.AclService;
import cc.altius.FASP.utils.LogUtils;
import cc.altius.utils.DateUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author altius
 */
@Repository
public class ProgramDaoImpl implements ProgramDao {

    @Autowired
    private LabelDao labelDao;
    @Autowired
    private AclService aclService;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public String sqlListString = "SELECT   "
            + "     p.PROGRAM_ID, p.`PROGRAM_CODE`, p.AIR_FREIGHT_PERC, p.SEA_FREIGHT_PERC, p.PLANNED_TO_SUBMITTED_LEAD_TIME,  "
            + "     cpv.VERSION_ID `CV_VERSION_ID`, cpv.NOTES `CV_VERSION_NOTES`, cpv.CREATED_DATE `CV_CREATED_DATE`, cpvcb.USER_ID `CV_CB_USER_ID`, cpvcb.USERNAME `CV_CB_USERNAME`, cpv.LAST_MODIFIED_DATE `CV_LAST_MODIFIED_DATE`, cpvlmb.USER_ID `CV_LMB_USER_ID`, cpvlmb.USERNAME `CV_LMB_USERNAME`,  "
            + "     vt.VERSION_TYPE_ID `CV_VERSION_TYPE_ID`, vt.LABEL_ID `CV_VERSION_TYPE_LABEL_ID`, vt.LABEL_EN `CV_VERSION_TYPE_LABEL_EN`, vt.LABEL_FR `CV_VERSION_TYPE_LABEL_FR`, vt.LABEL_SP `CV_VERSION_TYPE_LABEL_SP`, vt.LABEL_PR `CV_VERSION_TYPE_LABEL_PR`,  "
            + "     vs.VERSION_STATUS_ID `CV_VERSION_STATUS_ID`, vs.LABEL_ID `CV_VERSION_STATUS_LABEL_ID`, vs.LABEL_EN `CV_VERSION_STATUS_LABEL_EN`, vs.LABEL_FR `CV_VERSION_STATUS_LABEL_FR`, vs.LABEL_SP `CV_VERSION_STATUS_LABEL_SP`, vs.LABEL_PR `CV_VERSION_STATUS_LABEL_PR`,  "
            + "     p.SUBMITTED_TO_APPROVED_LEAD_TIME, p.APPROVED_TO_SHIPPED_LEAD_TIME, p.SHIPPED_TO_ARRIVED_BY_SEA_LEAD_TIME, p.SHIPPED_TO_ARRIVED_BY_AIR_LEAD_TIME, p.ARRIVED_TO_DELIVERED_LEAD_TIME,  "
            + "     p.PROGRAM_NOTES, pm.USERNAME `PROGRAM_MANAGER_USERNAME`, pm.USER_ID `PROGRAM_MANAGER_USER_ID`,  "
            + "     p.LABEL_ID, p.LABEL_EN, p.LABEL_FR, p.LABEL_PR, p.LABEL_SP,  "
            + "     rc.REALM_COUNTRY_ID, r.REALM_ID, r.REALM_CODE, r.MIN_MOS_MIN_GAURDRAIL, r.MIN_MOS_MAX_GAURDRAIL, r.MAX_MOS_MAX_GAURDRAIL,  "
            + "     r.LABEL_ID `REALM_LABEL_ID`, r.LABEL_EN `REALM_LABEL_EN`, r.LABEL_FR `REALM_LABEL_FR`, r.LABEL_PR `REALM_LABEL_PR`, r.LABEL_SP `REALM_LABEL_SP`,  "
            + "     c.COUNTRY_ID, c.COUNTRY_CODE, c.COUNTRY_CODE2,   "
            + "     c.LABEL_ID `COUNTRY_LABEL_ID`, c.LABEL_EN `COUNTRY_LABEL_EN`, c.LABEL_FR `COUNTRY_LABEL_FR`, c.LABEL_PR `COUNTRY_LABEL_PR`, c.LABEL_SP `COUNTRY_LABEL_SP`,  "
            + "     cu.CURRENCY_ID, cu.CURRENCY_CODE, cu.CONVERSION_RATE_TO_USD,   "
            + "     cu.LABEL_ID `CURRENCY_LABEL_ID`, cu.LABEL_EN `CURRENCY_LABEL_EN`, cu.LABEL_FR `CURRENCY_LABEL_FR`, cu.LABEL_PR `CURRENCY_LABEL_PR`, cu.LABEL_SP `CURRENCY_LABEL_SP`,  "
            + "     o.ORGANISATION_ID, o.ORGANISATION_CODE,  "
            + "     o.LABEL_ID `ORGANISATION_LABEL_ID`, o.LABEL_EN `ORGANISATION_LABEL_EN`, o.LABEL_FR `ORGANISATION_LABEL_FR`, o.LABEL_PR `ORGANISATION_LABEL_PR`, o.LABEL_SP `ORGANISATION_LABEL_SP`,  "
            + "     ha.HEALTH_AREA_ID, ha.HEALTH_AREA_CODE,  "
            + "     ha.LABEL_ID `HEALTH_AREA_LABEL_ID`, ha.LABEL_EN `HEALTH_AREA_LABEL_EN`, ha.LABEL_FR `HEALTH_AREA_LABEL_FR`, ha.LABEL_PR `HEALTH_AREA_LABEL_PR`, ha.LABEL_SP `HEALTH_AREA_LABEL_SP`,  "
            + "     re.REGION_ID, re.CAPACITY_CBM, re.GLN,  "
            + "     re.LABEL_ID `REGION_LABEL_ID`, re.LABEL_EN `REGION_LABEL_EN`, re.LABEL_FR `REGION_LABEL_FR`, re.LABEL_PR `REGION_LABEL_PR`, re.LABEL_SP `REGION_LABEL_SP`,  "
            + "     u.UNIT_ID, u.UNIT_CODE, u.LABEL_ID `UNIT_LABEL_ID`, u.LABEL_EN `UNIT_LABEL_EN`, u.LABEL_FR `UNIT_LABEL_FR`, u.LABEL_PR `UNIT_LABEL_PR`, u.LABEL_SP `UNIT_LABEL_SP`,  "
            + "     pv.VERSION_ID `VT_VERSION_ID`, pv.NOTES `VT_VERSION_NOTES`, pv.CREATED_DATE `VT_CREATED_DATE`, pvcb.USER_ID `VT_CB_USER_ID`, pvcb.USERNAME `VT_CB_USERNAME`, pv.LAST_MODIFIED_DATE `VT_LAST_MODIFIED_DATE`, pvlmb.USER_ID `VT_LMB_USER_ID`, pvlmb.USERNAME `VT_LMB_USERNAME`,  "
            + "     pvt.VERSION_TYPE_ID `VT_VERSION_TYPE_ID`, pvt.LABEL_ID `VT_VERSION_TYPE_LABEL_ID`, pvt.LABEL_EN `VT_VERSION_TYPE_LABEL_EN`, pvt.LABEL_FR `VT_VERSION_TYPE_LABEL_FR`, pvt.LABEL_SP `VT_VERSION_TYPE_LABEL_SP`, pvt.LABEL_PR `VT_VERSION_TYPE_LABEL_PR`,  "
            + "     pvs.VERSION_STATUS_ID `VT_VERSION_STATUS_ID`, pvs.LABEL_ID `VT_VERSION_STATUS_LABEL_ID`, pvs.LABEL_EN `VT_VERSION_STATUS_LABEL_EN`, pvs.LABEL_FR `VT_VERSION_STATUS_LABEL_FR`, pvs.LABEL_SP `VT_VERSION_STATUS_LABEL_SP`, pvs.LABEL_PR `VT_VERSION_STATUS_LABEL_PR`,  "
            + "     p.ACTIVE, cb.USER_ID `CB_USER_ID`, cb.USERNAME `CB_USERNAME`, p.CREATED_DATE, lmb.USER_ID `LMB_USER_ID`, lmb.USERNAME `LMB_USERNAME`, p.LAST_MODIFIED_DATE  "
            + " FROM vw_program p   "
            + " LEFT JOIN rm_realm_country rc ON p.REALM_COUNTRY_ID=rc.REALM_COUNTRY_ID  "
            + " LEFT JOIN vw_realm r ON rc.REALM_ID=r.REALM_ID  "
            + " LEFT JOIN vw_country c ON rc.COUNTRY_ID=c.COUNTRY_ID  "
            + " LEFT JOIN vw_currency cu ON rc.DEFAULT_CURRENCY_ID=cu.CURRENCY_ID  "
            + " LEFT JOIN vw_organisation o ON p.ORGANISATION_ID=o.ORGANISATION_ID  "
            + " LEFT JOIN vw_health_area ha ON p.HEALTH_AREA_ID=ha.HEALTH_AREA_ID  "
            + " LEFT JOIN us_user pm ON p.PROGRAM_MANAGER_USER_ID=pm.USER_ID  "
            + " LEFT JOIN rm_program_region pr ON p.PROGRAM_ID=pr.PROGRAM_ID  "
            + " LEFT JOIN vw_region re ON pr.REGION_ID=re.REGION_ID  "
            + " LEFT JOIN vw_unit u ON rc.PALLET_UNIT_ID=u.UNIT_ID  "
            + " LEFT JOIN us_user cb ON p.CREATED_BY=cb.USER_ID  "
            + " LEFT JOIN us_user lmb ON p.LAST_MODIFIED_BY=lmb.USER_ID  "
            + " LEFT JOIN rm_program_version cpv ON p.PROGRAM_ID=cpv.PROGRAM_ID AND p.CURRENT_VERSION_ID=cpv.VERSION_ID  "
            + " LEFT JOIN vw_version_type vt ON cpv.VERSION_TYPE_ID=vt.VERSION_TYPE_ID  "
            + " LEFT JOIN vw_version_status vs ON cpv.VERSION_STATUS_ID=vs.VERSION_STATUS_ID  "
            + " LEFT JOIN us_user cpvcb ON cpv.CREATED_BY=cpvcb.USER_ID  "
            + " LEFT JOIN us_user cpvlmb ON cpv.LAST_MODIFIED_BY=cpvlmb.USER_ID  "
            + " LEFT JOIN rm_program_version pv ON p.PROGRAM_ID=pv.PROGRAM_ID  "
            + " LEFT JOIN vw_version_type pvt ON pv.VERSION_TYPE_ID=pvt.VERSION_TYPE_ID  "
            + " LEFT JOIN vw_version_status pvs ON pv.VERSION_STATUS_ID=pvs.VERSION_STATUS_ID  "
            + " LEFT JOIN us_user pvcb ON pv.CREATED_BY=pvcb.USER_ID  "
            + " LEFT JOIN us_user pvlmb ON pv.LAST_MODIFIED_BY=pvlmb.USER_ID  "
            + " WHERE TRUE ";
    private final String sqlOrderBy = " ORDER BY p.PROGRAM_CODE, pv.VERSION_ID";

    public String sqlListStringForProgramPlanningUnit = "SELECT ppu.PROGRAM_PLANNING_UNIT_ID,   "
            + "    pg.PROGRAM_ID, pg.LABEL_ID `PROGRAM_LABEL_ID`, pg.LABEL_EN `PROGRAM_LABEL_EN`, pg.LABEL_FR `PROGRAM_LABEL_FR`, pg.LABEL_PR `PROGRAM_LABEL_PR`, pg.LABEL_SP `PROGRAM_LABEL_SP`,  "
            + "    pu.PLANNING_UNIT_ID, pu.LABEL_ID `PLANNING_UNIT_LABEL_ID`, pu.LABEL_EN `PLANNING_UNIT_LABEL_EN`, pu.LABEL_FR `PLANNING_UNIT_LABEL_FR`, pu.LABEL_PR `PLANNING_UNIT_LABEL_PR`, pu.LABEL_SP `PLANNING_UNIT_LABEL_SP`,  "
            + "    pc.PRODUCT_CATEGORY_ID, pc.LABEL_ID `PRODUCT_CATEGORY_LABEL_ID`, pc.LABEL_EN `PRODUCT_CATEGORY_LABEL_EN`, pc.LABEL_FR `PRODUCT_CATEGORY_LABEL_FR`, pc.LABEL_PR `PRODUCT_CATEGORY_LABEL_PR`, pc.LABEL_SP `PRODUCT_CATEGORY_LABEL_SP`,  "
            + "    ppu.REORDER_FREQUENCY_IN_MONTHS, ppu.MIN_MONTHS_OF_STOCK, ppu.LOCAL_PROCUREMENT_LEAD_TIME, ppu.SHELF_LIFE, ppu.CATALOG_PRICE, ppu.MONTHS_IN_PAST_FOR_AMC, ppu.MONTHS_IN_FUTURE_FOR_AMC,  "
            + "    ppu.ACTIVE, cb.USER_ID `CB_USER_ID`, cb.USERNAME `CB_USERNAME`, ppu.CREATED_DATE, lmb.USER_ID `LMB_USER_ID`, lmb.USERNAME `LMB_USERNAME`, ppu.LAST_MODIFIED_DATE  "
            + " FROM  rm_program_planning_unit ppu   "
            + " LEFT JOIN vw_program pg ON pg.PROGRAM_ID=ppu.PROGRAM_ID  "
            + " LEFT JOIN rm_realm_country rc ON pg.REALM_COUNTRY_ID=rc.REALM_COUNTRY_ID  "
            + " LEFT JOIN vw_planning_unit pu ON ppu.PLANNING_UNIT_ID=pu.PLANNING_UNIT_ID  "
            + " LEFT JOIN rm_forecasting_unit fu ON pu.FORECASTING_UNIT_ID=fu.FORECASTING_UNIT_ID  "
            + " LEFT JOIN vw_product_category pc ON fu.PRODUCT_CATEGORY_ID=pc.PRODUCT_CATEGORY_ID  "
            + " LEFT JOIN us_user cb ON ppu.CREATED_BY=cb.USER_ID  "
            + " LEFT JOIN us_user lmb ON ppu.LAST_MODIFIED_BY=lmb.USER_ID  "
            + " WHERE TRUE ";

    @Override
    @Transactional
    public int addProgram(Program p, int realmId, CustomUserDetails curUser) {
        Map<String, Object> params = new HashMap<>();
        Date curDate = DateUtils.getCurrentDateObject(DateUtils.EST);
        int labelId = this.labelDao.addLabel(p.getLabel(), LabelConstants.RM_PROGRAM, curUser.getUserId());
        SimpleJdbcInsert si = new SimpleJdbcInsert(dataSource).withTableName("rm_program").usingGeneratedKeyColumns("PROGRAM_ID");
        params.put("REALM_ID", realmId);
        params.put("REALM_COUNTRY_ID", p.getRealmCountry().getRealmCountryId());
        params.put("ORGANISATION_ID", p.getOrganisation().getId());
        params.put("HEALTH_AREA_ID", p.getHealthArea().getId());
        params.put("PROGRAM_CODE", p.getProgramCode());
        params.put("LABEL_ID", labelId);
        params.put("PROGRAM_MANAGER_USER_ID", p.getProgramManager().getUserId());
        params.put("PROGRAM_NOTES", p.getProgramNotes());
        params.put("AIR_FREIGHT_PERC", p.getAirFreightPerc());
        params.put("SEA_FREIGHT_PERC", p.getSeaFreightPerc());
        params.put("PLANNED_TO_SUBMITTED_LEAD_TIME", p.getPlannedToSubmittedLeadTime());
        params.put("SUBMITTED_TO_APPROVED_LEAD_TIME", p.getSubmittedToApprovedLeadTime());
        params.put("APPROVED_TO_SHIPPED_LEAD_TIME", p.getApprovedToShippedLeadTime());
        params.put("SHIPPED_TO_ARRIVED_BY_SEA_LEAD_TIME", p.getShippedToArrivedBySeaLeadTime());
        params.put("SHIPPED_TO_ARRIVED_BY_AIR_LEAD_TIME", p.getShippedToArrivedByAirLeadTime());
        params.put("ARRIVED_TO_DELIVERED_LEAD_TIME", p.getArrivedToDeliveredLeadTime());
        params.put("CURRENT_VERSION_ID", null);
        params.put("ACTIVE", true);
        params.put("CREATED_BY", curUser.getUserId());
        params.put("CREATED_DATE", curDate);
        params.put("LAST_MODIFIED_BY", curUser.getUserId());
        params.put("LAST_MODIFIED_DATE", curDate);
        int programId = si.executeAndReturnKey(params).intValue();
        si = new SimpleJdbcInsert(this.dataSource).withTableName("rm_program_region");
        SqlParameterSource[] paramList = new SqlParameterSource[p.getRegionArray().length];
        int i = 0;
        for (String rId : p.getRegionArray()) {
            params = new HashMap<>();
            params.put("REGION_ID", rId);
            params.put("PROGRAM_ID", programId);
            params.put("CREATED_BY", curUser.getUserId());
            params.put("CREATED_DATE", curDate);
            params.put("LAST_MODIFIED_BY", curUser.getUserId());
            params.put("LAST_MODIFIED_DATE", curDate);
            params.put("ACTIVE", true);
            paramList[i] = new MapSqlParameterSource(params);
            i++;
        }
        si.executeBatch(paramList);
        params.clear();
        params.put("curUser", curUser.getUserId());
        params.put("curDate", curDate);
        params.put("programId", programId);
        params.put("versionTypeId", 1);
        params.put("versionStatusId", 1);
        params.put("notes", "");
        Version version = new Version();
//        int versionId = this.namedParameterJdbcTemplate.queryForObject("CALL getVersionId(:programId,:versionTypeId,:versionStatusId,:notes,:curUser, :curDate)", params, new VersionRowMapper());
        version = this.namedParameterJdbcTemplate.queryForObject("CALL getVersionId(:programId,:versionTypeId,:versionStatusId,:notes,:curUser, :curDate)", params, new VersionRowMapper());
        params.put("versionId", version.getVersionId());
        this.namedParameterJdbcTemplate.update("UPDATE rm_program SET CURRENT_VERSION_ID=:versionId WHERE PROGRAM_ID=:programId", params);
        return programId;
    }

    @Override
    public int updateProgram(Program p, CustomUserDetails curUser) {
        Date curDate = DateUtils.getCurrentDateObject(DateUtils.EST);
        Map<String, Object> params = new HashMap<>();
        params.put("programId", p.getProgramId());
        params.put("labelEn", p.getLabel().getLabel_en());
        params.put("programManagerUserId", p.getProgramManager().getUserId());
        params.put("programNotes", p.getProgramNotes());
        params.put("airFreightPerc", p.getAirFreightPerc());
        params.put("seaFreightPerc", p.getSeaFreightPerc());
        params.put("plannedToSubmittedLeadTime", p.getPlannedToSubmittedLeadTime());
        params.put("submittedToApprovedLeadTime", p.getSubmittedToApprovedLeadTime());
        params.put("approvedToShippedLeadTime", p.getApprovedToShippedLeadTime());
        params.put("shippedToArrivedBySeaLeadTime", p.getShippedToArrivedBySeaLeadTime());
        params.put("shippedToArrivedByAirLeadTime", p.getShippedToArrivedByAirLeadTime());
        params.put("arrivedToDeliveredLeadTime", p.getArrivedToDeliveredLeadTime());
        params.put("active", p.isActive());
        params.put("curUser", curUser.getUserId());
        params.put("curDate", curDate);
        String sqlString = "UPDATE rm_program p "
                + "LEFT JOIN ap_label pl ON p.LABEL_ID=pl.LABEL_ID "
                + "SET "
                + "p.PROGRAM_MANAGER_USER_ID=:programManagerUserId, "
                + "p.PROGRAM_NOTES=:programNotes, "
                + "p.AIR_FREIGHT_PERC=:airFreightPerc, "
                + "p.SEA_FREIGHT_PERC=:seaFreightPerc, "
                + "p.PLANNED_TO_SUBMITTED_LEAD_TIME=:plannedToSubmittedLeadTime, "
                + "p.SUBMITTED_TO_APPROVED_LEAD_TIME=:submittedToApprovedLeadTime, "
                + "p.APPROVED_TO_SHIPPED_LEAD_TIME=:approvedToShippedLeadTime, "
                + "p.SHIPPED_TO_ARRIVED_BY_SEA_LEAD_TIME=:shippedToArrivedBySeaLeadTime, "
                + "p.SHIPPED_TO_ARRIVED_BY_AIR_LEAD_TIME=:shippedToArrivedByAirLeadTime, "
                + "p.ARRIVED_TO_DELIVERED_LEAD_TIME=:arrivedToDeliveredLeadTime, "
                + "p.ACTIVE=:active,"
                + "p.LAST_MODIFIED_BY=:curUser, "
                + "p.LAST_MODIFIED_DATE=:curDate, "
                + "pl.LABEL_EN=:labelEn, "
                + "pl.LAST_MODIFIED_BY=:curUser, "
                + "pl.LAST_MODIFIED_DATE=:curDate "
                + "WHERE p.PROGRAM_ID=:programId";
        int rows = this.namedParameterJdbcTemplate.update(sqlString, params);
        params.clear();
        params.put("programId", p.getProgramId());
        this.namedParameterJdbcTemplate.update("DELETE FROM rm_program_region WHERE PROGRAM_ID=:programId", params);
        SimpleJdbcInsert si = new SimpleJdbcInsert(this.dataSource).withTableName("rm_program_region");
        SqlParameterSource[] paramList = new SqlParameterSource[p.getRegionArray().length];
        int i = 0;
        for (String regionId : p.getRegionArray()) {
            params = new HashMap<>();
            params.put("PROGRAM_ID", p.getProgramId());
            params.put("REGION_ID", regionId);
            params.put("CREATED_BY", curUser.getUserId());
            params.put("CREATED_DATE", curDate);
            params.put("LAST_MODIFIED_BY", curUser.getUserId());
            params.put("LAST_MODIFIED_DATE", curDate);
            params.put("ACTIVE", true);
            paramList[i] = new MapSqlParameterSource(params);
            i++;
        }
        si.executeBatch(paramList);
        return rows;
    }

    @Override
    public List<ProgramDTO> getProgramListForDropdown(CustomUserDetails curUser) {
        Map<String, Object> params = new HashMap<>();
        String sql = "SELECT r.`PROGRAM_ID`,label.`LABEL_ID`,label.`LABEL_EN`,label.`LABEL_FR`,label.`LABEL_PR`,label.`LABEL_SP` "
                + "FROM rm_program r  "
                + "LEFT JOIN ap_label label ON label.`LABEL_ID`=r.`LABEL_ID` WHERE 1 ";
        int count = 1;
        for (UserAcl acl : curUser.getAclList()) {
            sql += "AND ("
                    + "(r.PROGRAM_ID=:programId" + count + " OR :programId" + count + "=-1)) ";
            params.put("programId" + count, acl.getProgramId());
            count++;
        }
        return this.namedParameterJdbcTemplate.query(sql, params, new ProgramDTORowMapper());
    }

    @Override
    public List<Program> getProgramListForProgramIds(String[] programIds, CustomUserDetails curUser) {
        StringBuilder sqlStringBuilder = new StringBuilder(this.sqlListString);
        StringBuilder paramBuilder = new StringBuilder();
        for (String pId : programIds) {
            paramBuilder.append("'").append(pId).append("',");
        }
        if (programIds.length > 0) {
            paramBuilder.setLength(paramBuilder.length() - 1);
        }
        sqlStringBuilder.append(" AND p.PROGRAM_ID IN (").append(paramBuilder).append(") ");
        Map<String, Object> params = new HashMap<>();
        this.aclService.addFullAclForProgram(sqlStringBuilder, params, "p", curUser);
        return this.namedParameterJdbcTemplate.query(sqlStringBuilder.toString(), params, new ProgramListResultSetExtractor());
    }

    @Override
    public List<Program> getProgramList(CustomUserDetails curUser, boolean active) {
        StringBuilder sqlStringBuilder = new StringBuilder(this.sqlListString);
        Map<String, Object> params = new HashMap<>();
        this.aclService.addUserAclForRealm(sqlStringBuilder, params, "r", curUser);
        this.aclService.addFullAclForProgram(sqlStringBuilder, params, "p", curUser);
        if (active) {
            sqlStringBuilder.append(" AND p.ACTIVE ").append(this.sqlOrderBy);
        }
        return this.namedParameterJdbcTemplate.query(sqlStringBuilder.toString(), params, new ProgramListResultSetExtractor());
    }

    @Override
    public List<Program> getProgramList(int realmId, CustomUserDetails curUser) {
        StringBuilder sqlStringBuilder = new StringBuilder(this.sqlListString);
        Map<String, Object> params = new HashMap<>();
        this.aclService.addUserAclForRealm(sqlStringBuilder, params, "r", curUser);
        this.aclService.addUserAclForRealm(sqlStringBuilder, params, "r", realmId, curUser);
        this.aclService.addFullAclForProgram(sqlStringBuilder, params, "p", curUser);
        sqlStringBuilder.append(this.sqlOrderBy);
        return this.namedParameterJdbcTemplate.query(sqlStringBuilder.toString(), params, new ProgramListResultSetExtractor());
    }

    @Override
    public Program getProgramById(int programId, CustomUserDetails curUser) {
        StringBuilder sqlStringBuilder = new StringBuilder(this.sqlListString).append(" AND p.PROGRAM_ID=:programId");
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        sqlStringBuilder.append(this.sqlOrderBy);
        Program p = this.namedParameterJdbcTemplate.query(sqlStringBuilder.toString(), params, new ProgramResultSetExtractor());
        if (p == null) {
            throw new EmptyResultDataAccessException(1);
        }
        if (this.aclService.checkProgramAccessForUser(curUser, p.getRealmCountry().getRealm().getRealmId(), p.getProgramId(), p.getHealthArea().getId(), p.getOrganisation().getId())) {
            return p;
        } else {
            return null;
        }
    }

    @Override
    public List<ProgramPlanningUnit> getPlanningUnitListForProgramId(int programId, boolean active, CustomUserDetails curUser) {
        StringBuilder sqlStringBuilder = new StringBuilder(this.sqlListStringForProgramPlanningUnit).append(" AND pg.PROGRAM_ID=:programId");
        if (active) {
            sqlStringBuilder = sqlStringBuilder.append(" AND ppu.ACTIVE ");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        return this.namedParameterJdbcTemplate.query(sqlStringBuilder.toString(), params, new ProgramPlanningUnitRowMapper());
    }

    @Override
    public List<SimpleObject> getPlanningUnitListForProgramIds(String programIds, CustomUserDetails curUser) {
        String sql = "SELECT pu.PLANNING_UNIT_ID `ID`, pu.LABEL_ID, pu.LABEL_EN, pu.LABEL_FR, pu.LABEL_SP, pu.LABEL_PR FROM rm_program p LEFT JOIN rm_program_planning_unit ppu ON p.PROGRAM_ID=ppu.PROGRAM_ID LEFT JOIN vw_planning_unit pu ON ppu.PLANNING_UNIT_ID=pu.PLANNING_UNIT_ID WHERE p.PROGRAM_ID IN (" + programIds + ") AND ppu.PROGRAM_ID IS NOT NULL";
        Map<String, Object> params = new HashMap<>();
        params.put("programIds", programIds);
        return this.namedParameterJdbcTemplate.query(sql, params, new SimpleObjectRowMapper());
    }

    @Override
    public int saveProgramPlanningUnit(ProgramPlanningUnit[] programPlanningUnits, CustomUserDetails curUser) {
        SimpleJdbcInsert si = new SimpleJdbcInsert(dataSource).withTableName("rm_program_planning_unit");
        List<SqlParameterSource> insertList = new ArrayList<>();
        List<SqlParameterSource> updateList = new ArrayList<>();
        int rowsEffected = 0;
        Date curDate = DateUtils.getCurrentDateObject(DateUtils.EST);
        Map<String, Object> params;
        for (ProgramPlanningUnit ppu : programPlanningUnits) {
            if (ppu.getProgramPlanningUnitId() == 0) {
                // Insert
                params = new HashMap<>();
                params.put("PLANNING_UNIT_ID", ppu.getPlanningUnit().getId());
                params.put("PROGRAM_ID", ppu.getProgram().getId());
                params.put("REORDER_FREQUENCY_IN_MONTHS", ppu.getReorderFrequencyInMonths());
                params.put("MIN_MONTHS_OF_STOCK", ppu.getMinMonthsOfStock());
                params.put("LOCAL_PROCUREMENT_LEAD_TIME", ppu.getLocalProcurementLeadTime());
                params.put("SHELF_LIFE", ppu.getShelfLife());
                params.put("CATALOG_PRICE", ppu.getCatalogPrice());
                params.put("MONTHS_IN_PAST_FOR_AMC", ppu.getMonthsInPastForAmc());
                params.put("MONTHS_IN_FUTURE_FOR_AMC", ppu.getMonthsInFutureForAmc());
                params.put("CREATED_DATE", curDate);
                params.put("CREATED_BY", curUser.getUserId());
                params.put("LAST_MODIFIED_DATE", curDate);
                params.put("LAST_MODIFIED_BY", curUser.getUserId());
                params.put("ACTIVE", true);
                insertList.add(new MapSqlParameterSource(params));
            } else {
                // Update
                params = new HashMap<>();
                params.put("programPlanningUnitId", ppu.getProgramPlanningUnitId());
                params.put("reorderFrequencyInMonths", ppu.getReorderFrequencyInMonths());
                params.put("minMonthsOfStock", ppu.getMinMonthsOfStock());
                params.put("localProcurementLeadTime", ppu.getLocalProcurementLeadTime());
                params.put("shelfLife", ppu.getShelfLife());
                params.put("catalogPrice", ppu.getCatalogPrice());
                params.put("monthsInPastForAmc", ppu.getMonthsInPastForAmc());
                params.put("monthsInFutureForAmc", ppu.getMonthsInFutureForAmc());
                params.put("curDate", curDate);
                params.put("curUser", curUser.getUserId());
                params.put("active", ppu.isActive());
                updateList.add(new MapSqlParameterSource(params));
            }
        }
        if (insertList.size() > 0) {
            SqlParameterSource[] insertParams = new SqlParameterSource[insertList.size()];
            rowsEffected += si.executeBatch(insertList.toArray(insertParams)).length;
        }
        if (updateList.size() > 0) {
            SqlParameterSource[] updateParams = new SqlParameterSource[updateList.size()];
            String sqlString = "UPDATE rm_program_planning_unit ppu SET ppu.MIN_MONTHS_OF_STOCK=:minMonthsOfStock,ppu.REORDER_FREQUENCY_IN_MONTHS=:reorderFrequencyInMonths, ppu.LOCAL_PROCUREMENT_LEAD_TIME=:localProcurementLeadTime, ppu.SHELF_LIFE=:shelfLife, ppu.CATALOG_PRICE=:catalogPrice, ppu.MONTHS_IN_PAST_FOR_AMC=:monthsInPastForAmc, ppu.MONTHS_IN_FUTURE_FOR_AMC=:monthsInFutureForAmc, ppu.ACTIVE=:active, ppu.LAST_MODIFIED_DATE=:curDate, ppu.LAST_MODIFIED_BY=:curUser WHERE ppu.PROGRAM_PLANNING_UNIT_ID=:programPlanningUnitId";
            rowsEffected += this.namedParameterJdbcTemplate.batchUpdate(sqlString, updateList.toArray(updateParams)).length;
        }
        return rowsEffected;
    }

    @Override
    public List<Program> getProgramListForSync(String lastSyncDate, CustomUserDetails curUser) {
        StringBuilder sqlStringBuilder = new StringBuilder(this.sqlListString).append(" AND p.LAST_MODIFIED_DATE>:lastSyncDate ");
        Map<String, Object> params = new HashMap<>();
        params.put("lastSyncDate", lastSyncDate);
        this.aclService.addUserAclForRealm(sqlStringBuilder, params, "r", curUser);
        this.aclService.addFullAclForProgram(sqlStringBuilder, params, "p", curUser);
        sqlStringBuilder.append(this.sqlOrderBy);
        return this.namedParameterJdbcTemplate.query(sqlStringBuilder.toString(), params, new ProgramListResultSetExtractor());
    }

    @Override
    public List<ProgramPlanningUnit> getProgramPlanningUnitListForSync(String lastSyncDate, CustomUserDetails curUser) {
        StringBuilder sqlStringBuilder = new StringBuilder(this.sqlListStringForProgramPlanningUnit).append(" AND ppu.LAST_MODIFIED_DATE>:lastSyncDate ");
        Map<String, Object> params = new HashMap<>();
        params.put("lastSyncDate", lastSyncDate);
        this.aclService.addUserAclForRealm(sqlStringBuilder, params, "rc", curUser);
        this.aclService.addFullAclForProgram(sqlStringBuilder, params, "pg", curUser);
        return this.namedParameterJdbcTemplate.query(sqlStringBuilder.toString(), params, new ProgramPlanningUnitRowMapper());
    }

    @Override
    public List<ProgramPlanningUnit> getPlanningUnitListForProgramAndCategoryId(int programId, int productCategoryId, boolean active, CustomUserDetails curUser) {
        String sqlListStr = this.sqlListStringForProgramPlanningUnit;
        StringBuilder sqlStringBuilder = new StringBuilder(sqlListStr).append(" AND pg.PROGRAM_ID=:programId");
        if (active) {
            sqlStringBuilder = sqlStringBuilder.append(" AND ppu.ACTIVE ");
        }
        Map<String, Object> params = new HashMap<>();
        if (productCategoryId > 0) {
            sqlStringBuilder = sqlStringBuilder.append(" AND fu.PRODUCT_CATEGORY_ID=:productCategoryId ");
            params.put("productCategoryId", productCategoryId);
        }

        params.put("programId", programId);
        return this.namedParameterJdbcTemplate.query(sqlStringBuilder.toString(), params, new ProgramPlanningUnitRowMapper());
    }

    @Override
    public Program getProgramList(int realmId, int programId, int versionId) {
        StringBuilder sqlStringBuilder = new StringBuilder(this.sqlListString);
        Map<String, Object> params = new HashMap<>();
        params.put("realmId", realmId);
        params.put("programId", programId);
        params.put("versionId", versionId);
        sqlStringBuilder.append(" AND rc.`REALM_ID`=:realmId AND p.`PROGRAM_ID`=:programId AND cpv.`VERSION_ID`=:versionId GROUP BY p.`PROGRAM_ID`");
        sqlStringBuilder.append(this.sqlOrderBy);
        return this.namedParameterJdbcTemplate.query(sqlStringBuilder.toString(), params, new ProgramResultSetExtractor());
    }

    @Override
    public List<ManualTaggingDTO> getShipmentListForManualTagging(int programId, int planningUnitId) {
        String sql = "CALL getShipmentListForManualLinking(:programId, :planningUnitId, -1)";
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("planningUnitId", planningUnitId);
        List<ManualTaggingDTO> list = this.namedParameterJdbcTemplate.query(sql, params, new ManualTaggingDTORowMapper());
        return list;
    }

    @Override
    public ManualTaggingOrderDTO getOrderDetailsByOrderNoAndPrimeLineNo(int programId, int planningUnitId, String orderNo, int primeLineNo) {
        String reason = "";
        String sql = "SELECT COUNT(*) FROM rm_manual_tagging m WHERE m.`ORDER_NO`=? AND m.`PRIME_LINE_NO`=? AND m.`ACTIVE`=1;";
        int count = this.jdbcTemplate.queryForObject(sql, Integer.class, orderNo, primeLineNo);
        if (count > 0) {
            reason = "static.mt.orderNoAlreadyTagged";
        } else {
            sql = "SELECT  IF(o.`PROGRAM_ID`=?,IF(sm.`SHIPMENT_STATUS_ID`!=7,IF(pu.`PROCUREMENT_AGENT_PLANNING_UNIT_ID` IS NOT NULL,\"\",\"static.mt.planningUnitNotMatch\"),\"static.mt.shipentDelivered\"),\"static.mt.programNotMatch\") AS REASON "
                    + " FROM rm_erp_order o "
                    + " LEFT JOIN (SELECT rc.REALM_COUNTRY_ID, cl.LABEL_EN, c.COUNTRY_CODE "
                    + " FROM rm_realm_country rc "
                    + " LEFT JOIN ap_country c ON rc.COUNTRY_ID=c.COUNTRY_ID "
                    + " LEFT JOIN ap_label cl ON c.LABEL_ID=cl.LABEL_ID) c1 ON c1.LABEL_EN=o.RECPIENT_COUNTRY "
                    + " LEFT JOIN rm_program p ON p.`REALM_COUNTRY_ID`=c1.REALM_COUNTRY_ID AND p.`PROGRAM_ID`=? "
                    + " LEFT JOIN rm_shipment_status_mapping sm ON sm.`EXTERNAL_STATUS_STAGE`=o.`STATUS` "
                    + " LEFT JOIN rm_procurement_agent_planning_unit pu ON LEFT(pu.`SKU_CODE`,12)=o.`PLANNING_UNIT_SKU_CODE` AND pu.`PROCUREMENT_AGENT_ID`=1 AND pu.`PLANNING_UNIT_ID`=? "
                    + " WHERE o.ORDER_NO=? AND o.PRIME_LINE_NO=? "
                    + " GROUP BY o.`ERP_ORDER_ID`;";
            reason = this.jdbcTemplate.queryForObject(sql, String.class, programId, programId, planningUnitId, orderNo, primeLineNo);
        }
        sql = "SELECT e.*,l.`LABEL_ID`,IF(l.`LABEL_EN` IS NOT NULL,l.`LABEL_EN`,'') AS LABEL_EN,l.`LABEL_FR`,l.`LABEL_PR`,l.`LABEL_SP` FROM rm_erp_order e "
                + " LEFT JOIN rm_procurement_agent_planning_unit pu ON LEFT(pu.`SKU_CODE`,12)=e.`PLANNING_UNIT_SKU_CODE` "
                + " AND pu.`PROCUREMENT_AGENT_ID`=1 "
                + " LEFT JOIN rm_planning_unit p ON p.`PLANNING_UNIT_ID`=pu.`PLANNING_UNIT_ID` "
                + " LEFT JOIN ap_label l ON l.`LABEL_ID`=p.`LABEL_ID` "
                + " WHERE e.ORDER_NO=? AND e.PRIME_LINE_NO=?; ";
        ManualTaggingOrderDTO erpOrderDTO = this.jdbcTemplate.queryForObject(sql, new ManualTaggingOrderDTORowMapper(), orderNo, primeLineNo);
        erpOrderDTO.setReason(reason);
        return erpOrderDTO;
    }

    @Override
    public int linkShipmentWithARTMIS(String orderNo, int primeLineNo, int shipmentId, CustomUserDetails curUser) {
        Date curDate = DateUtils.getCurrentDateObject(DateUtils.EST);
        String sql = "SELECT COUNT(*) FROM rm_manual_tagging m WHERE m.`ORDER_NO`=? AND m.`PRIME_LINE_NO`=? AND m.`ACTIVE`=1;";
        int count = this.jdbcTemplate.queryForObject(sql, Integer.class, orderNo, primeLineNo);
        if (count == 0) {
            sql = "INSERT INTO rm_manual_tagging VALUES (NULL,?,?,?,?,?,?,?,1,'');";
            return this.jdbcTemplate.update(sql, orderNo, primeLineNo, shipmentId, curDate, curUser.getUserId(), curDate, curUser.getUserId());
        } else {
            return -1;
        }
    }

    @Override
    public List<ManualTaggingDTO> getShipmentListForDelinking(int programId, int planningUnitId) {
        String sql = "CALL getShipmentListForDeLinking(:programId, :planningUnitId, -1)";
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("planningUnitId", planningUnitId);
        List<ManualTaggingDTO> list = this.namedParameterJdbcTemplate.query(sql, params, new ManualTaggingDTORowMapper());
        return list;
    }

    @Override
    public void delinkShipment(ManualTaggingOrderDTO erpOrderDTO, CustomUserDetails curUser) {
        Date curDate = DateUtils.getCurrentDateObject(DateUtils.EST);
        String sql = "SELECT st.`ERP_FLAG` FROM rm_shipment_trans st "
                + "WHERE st.`SHIPMENT_ID`=? AND st.`ACTIVE` ORDER BY st.`SHIPMENT_TRANS_ID` DESC LIMIT 1;";
        Integer erpFlag = this.jdbcTemplate.queryForObject(sql, Integer.class, erpOrderDTO.getShipmentId());
        if (erpFlag == 1) {
            sql = "SELECT s.`PARENT_SHIPMENT_ID` FROM rm_shipment s WHERE s.`SHIPMENT_ID`=?;";
            int parentShipmentId = this.jdbcTemplate.queryForObject(sql, Integer.class, erpOrderDTO.getShipmentId());
//            String sql1 = "UPDATE rm_shipment s SET s.`MAX_VERSION_ID`=s.`MAX_VERSION_ID`+1 WHERE s.`SHIPMENT_ID`=?;";
//            this.jdbcTemplate.update(sql1, parentShipmentId);
            sql = "INSERT INTO rm_shipment_trans "
                    + "SELECT NULL,?,st.`PLANNING_UNIT_ID`,st.`PROCUREMENT_AGENT_ID` ,st.`FUNDING_SOURCE_ID`, "
                    + "st.`BUDGET_ID`,st.`EXPECTED_DELIVERY_DATE`,st.`PROCUREMENT_UNIT_ID`,st.`SUPPLIER_ID`, "
                    + "st.`SHIPMENT_QTY`,st.`RATE`,st.`PRODUCT_COST`,st.`SHIPMENT_MODE`,st.`FREIGHT_COST`, "
                    + "st.`PLANNED_DATE`,st.`SUBMITTED_DATE`,st.`APPROVED_DATE`,st.`SHIPPED_DATE`, "
                    + "st.`ARRIVED_DATE`,st.`RECEIVED_DATE`,st.`SHIPMENT_STATUS_ID`,st.`NOTES`, "
                    + "0,NULL,NULL,st.`ACCOUNT_FLAG`,st.`EMERGENCY_ORDER`,st.`LOCAL_PROCUREMENT`,?,st.`DATA_SOURCE_ID`,?,s.`MAX_VERSION_ID`,1 "
                    + "FROM rm_shipment_trans st "
                    + "LEFT JOIN rm_shipment s ON s.`SHIPMENT_ID`=st.`SHIPMENT_ID` "
                    + "WHERE st.`SHIPMENT_ID`=? "
                    + "ORDER BY st.`SHIPMENT_TRANS_ID` DESC LIMIT 1;";
            this.jdbcTemplate.update(sql, parentShipmentId, curUser.getUserId(), curDate, parentShipmentId);

            sql = "SELECT s.`SHIPMENT_ID` FROM rm_shipment s WHERE s.`PARENT_SHIPMENT_ID`=?;";
            List<Integer> shipmentIdList = this.jdbcTemplate.queryForList(sql, Integer.class, parentShipmentId);
            for (int shipmentId1 : shipmentIdList) {
//                sql1 = "UPDATE rm_shipment s SET s.`MAX_VERSION_ID`=s.`MAX_VERSION_ID`+1 WHERE s.`SHIPMENT_ID`=?;";
//                this.jdbcTemplate.update(sql1, shipmentId1);

                sql = "INSERT INTO rm_shipment_trans "
                        + "SELECT NULL,?,st.`PLANNING_UNIT_ID`,st.`PROCUREMENT_AGENT_ID` ,st.`FUNDING_SOURCE_ID`, "
                        + "st.`BUDGET_ID`,st.`EXPECTED_DELIVERY_DATE`,st.`PROCUREMENT_UNIT_ID`,st.`SUPPLIER_ID`, "
                        + "st.`SHIPMENT_QTY`,st.`RATE`,st.`PRODUCT_COST`,st.`SHIPMENT_MODE`,st.`FREIGHT_COST`, "
                        + "st.`PLANNED_DATE`,st.`SUBMITTED_DATE`,st.`APPROVED_DATE`,st.`SHIPPED_DATE`, "
                        + "st.`ARRIVED_DATE`,st.`RECEIVED_DATE`,st.`SHIPMENT_STATUS_ID`,st.`NOTES`, "
                        + "0,st.`ORDER_NO`,st.`PRIME_LINE_NO`,st.`ACCOUNT_FLAG`,st.`EMERGENCY_ORDER`,st.`LOCAL_PROCUREMENT`,?,st.`DATA_SOURCE_ID`,?,s.`MAX_VERSION_ID`,0 "
                        + "FROM rm_shipment_trans st "
                        + "LEFT JOIN rm_shipment s ON s.`SHIPMENT_ID`=st.`SHIPMENT_ID` "
                        + "WHERE st.`SHIPMENT_ID`=? "
                        + "ORDER BY st.`SHIPMENT_TRANS_ID` DESC LIMIT 1;";
                this.jdbcTemplate.update(sql, shipmentId1, curUser.getUserId(), curDate, shipmentId1);
            }
        }
        sql = "UPDATE rm_manual_tagging m SET m.`ACTIVE`=0,m.`NOTES`=?,m.`LAST_MODIFIED_DATE`=?,m.`LAST_MODIFIED_BY`=? WHERE m.`SHIPMENT_ID`=?;";
        this.jdbcTemplate.update(sql, erpOrderDTO.getNotes(), curDate, curUser.getUserId(), erpOrderDTO.getShipmentId());

    }

    @Override
    public List<LoadProgram> getLoadProgram(CustomUserDetails curUser) {
        StringBuilder sb = new StringBuilder("SELECT  "
                + "    p.PROGRAM_ID, p.PROGRAM_CODE, p.LABEL_ID, p.LABEL_EN, p.LABEL_FR, p.LABEL_SP, p.LABEL_PR, "
                + "    rc.REALM_COUNTRY_ID, c.LABEL_ID `REALM_COUNTRY_LABEL_ID`, c.LABEL_EN `REALM_COUNTRY_LABEL_EN`, c.LABEL_FR `REALM_COUNTRY_LABEL_FR`, c.LABEL_SP `REALM_COUNTRY_LABEL_SP`, c.LABEL_PR `REALM_COUNTRY_LABEL_PR`, c.COUNTRY_CODE, "
                + "    ha.HEALTH_AREA_ID, ha.LABEL_ID `HEALTH_AREA_LABEL_ID`, ha.LABEL_EN `HEALTH_AREA_LABEL_EN`, ha.LABEL_FR `HEALTH_AREA_LABEL_FR`, ha.LABEL_SP `HEALTH_AREA_LABEL_SP`, ha.LABEL_PR `HEALTH_AREA_LABEL_PR`, ha.HEALTH_AREA_CODE, "
                + "    o.ORGANISATION_ID, o.LABEL_ID `ORGANISATION_LABEL_ID`, o.LABEL_EN `ORGANISATION_LABEL_EN`, o.LABEL_FR `ORGANISATION_LABEL_FR`, o.LABEL_SP `ORGANISATION_LABEL_SP`, o.LABEL_PR `ORGANISATION_LABEL_PR`, o.ORGANISATION_CODE, "
                + "    COUNT(pv.VERSION_ID) MAX_COUNT "
                + "FROM vw_program p  "
                + "LEFT JOIN rm_realm_country rc ON p.REALM_COUNTRY_ID=rc.REALM_COUNTRY_ID "
                + "LEFT JOIN vw_country c ON rc.COUNTRY_ID=c.COUNTRY_ID "
                + "LEFT JOIN vw_health_area ha ON p.HEALTH_AREA_ID=ha.HEALTH_AREA_ID "
                + "LEFT JOIN rm_health_area_country hac ON  ha.HEALTH_AREA_ID=hac.HEALTH_AREA_ID AND rc.REALM_COUNTRY_ID=hac.REALM_COUNTRY_ID "
                + "LEFT JOIN vw_organisation o ON p.ORGANISATION_ID=o.ORGANISATION_ID "
                + "LEFT JOIN rm_organisation_country oc ON o.ORGANISATION_ID=oc.ORGANISATION_ID AND rc.REALM_COUNTRY_ID=oc.REALM_COUNTRY_ID "
                + "LEFT JOIN rm_program_version pv ON p.PROGRAM_ID=pv.PROGRAM_ID "
                + "WHERE p.ACTIVE AND rc.ACTIVE AND ha.ACTIVE AND o.ACTIVE AND hac.ACTIVE AND oc.ACTIVE ");
        Map<String, Object> params = new HashMap<>();
        this.aclService.addFullAclForProgram(sb, params, "p", curUser);
        sb.append(" GROUP BY p.PROGRAM_ID");
        List<LoadProgram> programList = this.namedParameterJdbcTemplate.query(sb.toString(), params, new LoadProgramRowMapper());
        params.clear();
        params.put("programId", 0);
        for (LoadProgram lp : programList) {
            params.replace("programId", lp.getProgram().getId());
            lp.setVersionList(this.namedParameterJdbcTemplate.query("SELECT LPAD(pv.VERSION_ID,6,'0') VERSION_ID, vt.VERSION_TYPE_ID, vtl.LABEL_ID `VERSION_TYPE_LABEL_ID`, vtl.LABEL_EN `VERSION_TYPE_LABEL_EN`, vtl.LABEL_FR `VERSION_TYPE_LABEL_FR`, vtl.LABEL_SP `VERSION_TYPE_LABEL_SP`, vtl.LABEL_PR `VERSION_TYPE_LABEL_PR`, vs.VERSION_STATUS_ID, vsl.LABEL_ID `VERSION_STATUS_LABEL_ID`, vsl.LABEL_EN `VERSION_STATUS_LABEL_EN`, vsl.LABEL_FR `VERSION_STATUS_LABEL_FR`, vsl.LABEL_SP `VERSION_STATUS_LABEL_SP`, vsl.LABEL_PR `VERSION_STATUS_LABEL_PR`, cb.USER_ID, cb.USERNAME, pv.CREATED_DATE FROM vw_program p LEFT JOIN rm_program_version pv ON p.PROGRAM_ID=pv.PROGRAM_ID LEFT JOIN ap_version_type vt ON pv.VERSION_TYPE_ID=vt.VERSION_TYPE_ID LEFT JOIN ap_label vtl ON vt.LABEL_ID=vtl.LABEL_ID LEFT JOIN ap_version_status vs ON pv.VERSION_STATUS_ID=vs.VERSION_STATUS_ID LEFT JOIN ap_label vsl ON vs.LABEL_ID=vsl.LABEL_ID LEFT JOIN us_user cb ON pv.CREATED_BY=cb.USER_ID WHERE p.ACTIVE AND p.PROGRAM_ID=:programId ORDER BY pv.VERSION_ID DESC LIMIT 0,5", params, new LoadProgramVersionRowMapper()));
        }
        return programList;
    }

    /**
     *
     * @param programId
     * @param page Page no is the pagination value that you want to see. Starts
     * from 0 which is shown by default. Every time the user clicks on More...
     * you should increment the pagination for that Page and return it
     * @param curUser
     * @return
     */
    @Override
    public LoadProgram getLoadProgram(int programId, int page, CustomUserDetails curUser) {
        StringBuilder sb = new StringBuilder("SELECT  "
                + "    p.PROGRAM_ID, p.PROGRAM_CODE, p.LABEL_ID, p.LABEL_EN, p.LABEL_FR, p.LABEL_SP, p.LABEL_PR, "
                + "    rc.REALM_COUNTRY_ID, c.LABEL_ID `REALM_COUNTRY_LABEL_ID`, c.LABEL_EN `REALM_COUNTRY_LABEL_EN`, c.LABEL_FR `REALM_COUNTRY_LABEL_FR`, c.LABEL_SP `REALM_COUNTRY_LABEL_SP`, c.LABEL_PR `REALM_COUNTRY_LABEL_PR`, c.COUNTRY_CODE, "
                + "    ha.HEALTH_AREA_ID, ha.LABEL_ID `HEALTH_AREA_LABEL_ID`, ha.LABEL_EN `HEALTH_AREA_LABEL_EN`, ha.LABEL_FR `HEALTH_AREA_LABEL_FR`, ha.LABEL_SP `HEALTH_AREA_LABEL_SP`, ha.LABEL_PR `HEALTH_AREA_LABEL_PR`, ha.HEALTH_AREA_CODE, "
                + "    o.ORGANISATION_ID, o.LABEL_ID `ORGANISATION_LABEL_ID`, o.LABEL_EN `ORGANISATION_LABEL_EN`, o.LABEL_FR `ORGANISATION_LABEL_FR`, o.LABEL_SP `ORGANISATION_LABEL_SP`, o.LABEL_PR `ORGANISATION_LABEL_PR`, o.ORGANISATION_CODE, "
                + "    COUNT(pv.VERSION_ID) MAX_COUNT "
                + "FROM vw_program p  "
                + "LEFT JOIN rm_realm_country rc ON p.REALM_COUNTRY_ID=rc.REALM_COUNTRY_ID "
                + "LEFT JOIN vw_country c ON rc.COUNTRY_ID=c.COUNTRY_ID "
                + "LEFT JOIN vw_health_area ha ON p.HEALTH_AREA_ID=ha.HEALTH_AREA_ID "
                + "LEFT JOIN rm_health_area_country hac ON  ha.HEALTH_AREA_ID=hac.HEALTH_AREA_ID AND rc.REALM_COUNTRY_ID=hac.REALM_COUNTRY_ID "
                + "LEFT JOIN vw_organisation o ON p.ORGANISATION_ID=o.ORGANISATION_ID "
                + "LEFT JOIN rm_organisation_country oc ON o.ORGANISATION_ID=oc.ORGANISATION_ID AND rc.REALM_COUNTRY_ID=oc.REALM_COUNTRY_ID "
                + "LEFT JOIN rm_program_version pv ON p.PROGRAM_ID=pv.PROGRAM_ID "
                + "WHERE p.ACTIVE AND rc.ACTIVE AND ha.ACTIVE AND o.ACTIVE AND hac.ACTIVE AND oc.ACTIVE AND p.PROGRAM_ID=:programId ");
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        this.aclService.addFullAclForProgram(sb, params, "p", curUser);
        sb.append(" GROUP BY p.PROGRAM_ID");
        LoadProgram program = this.namedParameterJdbcTemplate.queryForObject(sb.toString(), params, new LoadProgramRowMapper());
        program.setCurrentPage(page);
        params.clear();
        params.put("programId", programId);
        int versionCount = this.namedParameterJdbcTemplate.queryForObject("SELECT COUNT(*) FROM rm_program_version pv WHERE pv.PROGRAM_ID=:programId", params, Integer.class);
        params.put("versionCount", versionCount);
        params.put("offsetNo", page * 5);
        int showCount = 0;
        if (versionCount - page * 5 > 5) {
            showCount = 5;
        } else if (versionCount - page * 5 > 0) {
            showCount = versionCount - page * 5;
        }
        params.put("showCount", showCount);
        program.setVersionList(this.namedParameterJdbcTemplate.query("SELECT LPAD(pv.VERSION_ID,6,'0') VERSION_ID, vt.VERSION_TYPE_ID, vtl.LABEL_ID `VERSION_TYPE_LABEL_ID`, vtl.LABEL_EN `VERSION_TYPE_LABEL_EN`, vtl.LABEL_FR `VERSION_TYPE_LABEL_FR`, vtl.LABEL_SP `VERSION_TYPE_LABEL_SP`, vtl.LABEL_PR `VERSION_TYPE_LABEL_PR`, vs.VERSION_STATUS_ID, vsl.LABEL_ID `VERSION_STATUS_LABEL_ID`, vsl.LABEL_EN `VERSION_STATUS_LABEL_EN`, vsl.LABEL_FR `VERSION_STATUS_LABEL_FR`, vsl.LABEL_SP `VERSION_STATUS_LABEL_SP`, vsl.LABEL_PR `VERSION_STATUS_LABEL_PR`, cb.USER_ID, cb.USERNAME, pv.CREATED_DATE FROM vw_program p LEFT JOIN rm_program_version pv ON p.PROGRAM_ID=pv.PROGRAM_ID LEFT JOIN ap_version_type vt ON pv.VERSION_TYPE_ID=vt.VERSION_TYPE_ID LEFT JOIN ap_label vtl ON vt.LABEL_ID=vtl.LABEL_ID LEFT JOIN ap_version_status vs ON pv.VERSION_STATUS_ID=vs.VERSION_STATUS_ID LEFT JOIN ap_label vsl ON vs.LABEL_ID=vsl.LABEL_ID LEFT JOIN us_user cb ON pv.CREATED_BY=cb.USER_ID WHERE p.ACTIVE AND p.PROGRAM_ID=:programId ORDER BY pv.VERSION_ID DESC LIMIT :offsetNo, :showCount", params, new LoadProgramVersionRowMapper()));
        return program;
    }

    @Override
    public boolean validateProgramCode(int realmId, int programId, String programCode, CustomUserDetails curUser) {
        String sql = "SELECT COUNT(*) FROM rm_program p LEFT JOIN rm_realm_country rc  ON p.REALM_COUNTRY_ID=rc.REALM_COUNTRY_ID WHERE rc.REALM_ID = :realmId AND p.PROGRAM_CODE = :programCode AND (:programId = 0 OR p.PROGRAM_ID != :programId)";
        Map<String, Object> params = new HashMap<>();
        params.put("realmId", realmId);
        params.put("programId", programId);
        params.put("programCode", programCode);
        return (this.namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class) == 0);
    }

    @Override
    public List<Program> getProgramListForSyncProgram(String programIdsString, CustomUserDetails curUser) {
        StringBuilder sqlStringBuilder = new StringBuilder(this.sqlListString).append(" AND p.PROGRAM_ID IN (").append(programIdsString).append(") ");
        Map<String, Object> params = new HashMap<>();
        this.aclService.addUserAclForRealm(sqlStringBuilder, params, "r", curUser);
        this.aclService.addFullAclForProgram(sqlStringBuilder, params, "p", curUser);
        sqlStringBuilder.append(this.sqlOrderBy);
        return this.namedParameterJdbcTemplate.query(sqlStringBuilder.toString(), params, new ProgramListResultSetExtractor());
    }

    @Override
    public List<ProgramPlanningUnit> getProgramPlanningUnitListForSyncProgram(String programIdsString, CustomUserDetails curUser) {
        StringBuilder sqlStringBuilder = new StringBuilder(this.sqlListStringForProgramPlanningUnit).append(" AND ppu.PROGRAM_ID IN (").append(programIdsString).append(") ");
        Map<String, Object> params = new HashMap<>();
        this.aclService.addUserAclForRealm(sqlStringBuilder, params, "rc", curUser);
        this.aclService.addFullAclForProgram(sqlStringBuilder, params, "pg", curUser);
        sqlStringBuilder.append(" ORDER BY pu.LABEL_EN");
        return this.namedParameterJdbcTemplate.query(sqlStringBuilder.toString(), params, new ProgramPlanningUnitRowMapper());
    }

}
