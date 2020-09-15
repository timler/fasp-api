/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.dao.impl;

import cc.altius.FASP.dao.ProgramDataDao;
import cc.altius.FASP.exception.CouldNotSaveException;
import cc.altius.FASP.model.Batch;
import cc.altius.FASP.model.BatchData;
import cc.altius.FASP.model.Consumption;
import cc.altius.FASP.model.ConsumptionBatchInfo;
import cc.altius.FASP.model.CustomUserDetails;
import cc.altius.FASP.model.Inventory;
import cc.altius.FASP.model.InventoryBatchInfo;
import cc.altius.FASP.model.MasterSupplyPlan;
import cc.altius.FASP.model.NewSupplyPlan;
import cc.altius.FASP.model.ProblemReport;
import cc.altius.FASP.model.ProblemReportTrans;
import cc.altius.FASP.model.ProgramData;
import cc.altius.FASP.model.ProgramVersion;
import cc.altius.FASP.model.Shipment;
import cc.altius.FASP.model.ShipmentBatchInfo;
import cc.altius.FASP.model.SimpleObject;
import cc.altius.FASP.model.SimplifiedSupplyPlan;
import cc.altius.FASP.model.SupplyPlan;
import cc.altius.FASP.model.SupplyPlanBatchInfo;
import cc.altius.FASP.model.SupplyPlanDate;
import cc.altius.FASP.model.Version;
import cc.altius.FASP.model.rowMapper.BatchRowMapper;
import cc.altius.FASP.model.rowMapper.ConsumptionListResultSetExtractor;
import cc.altius.FASP.model.rowMapper.InventoryListResultSetExtractor;
import cc.altius.FASP.model.rowMapper.NewSupplyPlanBatchResultSetExtractor;
import cc.altius.FASP.model.rowMapper.NewSupplyPlanRegionResultSetExtractor;
import cc.altius.FASP.model.rowMapper.ProgramVersionRowMapper;
import cc.altius.FASP.model.rowMapper.VersionRowMapper;
import cc.altius.FASP.model.rowMapper.ShipmentListResultSetExtractor;
import cc.altius.FASP.model.rowMapper.SimpleObjectRowMapper;
import cc.altius.FASP.model.rowMapper.SimplifiedSupplyPlanResultSetExtractor;
import cc.altius.FASP.model.rowMapper.SupplyPlanResultSetExtractor;
import cc.altius.FASP.service.AclService;
import cc.altius.utils.DateUtils;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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
public class ProgramDataDaoImpl implements ProgramDataDao {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired
    private AclService aclService;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Version getVersionInfo(int programId, int versionId) {
        if (versionId == -1) {
            String sqlString = "SELECT MAX(pv.VERSION_ID) FROM rm_program_version pv WHERE pv.PROGRAM_ID=:programId";
            Map<String, Object> params = new HashMap<>();
            params.put("programId", programId);
            versionId = this.namedParameterJdbcTemplate.queryForObject(sqlString, params, Integer.class);
        }
        String sqlString = "SELECT pv.VERSION_ID, "
                + "    pv.PROGRAM_ID, pv.NOTES, pv.LAST_MODIFIED_DATE, lmb.USER_ID `LMB_USER_ID`, lmb.USERNAME `LMB_USERNAME`, pv.CREATED_DATE, cb.USER_ID `CB_USER_ID`, cb.USERNAME `CB_USERNAME`,  "
                + "    vt.VERSION_TYPE_ID, vtl.LABEL_ID `VERSION_TYPE_LABEL_ID`, vtl.LABEL_EN `VERSION_TYPE_LABEL_EN`, vtl.LABEL_FR `VERSION_TYPE_LABEL_FR`, vtl.LABEL_SP `VERSION_TYPE_LABEL_SP`, vtl.LABEL_PR `VERSION_TYPE_LABEL_PR`, "
                + "    vs.VERSION_STATUS_ID, vsl.LABEL_ID `VERSION_STATUS_LABEL_ID`, vsl.LABEL_EN `VERSION_STATUS_LABEL_EN`, vsl.LABEL_FR `VERSION_STATUS_LABEL_FR`, vsl.LABEL_SP `VERSION_STATUS_LABEL_SP`, vsl.LABEL_PR `VERSION_STATUS_LABEL_PR` "
                + "FROM rm_program_version pv  "
                + "LEFT JOIN ap_version_type vt ON pv.VERSION_TYPE_ID=vt.VERSION_TYPE_ID "
                + "LEFT JOIN ap_label vtl ON vt.LABEL_ID=vtl.LABEL_ID "
                + "LEFT JOIN ap_version_status vs ON pv.VERSION_STATUS_ID=vs.VERSION_STATUS_ID "
                + "LEFT JOIN ap_label vsl ON vs.LABEL_ID=vsl.LABEL_ID "
                + "LEFT JOIN us_user cb ON pv.CREATED_BY=cb.USER_ID "
                + "LEFT JOIN us_user lmb ON pv.LAST_MODIFIED_BY=lmb.USER_ID "
                + "WHERE pv.PROGRAM_ID=:programId AND pv.VERSION_ID=:versionId";
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("versionId", versionId);
        return this.namedParameterJdbcTemplate.queryForObject(sqlString, params, new VersionRowMapper());
    }

    @Override
    public List<Consumption> getConsumptionList(int programId, int versionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("versionId", versionId);
        return this.namedParameterJdbcTemplate.query("CALL getConsumptionData(:programId, :versionId)", params, new ConsumptionListResultSetExtractor());
    }

    @Override
    public List<Inventory> getInventoryList(int programId, int versionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("versionId", versionId);
        return this.namedParameterJdbcTemplate.query("CALL getInventoryData(:programId, :versionId)", params, new InventoryListResultSetExtractor());
    }

    @Override
    public List<Shipment> getShipmentList(int programId, int versionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("versionId", versionId);
        return this.namedParameterJdbcTemplate.query("CALL getShipmentData(:programId, :versionId)", params, new ShipmentListResultSetExtractor());
    }

    @Override
    @Transactional
    public Version saveProgramData(ProgramData programData, CustomUserDetails curUser) throws CouldNotSaveException {
        Date curDate = DateUtils.getCurrentDateObject(DateUtils.EST);
        // Check which records have changed
        Map<String, Object> params = new HashMap<>();

        // ########################### Consumption ############################################
        String sqlString = "DROP TEMPORARY TABLE IF EXISTS `tmp_consumption`";
//        String sqlString = "DROP TABLE IF EXISTS `tmp_consumption`";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "CREATE TEMPORARY TABLE `tmp_consumption` ( "
                //        sqlString = "CREATE TABLE `tmp_consumption` ( "
                + "  `ID` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT, "
                + "  `CONSUMPTION_ID` INT UNSIGNED NULL, "
                + "  `REGION_ID` INT(10) UNSIGNED NOT NULL, "
                + "  `PLANNING_UNIT_ID` INT(10) UNSIGNED NOT NULL, "
                + "  `REALM_COUNTRY_PLANNING_UNIT_ID` INT(10) UNSIGNED NOT NULL, "
                + "  `CONSUMPTION_DATE` DATE NOT NULL, "
                + "  `ACTUAL_FLAG` TINYINT UNSIGNED NOT NULL, "
                + "  `RCPU_QTY` DOUBLE UNSIGNED NOT NULL, "
                + "  `QTY` DOUBLE UNSIGNED NOT NULL, "
                + "  `DAYS_OF_STOCK_OUT` INT UNSIGNED NOT NULL, "
                + "  `DATA_SOURCE_ID` INT(10) UNSIGNED NOT NULL, "
                + "  `NOTES` TEXT NULL, "
                + "  `ACTIVE` TINYINT UNSIGNED NOT NULL DEFAULT 1, "
                + "  `VERSION_ID` INT(10) NULL, "
                + "  `CHANGED` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0, "
                + "  PRIMARY KEY (`ID`), "
                + "  INDEX `fk_tmp_consumption_1_idx` (`CONSUMPTION_ID` ASC), "
                + "  INDEX `fk_tmp_consumption_2_idx` (`REGION_ID` ASC), "
                + "  INDEX `fk_tmp_consumption_3_idx` (`PLANNING_UNIT_ID` ASC), "
                + "  INDEX `fk_tmp_consumption_4_idx` (`DATA_SOURCE_ID` ASC),"
                + "  INDEX `fk_tmp_consumption_5_idx` (`VERSION_ID` ASC))";
        this.namedParameterJdbcTemplate.update(sqlString, params);

        sqlString = "DROP TEMPORARY TABLE IF EXISTS `tmp_consumption_batch_info`";
//        sqlString = "DROP TABLE IF EXISTS `tmp_consumption_batch_info`";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "CREATE TEMPORARY TABLE `tmp_consumption_batch_info` ( "
                //        sqlString = "CREATE TABLE `tmp_consumption_batch_info` ( "
                + "  `ID` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT, "
                + "  `PARENT_ID` INT(10) UNSIGNED NOT NULL, "
                + "  `CONSUMPTION_TRANS_BATCH_INFO_ID` INT(10) UNSIGNED NULL, "
                + "  `CONSUMPTION_TRANS_ID` INT(10) UNSIGNED NULL, "
                + "  `BATCH_ID` INT(10) NOT NULL, "
                + "  `BATCH_QTY` INT(10) UNSIGNED NOT NULL, "
                + "  `CHANGED` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0, "
                + "  PRIMARY KEY (`ID`), "
                + "  INDEX `fk_tmp_consumption_1_idx` (`CONSUMPTION_TRANS_ID` ASC), "
                + "  INDEX `fk_tmp_consumption_2_idx` (`CONSUMPTION_TRANS_BATCH_INFO_ID` ASC), "
                + "  INDEX `fk_tmp_consumption_3_idx` (`BATCH_ID` ASC))";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        final List<SqlParameterSource> insertList = new ArrayList<>();
        final List<SqlParameterSource> insertBatchList = new ArrayList<>();
        int id = 1;
        for (Consumption c : programData.getConsumptionList()) {
            Map<String, Object> tp = new HashMap<>();
            tp.put("ID", id);
            tp.put("CONSUMPTION_ID", (c.getConsumptionId() == 0 ? null : c.getConsumptionId()));
            tp.put("REGION_ID", c.getRegion().getId());
            tp.put("REALM_COUNTRY_PLANNING_UNIT_ID", c.getRealmCountryPlanningUnit().getId());
            tp.put("PLANNING_UNIT_ID", c.getPlanningUnit().getId());
            tp.put("CONSUMPTION_DATE", c.getConsumptionDate());
            tp.put("ACTUAL_FLAG", c.isActualFlag());
            tp.put("RCPU_QTY", c.getConsumptionRcpuQty());
            tp.put("QTY", c.getConsumptionQty());
            tp.put("DAYS_OF_STOCK_OUT", c.getDayOfStockOut());
            tp.put("DATA_SOURCE_ID", c.getDataSource().getId());
            tp.put("NOTES", c.getNotes());
            tp.put("ACTIVE", c.isActive());
            tp.put("VERSION_ID", c.getVersionId());
            insertList.add(new MapSqlParameterSource(tp));
            SimpleJdbcInsert batchInsert = new SimpleJdbcInsert(dataSource).withTableName("rm_batch_info").usingGeneratedKeyColumns("BATCH_ID");
            for (ConsumptionBatchInfo b : c.getBatchInfoList()) {
                if (b.getBatch().getBatchId() == 0) {
                    Map<String, Object> batchParams = new HashMap<>();
                    batchParams.put("BATCH_NO", b.getBatch().getBatchNo());
                    batchParams.put("PROGRAM_ID", programData.getProgramId());
                    batchParams.put("PLANNING_UNIT_ID", c.getPlanningUnit().getId());
                    batchParams.put("AUTO_GENERATED", b.getBatch().isAutoGenerated());
                    batchParams.put("EXPIRY_DATE", b.getBatch().getExpiryDate());
                    batchParams.put("CREATED_DATE", curDate);
                    try {
                        b.getBatch().setBatchId(this.namedParameterJdbcTemplate.queryForObject("SELECT bi.BATCH_ID FROM rm_batch_info bi WHERE bi.BATCH_NO=:BATCH_NO AND bi.PROGRAM_ID=:PROGRAM_ID AND bi.EXPIRY_DATE=:EXPIRY_DATE", batchParams, Integer.class));
                    } catch (DataAccessException d) {
                        b.getBatch().setBatchId(batchInsert.executeAndReturnKey(batchParams).intValue());
                    }
                }
                Map<String, Object> tb = new HashMap<>();
                tb.put("CONSUMPTION_TRANS_ID", null);
                tb.put("CONSUMPTION_TRANS_BATCH_INFO_ID", (b.getConsumptionTransBatchInfoId() == 0 ? null : b.getConsumptionTransBatchInfoId()));
                tb.put("PARENT_ID", id);
                tb.put("BATCH_ID", b.getBatch().getBatchId());
                tb.put("BATCH_QTY", b.getConsumptionQty());
                insertBatchList.add(new MapSqlParameterSource(tb));
            }
            id++;
        }

        SqlParameterSource[] insertConsumption = new SqlParameterSource[insertList.size()];
        sqlString = " INSERT INTO tmp_consumption (ID, CONSUMPTION_ID, REGION_ID, PLANNING_UNIT_ID, REALM_COUNTRY_PLANNING_UNIT_ID, CONSUMPTION_DATE, ACTUAL_FLAG, QTY, RCPU_QTY, DAYS_OF_STOCK_OUT, DATA_SOURCE_ID, NOTES, ACTIVE, VERSION_ID) VALUES (:ID, :CONSUMPTION_ID, :REGION_ID, :PLANNING_UNIT_ID, :REALM_COUNTRY_PLANNING_UNIT_ID, :CONSUMPTION_DATE, :ACTUAL_FLAG, :QTY, :RCPU_QTY, :DAYS_OF_STOCK_OUT, :DATA_SOURCE_ID, :NOTES, :ACTIVE, :VERSION_ID)";
        this.namedParameterJdbcTemplate.batchUpdate(sqlString, insertList.toArray(insertConsumption));
        if (insertBatchList.size() > 0) {
            SqlParameterSource[] insertConsumptionBatch = new SqlParameterSource[insertBatchList.size()];
            sqlString = "INSERT INTO tmp_consumption_batch_info (PARENT_ID, CONSUMPTION_TRANS_ID, CONSUMPTION_TRANS_BATCH_INFO_ID, BATCH_ID, BATCH_QTY) VALUES (:PARENT_ID, :CONSUMPTION_TRANS_ID, :CONSUMPTION_TRANS_BATCH_INFO_ID, :BATCH_ID, :BATCH_QTY)";
            this.namedParameterJdbcTemplate.batchUpdate(sqlString, insertBatchList.toArray(insertConsumptionBatch));
        }
        params.clear();
        sqlString = "UPDATE tmp_consumption_batch_info tcbi LEFT JOIN rm_consumption_trans_batch_info ctbi ON tcbi.CONSUMPTION_TRANS_BATCH_INFO_ID=ctbi.CONSUMPTION_TRANS_BATCH_INFO_ID SET tcbi.CONSUMPTION_TRANS_ID=ctbi.CONSUMPTION_TRANS_ID WHERE tcbi.CONSUMPTION_TRANS_BATCH_INFO_ID IS NOT NULL";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        params.clear();
        // Update the VersionId's in tmp_consumption with the ones from consumption_trans based on VersionId
//        sqlString = "UPDATE tmp_consumption tc LEFT JOIN rm_consumption c ON tc.CONSUMPTION_ID=c.CONSUMPTION_ID SET tc.VERSION_ID=c.MAX_VERSION_ID WHERE tc.CONSUMPTION_ID IS NOT NULL";
//        this.namedParameterJdbcTemplate.update(sqlString, params);
        // Flag the rows for changed records
        sqlString = "UPDATE tmp_consumption tc LEFT JOIN rm_consumption c ON tc.CONSUMPTION_ID=c.CONSUMPTION_ID LEFT JOIN rm_consumption_trans ct ON tc.CONSUMPTION_ID=ct.CONSUMPTION_ID AND tc.VERSION_ID=ct.VERSION_ID SET tc.CHANGED=1 WHERE "
                + "tc.REGION_ID!=ct.REGION_ID OR "
                + "tc.PLANNING_UNIT_ID!=ct.PLANNING_UNIT_ID OR "
                + "tc.REALM_COUNTRY_PLANNING_UNIT_ID!=ct.REALM_COUNTRY_PLANNING_UNIT_ID OR "
                + "tc.CONSUMPTION_DATE!=ct.CONSUMPTION_DATE OR "
                + "tc.ACTUAL_FLAG!=ct.ACTUAL_FLAG OR "
                + "tc.QTY!=ct.CONSUMPTION_QTY OR "
                + "tc.RCPU_QTY!=ct.CONSUMPTION_RCPU_QTY OR "
                + "tc.DAYS_OF_STOCK_OUT!=ct.DAYS_OF_STOCK_OUT OR "
                + "tc.DATA_SOURCE_ID!=ct.DATA_SOURCE_ID OR "
                + "tc.NOTES!=ct.NOTES OR "
                + "tc.ACTIVE!=ct.ACTIVE OR "
                + "tc.CONSUMPTION_ID IS NULL";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "UPDATE tmp_consumption_batch_info tcbi LEFT JOIN rm_consumption_trans_batch_info ctbi ON tcbi.CONSUMPTION_TRANS_BATCH_INFO_ID=ctbi.CONSUMPTION_TRANS_BATCH_INFO_ID SET `CHANGED`=1 WHERE tcbi.CONSUMPTION_TRANS_BATCH_INFO_ID IS NULL OR tcbi.BATCH_ID!=ctbi.BATCH_ID OR tcbi.BATCH_QTY!=ctbi.CONSUMPTION_QTY";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "UPDATE tmp_consumption tc LEFT JOIN tmp_consumption_batch_info tcbi ON tc.ID = tcbi.PARENT_ID SET tc.CHANGED=1 WHERE tcbi.CHANGED=1";
        this.namedParameterJdbcTemplate.update(sqlString, params);

        // Check if there are any rows that need to be added
        params.clear();
        sqlString = "SELECT COUNT(*) FROM tmp_consumption tc WHERE tc.CHANGED=1";
        int consumptionRows = this.namedParameterJdbcTemplate.queryForObject(sqlString, params, Integer.class);

        Version version = null;
        if (consumptionRows > 0) {
            params.put("programId", programData.getProgramId());
            params.put("curUser", curUser.getUserId());
            params.put("curDate", curDate);
            params.put("versionTypeId", programData.getVersionType().getId());
            params.put("versionStatusId", programData.getVersionStatus().getId());
            params.put("notes", programData.getNotes());
            sqlString = "CALL getVersionId(:programId, :versionTypeId, :versionStatusId, :notes, :curUser, :curDate)";
            version = this.namedParameterJdbcTemplate.queryForObject(sqlString, params, new VersionRowMapper());
            params.put("versionId", version.getVersionId());
            // Insert the rows where Consumption Id is not null
            sqlString = "INSERT INTO rm_consumption_trans SELECT null, tc.CONSUMPTION_ID, tc.REGION_ID, tc.PLANNING_UNIT_ID, tc.CONSUMPTION_DATE, tc.REALM_COUNTRY_PLANNING_UNIT_ID, tc.ACTUAL_FLAG, tc.QTY, tc.RCPU_QTY, tc.DAYS_OF_STOCK_OUT, tc.DATA_SOURCE_ID, tc.NOTES, tc.ACTIVE, :curUser, :curDate, :versionId"
                    + " FROM fasp.tmp_consumption tc "
                    + " WHERE tc.CHANGED=1 AND tc.CONSUMPTION_ID!=0";
            consumptionRows = this.namedParameterJdbcTemplate.update(sqlString, params);
            params.clear();
            params.put("versionId", version.getVersionId());
            // Update the rm_consumption table with the latest versionId
            sqlString = "UPDATE tmp_consumption tc LEFT JOIN rm_consumption c ON c.CONSUMPTION_ID=tc.CONSUMPTION_ID SET c.MAX_VERSION_ID=:versionId WHERE tc.CONSUMPTION_ID IS NOT NULL AND tc.CHANGED=1";
            this.namedParameterJdbcTemplate.update(sqlString, params);
            // Insert into rm_consumption_trans_batch_info where the consumption record was already existing but has changed
            sqlString = "INSERT INTO rm_consumption_trans_batch_info SELECT null, ct.CONSUMPTION_TRANS_ID, tcbi.BATCH_ID, tcbi.BATCH_QTY from tmp_consumption tc left join tmp_consumption_batch_info tcbi ON tcbi.PARENT_ID=tc.ID LEFT JOIN rm_consumption_trans ct ON tc.CONSUMPTION_ID=ct.CONSUMPTION_ID AND ct.VERSION_ID=:versionId WHERE tc.CHANGED=1 AND tc.CONSUMPTION_ID IS NOT NULL AND tcbi.PARENT_ID IS NOT NULL";
            this.namedParameterJdbcTemplate.update(sqlString, params);

            sqlString = "SELECT tc.ID FROM tmp_consumption tc WHERE tc.CONSUMPTION_ID IS NULL OR tc.CONSUMPTION_ID=0";
            List<Integer> idListForInsert = this.namedParameterJdbcTemplate.queryForList(sqlString, params, Integer.class);
            params.put("id", 0);
            params.put("versionId", version.getVersionId());
            params.put("programId", programData.getProgramId());
            params.put("curUser", curUser.getUserId());
            params.put("curDate", curDate);
            for (Integer tmpId : idListForInsert) {
                sqlString = "INSERT INTO rm_consumption (PROGRAM_ID, CREATED_BY, CREATED_DATE, LAST_MODIFIED_BY, LAST_MODIFIED_DATE, MAX_VERSION_ID) VALUES (:programId, :curUser, :curDate, :curUser, :curDate, :versionId)";
                consumptionRows += this.namedParameterJdbcTemplate.update(sqlString, params);
                params.replace("id", tmpId);
                sqlString = "INSERT INTO rm_consumption_trans SELECT null, LAST_INSERT_ID(), tc.REGION_ID, tc.PLANNING_UNIT_ID, tc.CONSUMPTION_DATE, tc.REALM_COUNTRY_PLANNING_UNIT_ID, tc.ACTUAL_FLAG, tc.QTY, tc.RCPU_QTY, tc.DAYS_OF_STOCK_OUT, tc.DATA_SOURCE_ID, tc.NOTES, tc.ACTIVE, :curUser, :curDate, :versionId FROM tmp_consumption tc WHERE tc.ID=:id";
                this.namedParameterJdbcTemplate.update(sqlString, params);
                sqlString = "INSERT INTO rm_consumption_trans_batch_info SELECT null, LAST_INSERT_ID(), tcbi.BATCH_ID, tcbi.BATCH_QTY from tmp_consumption_batch_info tcbi WHERE tcbi.PARENT_ID=:id";
                this.namedParameterJdbcTemplate.update(sqlString, params);
            }
        }
        // ########################### Consumption ############################################

        // ###########################  Inventory  ############################################
        params.clear();
        sqlString = "DROP TEMPORARY TABLE IF EXISTS `tmp_inventory`";
//        sqlString = "DROP TABLE IF EXISTS `tmp_inventory`";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "CREATE TEMPORARY TABLE `tmp_inventory` ( "
                //        sqlString = "CREATE TABLE `tmp_inventory` ( "
                + "  `ID` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT, "
                + "  `INVENTORY_ID` INT UNSIGNED NULL, "
                + "  `INVENTORY_DATE` DATE NOT NULL, "
                + "  `REGION_ID` INT(10) UNSIGNED NULL, "
                + "  `REALM_COUNTRY_PLANNING_UNIT_ID` INT(10) UNSIGNED NOT NULL, "
                + "  `ACTUAL_QTY` INT(10) UNSIGNED NULL, "
                + "  `ADJUSTMENT_QTY` INT(10) NOT NULL, "
                + "  `DATA_SOURCE_ID` INT(10) UNSIGNED NOT NULL, "
                + "  `NOTES` TEXT NULL, "
                + "  `ACTIVE` TINYINT UNSIGNED NOT NULL DEFAULT 1, "
                + "  `VERSION_ID` INT(10) NULL, "
                + "  `CHANGED` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0, "
                + "  PRIMARY KEY (`ID`), "
                + "  INDEX `fk_tmp_inventory_1_idx` (`INVENTORY_ID` ASC), "
                + "  INDEX `fk_tmp_inventory_2_idx` (`REGION_ID` ASC), "
                + "  INDEX `fk_tmp_inventory_3_idx` (`REALM_COUNTRY_PLANNING_UNIT_ID` ASC), "
                + "  INDEX `fk_tmp_inventory_4_idx` (`DATA_SOURCE_ID` ASC), "
                + "  INDEX `fk_tmp_inventory_5_idx` (`VERSION_ID` ASC))";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "DROP TEMPORARY TABLE IF EXISTS `tmp_inventory_batch_info`";
//        sqlString = "DROP TABLE IF EXISTS `tmp_inventory_batch_info`";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "CREATE TEMPORARY TABLE `tmp_inventory_batch_info` ( "
                //        sqlString = "CREATE TABLE `tmp_inventory_batch_info` ( "
                + "  `ID` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT, "
                + "  `PARENT_ID` INT(10) UNSIGNED NOT NULL, "
                + "  `INVENTORY_TRANS_BATCH_INFO_ID` INT(10) UNSIGNED NULL, "
                + "  `INVENTORY_TRANS_ID` INT(10) UNSIGNED NULL, "
                + "  `BATCH_ID` INT(10) NOT NULL, "
                + "  `ACTUAL_QTY` INT(10) UNSIGNED NULL, "
                + "  `ADJUSTMENT_QTY` INT(10) NOT NULL, "
                + "  `CHANGED` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0, "
                + "  PRIMARY KEY (`ID`), "
                + "  INDEX `fk_tmp_consumption_1_idx` (`INVENTORY_TRANS_ID` ASC), "
                + "  INDEX `fk_tmp_consumption_2_idx` (`INVENTORY_TRANS_BATCH_INFO_ID` ASC), "
                + "  INDEX `fk_tmp_consumption_3_idx` (`BATCH_ID` ASC))";
        this.namedParameterJdbcTemplate.update(sqlString, params);

        insertList.clear();
        insertBatchList.clear();
        id = 1;
        for (Inventory i : programData.getInventoryList()) {
            Map<String, Object> tp = new HashMap<>();
            tp.put("ID", id);
            tp.put("INVENTORY_ID", (i.getInventoryId() == 0 ? null : i.getInventoryId()));
            tp.put("INVENTORY_DATE", i.getInventoryDate());
            tp.put("REGION_ID", i.getRegion().getId());
            tp.put("REALM_COUNTRY_PLANNING_UNIT_ID", i.getRealmCountryPlanningUnit().getId());
            tp.put("ACTUAL_QTY", i.getActualQty());
            tp.put("ADJUSTMENT_QTY", i.getAdjustmentQty());
            tp.put("DATA_SOURCE_ID", i.getDataSource().getId());
            tp.put("NOTES", i.getNotes());
            tp.put("ACTIVE", i.isActive());
            insertList.add(new MapSqlParameterSource(tp));
            SimpleJdbcInsert batchInsert = new SimpleJdbcInsert(dataSource).withTableName("rm_batch_info").usingGeneratedKeyColumns("BATCH_ID");
            for (InventoryBatchInfo b : i.getBatchInfoList()) {
                if (b.getBatch().getBatchId() == 0) {
                    Map<String, Object> batchParams = new HashMap<>();
                    batchParams.put("BATCH_NO", b.getBatch().getBatchNo());
                    batchParams.put("PROGRAM_ID", programData.getProgramId());
                    batchParams.put("PLANNING_UNIT_ID", i.getPlanningUnit().getId());
                    batchParams.put("AUTO_GENERATED", b.getBatch().isAutoGenerated());
                    batchParams.put("EXPIRY_DATE", b.getBatch().getExpiryDate());
                    batchParams.put("CREATED_DATE", curDate);
                    try {
                        b.getBatch().setBatchId(this.namedParameterJdbcTemplate.queryForObject("SELECT bi.BATCH_ID FROM rm_batch_info bi WHERE bi.BATCH_NO=:BATCH_NO AND bi.PROGRAM_ID=:PROGRAM_ID AND bi.EXPIRY_DATE=:EXPIRY_DATE", batchParams, Integer.class));
                    } catch (DataAccessException d) {
                        b.getBatch().setBatchId(batchInsert.executeAndReturnKey(batchParams).intValue());
                    }
                }
                Map<String, Object> tb = new HashMap<>();
                tb.put("INVENTORY_TRANS_ID", null);
                tb.put("INVENTORY_TRANS_BATCH_INFO_ID", (b.getInventoryTransBatchInfoId() == 0 ? null : b.getInventoryTransBatchInfoId()));
                tb.put("PARENT_ID", id);
                tb.put("BATCH_ID", b.getBatch().getBatchId());
                tb.put("ACTUAL_QTY", b.getActualQty());
                tb.put("ADJUSTMENT_QTY", b.getAdjustmentQty());
                insertBatchList.add(new MapSqlParameterSource(tb));
            }
            id++;
        }

        SqlParameterSource[] insertInventory = new SqlParameterSource[insertList.size()];
        sqlString = " INSERT INTO tmp_inventory (ID, INVENTORY_ID, REGION_ID, REALM_COUNTRY_PLANNING_UNIT_ID, INVENTORY_DATE, ACTUAL_QTY, ADJUSTMENT_QTY, DATA_SOURCE_ID, NOTES, ACTIVE) VALUES (:ID, :INVENTORY_ID, :REGION_ID, :REALM_COUNTRY_PLANNING_UNIT_ID, :INVENTORY_DATE, :ACTUAL_QTY, :ADJUSTMENT_QTY, :DATA_SOURCE_ID, :NOTES, :ACTIVE)";
        this.namedParameterJdbcTemplate.batchUpdate(sqlString, insertList.toArray(insertInventory));
        if (insertBatchList.size() > 0) {
            SqlParameterSource[] insertInventoryBatch = new SqlParameterSource[insertBatchList.size()];
            sqlString = "INSERT INTO tmp_inventory_batch_info (PARENT_ID, INVENTORY_TRANS_ID, INVENTORY_TRANS_BATCH_INFO_ID, BATCH_ID, ACTUAL_QTY, ADJUSTMENT_QTY) VALUES (:PARENT_ID, :INVENTORY_TRANS_ID, :INVENTORY_TRANS_BATCH_INFO_ID, :BATCH_ID, :ACTUAL_QTY, :ADJUSTMENT_QTY)";
            this.namedParameterJdbcTemplate.batchUpdate(sqlString, insertBatchList.toArray(insertInventoryBatch));
        }
        params.clear();
        sqlString = "UPDATE tmp_inventory_batch_info tibi LEFT JOIN rm_inventory_trans_batch_info itbi ON tibi.INVENTORY_TRANS_BATCH_INFO_ID=itbi.INVENTORY_TRANS_BATCH_INFO_ID SET tibi.INVENTORY_TRANS_ID=itbi.INVENTORY_TRANS_ID WHERE tibi.INVENTORY_TRANS_BATCH_INFO_ID IS NOT NULL";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        params.clear();
        // Update the VersionId's in tmp_inventory with the ones from inventory_trans based on VersionId
//        sqlString = "UPDATE tmp_inventory ti LEFT JOIN rm_inventory i ON ti.INVENTORY_ID=i.INVENTORY_ID SET ti.VERSION_ID=i.MAX_VERSION_ID WHERE ti.INVENTORY_ID IS NOT NULL";
//        this.namedParameterJdbcTemplate.update(sqlString, params);
        // Flag the rows for changed records
        sqlString = "UPDATE tmp_inventory ti LEFT JOIN rm_inventory i ON ti.INVENTORY_ID=i.INVENTORY_ID LEFT JOIN rm_inventory_trans it ON ti.INVENTORY_ID=it.INVENTORY_ID AND ti.VERSION_ID=it.VERSION_ID SET ti.CHANGED=1 WHERE ti.REGION_ID!=it.REGION_ID OR ti.REALM_COUNTRY_PLANNING_UNIT_ID!=it.REALM_COUNTRY_PLANNING_UNIT_ID OR ti.INVENTORY_DATE!=it.INVENTORY_DATE OR ti.ACTUAL_QTY!=it.ACTUAL_QTY OR ti.ADJUSTMENT_QTY!=it.ADJUSTMENT_QTY OR ti.DATA_SOURCE_ID!=it.DATA_SOURCE_ID OR ti.NOTES!=it.NOTES OR ti.ACTIVE!=it.ACTIVE OR ti.INVENTORY_ID IS NULL";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "UPDATE tmp_inventory_batch_info tibi LEFT JOIN rm_inventory_trans_batch_info itbi ON tibi.INVENTORY_TRANS_BATCH_INFO_ID=itbi.INVENTORY_TRANS_BATCH_INFO_ID SET `CHANGED`=1 WHERE tibi.INVENTORY_TRANS_BATCH_INFO_ID IS NULL OR tibi.BATCH_ID!=itbi.BATCH_ID OR tibi.ACTUAL_QTY!=itbi.ACTUAL_QTY OR tibi.ADJUSTMENT_QTY!=itbi.ADJUSTMENT_QTY";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "UPDATE tmp_inventory ti LEFT JOIN tmp_inventory_batch_info tibi ON ti.ID = tibi.PARENT_ID SET ti.CHANGED=1 WHERE tibi.CHANGED=1";
        this.namedParameterJdbcTemplate.update(sqlString, params);

        // Check if there are any rows that need to be added
        params.clear();
        sqlString = "SELECT COUNT(*) FROM tmp_inventory ti WHERE ti.CHANGED=1";
        int inventoryRows = this.namedParameterJdbcTemplate.queryForObject(sqlString, params, Integer.class);
        if (inventoryRows > 0) {
            if (version == null) {
                params.put("programId", programData.getProgramId());
                params.put("curUser", curUser.getUserId());
                params.put("curDate", curDate);
                params.put("versionTypeId", programData.getVersionType().getId());
                params.put("versionStatusId", programData.getVersionStatus().getId());
                params.put("notes", programData.getNotes());
                sqlString = "CALL getVersionId(:programId, :versionTypeId, :versionStatusId, :notes, :curUser, :curDate)";
                version = this.namedParameterJdbcTemplate.queryForObject(sqlString, params, new VersionRowMapper());
            }
            params.put("programId", programData.getProgramId());
            params.put("curUser", curUser.getUserId());
            params.put("curDate", curDate);
            params.put("versionId", version.getVersionId());
            // Insert the rows where Inventory Id is not null
            sqlString = "INSERT INTO rm_inventory_trans SELECT null, ti.INVENTORY_ID, ti.INVENTORY_DATE, ti.REGION_ID, ti.REALM_COUNTRY_PLANNING_UNIT_ID, ti.ACTUAL_QTY, ti.ADJUSTMENT_QTY, ti.DATA_SOURCE_ID, ti.NOTES, ti.ACTIVE, :curUser, :curDate, :versionId"
                    + " FROM tmp_inventory ti "
                    + " WHERE ti.CHANGED=1 AND ti.INVENTORY_ID!=0";
            inventoryRows = this.namedParameterJdbcTemplate.update(sqlString, params);
            params.clear();
            params.put("versionId", version.getVersionId());
            // Update the rm_inventory table with the latest versionId
            sqlString = "UPDATE tmp_inventory ti LEFT JOIN rm_inventory i ON i.INVENTORY_ID=ti.INVENTORY_ID SET i.MAX_VERSION_ID=:versionId WHERE ti.INVENTORY_ID IS NOT NULL AND ti.CHANGED=1";
            this.namedParameterJdbcTemplate.update(sqlString, params);
            // Insert into rm_inventory_trans_batch_info where the inventory record was already existing but has changed
            sqlString = "INSERT INTO rm_inventory_trans_batch_info SELECT null, it.INVENTORY_TRANS_ID, tibi.BATCH_ID, tibi.ACTUAL_QTY, tibi.ADJUSTMENT_QTY from tmp_inventory ti left join tmp_inventory_batch_info tibi ON tibi.PARENT_ID=ti.ID LEFT JOIN rm_inventory_trans it ON ti.INVENTORY_ID=it.INVENTORY_ID AND it.VERSION_ID=:versionId WHERE ti.CHANGED=1 AND ti.INVENTORY_ID IS NOT NULL AND tibi.PARENT_ID IS NOT NULL";
            this.namedParameterJdbcTemplate.update(sqlString, params);

            sqlString = "SELECT ti.ID FROM tmp_inventory ti WHERE ti.INVENTORY_ID IS NULL OR ti.INVENTORY_ID=0";
            List<Integer> idListForInsert = this.namedParameterJdbcTemplate.queryForList(sqlString, params, Integer.class);
            params.put("id", 0);
            params.put("versionId", version.getVersionId());
            params.put("programId", programData.getProgramId());
            params.put("curUser", curUser.getUserId());
            params.put("curDate", curDate);
            for (Integer tmpId : idListForInsert) {
                sqlString = "INSERT INTO rm_inventory (PROGRAM_ID, CREATED_BY, CREATED_DATE, LAST_MODIFIED_BY, LAST_MODIFIED_DATE, MAX_VERSION_ID) VALUES (:programId, :curUser, :curDate, :curUser, :curDate, :versionId)";
                consumptionRows += this.namedParameterJdbcTemplate.update(sqlString, params);
                params.replace("id", tmpId);
                sqlString = "INSERT INTO rm_inventory_trans SELECT null, LAST_INSERT_ID(), ti.INVENTORY_DATE, ti.REGION_ID, ti.REALM_COUNTRY_PLANNING_UNIT_ID, ti.ACTUAL_QTY, ti.ADJUSTMENT_QTY, ti.DATA_SOURCE_ID, ti.NOTES, ti.ACTIVE, :curUser, :curDate, :versionId FROM tmp_inventory ti WHERE ti.ID=:id";
                this.namedParameterJdbcTemplate.update(sqlString, params);
                sqlString = "INSERT INTO rm_inventory_trans_batch_info SELECT null, LAST_INSERT_ID(), tibi.BATCH_ID, tibi.ACTUAL_QTY, tibi.ADJUSTMENT_QTY from tmp_inventory_batch_info tibi WHERE tibi.PARENT_ID=:id";
                this.namedParameterJdbcTemplate.update(sqlString, params);
            }
        }

        // ###########################  Inventory  ############################################
        // ###########################  Shipment  #############################################
        params.clear();
        sqlString = "DROP TEMPORARY TABLE IF EXISTS `tmp_shipment`";
//        sqlString = "DROP TABLE IF EXISTS `tmp_shipment`";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "CREATE TEMPORARY TABLE `tmp_shipment` ( "
                //        sqlString = "CREATE TABLE `tmp_shipment` ( "
                + "  `ID` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT, "
                + "  `SHIPMENT_ID` INT(10) UNSIGNED NULL, "
                + "  `PARENT_SHIPMENT_ID` INT(10) UNSIGNED NULL, "
                + "  `SUGGESTED_QTY` INT(10) UNSIGNED NULL, "
                + "  `PROCUREMENT_AGENT_ID` INT(10) UNSIGNED NULL, "
                + "  `FUNDING_SOURCE_ID` INT(10) UNSIGNED NULL, "
                + "  `BUDGET_ID` INT(10) UNSIGNED NULL, "
                + "  `ACCOUNT_FLAG` TINYINT(1) UNSIGNED NULL, "
                + "  `ERP_FLAG` TINYINT(1) UNSIGNED NULL, "
                + "  `CURRENCY_ID` INT(10) UNSIGNED NULL, "
                + "  `CONVERSION_RATE_TO_USD` DECIMAL(12,2) UNSIGNED NULL, "
                + "  `EMERGENCY_ORDER` TINYINT(1) UNSIGNED NOT NULL, "
                + "  `PLANNING_UNIT_ID` INT(10) UNSIGNED NOT NULL, "
                + "  `EXPECTED_DELIVERY_DATE` DATE NOT NULL, "
                + "  `PROCUREMENT_UNIT_ID` INT(10) UNSIGNED NULL, "
                + "  `SUPPLIER_ID` INT(10) UNSIGNED NULL, "
                + "  `SHIPMENT_QTY` INT(10) UNSIGNED NULL, "
                + "  `RATE` DECIMAL(12,2) NOT NULL, "
                + "  `PRODUCT_COST` DECIMAL(12,2) UNSIGNED NOT NULL, "
                + "  `SHIPMENT_MODE` VARCHAR(4) NOT NULL, "
                + "  `FREIGHT_COST` DECIMAL(12,2) UNSIGNED NOT NULL, "
                + "  `PLANNED_DATE` DATE NULL, "
                + "  `SUBMITTED_DATE` DATE NULL, "
                + "  `APPROVED_DATE` DATE NULL, "
                + "  `SHIPPED_DATE` DATE NULL, "
                + "  `ARRIVED_DATE` DATE NULL, "
                + "  `RECEIVED_DATE` DATE NULL, "
                + "  `SHIPMENT_STATUS_ID` INT(10) UNSIGNED NOT NULL, "
                + "  `DATA_SOURCE_ID` INT(10) UNSIGNED NOT NULL, "
                + "  `NOTES` TEXT NULL, "
                + "  `ORDER_NO` VARCHAR(15) NULL, "
                + "  `PRIME_LINE_NO` VARCHAR(10) NULL, "
                + "  `ACTIVE` TINYINT(1) UNSIGNED NOT NULL DEFAULT 1, "
                + "  `VERSION_ID` INT(10) NULL, "
                + "  `CHANGED` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0, "
                + "  PRIMARY KEY (`ID`), "
                + "  INDEX `fk_tmp_shipment_1_idx` (`SHIPMENT_ID` ASC), "
                + "  INDEX `fk_tmp_shipment_2_idx` (`PLANNING_UNIT_ID` ASC), "
                + "  INDEX `fk_tmp_shipment_3_idx` (`PROCUREMENT_UNIT_ID` ASC), "
                + "  INDEX `fk_tmp_shipment_4_idx` (`SUPPLIER_ID` ASC), "
                + "  INDEX `fk_tmp_shipment_5_idx` (`SHIPMENT_STATUS_ID` ASC), "
                + "  INDEX `fk_tmp_shipment_6_idx` (`ORDER_NO` ASC), "
                + "  INDEX `fk_tmp_shipment_7_idx` (`PRIME_LINE_NO` ASC), "
                + "  INDEX `fk_tmp_shipment_8_idx` (`DATA_SOURCE_ID` ASC), "
                + "  INDEX `fk_tmp_shipment_9_idx` (`VERSION_ID` ASC),"
                + "  INDEX `fk_tmp_shipment_10_idx` (`PROCUREMENT_AGENT_ID` ASC), "
                + "  INDEX `fk_tmp_shipment_11_idx` (`FUNDING_SOURCE_ID` ASC), "
                + "  INDEX `fk_tmp_shipment_12_idx` (`BUDGET_ID` ASC) )";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "DROP TEMPORARY TABLE IF EXISTS `tmp_shipment_batch_info`";
//        sqlString = "DROP TABLE IF EXISTS `tmp_shipment_batch_info`";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "CREATE TEMPORARY TABLE `tmp_shipment_batch_info` ( "
                //        sqlString = "CREATE TABLE `tmp_shipment_batch_info` ( "
                + "  `ID` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT, "
                + "  `PARENT_ID` INT(10) UNSIGNED NOT NULL, "
                + "  `SHIPMENT_TRANS_BATCH_INFO_ID` INT(10) UNSIGNED NULL, "
                + "  `SHIPMENT_TRANS_ID` INT(10) UNSIGNED NULL, "
                + "  `BATCH_ID` INT(10) NOT NULL, "
                + "  `BATCH_SHIPMENT_QTY` INT(10) UNSIGNED NOT NULL, "
                + "  `CHANGED` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0, "
                + "  PRIMARY KEY (`ID`), "
                + "  INDEX `fk_tmp_consumption_1_idx` (`SHIPMENT_TRANS_ID` ASC), "
                + "  INDEX `fk_tmp_consumption_2_idx` (`SHIPMENT_TRANS_BATCH_INFO_ID` ASC), "
                + "  INDEX `fk_tmp_consumption_3_idx` (`BATCH_ID` ASC))";
        this.namedParameterJdbcTemplate.update(sqlString, params);

        insertList.clear();
        insertBatchList.clear();
        id = 1;
        for (Shipment s : programData.getShipmentList()) {
            Map<String, Object> tp = new HashMap<>();
            tp.put("ID", id);
            tp.put("SHIPMENT_ID", (s.getShipmentId() == 0 ? null : s.getShipmentId()));
            tp.put("PARENT_SHIPMENT_ID", s.getParentShipmentId());
            tp.put("SUGGESTED_QTY", s.getSuggestedQty());
            tp.put("PROCUREMENT_AGENT_ID", (s.getProcurementAgent() == null || s.getProcurementAgent().getId() == null || s.getProcurementAgent().getId() == 0 ? null : s.getProcurementAgent().getId()));
            tp.put("FUNDING_SOURCE_ID", (s.getFundingSource() == null || s.getFundingSource().getId() == null || s.getFundingSource().getId() == 0 ? null : s.getFundingSource().getId()));
            tp.put("BUDGET_ID", (s.getBudget() == null || s.getBudget().getId() == null || s.getBudget().getId() == 0 ? null : s.getBudget().getId()));
            tp.put("ACCOUNT_FLAG", s.isAccountFlag());
            tp.put("ERP_FLAG", s.isErpFlag());
            tp.put("CURRENCY_ID", s.getCurrency().getCurrencyId());
            tp.put("CONVERSION_RATE_TO_USD", s.getCurrency().getConversionRateToUsd());
            tp.put("EMERGENCY_ORDER", s.isEmergencyOrder());
            tp.put("PLANNING_UNIT_ID", s.getPlanningUnit().getId());
            tp.put("EXPECTED_DELIVERY_DATE", s.getExpectedDeliveryDate());
            tp.put("PROCUREMENT_UNIT_ID", (s.getProcurementUnit() == null || s.getProcurementUnit().getId() == null || s.getProcurementUnit().getId() == 0 ? null : s.getProcurementUnit().getId()));
            tp.put("SUPPLIER_ID", (s.getSupplier() == null || s.getSupplier().getId() == null || s.getSupplier().getId() == 0 ? null : s.getSupplier().getId()));
            tp.put("SHIPMENT_QTY", s.getShipmentQty());
            tp.put("RATE", s.getRate());
            tp.put("PRODUCT_COST", s.getProductCost());
            tp.put("SHIPMENT_MODE", s.getShipmentMode());
            tp.put("FREIGHT_COST", s.getFreightCost());
            tp.put("PLANNED_DATE", s.getPlannedDate());
            tp.put("SUBMITTED_DATE", s.getSubmittedDate());
            tp.put("APPROVED_DATE", s.getApprovedDate());
            tp.put("SHIPPED_DATE", s.getShippedDate());
            tp.put("ARRIVED_DATE", s.getArrivedDate());
            tp.put("RECEIVED_DATE", s.getReceivedDate());
            tp.put("SHIPMENT_STATUS_ID", s.getShipmentStatus().getId());
            tp.put("DATA_SOURCE_ID", s.getDataSource().getId());
            tp.put("NOTES", s.getNotes());
            tp.put("ORDER_NO", s.getOrderNo());
            tp.put("PRIME_LINE_NO", s.getPrimeLineNo());
            tp.put("ACTIVE", s.isActive());
            insertList.add(new MapSqlParameterSource(tp));
            SimpleJdbcInsert batchInsert = new SimpleJdbcInsert(dataSource).withTableName("rm_batch_info").usingGeneratedKeyColumns("BATCH_ID");
            for (ShipmentBatchInfo b : s.getBatchInfoList()) {
                if (b.getBatch().getBatchId() == 0) {
                    Map<String, Object> batchParams = new HashMap<>();
                    batchParams.put("BATCH_NO", b.getBatch().getBatchNo());
                    batchParams.put("PROGRAM_ID", programData.getProgramId());
                    batchParams.put("PLANNING_UNIT_ID", s.getPlanningUnit().getId());
                    batchParams.put("EXPIRY_DATE", b.getBatch().getExpiryDate());
                    batchParams.put("AUTO_GENERATED", b.getBatch().isAutoGenerated());
                    batchParams.put("CREATED_DATE", curDate);
                    try {
                        b.getBatch().setBatchId(this.namedParameterJdbcTemplate.queryForObject("SELECT bi.BATCH_ID FROM rm_batch_info bi WHERE bi.BATCH_NO=:BATCH_NO AND bi.PROGRAM_ID=:PROGRAM_ID AND bi.EXPIRY_DATE=:EXPIRY_DATE", batchParams, Integer.class));
                    } catch (DataAccessException d) {
                        b.getBatch().setBatchId(batchInsert.executeAndReturnKey(batchParams).intValue());
                    }
                }
                Map<String, Object> tb = new HashMap<>();
                tb.put("SHIPMENT_TRANS_ID", null);
                tb.put("SHIPMENT_TRANS_BATCH_INFO_ID", (b.getShipmentTransBatchInfoId() == 0 ? null : b.getShipmentTransBatchInfoId()));
                tb.put("PARENT_ID", id);
                tb.put("BATCH_ID", b.getBatch().getBatchId());
                tb.put("BATCH_SHIPMENT_QTY", b.getShipmentQty());
                insertBatchList.add(new MapSqlParameterSource(tb));
            }
//            for (ShipmentBudget sb : s.getShipmentBudgetList()) {
//                Map<String, Object> tsb = new HashMap<>();
//                tsb.put("PARENT_ID", id);
//                tsb.put("SHIPMENT_BUDGET_ID", (sb.getShipmentBudgetId() == 0 ? null : sb.getShipmentBudgetId()));
//                tsb.put("BUDGET_ID", sb.getBudget().getId());
//                tsb.put("BUDGET_AMT", sb.getBudgetAmt());
//                tsb.put("CONVERSION_RATE_TO_USD", sb.getConversionRateToUsd());
//                tsb.put("CURRENCY_ID", sb.getCurrency().getCurrencyId());
//                tsb.put("ACTIVE", sb.isActive());
//                insertBudgetList.add(new MapSqlParameterSource(tsb));
//            }
            id++;
        }

        SqlParameterSource[] insertShipment = new SqlParameterSource[insertList.size()];
        sqlString = " INSERT INTO tmp_shipment (`ID`, `SHIPMENT_ID`, `PARENT_SHIPMENT_ID`, `SUGGESTED_QTY`, `PROCUREMENT_AGENT_ID`, `ACCOUNT_FLAG`, "
                + "`ERP_FLAG`, `CURRENCY_ID`, `CONVERSION_RATE_TO_USD`, `EMERGENCY_ORDER`, `PLANNING_UNIT_ID`, "
                + "`EXPECTED_DELIVERY_DATE`, `PROCUREMENT_UNIT_ID`, `SUPPLIER_ID`, `SHIPMENT_QTY`, `RATE`, "
                + "`PRODUCT_COST`, `SHIPMENT_MODE`, `FREIGHT_COST`, `PLANNED_DATE`, `SUBMITTED_DATE`, "
                + "`APPROVED_DATE`, `SHIPPED_DATE`, `ARRIVED_DATE`, `RECEIVED_DATE`, `SHIPMENT_STATUS_ID`, "
                + "`DATA_SOURCE_ID`, `NOTES`, `ORDER_NO`, `PRIME_LINE_NO`, `ACTIVE`, "
                + "`FUNDING_SOURCE_ID`, `BUDGET_ID`) VALUES ("
                + ":ID, :SHIPMENT_ID, :PARENT_SHIPMENT_ID, :SUGGESTED_QTY, :PROCUREMENT_AGENT_ID, :ACCOUNT_FLAG, "
                + ":ERP_FLAG, :CURRENCY_ID, :CONVERSION_RATE_TO_USD, :EMERGENCY_ORDER, :PLANNING_UNIT_ID, "
                + ":EXPECTED_DELIVERY_DATE, :PROCUREMENT_UNIT_ID, :SUPPLIER_ID, :SHIPMENT_QTY, :RATE, "
                + ":PRODUCT_COST, :SHIPMENT_MODE, :FREIGHT_COST, :PLANNED_DATE, :SUBMITTED_DATE, "
                + ":APPROVED_DATE, :SHIPPED_DATE, :ARRIVED_DATE, :RECEIVED_DATE, :SHIPMENT_STATUS_ID, "
                + ":DATA_SOURCE_ID, :NOTES, :ORDER_NO, :PRIME_LINE_NO, :ACTIVE, "
                + ":FUNDING_SOURCE_ID, :BUDGET_ID)";
        this.namedParameterJdbcTemplate.batchUpdate(sqlString, insertList.toArray(insertShipment));
        if (insertBatchList.size() > 0) {
            SqlParameterSource[] insertShipmentBatch = new SqlParameterSource[insertBatchList.size()];
            sqlString = "INSERT INTO tmp_shipment_batch_info (PARENT_ID, SHIPMENT_TRANS_ID, SHIPMENT_TRANS_BATCH_INFO_ID, BATCH_ID, BATCH_SHIPMENT_QTY) VALUES (:PARENT_ID, :SHIPMENT_TRANS_ID, :SHIPMENT_TRANS_BATCH_INFO_ID, :BATCH_ID, :BATCH_SHIPMENT_QTY)";
            this.namedParameterJdbcTemplate.batchUpdate(sqlString, insertBatchList.toArray(insertShipmentBatch));
        }
        params.clear();
        sqlString = "UPDATE tmp_shipment_batch_info tsbi LEFT JOIN rm_shipment_trans_batch_info stbi ON tsbi.SHIPMENT_TRANS_BATCH_INFO_ID=stbi.SHIPMENT_TRANS_BATCH_INFO_ID SET tsbi.SHIPMENT_TRANS_ID=stbi.SHIPMENT_TRANS_ID WHERE tsbi.SHIPMENT_TRANS_BATCH_INFO_ID IS NOT NULL";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        params.clear();
        // Update the VersionId's in tmp_shipment with the ones from shipment_trans based on VersionId
//        sqlString = "UPDATE tmp_shipment ts LEFT JOIN rm_shipment s ON ts.SHIPMENT_ID=s.SHIPMENT_ID SET ts.VERSION_ID=s.MAX_VERSION_ID WHERE ts.SHIPMENT_ID IS NOT NULL";
//        this.namedParameterJdbcTemplate.update(sqlString, params);
        // Flag the rows for changed records
        sqlString = "UPDATE tmp_shipment ts LEFT JOIN rm_shipment s ON ts.SHIPMENT_ID=s.SHIPMENT_ID LEFT JOIN rm_shipment_trans st ON ts.SHIPMENT_ID=st.SHIPMENT_ID AND ts.VERSION_ID=st.VERSION_ID "
                + "SET ts.CHANGED=1 WHERE "
                + "ts.SHIPMENT_ID!=st.SHIPMENT_ID OR "
                + "ts.SUGGESTED_QTY!=s.SUGGESTED_QTY OR "
                + "ts.CURRENCY_ID!=s.CURRENCY_ID OR "
                + "ts.PARENT_SHIPMENT_ID!=s.PARENT_SHIPMENT_ID OR "
                + "ts.PROCUREMENT_AGENT_ID!=st.PROCUREMENT_AGENT_ID OR "
                + "ts.FUNDING_SOURCE_ID!=st.FUNDING_SOURCE_ID OR "
                + "ts.BUDGET_ID!=st.BUDGET_ID OR "
                + "ts.ACCOUNT_FLAG!=st.ACCOUNT_FLAG OR "
                + "ts.ERP_FLAG!=st.ERP_FLAG OR "
                + "ts.CONVERSION_RATE_TO_USD!=s.CONVERSION_RATE_TO_USD OR "
                + "ts.EMERGENCY_ORDER!=st.EMERGENCY_ORDER OR "
                + "ts.PLANNING_UNIT_ID!=st.PLANNING_UNIT_ID OR "
                + "ts.EXPECTED_DELIVERY_DATE!=st.EXPECTED_DELIVERY_DATE OR "
                + "ts.PROCUREMENT_UNIT_ID!=st.PROCUREMENT_UNIT_ID OR "
                + "ts.SUPPLIER_ID!=st.SUPPLIER_ID OR "
                + "ts.SHIPMENT_QTY!=st.SHIPMENT_QTY OR "
                + "ts.RATE!=st.RATE OR "
                + "ts.PRODUCT_COST!=st.PRODUCT_COST OR "
                + "ts.SHIPMENT_MODE!=st.SHIPMENT_MODE OR "
                + "ts.FREIGHT_COST!=st.FREIGHT_COST OR "
                + "ts.PLANNED_DATE!=st.PLANNED_DATE OR "
                + "ts.SUBMITTED_DATE!=st.SUBMITTED_DATE OR "
                + "ts.APPROVED_DATE!=st.APPROVED_DATE OR "
                + "ts.SHIPPED_DATE!=st.SHIPPED_DATE OR "
                + "ts.ARRIVED_DATE!=st.ARRIVED_DATE OR "
                + "ts.RECEIVED_DATE!=st.RECEIVED_DATE OR "
                + "ts.SHIPMENT_STATUS_ID!=st.SHIPMENT_STATUS_ID OR "
                + "ts.DATA_SOURCE_ID!=st.DATA_SOURCE_ID OR "
                + "ts.NOTES!=st.NOTES OR "
                + "ts.ORDER_NO!=st.ORDER_NO OR "
                + "ts.PRIME_LINE_NO!=st.PRIME_LINE_NO OR "
                + "ts.ACTIVE!=st.ACTIVE OR "
                + "ts.SHIPMENT_ID IS NULL";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "UPDATE tmp_shipment_batch_info tsbi LEFT JOIN rm_shipment_trans_batch_info stbi ON tsbi.SHIPMENT_TRANS_BATCH_INFO_ID=stbi.SHIPMENT_TRANS_BATCH_INFO_ID SET `CHANGED`=1 WHERE tsbi.SHIPMENT_TRANS_BATCH_INFO_ID IS NULL OR tsbi.BATCH_ID!=stbi.BATCH_ID OR tsbi.BATCH_SHIPMENT_QTY!=stbi.BATCH_SHIPMENT_QTY";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "UPDATE tmp_shipment ts LEFT JOIN tmp_shipment_batch_info tsbi ON ts.ID = tsbi.PARENT_ID SET ts.CHANGED=1 WHERE tsbi.CHANGED=1";
        this.namedParameterJdbcTemplate.update(sqlString, params);

        // Check if there are any rows that need to be added
        params.clear();
        sqlString = "SELECT COUNT(*) FROM tmp_shipment ts WHERE ts.CHANGED=1";
        int shipmentRows = this.namedParameterJdbcTemplate.queryForObject(sqlString, params, Integer.class);
        if (shipmentRows > 0) {
            if (version == null) {
                params.put("programId", programData.getProgramId());
                params.put("curUser", curUser.getUserId());
                params.put("curDate", curDate);
                params.put("versionTypeId", programData.getVersionType().getId());
                params.put("versionStatusId", programData.getVersionStatus().getId());
                params.put("notes", programData.getNotes());
                sqlString = "CALL getVersionId(:programId, :versionTypeId, :versionStatusId, :notes, :curUser, :curDate)";
                version = this.namedParameterJdbcTemplate.queryForObject(sqlString, params, new VersionRowMapper());
            }
            params.put("versionId", version.getVersionId());
            // Insert the rows where Shipment Id is not null
            sqlString = "UPDATE tmp_shipment ts LEFT JOIN rm_shipment s ON ts.SHIPMENT_ID=s.SHIPMENT_ID SET "
                    + "s.SUGGESTED_QTY=ts.SUGGESTED_QTY, "
                    //                    + "s.PROCUREMENT_AGENT_ID=ts.PROCUREMENT_AGENT_ID, "
                    //                    + "s.ACCOUNT_FLAG=ts.ACCOUNT_FLAG, "
                    //                    + "s.ERP_FLAG=ts.ERP_FLAG, "
                    + "s.CURRENCY_ID=ts.CURRENCY_ID, "
                    + "s.CONVERSION_RATE_TO_USD=ts.CONVERSION_RATE_TO_USD, "
                    //                    + "s.EMERGENCY_ORDER=ts.EMERGENCY_ORDER, "
                    + "s.MAX_VERSION_ID=:versionId "
                    + "WHERE ts.SHIPMENT_ID IS NOT NULL AND ts.CHANGED=1";
            shipmentRows = this.namedParameterJdbcTemplate.update(sqlString, params);
            params.put("curUser", curUser.getUserId());
            params.put("curDate", curDate);
            sqlString = "INSERT INTO rm_shipment_trans ("
                    + "SHIPMENT_ID, PLANNING_UNIT_ID, EXPECTED_DELIVERY_DATE, PROCUREMENT_UNIT_ID, SUPPLIER_ID, "
                    + "SHIPMENT_QTY, RATE, PRODUCT_COST, SHIPMENT_MODE, FREIGHT_COST, "
                    + "PLANNED_DATE, SUBMITTED_DATE, APPROVED_DATE, SHIPPED_DATE, ARRIVED_DATE, "
                    + "RECEIVED_DATE, SHIPMENT_STATUS_ID, DATA_SOURCE_ID, NOTES, ORDER_NO, "
                    + "PRIME_LINE_NO, ACTIVE, LAST_MODIFIED_BY, LAST_MODIFIED_DATE, VERSION_ID, "
                    + "PROCUREMENT_AGENT_ID, FUNDING_SOURCE_ID, BUDGET_ID, ACCOUNT_FLAG, ERP_FLAG, "
                    + "EMERGENCY_ORDER) "
                    + "SELECT "
                    + "ts.SHIPMENT_ID, ts.PLANNING_UNIT_ID, ts.EXPECTED_DELIVERY_DATE, IF(ts.PROCUREMENT_UNIT_ID=0,null,ts.PROCUREMENT_UNIT_ID), IF(ts.SUPPLIER_ID=0,null,ts.SUPPLIER_ID), "
                    + "ts.SHIPMENT_QTY, ts.RATE, ts.PRODUCT_COST, ts.SHIPMENT_MODE, ts.FREIGHT_COST, "
                    + "ts.PLANNED_DATE, ts.SUBMITTED_DATE, ts.APPROVED_DATE, ts.SHIPPED_DATE, ts.ARRIVED_DATE, "
                    + "ts.RECEIVED_DATE, ts.SHIPMENT_STATUS_ID, ts.DATA_SOURCE_ID, ts.NOTES, ts.ORDER_NO, "
                    + "ts.PRIME_LINE_NO, ts.ACTIVE, :curUser, :curDate, :versionId, "
                    + "ts.PROCUREMENT_AGENT_ID, ts.FUNDING_SOURCE_ID, ts.BUDGET_ID, ts.ACCOUNT_FLAG, ts.ERP_FLAG, "
                    + "ts.EMERGENCY_ORDER FROM tmp_shipment ts WHERE ts.CHANGED=1 AND ts.SHIPMENT_ID IS NOT NULL";
            shipmentRows = this.namedParameterJdbcTemplate.update(sqlString, params);
            params.clear();
            params.put("versionId", version.getVersionId());

            // Insert into rm_shipment_trans_batch_info where the shipment record was already existing but has changed
            sqlString = "INSERT INTO rm_shipment_trans_batch_info SELECT null, st.SHIPMENT_TRANS_ID, tsbi.BATCH_ID, tsbi.BATCH_SHIPMENT_QTY FROM tmp_shipment ts left join tmp_shipment_batch_info tsbi ON tsbi.PARENT_ID=ts.ID LEFT JOIN rm_shipment_trans st ON ts.SHIPMENT_ID=st.SHIPMENT_ID AND st.VERSION_ID=:versionId WHERE ts.SHIPMENT_ID IS NOT NULL AND ts.CHANGED=1 AND tsbi.PARENT_ID IS NOT NULL";
            this.namedParameterJdbcTemplate.update(sqlString, params);
            params.clear();

            sqlString = "SELECT ts.ID FROM tmp_shipment ts WHERE ts.SHIPMENT_ID IS NULL OR ts.SHIPMENT_ID=0";
            List<Integer> idListForInsert = this.namedParameterJdbcTemplate.queryForList(sqlString, params, Integer.class);
            params.put("id", 0);
            params.put("versionId", version.getVersionId());
            params.put("programId", programData.getProgramId());
            params.put("curUser", curUser.getUserId());
            params.put("curDate", curDate);
            for (Integer tmpId : idListForInsert) {
                sqlString = "INSERT INTO rm_shipment ("
                        + "PROGRAM_ID, SUGGESTED_QTY, CURRENCY_ID, CONVERSION_RATE_TO_USD, CREATED_BY, "
                        + "CREATED_DATE, LAST_MODIFIED_BY, LAST_MODIFIED_DATE, MAX_VERSION_ID) SELECT "
                        + ":programId, ts.SUGGESTED_QTY, ts.CURRENCY_ID, ts.CONVERSION_RATE_TO_USD, :curUser, "
                        + ":curDate, :curUser, :curDate, :versionId FROM tmp_shipment ts WHERE ts.ID=:id";
                params.replace("id", tmpId);
                consumptionRows += this.namedParameterJdbcTemplate.update(sqlString, params);
                sqlString = "SELECT LAST_INSERT_ID()";
                int shipmentId = this.namedParameterJdbcTemplate.queryForObject(sqlString, params, Integer.class);
                params.put("shipmentId", shipmentId);
                sqlString = "INSERT INTO rm_shipment_trans ("
                        + "SHIPMENT_ID, PLANNING_UNIT_ID, EXPECTED_DELIVERY_DATE, PROCUREMENT_UNIT_ID, SUPPLIER_ID, "
                        + "SHIPMENT_QTY, RATE, PRODUCT_COST, SHIPMENT_MODE, FREIGHT_COST, "
                        + "PLANNED_DATE, SUBMITTED_DATE, APPROVED_DATE, SHIPPED_DATE, ARRIVED_DATE, "
                        + "RECEIVED_DATE, SHIPMENT_STATUS_ID, DATA_SOURCE_ID, NOTES, ORDER_NO, "
                        + "PRIME_LINE_NO, ACTIVE, LAST_MODIFIED_BY, LAST_MODIFIED_DATE, VERSION_ID, "
                        + "PROCUREMENT_AGENT_ID, FUNDING_SOURCE_ID, BUDGET_ID, ACCOUNT_FLAG, ERP_FLAG, "
                        + "EMERGENCY_ORDER) SELECT "
                        + ":shipmentId, ts.PLANNING_UNIT_ID, ts.EXPECTED_DELIVERY_DATE, IF(ts.PROCUREMENT_UNIT_ID=0,null,ts.PROCUREMENT_UNIT_ID), IF(ts.SUPPLIER_ID=0,null,ts.SUPPLIER_ID), "
                        + "ts.SHIPMENT_QTY, ts.RATE, ts.PRODUCT_COST, ts.SHIPMENT_MODE, ts.FREIGHT_COST, "
                        + "ts.PLANNED_DATE, ts.SUBMITTED_DATE, ts.APPROVED_DATE, ts.SHIPPED_DATE, ts.ARRIVED_DATE, "
                        + "ts.RECEIVED_DATE, ts.SHIPMENT_STATUS_ID, ts.DATA_SOURCE_ID, ts.NOTES, ts.ORDER_NO, "
                        + "ts.PRIME_LINE_NO, ts.ACTIVE, :curUser, :curDate, :versionId, "
                        + "ts.PROCUREMENT_AGENT_ID, ts.FUNDING_SOURCE_ID, ts.BUDGET_ID, ts.ACCOUNT_FLAG, ts.ERP_FLAG, "
                        + "ts.EMERGENCY_ORDER "
                        + "FROM tmp_shipment ts WHERE ts.ID=:id";
                this.namedParameterJdbcTemplate.update(sqlString, params);
                sqlString = "INSERT INTO rm_shipment_trans_batch_info (SHIPMENT_TRANS_ID, BATCH_ID, BATCH_SHIPMENT_QTY) SELECT LAST_INSERT_ID(), tsbi.BATCH_ID, tsbi.BATCH_SHIPMENT_QTY from tmp_shipment_batch_info tsbi WHERE tsbi.PARENT_ID=:id";
                this.namedParameterJdbcTemplate.update(sqlString, params);
            }
        }
        // ###########################  Shipment  ############################################

        // #########################  Problem Report #########################################
        insertList.clear();
        insertBatchList.clear();
        id = 1;
        for (ProblemReport pr : programData.getProblemReportList()) {
            Map<String, Object> tp = new HashMap<>();
            tp.put("PROBLEM_REPORT_ID", (pr.getProblemReportId() == 0 ? null : pr.getProblemReportId()));
            tp.put("REALM_PROBLEM_ID", pr.getRealmProblem().getRealmProblemId());
            tp.put("PROGRAM_ID", pr.getProgram().getId());
            tp.put("VERSION_ID", (version == null ? pr.getVersionId() : version.getVersionId()));
            tp.put("PROBLEM_TYPE_ID", pr.getProblemType().getId());
            tp.put("PROBLEM_STATUS_ID", pr.getProblemStatus().getId());
            tp.put("DATA1", pr.getDt()); // Dt
            tp.put("DATA2", pr.getRegion().getId()); // RegionId
            tp.put("DATA3", pr.getPlanningUnit().getId()); // PlanningUnitId
            tp.put("DATA4", pr.getShipmentId()); // ShipmentId
            tp.put("DATA5", pr.getData5());
            tp.put("CREATED_BY", pr.getCreatedBy().getUserId());
            tp.put("CREATED_DATE", pr.getCreatedDate());
            tp.put("LAST_MODIFIED_BY", pr.getLastModifiedBy().getUserId());
            tp.put("LAST_MODIFIED_DATE", pr.getLastModifiedDate());
            if (pr.getProblemReportId() == 0) {
                this.namedParameterJdbcTemplate.update("INSERT INTO `rm_problem_report` (`REALM_PROBLEM_ID`, `PROGRAM_ID`, `VERSION_ID`, `PROBLEM_TYPE_ID`, `PROBLEM_STATUS_ID`, `DATA1`, `DATA2`, `DATA3`, `DATA4`, `DATA5`, `CREATED_BY`, `CREATED_DATE`, `LAST_MODIFIED_BY`, `LAST_MODIFIED_DATE`) VALUES (:REALM_PROBLEM_ID, :PROGRAM_ID, :VERSION_ID, :PROBLEM_TYPE_ID, :PROBLEM_STATUS_ID, :DATA1, :DATA2, :DATA3, :DATA4, :DATA5, :CREATED_BY, :CREATED_DATE, :LAST_MODIFIED_BY, :LAST_MODIFIED_DATE)", tp);
                sqlString = "SELECT LAST_INSERT_ID()";
                pr.setProblemReportId(this.jdbcTemplate.queryForObject(sqlString, Integer.class));
            } else {
                sqlString = "UPDATE rm_problem_report pr SET pr.PROBLEM_STATUS_ID=:PROBLEM_STATUS_ID, pr.LAST_MODIFIED_BY=:LAST_MODIFIED_BY, pr.LAST_MODIFIED_DATE=:LAST_MODIFIED_DATE WHERE pr.PROBLEM_REPORT_ID=:PROBLEM_REPORT_ID";
                this.namedParameterJdbcTemplate.update(sqlString, tp);
            }
            for (ProblemReportTrans prt : pr.getProblemTransList()) {
                if (prt.getProblemReportTransId() == 0) {
                    Map<String, Object> transParams = new HashMap<>();
                    transParams.put("PROBLEM_REPORT_ID", pr.getProblemReportId());
                    transParams.put("PROBLEM_STATUS_ID", prt.getProblemStatus().getId());
                    transParams.put("NOTES", prt.getNotes());
                    transParams.put("CREATED_BY", prt.getCreatedBy().getUserId());
                    transParams.put("CREATED_DATE", prt.getCreatedDate());
                    this.namedParameterJdbcTemplate.update("INSERT INTO `rm_problem_report_trans` (`PROBLEM_REPORT_ID`, `PROBLEM_STATUS_ID`, `NOTES`, `CREATED_BY`, `CREATED_DATE`) VALUES (:PROBLEM_REPORT_ID, :PROBLEM_STATUS_ID, :NOTES, :CREATED_BY, :CREATED_DATE)", transParams);
                }
            }
        }
        // #########################  Problem Report #########################################
        sqlString = "DROP TEMPORARY TABLE IF EXISTS `tmp_consumption`";
        this.namedParameterJdbcTemplate.update(sqlString, params);

        sqlString = "DROP TEMPORARY TABLE IF EXISTS `tmp_consumption_trans_batch_info`";
        this.namedParameterJdbcTemplate.update(sqlString, params);

        sqlString = "DROP TEMPORARY TABLE IF EXISTS `tmp_inventory`";
        this.namedParameterJdbcTemplate.update(sqlString, params);

        sqlString = "DROP TEMPORARY TABLE IF EXISTS `tmp_inventory_trans_batch_info`";
        this.namedParameterJdbcTemplate.update(sqlString, params);

        sqlString = "DROP TEMPORARY TABLE IF EXISTS `tmp_shipment`";
        this.namedParameterJdbcTemplate.update(sqlString, params);

        sqlString = "DROP TEMPORARY TABLE IF EXISTS `tmp_shipment_trans_batch_info`";
        this.namedParameterJdbcTemplate.update(sqlString, params);

        if (version == null) {
            return new Version(0, null, null, null, null, null, null, null);
        } else {
            return version;
        }
    }

    @Override
    public List<SimpleObject> getVersionTypeList() {
        String sqlString = "SELECT vt.VERSION_TYPE_ID `ID`, vtl.LABEL_ID, vtl.LABEL_EN, vtl.LABEL_FR, vtl.LABEL_SP, vtl.LABEL_PR  FROM ap_version_type vt LEFT JOIN ap_label vtl ON vt.LABEL_ID=vtl.LABEL_ID";
        return this.namedParameterJdbcTemplate.query(sqlString, new SimpleObjectRowMapper());
    }

    @Override
    public List<SimpleObject> getVersionStatusList() {
        String sqlString = "SELECT vs.VERSION_STATUS_ID `ID`, vsl.LABEL_ID, vsl.LABEL_EN, vsl.LABEL_FR, vsl.LABEL_SP, vsl.LABEL_PR  FROM ap_version_status vs LEFT JOIN ap_label vsl ON vs.LABEL_ID=vsl.LABEL_ID";
        return this.namedParameterJdbcTemplate.query(sqlString, new SimpleObjectRowMapper());
    }

    @Override
    public List<Batch> getBatchList(int programId, int versionId
    ) {
        String sqlString = "SELECT bi.BATCH_ID, bi.BATCH_NO, bi.PROGRAM_ID, bi.PLANNING_UNIT_ID `BATCH_PLANNING_UNIT_ID`, bi.`AUTO_GENERATED`, bi.EXPIRY_DATE, bi.CREATED_DATE FROM rm_batch_info bi WHERE bi.PROGRAM_ID=:programId";
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        return this.namedParameterJdbcTemplate.query(sqlString, params, new BatchRowMapper());
    }

    @Override
    public List<ProgramVersion> getProgramVersionList(int programId, int versionId, int realmCountryId, int healthAreaId, int organisationId, int versionTypeId, int versionStatusId, String startDate,
            String stopDate, CustomUserDetails curUser
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("realmCountryId", realmCountryId);
        params.put("healthAreaId", healthAreaId);
        params.put("organisationId", organisationId);
        params.put("versionId", versionId);
        params.put("versionTypeId", versionTypeId);
        params.put("versionStatusId", versionStatusId);
        params.put("startDate", startDate + " 00:00:00");
        params.put("stopDate", stopDate + " 23:59:59");
        StringBuilder sb = new StringBuilder("SELECT  "
                + "     pv.PROGRAM_VERSION_ID, pv.VERSION_ID, pv.NOTES, cb.USER_ID `CB_USER_ID`, cb.USERNAME `CB_USERNAME`, pv.CREATED_DATE, lmb.USER_ID `LMB_USER_ID`, lmb.USERNAME `LMB_USERNAME`, pv.LAST_MODIFIED_DATE, "
                + "     p.PROGRAM_ID, pl.LABEL_ID `PROGRAM_LABEL_ID`, pl.LABEL_EN `PROGRAM_LABEL_EN`, pl.LABEL_FR `PROGRAM_LABEL_FR`, pl.LABEL_SP `PROGRAM_LABEL_SP`, pl.LABEL_PR `PROGRAM_LABEL_PR`, "
                + "     rc.REALM_COUNTRY_ID, c.COUNTRY_CODE, cl.LABEL_ID `COUNTRY_LABEL_ID`, cl.LABEL_EN `COUNTRY_LABEL_EN`, cl.LABEL_FR `COUNTRY_LABEL_FR`, cl.LABEL_SP `COUNTRY_LABEL_SP`, cl.LABEL_PR `COUNTRY_LABEL_PR`, "
                + "     ha.HEALTH_AREA_ID, ha.HEALTH_AREA_CODE, hal.LABEL_ID `HEALTH_AREA_LABEL_ID`, hal.LABEL_EN `HEALTH_AREA_LABEL_EN`, hal.LABEL_FR `HEALTH_AREA_LABEL_FR`, hal.LABEL_SP `HEALTH_AREA_LABEL_SP`, hal.LABEL_PR `HEALTH_AREA_LABEL_PR`, "
                + "     o.ORGANISATION_ID, o.ORGANISATION_CODE, ol.LABEL_ID `ORGANISATION_LABEL_ID`, ol.LABEL_EN `ORGANISATION_LABEL_EN`, ol.LABEL_FR `ORGANISATION_LABEL_FR`, ol.LABEL_SP `ORGANISATION_LABEL_SP`, ol.LABEL_PR `ORGANISATION_LABEL_PR`, "
                + "     vt.VERSION_TYPE_ID, vtl.LABEL_ID `VERSION_TYPE_LABEL_ID`, vtl.LABEL_EN `VERSION_TYPE_LABEL_EN`, vtl.LABEL_FR `VERSION_TYPE_LABEL_FR`, vtl.LABEL_SP `VERSION_TYPE_LABEL_SP`, vtl.LABEL_PR `VERSION_TYPE_LABEL_PR`, "
                + "     vs.VERSION_STATUS_ID, vsl.LABEL_ID `VERSION_STATUS_LABEL_ID`, vsl.LABEL_EN `VERSION_STATUS_LABEL_EN`, vsl.LABEL_FR `VERSION_STATUS_LABEL_FR`, vsl.LABEL_SP `VERSION_STATUS_LABEL_SP`, vsl.LABEL_PR `VERSION_STATUS_LABEL_PR` "
                + "FROM rm_program_version pv  "
                + "LEFT JOIN rm_program p ON pv.PROGRAM_ID=p.PROGRAM_ID "
                + "LEFT JOIN ap_label pl ON p.LABEL_ID=pl.LABEL_ID "
                + "LEFT JOIN rm_realm_country rc ON p.REALM_COUNTRY_ID=rc.REALM_COUNTRY_ID "
                + "LEFT JOIN ap_country c ON rc.COUNTRY_ID=c.COUNTRY_ID "
                + "LEFT JOIN ap_label cl ON c.LABEL_ID=cl.LABEL_ID "
                + "LEFT JOIN rm_health_area ha ON p.HEALTH_AREA_ID=ha.HEALTH_AREA_ID "
                + "LEFT JOIN ap_label hal ON ha.LABEL_ID=hal.LABEL_ID "
                + "LEFT JOIN rm_organisation o ON p.ORGANISATION_ID=o.ORGANISATION_ID "
                + "LEFT JOIN ap_label ol ON o.LABEL_ID=ol.LABEL_ID "
                + "LEFT JOIN us_user cb ON pv.CREATED_BY=cb.USER_ID "
                + "LEFT JOIN us_user lmb ON pv.LAST_MODIFIED_BY=lmb.USER_ID "
                + "LEFT JOIN ap_version_type vt ON pv.VERSION_TYPE_ID=vt.VERSION_TYPE_ID "
                + "LEFT JOIN ap_label vtl ON vt.LABEL_ID=vtl.LABEL_ID "
                + "LEFT JOIN ap_version_status vs ON pv.VERSION_STATUS_ID=vs.VERSION_STATUS_ID "
                + "LEFT JOIN ap_label vsl ON vs.LABEL_ID=vsl.LABEL_ID "
                + "WHERE TRUE "
                + "AND (:programId = -1 OR p.PROGRAM_ID = :programId ) "
                + "AND (:versionId = -1 OR pv.VERSION_ID = :versionId ) "
                + "AND (:realmCountryId = -1 OR p.REALM_COUNTRY_ID= :realmCountryId ) "
                + "AND (:healthAreaId = -1 OR p.HEALTH_AREA_ID = :healthAreaId ) "
                + "AND (:organisationId = -1 OR p.ORGANISATION_ID = :organisationId) "
                + "AND (:versionTypeId = -1 OR pv.VERSION_TYPE_ID = :versionTypeId) "
                + "AND (:versionStatusId = -1 OR pv.VERSION_STATUS_ID = :versionStatusId) "
                + "AND pv.CREATED_DATE BETWEEN :startDate AND :stopDate ");
        this.aclService.addFullAclForProgram(sb, params, "p", curUser);
        return this.namedParameterJdbcTemplate.query(sb.toString(), params, new ProgramVersionRowMapper());

    }

    @Override
    public List<ProgramVersion> getProgramVersionForARTMIS(int realmId
    ) {
        String sql = "SELECT  "
                + " pv.PROGRAM_VERSION_ID, pv.VERSION_ID, pv.NOTES, cb.USER_ID `CB_USER_ID`, cb.USERNAME `CB_USERNAME`, pv.CREATED_DATE, lmb.USER_ID `LMB_USER_ID`, lmb.USERNAME `LMB_USERNAME`, pv.LAST_MODIFIED_DATE, "
                + " p.PROGRAM_ID, pl.LABEL_ID `PROGRAM_LABEL_ID`, pl.LABEL_EN `PROGRAM_LABEL_EN`, pl.LABEL_FR `PROGRAM_LABEL_FR`, pl.LABEL_SP `PROGRAM_LABEL_SP`, pl.LABEL_PR `PROGRAM_LABEL_PR`, "
                + " rc.REALM_COUNTRY_ID, c.COUNTRY_CODE, cl.LABEL_ID `COUNTRY_LABEL_ID`, cl.LABEL_EN `COUNTRY_LABEL_EN`, cl.LABEL_FR `COUNTRY_LABEL_FR`, cl.LABEL_SP `COUNTRY_LABEL_SP`, cl.LABEL_PR `COUNTRY_LABEL_PR`, "
                + " ha.HEALTH_AREA_ID, ha.HEALTH_AREA_CODE, hal.LABEL_ID `HEALTH_AREA_LABEL_ID`, hal.LABEL_EN `HEALTH_AREA_LABEL_EN`, hal.LABEL_FR `HEALTH_AREA_LABEL_FR`, hal.LABEL_SP `HEALTH_AREA_LABEL_SP`, hal.LABEL_PR `HEALTH_AREA_LABEL_PR`, "
                + " o.ORGANISATION_ID, o.ORGANISATION_CODE, ol.LABEL_ID `ORGANISATION_LABEL_ID`, ol.LABEL_EN `ORGANISATION_LABEL_EN`, ol.LABEL_FR `ORGANISATION_LABEL_FR`, ol.LABEL_SP `ORGANISATION_LABEL_SP`, ol.LABEL_PR `ORGANISATION_LABEL_PR`, "
                + " vt.VERSION_TYPE_ID, vtl.LABEL_ID `VERSION_TYPE_LABEL_ID`, vtl.LABEL_EN `VERSION_TYPE_LABEL_EN`, vtl.LABEL_FR `VERSION_TYPE_LABEL_FR`, vtl.LABEL_SP `VERSION_TYPE_LABEL_SP`, vtl.LABEL_PR `VERSION_TYPE_LABEL_PR`, "
                + " vs.VERSION_STATUS_ID, vsl.LABEL_ID `VERSION_STATUS_LABEL_ID`, vsl.LABEL_EN `VERSION_STATUS_LABEL_EN`, vsl.LABEL_FR `VERSION_STATUS_LABEL_FR`, vsl.LABEL_SP `VERSION_STATUS_LABEL_SP`, vsl.LABEL_PR `VERSION_STATUS_LABEL_PR` "
                + " FROM rm_program_version pv  "
                + " LEFT JOIN rm_program p ON pv.PROGRAM_ID=p.PROGRAM_ID "
                + " LEFT JOIN ap_label pl ON p.LABEL_ID=pl.LABEL_ID "
                + " LEFT JOIN rm_realm_country rc ON p.REALM_COUNTRY_ID=rc.REALM_COUNTRY_ID "
                + " LEFT JOIN rm_realm realm ON realm.`REALM_ID`=rc.`REALM_ID` "
                + " LEFT JOIN ap_country c ON rc.COUNTRY_ID=c.COUNTRY_ID  "
                + " LEFT JOIN ap_label cl ON c.LABEL_ID=cl.LABEL_ID  "
                + " LEFT JOIN rm_health_area ha ON p.HEALTH_AREA_ID=ha.HEALTH_AREA_ID "
                + " LEFT JOIN ap_label hal ON ha.LABEL_ID=hal.LABEL_ID "
                + " LEFT JOIN rm_organisation o ON p.ORGANISATION_ID=o.ORGANISATION_ID "
                + " LEFT JOIN ap_label ol ON o.LABEL_ID=ol.LABEL_ID "
                + " LEFT JOIN us_user cb ON pv.CREATED_BY=cb.USER_ID "
                + " LEFT JOIN us_user lmb ON pv.LAST_MODIFIED_BY=lmb.USER_ID "
                + " LEFT JOIN ap_version_type vt ON pv.VERSION_TYPE_ID=vt.VERSION_TYPE_ID "
                + " LEFT JOIN ap_label vtl ON vt.LABEL_ID=vtl.LABEL_ID "
                + " LEFT JOIN ap_version_status vs ON pv.VERSION_STATUS_ID=vs.VERSION_STATUS_ID "
                + " LEFT JOIN ap_label vsl ON vs.LABEL_ID=vsl.LABEL_ID "
                + " WHERE TRUE AND rc.`REALM_ID`=? AND pv.`SENT_TO_ARTMIS`=0 AND pv.`VERSION_TYPE_ID`=2 AND pv.`VERSION_STATUS_ID`=2;";
        return this.jdbcTemplate.query(sql, new ProgramVersionRowMapper(), realmId);
    }

    @Override
    public Version updateProgramVersion(int programId, int versionId, int versionStatusId, String notes,
            CustomUserDetails curUser
    ) {
        String sqlString = "UPDATE rm_program_version pv SET pv.VERSION_STATUS_ID=:versionStatusId,pv.NOTES=:notes, pv.LAST_MODIFIED_DATE=:curDate, pv.LAST_MODIFIED_BY=:curUser WHERE pv.PROGRAM_ID=:programId AND pv.VERSION_ID=:versionId";
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("versionId", versionId);
        params.put("versionStatusId", versionStatusId);
        params.put("notes", notes);
        params.put("curUser", curUser.getUserId());
        params.put("curDate", DateUtils.getCurrentDateObject(DateUtils.EST));
        this.namedParameterJdbcTemplate.update(sqlString, params);
        String sql = "INSERT INTO rm_program_version_trans SELECT NULL,pv.PROGRAM_VERSION_ID,pv.VERSION_TYPE_ID,?,?,?,? FROM  rm_program_version pv  "
                + "WHERE pv.`PROGRAM_ID`=? AND pv.`VERSION_ID`=? ";
        this.jdbcTemplate.update(sql, versionStatusId, notes, curUser.getUserId(), DateUtils.getCurrentDateObject(DateUtils.EST), programId, versionId);
        return this.getVersionInfo(programId, versionId);
    }

    /**
     *
     * @param orderNo
     * @param primeLineNo
     * @param realmCountryId
     * @param planningUnitId
     * @return 0-Okay to go ahead link 1- Order not found, 2- Already linked
     * 3-Order not for this Country, 4-Order not for this Planning Unit
     *
     */
    @Override
    public int checkErpOrder(String orderNo, String primeLineNo,
            int realmCountryId, int planningUnitId
    ) {
        String sqlString = "SELECT IF(steop.SHIPMENT_TRANS_ERP_ORDER_ID IS NOT NULL, 2, IF(c1.REALM_COUNTRY_ID!=:realmCountryId, 3, IF (papu.PLANNING_UNIT_ID!=:planningUnitId, 4, 0))) `REASON` "
                + "FROM rm_erp_order eo "
                + "LEFT JOIN rm_shipment_trans_erp_order_mapping steop ON eo.ERP_ORDER_ID=steop.ERP_ORDER_ID "
                + "LEFT JOIN (SELECT rc.REALM_COUNTRY_ID, cl.LABEL_EN, c.COUNTRY_CODE FROM rm_realm_country rc LEFT JOIN ap_country c ON rc.COUNTRY_ID=c.COUNTRY_ID LEFT JOIN ap_label cl ON c.LABEL_ID=cl.LABEL_ID) c1 ON c1.LABEL_EN=eo.RECPIENT_COUNTRY "
                + "LEFT JOIN rm_procurement_agent_planning_unit papu ON eo.PLANNING_UNIT_SKU_CODE=papu.SKU_CODE AND papu.PROCUREMENT_AGENT_ID=1 "
                + "WHERE eo.ORDER_NO=:orderNo AND eo.PRIME_LINE_NO=:primeLineNo";

        Map<String, Object> params = new HashMap<>();
        params.put("orderNo", orderNo);
        params.put("primeLineNo", primeLineNo);
        params.put("realmCountryId", realmCountryId);
        params.put("planningUnitId", planningUnitId);
        try {
            return this.namedParameterJdbcTemplate.queryForObject(sqlString, params, Integer.class
            );
        } catch (DataAccessException de) {
            return 1; // Order not found
        }
    }

//    @Override
//    public void buildStockBalances(int programId, int versionId) {
//        String sqlString = "CALL buildSupplyPlan(:programId, :versionId)";
//        Map<String, Object> params = new HashMap<>();
//        params.put("programId", programId);
//        params.put("versionId", versionId);
//        this.namedParameterJdbcTemplate.update(sqlString, params);
//        sqlString = "SELECT spbi.TRANS_DATE, SUM(spbi.FORECASTED_CONSUMPTION_QTY+spbi.EXPIRED_CONSUMPTION) CONSUMPTION FROM rm_supply_plan_batch_info spbi WHERE spbi.PROGRAM_ID=:progarmId AND spbi.VERSION_ID=:versionId GROUP BY spbi.TRANS_DATE HAVING SUM(spbi.FORECASTED_CONSUMPTION_QTY)>0 OR SUM(spbi.EXPIRED_CONSUMPTION)>0";
//        List<UnaccountedConsumption> ucList = this.namedParameterJdbcTemplate.query(sqlString, new UnaccountedConsumptionRowMapper());
//        for (UnaccountedConsumption u : ucList) {
//
//        }
//    }
    @Override
    public List<SimplifiedSupplyPlan> updateSupplyPlanBatchInfo(SupplyPlan sp) {
        String sqlString = "UPDATE rm_supply_plan_batch_info spbi "
                + "SET "
                + "spbi.EXPIRED_STOCK=:expired, spbi.CALCULATED_CONSUMPTION=:calculatedConsumption, "
                + "spbi.FINAL_OPENING_BALANCE=:openingBalance, spbi.FINAL_CLOSING_BALANCE=:closingBalance, "
                + "spbi.UNMET_DEMAND=:unmetDemand, "
                + "spbi.EXPIRED_STOCK_WPS=:expiredWps, spbi.CALCULATED_CONSUMPTION_WPS=:calculatedConsumptionWps, "
                + "spbi.FINAL_OPENING_BALANCE_WPS=:openingBalanceWps, spbi.FINAL_CLOSING_BALANCE_WPS=:closingBalanceWps, "
                + "spbi.UNMET_DEMAND_WPS=:unmetDemandWps "
                + "WHERE spbi.SUPPLY_PLAN_BATCH_INFO_ID=:supplyPlanBatchInfoId";
        List<SqlParameterSource> batchParams = new ArrayList<>();
        for (SupplyPlanDate sd : sp.getSupplyPlanDateList()) {
            for (SupplyPlanBatchInfo sbi : sd.getBatchList()) {
                if (sbi.hasData()) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("expired", sbi.getExpiredStock());
                    params.put("calculatedConsumption", sbi.getCalculatedConsumption());
                    params.put("openingBalance", sbi.getOpeningBalance());
                    params.put("closingBalance", sbi.getClosingBalance());
                    params.put("unmetDemand", sbi.getUnmetDemand());
                    params.put("expiredWps", sbi.getExpiredStockWps());
                    params.put("calculatedConsumptionWps", sbi.getCalculatedConsumptionWps());
                    params.put("openingBalanceWps", sbi.getOpeningBalanceWps());
                    params.put("closingBalanceWps", sbi.getClosingBalanceWps());
                    params.put("unmetDemandWps", sbi.getUnmetDemandWps());
                    params.put("supplyPlanBatchInfoId", sbi.getSupplyPlanId());
                    batchParams.add(new MapSqlParameterSource(params));
                }
            }
        }
        SqlParameterSource[] updateParams = new SqlParameterSource[batchParams.size()];
        this.namedParameterJdbcTemplate.batchUpdate(sqlString, batchParams.toArray(updateParams));
        Map<String, Object> params = new HashMap<>();
        params.put("programId", sp.getProgramId());
        params.put("versionId", sp.getVersionId());
//        this.namedParameterJdbcTemplate.update("DELETE spbi.* FROM rm_supply_plan_batch_info spbi WHERE spbi.PROGRAM_ID=:programId AND !(spbi.SHIPMENT_QTY!=0 OR spbi.FORECASTED_CONSUMPTION_QTY!=0 OR spbi.ACTUAL_CONSUMPTION_QTY!=0 OR spbi.ADJUSTMENT_MULTIPLIED_QTY!=0 OR spbi.FINAL_OPENING_BALANCE!=0 OR spbi.FINAL_CLOSING_BALANCE!=0 OR spbi.FINAL_OPENING_BALANCE_WPS!=0 OR spbi.FINAL_CLOSING_BALANCE_WPS!=0 OR spbi.BATCH_ID=0)", params);
        sqlString = "DELETE spa.* FROM rm_supply_plan_amc spa WHERE spa.PROGRAM_ID=:programId AND spa.VERSION_ID=:versionId";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "INSERT INTO `rm_supply_plan_amc` ( "
                + "    `PROGRAM_ID`, `VERSION_ID`, `PLANNING_UNIT_ID`, `TRANS_DATE`,  "
                + "    `OPENING_BALANCE`, `OPENING_BALANCE_WPS`,  "
                + "    `MANUAL_PLANNED_SHIPMENT_QTY`, `MANUAL_SUBMITTED_SHIPMENT_QTY`, `MANUAL_APPROVED_SHIPMENT_QTY`, `MANUAL_SHIPPED_SHIPMENT_QTY`, `MANUAL_RECEIVED_SHIPMENT_QTY`, `MANUAL_ONHOLD_SHIPMENT_QTY`, "
                + "    `ERP_PLANNED_SHIPMENT_QTY`, `ERP_SUBMITTED_SHIPMENT_QTY`, `ERP_APPROVED_SHIPMENT_QTY`, `ERP_SHIPPED_SHIPMENT_QTY`, `ERP_RECEIVED_SHIPMENT_QTY`, `ERP_ONHOLD_SHIPMENT_QTY`, "
                + "     `SHIPMENT_QTY`, `FORECASTED_CONSUMPTION_QTY`, `ACTUAL_CONSUMPTION_QTY`, `ACTUAL`,  "
                + "    `ADJUSTMENT_MULTIPLIED_QTY`, `STOCK_MULTIPLIED_QTY`, `EXPIRED_STOCK`, `EXPIRED_STOCK_WPS`,  "
                + "    `CLOSING_BALANCE`, `CLOSING_BALANCE_WPS`,  "
                + "    `UNMET_DEMAND`, `UNMET_DEMAND_WPS`)  "
                + "SELECT  "
                + "    spbi.PROGRAM_ID, spbi.VERSION_ID, spbi.PLANNING_UNIT_ID, spbi.TRANS_DATE,  "
                + "    SUM(spbi.FINAL_OPENING_BALANCE), SUM(spbi.FINAL_OPENING_BALANCE_WPS), "
                + "    SUM(spbi.MANUAL_PLANNED_SHIPMENT_QTY), SUM(spbi.MANUAL_SUBMITTED_SHIPMENT_QTY), SUM(spbi.MANUAL_APPROVED_SHIPMENT_QTY), SUM(spbi.MANUAL_SHIPPED_SHIPMENT_QTY), SUM(spbi.MANUAL_RECEIVED_SHIPMENT_QTY), SUM(spbi.MANUAL_ONHOLD_SHIPMENT_QTY), "
                + "    SUM(spbi.ERP_PLANNED_SHIPMENT_QTY), SUM(spbi.ERP_SUBMITTED_SHIPMENT_QTY), SUM(spbi.ERP_APPROVED_SHIPMENT_QTY), SUM(spbi.ERP_SHIPPED_SHIPMENT_QTY), SUM(spbi.ERP_RECEIVED_SHIPMENT_QTY), SUM(spbi.ERP_ONHOLD_SHIPMENT_QTY), "
                + "     SUM(spbi.SHIPMENT_QTY), SUM(spbi.FORECASTED_CONSUMPTION_QTY), SUM(spbi.ACTUAL_CONSUMPTION_QTY), BIT_OR(spbi.ACTUAL), "
                + "    SUM(spbi.ADJUSTMENT_MULTIPLIED_QTY), SUM(spbi.STOCK_MULTIPLIED_QTY), SUM(spbi.EXPIRED_STOCK), SUM(spbi.EXPIRED_STOCK_WPS), "
                + "    SUM(spbi.FINAL_CLOSING_BALANCE), SUM(spbi.FINAL_CLOSING_BALANCE_WPS), "
                + "    SUM(spbi.UNMET_DEMAND), SUM(spbi.UNMET_DEMAND_WPS) "
                + "FROM fasp.rm_supply_plan_batch_info spbi WHERE spbi.PROGRAM_ID=:programId AND spbi.VERSION_ID=:versionId group by spbi.PLANNING_UNIT_ID, spbi.TRANS_DATE";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "UPDATE rm_supply_plan_amc spa SET spa.ACTUAL = null, spa.FORECASTED_CONSUMPTION_QTY = null WHERE spa.ACTUAL=0 AND spa.FORECASTED_CONSUMPTION_QTY =0 AND spa.PROGRAM_ID=:programId AND spa.VERSION_ID=:versionId";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        sqlString = "UPDATE rm_supply_plan_amc spa  "
                + "LEFT JOIN rm_program_planning_unit ppu ON spa.PROGRAM_ID=ppu.PROGRAM_ID AND spa.PLANNING_UNIT_ID=ppu.PLANNING_UNIT_ID "
                + "LEFT JOIN rm_program p ON spa.PROGRAM_ID=p.PROGRAM_ID "
                + "LEFT JOIN rm_realm_country rc ON p.REALM_COUNTRY_ID=rc.REALM_COUNTRY_ID "
                + "LEFT JOIN rm_realm r ON rc.REALM_ID=r.REALM_ID "
                + "LEFT JOIN ( "
                + "    SELECT spa.PROGRAM_ID, spa.VERSION_ID, spa.PLANNING_UNIT_ID, spa.TRANS_DATE, ppu.MONTHS_IN_PAST_FOR_AMC, ppu.MONTHS_IN_FUTURE_FOR_AMC, SUBDATE(spa.TRANS_DATE, INTERVAL ppu.MONTHS_IN_PAST_FOR_AMC MONTH), ADDDATE(spa.TRANS_DATE, INTERVAL ppu.MONTHS_IN_FUTURE_FOR_AMC-1 MONTH), "
                + "        SUM(IF(spa2.ACTUAL, spa2.ACTUAL_CONSUMPTION_QTY,spa2.FORECASTED_CONSUMPTION_QTY)) AMC_SUM, "
                + "        AVG(IF(spa2.ACTUAL, spa2.ACTUAL_CONSUMPTION_QTY,spa2.FORECASTED_CONSUMPTION_QTY)) AMC, COUNT(IF(spa2.ACTUAL, spa2.ACTUAL_CONSUMPTION_QTY,spa2.FORECASTED_CONSUMPTION_QTY)) AMC_COUNT "
                + "    FROM rm_supply_plan_amc spa  "
                + "    LEFT JOIN rm_program_planning_unit ppu ON spa.PLANNING_UNIT_ID=ppu.PLANNING_UNIT_ID AND spa.PROGRAM_ID=ppu.PROGRAM_ID "
                + "    LEFT JOIN rm_supply_plan_amc spa2 ON  "
                + "        spa.PROGRAM_ID=spa2.PROGRAM_ID  "
                + "        AND spa.VERSION_ID=spa2.VERSION_ID "
                + "        AND spa.PLANNING_UNIT_ID=spa2.PLANNING_UNIT_ID  "
                + "        AND spa2.TRANS_DATE BETWEEN SUBDATE(spa.TRANS_DATE, INTERVAL ppu.MONTHS_IN_PAST_FOR_AMC MONTH) AND ADDDATE(spa.TRANS_DATE, INTERVAL ppu.MONTHS_IN_FUTURE_FOR_AMC-1 MONTH) "
                + "    WHERE spa.PROGRAM_ID=@programId AND spa.VERSION_ID=@versionId "
                + "GROUP BY spa.PLANNING_UNIT_ID, spa.TRANS_DATE) amc ON spa.PROGRAM_ID=amc.PROGRAM_ID AND spa.VERSION_ID=amc.VERSION_ID AND spa.PLANNING_UNIT_ID=amc.PLANNING_UNIT_ID AND spa.TRANS_DATE=amc.TRANS_DATE "
                + "SET  "
                + "    spa.AMC=amc.AMC,  "
                + "    spa.AMC_COUNT=amc.AMC_COUNT,  "
                + "    spa.MOS=IF(amc.AMC IS NULL OR amc.AMC=0, null, spa.CLOSING_BALANCE_WPS/amc.AMC), "
                + "    spa.MIN_STOCK_MOS = IF(ppu.MIN_MONTHS_OF_STOCK<r.MIN_MOS_MIN_GAURDRAIL, r.MIN_MOS_MIN_GAURDRAIL, IF(ppu.MIN_MONTHS_OF_STOCK>r.MIN_MOS_MAX_GAURDRAIL, r.MIN_MOS_MAX_GAURDRAIL, ppu.MIN_MONTHS_OF_STOCK)), "
                + "    spa.MAX_STOCK_MOS = IF(ppu.MIN_MONTHS_OF_STOCK+ppu.REORDER_FREQUENCY_IN_MONTHS>r.MAX_MOS_MAX_GAURDRAIL, r.MAX_MOS_MAX_GAURDRAIL, ppu.MIN_MONTHS_OF_STOCK+ppu.REORDER_FREQUENCY_IN_MONTHS), "
                + "    spa.MIN_STOCK_QTY = IF(ppu.MIN_MONTHS_OF_STOCK<r.MIN_MOS_MIN_GAURDRAIL, r.MIN_MOS_MIN_GAURDRAIL, IF(ppu.MIN_MONTHS_OF_STOCK>r.MIN_MOS_MAX_GAURDRAIL, r.MIN_MOS_MAX_GAURDRAIL, ppu.MIN_MONTHS_OF_STOCK)) * amc.AMC, "
                + "    spa.MAX_STOCK_QTY = IF(ppu.MIN_MONTHS_OF_STOCK+ppu.REORDER_FREQUENCY_IN_MONTHS>r.MAX_MOS_MAX_GAURDRAIL, r.MAX_MOS_MAX_GAURDRAIL, ppu.MIN_MONTHS_OF_STOCK+ppu.REORDER_FREQUENCY_IN_MONTHS) * amc.AMC "
                + "WHERE spa.PROGRAM_ID=@programId and spa.VERSION_ID=@versionId";
        this.namedParameterJdbcTemplate.update(sqlString, params);
        return this.getSimplifiedSupplyPlan(sp.getProgramId(), sp.getVersionId());
    }

    public List<SimplifiedSupplyPlan> getSimplifiedSupplyPlan(int programId, int versionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("versionId", versionId);
        String sqlString = "SELECT  "
                + "    spa.`PROGRAM_ID`, spa.`VERSION_ID`, "
                + "    spa.`PLANNING_UNIT_ID`, spa.`TRANS_DATE`,  "
                + "    spa.`OPENING_BALANCE`, spa.`OPENING_BALANCE_WPS`, "
                + "    spa.`ACTUAL` `ACTUAL_FLAG`, IF(spa.`ACTUAL`, spa.`ACTUAL_CONSUMPTION_QTY`, spa.`FORECASTED_CONSUMPTION_QTY`) `CONSUMPTION_QTY`, "
                + "    spa.`ADJUSTMENT_MULTIPLIED_QTY`, spa.`STOCK_MULTIPLIED_QTY`, "
                + "    spa.`REGION_COUNT`, spa.`REGION_COUNT_FOR_STOCK`, "
                + "    spa.`MANUAL_PLANNED_SHIPMENT_QTY`, spa.`MANUAL_SUBMITTED_SHIPMENT_QTY`, spa.`MANUAL_APPROVED_SHIPMENT_QTY`, spa.`MANUAL_SHIPPED_SHIPMENT_QTY`, spa.`MANUAL_RECEIVED_SHIPMENT_QTY`, spa.`MANUAL_ONHOLD_SHIPMENT_QTY`, "
                + "    spa.`ERP_PLANNED_SHIPMENT_QTY`, spa.`ERP_SUBMITTED_SHIPMENT_QTY`, spa.`ERP_APPROVED_SHIPMENT_QTY`, spa.`ERP_SHIPPED_SHIPMENT_QTY`, spa.`ERP_RECEIVED_SHIPMENT_QTY`, spa.`ERP_ONHOLD_SHIPMENT_QTY`, "
                + "    spa.`EXPIRED_STOCK`, spa.`EXPIRED_STOCK_WPS`, "
                + "    spa.`NATIONAL_ADJUSTMENT`, spa.`NATIONAL_ADJUSTMENT_WPS`, "
                + "    spa.`CLOSING_BALANCE`, spa.`CLOSING_BALANCE_WPS`, "
                + "    spa.`UNMET_DEMAND`, spa.`UNMET_DEMAND_WPS`, "
                + "    spa.`AMC`, spa.`AMC_COUNT`, spa.`MOS`, spa.`MOS_WPS`, spa.`MIN_STOCK_MOS`, spa.`MIN_STOCK_QTY`, spa.`MAX_STOCK_MOS`, spa.`MAX_STOCK_QTY`, "
                + "    b2.`BATCH_ID`, b2.`BATCH_NO`, b2.`EXPIRY_DATE`, b2.`AUTO_GENERATED`, b2.`BATCH_CLOSING_BALANCE`, b2.`BATCH_CLOSING_BALANCE_WPS`, b2.`BATCH_EXPIRED_STOCK`, b2.`BATCH_EXPIRED_STOCK_WPS` "
                + "FROM rm_supply_plan_amc spa  "
                + "LEFT JOIN (SELECT spbq.`PLANNING_UNIT_ID`, spbq.`TRANS_DATE`, spbq.`BATCH_ID`, bi.`BATCH_NO`, bi.`EXPIRY_DATE`, bi.`AUTO_GENERATED`, SUM(spbq.`CLOSING_BALANCE`) `BATCH_CLOSING_BALANCE`, SUM(spbq.`CLOSING_BALANCE_WPS`) `BATCH_CLOSING_BALANCE_WPS`, SUM(spbq.`EXPIRED_STOCK_WPS`) `BATCH_EXPIRED_STOCK_WPS`, SUM(spbq.`EXPIRED_STOCK`) `BATCH_EXPIRED_STOCK`  FROM rm_supply_plan_batch_qty spbq LEFT JOIN rm_batch_info bi ON spbq.`BATCH_ID`=bi.`BATCH_ID` WHERE spbq.`PROGRAM_ID`=:programId and spbq.`VERSION_ID`=:versionId GROUP by spbq.`PLANNING_UNIT_ID`, spbq.`TRANS_DATE`, spbq.`BATCH_ID`) b2 ON spa.`PLANNING_UNIT_ID`=b2.`PLANNING_UNIT_ID` AND spa.`TRANS_DATE`=b2.`TRANS_DATE` "
                + "WHERE spa.`PROGRAM_ID`=:programId AND spa.`VERSION_ID`=:versionId ";
        return this.namedParameterJdbcTemplate.query(sqlString, params, new SimplifiedSupplyPlanResultSetExtractor());
    }

    @Override
    public SupplyPlan getSupplyPlan(int programId, int versionId) {
        System.out.println("Going to call buildSimpleSupplyPlan SP");
        System.out.println(new Date());
        String sqlString = "CALL buildSimpleSupplyPlan(:programId, :versionId)";
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("versionId", versionId);
        SupplyPlan sp = this.namedParameterJdbcTemplate.query(sqlString, params, new SupplyPlanResultSetExtractor());
        System.out.println("SP call completed");
        System.out.println(new Date());
        System.out.println("Going through the loops to calculate on basis of FEFO");
        System.out.println(new Date());
        System.out.println("PLANNING_UNIT_ID\tBATCH_ID\tTRANS_DATE\tEXPIRY_DATE\tSHIPMENT_QTY\tSHIPMENT_QTY_WPS\tCONSUMPTION\tADJUSTMENT\tEXPIRED\tUNALLOCATED\tCALCULCATED\tOPEN_BAL\tCLOSE_BAL\tUNMET DEMAND\tEXPIRED_WPS\tUNALLOCATED_WPS\tCALCULCATED_WPS\tOPEN_BAL_WPS\tCLOSE_BAL_WPS\tUNMET DEMAND_WPS");
        for (SupplyPlanDate sd : sp.getSupplyPlanDateList()) {
            for (SupplyPlanBatchInfo spbi : sd.getBatchList()) {
                int prevCB = sp.getPrevClosingBalance(sd.getPlanningUnitId(), spbi.getBatchId(), sd.getPrevTransDate());
                spbi.setOpeningBalance(prevCB, sd.getTransDate());
                sd.setUnallocatedConsumption(spbi.updateUnAllocatedCountAndExpiredStock(sd.getTransDate(), sd.getUnallocatedConsumption()));
                int prevCBWps = sp.getPrevClosingBalanceWps(sd.getPlanningUnitId(), spbi.getBatchId(), sd.getPrevTransDate());
                spbi.setOpeningBalanceWps(prevCBWps, sd.getTransDate());
                sd.setUnallocatedConsumptionWps(spbi.updateUnAllocatedCountAndExpiredStockWps(sd.getTransDate(), sd.getUnallocatedConsumptionWps()));
            }
            int unallocatedConsumption = sd.getUnallocatedConsumption();
            int unallocatedConsumptionWps = sd.getUnallocatedConsumptionWps();
            for (SupplyPlanBatchInfo spbi : sd.getBatchList()) {
                sd.setUnallocatedConsumption(unallocatedConsumption);
                sd.setUnallocatedConsumptionWps(unallocatedConsumptionWps);
                unallocatedConsumption = spbi.updateCB(unallocatedConsumption);
                unallocatedConsumptionWps = spbi.updateCBWps(unallocatedConsumptionWps);
                if (spbi.getBatchId() == 0 && unallocatedConsumption > 0) {
                    spbi.setUnmetDemand(unallocatedConsumption);
                    sd.setUnallocatedConsumption(0);
                }
                if (spbi.getBatchId() == 0 && unallocatedConsumptionWps > 0) {
                    spbi.setUnmetDemandWps(unallocatedConsumptionWps);
                    sd.setUnallocatedConsumptionWps(0);
                }
                System.out.println(sd.getPlanningUnitId() + "\t\t" + spbi.getBatchId() + "\t\t" + sd.getTransDate() + "\t\t" + spbi.getExpiryDate() + "\t\t" + spbi.getShipmentQty() + "\t\t" + (spbi.getShipmentQty() - spbi.getManualPlannedShipmentQty()) + "\t\t" + spbi.getConsumption() + "\t\t" + spbi.getAdjustment() + "\t\t" + spbi.getExpiredStock() + "\t\t" + sd.getUnallocatedConsumption() + "\t\t" + spbi.getCalculatedConsumption() + "\t\t" + spbi.getOpeningBalance() + "\t\t" + spbi.getClosingBalance() + "\t\t" + spbi.getUnmetDemand() + "\t\t" + spbi.getExpiredStockWps() + "\t\t" + sd.getUnallocatedConsumptionWps() + "\t\t" + spbi.getCalculatedConsumptionWps() + "\t\t" + spbi.getOpeningBalanceWps() + "\t\t" + spbi.getClosingBalanceWps() + "\t\t" + spbi.getUnmetDemandWps());
            }
        }
        System.out.println("Completed loops");
        System.out.println(new Date());
        return sp;
    }

    @Override
    @Transactional
    public List<SimplifiedSupplyPlan> getNewSupplyPlanList(int programId, int versionId, boolean rebuild) throws ParseException {
        Map<Integer, Integer> newBatchSubstituteMap = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("versionId", versionId);
        if (rebuild == true) {
            MasterSupplyPlan msp = new MasterSupplyPlan(programId, versionId);
            String sqlString = "CALL buildNewSupplyPlanRegion(:programId, :versionId)";

            List<NewSupplyPlan> spList = this.namedParameterJdbcTemplate.query(sqlString, params, new NewSupplyPlanRegionResultSetExtractor());
            sqlString = "CALL buildNewSupplyPlanBatch(:programId, :versionId)";
            msp.setNspList(this.namedParameterJdbcTemplate.query(sqlString, params, new NewSupplyPlanBatchResultSetExtractor(spList)));
            msp.buildPlan();
            // Store the data in rm_supply_plan_amc and rm_supply_plan_batch_info
            List<MapSqlParameterSource> amcParams = new LinkedList<>();
            List<MapSqlParameterSource> batchParams = new LinkedList<>();
            int i = 0;
            for (NewSupplyPlan nsp : msp.getNspList()) {
                MapSqlParameterSource a1 = new MapSqlParameterSource();
                a1.addValue("PROGRAM_ID", msp.getProgramId());
                a1.addValue("VERSION_ID", msp.getVersionId());
                a1.addValue("PLANNING_UNIT_ID", nsp.getPlanningUnitId());
                a1.addValue("TRANS_DATE", nsp.getTransDate());
                a1.addValue("OPENING_BALANCE", nsp.getOpeningBalance());
                a1.addValue("OPENING_BALANCE_WPS", nsp.getOpeningBalanceWps());
                a1.addValue("MANUAL_PLANNED_SHIPMENT_QTY", nsp.getPlannedShipmentsTotalData());
                a1.addValue("MANUAL_SUBMITTED_SHIPMENT_QTY", nsp.getSubmittedShipmentsTotalData());
                a1.addValue("MANUAL_APPROVED_SHIPMENT_QTY", nsp.getApprovedShipmentsTotalData());
                a1.addValue("MANUAL_SHIPPED_SHIPMENT_QTY", nsp.getShippedShipmentsTotalData());
                a1.addValue("MANUAL_RECEIVED_SHIPMENT_QTY", nsp.getReceivedShipmentsTotalData());
                a1.addValue("MANUAL_ONHOLD_SHIPMENT_QTY", nsp.getOnholdShipmentsTotalData());
                a1.addValue("ERP_PLANNED_SHIPMENT_QTY", nsp.getPlannedErpShipmentsTotalData());
                a1.addValue("ERP_SUBMITTED_SHIPMENT_QTY", nsp.getSubmittedErpShipmentsTotalData());
                a1.addValue("ERP_APPROVED_SHIPMENT_QTY", nsp.getApprovedErpShipmentsTotalData());
                a1.addValue("ERP_SHIPPED_SHIPMENT_QTY", nsp.getShippedErpShipmentsTotalData());
                a1.addValue("ERP_RECEIVED_SHIPMENT_QTY", nsp.getReceivedErpShipmentsTotalData());
                a1.addValue("ERP_ONHOLD_SHIPMENT_QTY", nsp.getOnholdErpShipmentsTotalData());
                a1.addValue("SHIPMENT_QTY", nsp.getManualShipmentTotal() + nsp.getErpShipmentTotal());
                a1.addValue("FORECASTED_CONSUMPTION_QTY", nsp.getForecastedConsumptionQty());
                a1.addValue("ACTUAL_CONSUMPTION_QTY", nsp.getActualConsumptionQty());
                a1.addValue("ACTUAL", nsp.isActualConsumptionFlag());
                a1.addValue("ADJUSTMENT_MULTIPLIED_QTY", nsp.getAdjustmentQty());
                a1.addValue("STOCK_MULTIPLIED_QTY", nsp.getStockQty());
                a1.addValue("REGION_COUNT", nsp.getRegionCount());
                a1.addValue("REGION_COUNT_FOR_STOCK", nsp.getRegionCountForStock());
                a1.addValue("NATIONAL_ADJUSTMENT", nsp.getNationalAdjustment());
                a1.addValue("NATIONAL_ADJUSTMENT_WPS", nsp.getNationalAdjustmentWps());
                a1.addValue("EXPIRED_STOCK", nsp.getExpiredStock());
                a1.addValue("EXPIRED_STOCK_WPS", nsp.getExpiredStockWps());
                a1.addValue("CLOSING_BALANCE", nsp.getClosingBalance());
                a1.addValue("CLOSING_BALANCE_WPS", nsp.getClosingBalanceWps());
                a1.addValue("UNMET_DEMAND", nsp.getUnmetDemand());
                a1.addValue("UNMET_DEMAND_WPS", nsp.getUnmetDemandWps());
                amcParams.add(a1);
                for (BatchData bd : nsp.getBatchDataList()) {
                    int batchId = 0;
                    if (bd.getBatchId() < 0) {
                        // This is a new Batch so check if it has just been created if not then create it
                        batchId = newBatchSubstituteMap.getOrDefault(bd.getBatchId(), 0);
                        if (batchId == 0) {
                            Map<String, Object> newBatchParams = new HashMap<>();
                            newBatchParams.put("programId", msp.getProgramId());
                            newBatchParams.put("planningUnitId", nsp.getPlanningUnitId());
                            newBatchParams.put("transDate", nsp.getTransDate());
                            newBatchParams.put("curDate", DateUtils.getCurrentDateObject(DateUtils.EST));
                            String sql = "INSERT INTO `rm_batch_info` SELECT "
                                    + "    null, "
                                    + "    ppu.PROGRAM_ID, "
                                    + "    ppu.PLANNING_UNIT_ID, "
                                    + "    CONCAT(LPAD(ppu.PROGRAM_ID, 6,'0'), LPAD(ppu.PLANNING_UNIT_ID, 8,'0'), date_format(CONCAT(LEFT(ADDDATE(:transDate, INTERVAL ppu.SHELF_LIFE MONTH),7),'-01'), '%y%m%d'), SUBSTRING('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', RAND()*36+1, 1), SUBSTRING('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', RAND()*36+1, 1), SUBSTRING('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', RAND()*36+1, 1)), "
                                    + "    CONCAT(LEFT(ADDDATE(:transDate, INTERVAL ppu.SHELF_LIFE MONTH),7),'-01'), "
                                    + "    :curDate, "
                                    + "    null, 1 FROM rm_program_planning_unit ppu where ppu.PROGRAM_ID=:programId AND ppu.PLANNING_UNIT_ID=:planningUnitId";
                            this.namedParameterJdbcTemplate.update(sql, newBatchParams);
                            batchId = this.jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
                            newBatchSubstituteMap.put(bd.getBatchId(), batchId);
                        }
                        bd.setBatchId(batchId);
                    }
                    MapSqlParameterSource b1 = new MapSqlParameterSource();
                    b1.addValue("PROGRAM_ID", msp.getProgramId());
                    b1.addValue("VERSION_ID", msp.getVersionId());
                    b1.addValue("PLANNING_UNIT_ID", nsp.getPlanningUnitId());
                    b1.addValue("TRANS_DATE", nsp.getTransDate());
                    b1.addValue("BATCH_ID", bd.getBatchId());
                    b1.addValue("EXPIRY_DATE", bd.getExpiryDate());
                    b1.addValue("ACTUAL_CONSUMPTION_QTY", bd.getActualConsumption());
                    b1.addValue("ACTUAL", bd.isUseActualConsumption());
                    b1.addValue("SHIPMENT_QTY", bd.getShipment());
                    b1.addValue("SHIPMENT_QTY_WPS", bd.getShipmentWps());
                    b1.addValue("ADJUSTMENT_MULTIPLIED_QTY", bd.getAdjustment());
                    b1.addValue("STOCK_MULTIPLIED_QTY", bd.getStock());
                    b1.addValue("ALL_REGIONS_REPORTED_STOCK", bd.isAllRegionsReportedStock());
                    b1.addValue("USE_ADJUSTMENT", bd.isUseAdjustment());
                    b1.addValue("OPENING_BALANCE", bd.getOpeningBalance());
                    b1.addValue("OPENING_BALANCE_WPS", bd.getOpeningBalanceWps());
                    b1.addValue("EXPIRED_STOCK", bd.getExpiredStock());
                    b1.addValue("EXPIRED_STOCK_WPS", bd.getExpiredStockWps());
                    b1.addValue("CALCULATED_CONSUMPTION", bd.getCalculatedConsumption());
                    b1.addValue("CALCULATED_CONSUMPTION_WPS", bd.getCalculatedConsumptionWps());
                    b1.addValue("CLOSING_BALANCE", bd.getClosingBalance());
                    b1.addValue("CLOSING_BALANCE_WPS", bd.getClosingBalanceWps());
                    batchParams.add(b1);
                }
                i++;
            }
            this.namedParameterJdbcTemplate.update("DELETE sma.* FROM rm_supply_plan_amc sma WHERE sma.PROGRAM_ID=:programId AND sma.VERSION_ID=:versionId", params);
            SimpleJdbcInsert si = new SimpleJdbcInsert(jdbcTemplate).withTableName("rm_supply_plan_amc");
            MapSqlParameterSource[] amcParamsArray = new MapSqlParameterSource[amcParams.size()];
            amcParams.toArray(amcParamsArray);
            si.executeBatch(amcParamsArray);
            this.namedParameterJdbcTemplate.update("DELETE smq.* FROM rm_supply_plan_batch_qty smq WHERE smq.PROGRAM_ID=:programId AND smq.VERSION_ID=:versionId", params);
            MapSqlParameterSource[] batchParamsArray = new MapSqlParameterSource[batchParams.size()];
            batchParams.toArray(batchParamsArray);
            si = new SimpleJdbcInsert(jdbcTemplate).withTableName("rm_supply_plan_batch_qty");
            si.executeBatch(batchParamsArray);
            sqlString = "UPDATE rm_supply_plan_amc spa  "
                    + "LEFT JOIN rm_program_planning_unit ppu ON spa.PROGRAM_ID=ppu.PROGRAM_ID AND spa.PLANNING_UNIT_ID=ppu.PLANNING_UNIT_ID "
                    + "LEFT JOIN rm_program p ON spa.PROGRAM_ID=p.PROGRAM_ID "
                    + "LEFT JOIN rm_realm_country rc ON p.REALM_COUNTRY_ID=rc.REALM_COUNTRY_ID "
                    + "LEFT JOIN rm_realm r ON rc.REALM_ID=r.REALM_ID "
                    + "LEFT JOIN ( "
                    + "    SELECT spa.PROGRAM_ID, spa.VERSION_ID, spa.PLANNING_UNIT_ID, spa.TRANS_DATE, ppu.MONTHS_IN_PAST_FOR_AMC, ppu.MONTHS_IN_FUTURE_FOR_AMC, SUBDATE(spa.TRANS_DATE, INTERVAL ppu.MONTHS_IN_PAST_FOR_AMC MONTH), ADDDATE(spa.TRANS_DATE, INTERVAL ppu.MONTHS_IN_FUTURE_FOR_AMC-1 MONTH), "
                    + "        SUM(IF(spa2.ACTUAL, spa2.ACTUAL_CONSUMPTION_QTY,spa2.FORECASTED_CONSUMPTION_QTY)) AMC_SUM, "
                    + "        AVG(IF(spa2.ACTUAL, spa2.ACTUAL_CONSUMPTION_QTY,spa2.FORECASTED_CONSUMPTION_QTY)) AMC, COUNT(IF(spa2.ACTUAL, spa2.ACTUAL_CONSUMPTION_QTY,spa2.FORECASTED_CONSUMPTION_QTY)) AMC_COUNT "
                    + "    FROM rm_supply_plan_amc spa  "
                    + "    LEFT JOIN rm_program_planning_unit ppu ON spa.PLANNING_UNIT_ID=ppu.PLANNING_UNIT_ID AND spa.PROGRAM_ID=ppu.PROGRAM_ID "
                    + "    LEFT JOIN rm_supply_plan_amc spa2 ON  "
                    + "        spa.PROGRAM_ID=spa2.PROGRAM_ID  "
                    + "        AND spa.VERSION_ID=spa2.VERSION_ID "
                    + "        AND spa.PLANNING_UNIT_ID=spa2.PLANNING_UNIT_ID  "
                    + "        AND spa2.TRANS_DATE BETWEEN SUBDATE(spa.TRANS_DATE, INTERVAL ppu.MONTHS_IN_PAST_FOR_AMC MONTH) AND ADDDATE(spa.TRANS_DATE, INTERVAL ppu.MONTHS_IN_FUTURE_FOR_AMC-1 MONTH) "
                    + "    WHERE spa.PROGRAM_ID=@programId AND spa.VERSION_ID=@versionId "
                    + "GROUP BY spa.PLANNING_UNIT_ID, spa.TRANS_DATE) amc ON spa.PROGRAM_ID=amc.PROGRAM_ID AND spa.VERSION_ID=amc.VERSION_ID AND spa.PLANNING_UNIT_ID=amc.PLANNING_UNIT_ID AND spa.TRANS_DATE=amc.TRANS_DATE "
                    + "SET  "
                    + "    spa.AMC=amc.AMC,  "
                    + "    spa.AMC_COUNT=amc.AMC_COUNT,  "
                    + "    spa.MOS=IF(amc.AMC IS NULL OR amc.AMC=0, null, spa.CLOSING_BALANCE/amc.AMC), "
                    + "    spa.MOS_WPS=IF(amc.AMC IS NULL OR amc.AMC=0, null, spa.CLOSING_BALANCE_WPS/amc.AMC), "
                    + "    spa.MIN_STOCK_MOS = IF(ppu.MIN_MONTHS_OF_STOCK<r.MIN_MOS_MIN_GAURDRAIL, r.MIN_MOS_MIN_GAURDRAIL, IF(ppu.MIN_MONTHS_OF_STOCK>r.MIN_MOS_MAX_GAURDRAIL, r.MIN_MOS_MAX_GAURDRAIL, ppu.MIN_MONTHS_OF_STOCK)), "
                    + "    spa.MAX_STOCK_MOS = IF(ppu.MIN_MONTHS_OF_STOCK+ppu.REORDER_FREQUENCY_IN_MONTHS>r.MAX_MOS_MAX_GAURDRAIL, r.MAX_MOS_MAX_GAURDRAIL, ppu.MIN_MONTHS_OF_STOCK+ppu.REORDER_FREQUENCY_IN_MONTHS), "
                    + "    spa.MIN_STOCK_QTY = IF(ppu.MIN_MONTHS_OF_STOCK<r.MIN_MOS_MIN_GAURDRAIL, r.MIN_MOS_MIN_GAURDRAIL, IF(ppu.MIN_MONTHS_OF_STOCK>r.MIN_MOS_MAX_GAURDRAIL, r.MIN_MOS_MAX_GAURDRAIL, ppu.MIN_MONTHS_OF_STOCK)) * amc.AMC, "
                    + "    spa.MAX_STOCK_QTY = IF(ppu.MIN_MONTHS_OF_STOCK+ppu.REORDER_FREQUENCY_IN_MONTHS>r.MAX_MOS_MAX_GAURDRAIL, r.MAX_MOS_MAX_GAURDRAIL, ppu.MIN_MONTHS_OF_STOCK+ppu.REORDER_FREQUENCY_IN_MONTHS) * amc.AMC "
                    + "WHERE spa.PROGRAM_ID=@programId and spa.VERSION_ID=@versionId";
            this.namedParameterJdbcTemplate.update(sqlString, params);
//            msp.printSupplyPlan();
        }

        return getSimplifiedSupplyPlan(programId, versionId);
    }

    @Override
    public int updateSentToARTMISFlag(String programVersionIds) {
        String sql = "UPDATE rm_program_version p SET p.`SENT_TO_ARTMIS`=1 WHERE p.`PROGRAM_VERSION_ID` IN (" + programVersionIds + ");";
        return this.jdbcTemplate.update(sql);
    }

    @Override
    public List<Shipment> getShipmentListForSync(int programId, int versionId, String lastSyncDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("versionId", versionId);
        params.put("lastSyncDate", lastSyncDate);
        return this.namedParameterJdbcTemplate.query("CALL getShipmentDataForSync(:programId, :versionId, :lastSyncDate)", params, new ShipmentListResultSetExtractor());
    }

    @Override
    public List<Batch> getBatchListForSync(int programId, int versionId, String lastSyncDate) {
        String sqlString = "SELECT bi.BATCH_ID, bi.BATCH_NO, bi.PROGRAM_ID, bi.PLANNING_UNIT_ID `BATCH_PLANNING_UNIT_ID`, bi.`AUTO_GENERATED`, bi.EXPIRY_DATE, bi.CREATED_DATE FROM rm_batch_info bi WHERE bi.PROGRAM_ID=:programId AND bi.CREATED_DATE > :lastSyncDate";
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("lastSyncDate", lastSyncDate);
        return this.namedParameterJdbcTemplate.query(sqlString, params, new BatchRowMapper());
    }

}
