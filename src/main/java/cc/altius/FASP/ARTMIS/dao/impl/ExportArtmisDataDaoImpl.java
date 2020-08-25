/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.ARTMIS.dao.impl;

import cc.altius.FASP.ARTMIS.dao.ExportArtmisDataDao;
import cc.altius.FASP.model.DTO.ExportOrderDataDTO;
import cc.altius.FASP.model.DTO.ExportProgramDataDTO;
import cc.altius.FASP.model.DTO.rowMapper.ExportOrderDataDTORowMapper;
import cc.altius.FASP.model.DTO.rowMapper.ExportProgramDataDTORowMapper;
import cc.altius.utils.DateUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 *
 * @author altius
 */
@Repository
public class ExportArtmisDataDaoImpl implements ExportArtmisDataDao {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<ExportProgramDataDTO> exportProgramData() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()) + " 00:00:00";
        String today = DateUtils.getCurrentDateString(DateUtils.EST, DateUtils.YMD) + " 23:59:59";

        String sql = "SELECT p.PROGRAM_ID, p.PROGRAM_CODE, p.LABEL_EN `PROGRAM_NAME`, c.COUNTRY_CODE2, ha.LABEL_EN `TECHNICAL_AREA_NAME`,p.`ACTIVE`   "
                + "FRom vw_program p LEFT JOIN rm_realm_country rc ON p.REALM_COUNTRY_ID=rc.REALM_COUNTRY_ID "
                + "LEFT JOIN vw_country c ON rc.COUNTRY_ID=c.COUNTRY_ID "
                + "LEFT JOIN vw_health_area ha ON p.HEALTH_AREA_ID=ha.HEALTH_AREA_ID"
                + "WHERE p.`LAST_MODIFIED_DATE` BETWEEN ? AND ?;";
        return this.jdbcTemplate.query(sql, new ExportProgramDataDTORowMapper(), yesterday, today);
    }

    @Override
    public List<ExportOrderDataDTO> exportOrderData() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()) + " 00:00:00";
        String today = DateUtils.getCurrentDateString(DateUtils.EST, DateUtils.YMD) + " 23:59:59";
        String sql = "SELECT s1.SHIPMENT_ID, papu.SKU_CODE, s1.PROGRAM_ID, pa.PROCUREMENT_AGENT_CODE, st.SHIPMENT_QTY, "
                + "COALESCE(st.`RECEIVED_DATE`,st.EXPECTED_DELIVERY_DATE)  AS EXPECTED_DELIVERY_DATE,st.`ACTIVE` "
                + "FROM "
                + "( "
                + "    SELECT s.SHIPMENT_ID, s.PROGRAM_ID, MAX(st.SHIPMENT_TRANS_ID) SHIPMENT_TRANS_ID, s.MAX_VERSION_ID "
                + "FROM rm_shipment s "
                + "LEFT JOIN rm_shipment_trans st ON s.SHIPMENT_ID=st.SHIPMENT_ID AND st.VERSION_ID=s.MAX_VERSION_ID "
                + "    LEFT JOIN vw_program p ON s.PROGRAM_ID=p.PROGRAM_ID "
                + "GROUP BY s.SHIPMENT_ID "
                + ") s1 "
                + "LEFT JOIN rm_shipment_trans st ON s1.SHIPMENT_TRANS_ID=st.SHIPMENT_TRANS_ID "
                + "LEFT JOIN vw_procurement_agent pa ON st.PROCUREMENT_AGENT_ID=pa.PROCUREMENT_AGENT_ID "
                + "LEFT JOIN rm_procurement_agent_planning_unit papu ON pa.PROCUREMENT_AGENT_ID=papu.PROCUREMENT_AGENT_ID AND st.PLANNING_UNIT_ID=papu.PLANNING_UNIT_ID "
                + "WHERE st.PROCUREMENT_AGENT_ID = 1 AND st.`LAST_MODIFIED_DATE` BETWEEN ? AND ?;";
        return this.jdbcTemplate.query(sql, new ExportOrderDataDTORowMapper(), yesterday, today);
    }

}