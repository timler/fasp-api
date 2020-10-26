///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package cc.altius.FASP.ARTMIS.dao.impl;
//
//import cc.altius.FASP.dao.impl.ProgramDataDaoImpl;
//import cc.altius.FASP.model.DTO.ErpOrderDTO;
//import cc.altius.FASP.model.DTO.rowMapper.ErpOrderDTORowMapper;
//import cc.altius.FASP.model.EmailTemplate;
//import cc.altius.FASP.model.Emailer;
//import cc.altius.FASP.service.EmailService;
//import cc.altius.utils.DateUtils;
//import java.io.File;
//import java.io.FileFilter;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.List;
//import java.util.Map;
//import javax.sql.DataSource;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import org.apache.commons.collections4.map.HashedMap;
//import org.apache.commons.io.filefilter.WildcardFileFilter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
//import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.jdbc.support.GeneratedKeyHolder;
//import org.springframework.jdbc.support.KeyHolder;
//import org.springframework.stereotype.Repository;
//import org.springframework.transaction.annotation.Transactional;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;
//import cc.altius.FASP.ARTMIS.dao.ImportArtmisDataDao;
//import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
//
///**
// *
// * @author altius
// */
//@Repository
//public class ImportArtmisDataDaoImpl implements ImportArtmisDataDao {
//
//    private JdbcTemplate jdbcTemplate;
//    private DataSource dataSource;
//    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
//
//    @Autowired
//    public void setDataSource(DataSource dataSource) {
//        this.dataSource = dataSource;
//        this.jdbcTemplate = new JdbcTemplate(dataSource);
//        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
//    }
//    @Value("${qat.filePath}")
//    private String QAT_FILE_PATH;
//    @Value("${catalogFilePath}")
//    private String CATALOG_FILE_PATH;
//    @Value("${catalogBkpFilePath}")
//    private String BKP_CATALOG_FILE_PATH;
//    @Value("${email.toList}")
//    private String toList;
//    @Value("${email.ccList}")
//    private String ccList;
//
//    @Autowired
//    private EmailService emailService;
//    @Autowired
//    private ProgramDataDaoImpl programDataDaoImpl;
//    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//
//    // Batch Query
////    -- INSERT IGNORE INTO rm_batch_info 
////             SELECT NULL,?,?,IF(s.`BATCH_NO` != 99 AND s.`BATCH_NO` != -99 AND s.`BATCH_NO` != 'NOBATCH',s.`BATCH_NO`,CONCAT()),s.`EXPIRY_DATE`,?,NULL,0 
////            FROM rm_erp_shipment s 
////            LEFT JOIN rm_erp_order eo ON eo.`ERP_ORDER_ID`=s.`ERP_ORDER_ID`
////            LEFT JOIN rm_procurement_agent_planning_unit papu ON LEFT(papu.`SKU_CODE`,12)=eo.`PLANNING_UNIT_SKU_CODE` AND papu.`PROCUREMENT_AGENT_ID`=1
////            LEFT JOIN rm_shipment_trans st ON st.`SHIPMENT_ID`=? AND st.`VERSION_ID`=(SELECT MAX(rst.`VERSION_ID`) FROM rm_shipment_trans rst WHERE rst.`SHIPMENT_ID`=?)
////            LEFT JOIN rm_shipment rs ON rs.`SHIPMENT_ID`=st.`SHIPMENT_ID`
////            LEFT JOIN rm_program_planning_unit ppu ON ppu.`PROGRAM_ID`=rs.`PROGRAM_ID` AND ppu.`PLANNING_UNIT_ID`=papu.`PLANNING_UNIT_ID`
////            WHERE s.`FLAG`=1 AND s.`ERP_ORDER_ID`=?
////            GROUP BY s.`BATCH_NO`
////    INSERT INTO `rm_batch_info` (`PROGRAM_ID`, `PLANNING_UNIT_ID`, `BATCH_NO`, `EXPIRY_DATE`, `CREATED_DATE`, `TMP_ID`, `AUTO_GENERATED`) "
////                + "SELECT s.PROGRAM_ID, st.PLANNING_UNIT_ID, CONCAT(LPAD(s.PROGRAM_ID, 6,'0'), LPAD(st.PLANNING_UNIT_ID, 8,'0'), date_format(CONCAT(LEFT(ADDDATE(COALESCE(st.RECEIVED_DATE,st.EXPECTED_DELIVERY_DATE), INTERVAL ppu.SHELF_LIFE MONTH),7),'-01'), '%y%m%d'), SUBSTRING('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', RAND()*36+1, 1), SUBSTRING('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', RAND()*36+1, 1), SUBSTRING('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', RAND()*36+1, 1)) `BATCH_NO`,    CONCAT(LEFT(ADDDATE(COALESCE(st.RECEIVED_DATE,st.EXPECTED_DELIVERY_DATE), INTERVAL ppu.SHELF_LIFE MONTH),7),'-01') `EXPIRY_DATE`, ?, st.`SHIPMENT_TRANS_ID`, 1 FROM  rm_shipment s "
////                + "LEFT JOIN rm_shipment_trans st ON s.SHIPMENT_ID=st.SHIPMENT_ID AND st.VERSION_ID=1 "
////                + "LEFT JOIN rm_program_planning_unit ppu ON s.PROGRAM_ID=ppu.PROGRAM_ID AND st.PLANNING_UNIT_ID=ppu.PLANNING_UNIT_ID "
////                + "WHERE s.PROGRAM_ID IN (SELECT ts.PROGRAM_ID FROM tmp_shipment_w_orders ts WHERE ts.PROGRAM_ID IS NOT NULL GROUP BY ts.PROGRAM_ID) AND st.ACTIVE
//    private final String insertIntoBatch = "INSERT IGNORE INTO rm_batch_info "
//            + "SELECT NULL,?,?,s.`BATCH_NO`,s.`EXPIRY_DATE`,?,null,0 FROM rm_erp_shipment s "
//            + "WHERE s.`FLAG`=1 AND s.`ERP_ORDER_ID`=? ";
//
//    //        sql = "DROP TEMPORARY TABLE IF EXISTS tmp_erp_order";
//    //        sql = "DROP TEMPORARY TABLE IF EXISTS tmp_erp_shipment";
//    private final String deleteErpShipmentTable = "DROP TABLE IF EXISTS tmp_erp_shipment";
//
////        sql = "CREATE TEMPORARY TABLE `tmp_erp_shipment` ( "
//    private final String createErpShipmentTable = "CREATE TABLE `tmp_erp_shipment` ( "
//            + "  `TEMP_SHIPMENT_ID` int(11) NOT NULL AUTO_INCREMENT, "
//            + "  `KN_SHIPMENT_NUMBER` varchar(45) COLLATE utf8_bin NOT NULL, "
//            + "  `ORDER_NUMBER` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//            + "  `PRIME_LINE_NO` int(11) DEFAULT NULL, "
//            + "  `BATCH_NO` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//            + "  `ITEM_ID` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//            + "  `EXPIRATION_DATE` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//            + "  `SHIPPED_QUANTITY` int(11) DEFAULT NULL, "
//            + "  `DELIVERED_QUANTITY` int(11) DEFAULT NULL, "
//            + "  `STATUS_NAME` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//            + "  `EXTERNAL_STATUS_STAGE` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//            + "  `ACTUAL_SHIPMENT_DATE` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//            + "  `ACTUAL_DELIVERY_DATE` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//            + "  `STATUS` int(11) DEFAULT NULL, "
//            + "  PRIMARY KEY (`TEMP_SHIPMENT_ID`), "
//            + "  UNIQUE KEY `TEMP_SHIPMENT_ID_UNIQUE` (`TEMP_SHIPMENT_ID`) "
//            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin";
//
//    private final String updateErpOrderTable = "UPDATE tmp_erp_order t "
//            + "LEFT JOIN rm_erp_order o ON t.`ORDER_NUMBER`=o.`ORDER_NO` AND t.`PRIME_LINE_NO`=o.`PRIME_LINE_NO` "
//            + "SET "
//            + "o.`RO_NO`=t.`RO_NO`, "
//            + "o.`RO_PRIME_LINE_NO`=t.`RO_PRIME_LINE_NO`, "
//            + "o.`ORDER_TYPE`=t.`ORDER_TYPE_IND`, "
//            + "o.`CREATED_DATE`=IFNULL(CONCAT(LEFT(t.`ORDER_ENTRY_DATE`,10),' ',REPLACE(MID(t.`ORDER_ENTRY_DATE`,12,8),'.',':')),NULL), "
//            + "o.`PARENT_RO`=t.`PARENT_RO`, "
//            + "o.`PARENT_CREATED_DATE`=IFNULL(CONCAT(LEFT(t.`PARENT_ORDER_ENTRY_DATE`,10),' ',REPLACE(MID(t.`PARENT_ORDER_ENTRY_DATE`,12,8),'.',':')),NULL), "
//            + "o.`PLANNING_UNIT_SKU_CODE`=LEFT(t.`ITEM_ID`,12), "
//            + "o.`PROCUREMENT_UNIT_SKU_CODE`=IF(LENGTH(t.`ITEM_ID`)=15,t.`ITEM_ID`,NULL), "
//            + "o.`QTY`=t.`ORDERED_QTY`, "
//            + "o.`ORDERD_DATE`=IFNULL(CONCAT(LEFT(t.`PO_RELEASED_FOR_FULFILLMENT_DATE`,10),' ',REPLACE(MID(t.`PO_RELEASED_FOR_FULFILLMENT_DATE`,12,8),'.',':')),NULL), "
//            + "o.`CURRENT_ESTIMATED_DELIVERY_DATE`=IFNULL(CONCAT(LEFT(t.`LATEST_ESTIMATED_DELIVERY_DATE`,10),' ',REPLACE(MID(t.`LATEST_ESTIMATED_DELIVERY_DATE`,12,8),'.',':')),NULL), "
//            + "o.`REQ_DELIVERY_DATE`=IFNULL(CONCAT(LEFT(t.`REQ_DELIVERY_DATE`,10),' ',REPLACE(MID(t.`REQ_DELIVERY_DATE`,12,8),'.',':')),NULL), "
//            + "o.`AGREED_DELIVERY_DATE`=IFNULL(CONCAT(LEFT(t.`REVISED_AGREED_DELIVERY_DATE`,10),' ',REPLACE(MID(t.`REVISED_AGREED_DELIVERY_DATE`,12,8),'.',':')),NULL), "
//            + "o.`SUPPLIER_NAME`=t.`ITEM_SUPPLIER_NAME`, "
//            + "o.`PRICE`=t.`UNIT_PRICE`, "
//            + "o.`SHIPPING_COST`=COALESCE(IF(t.`TOTAL_ACTUAL_FREIGHT_COST`=0,NULL,t.`TOTAL_ACTUAL_FREIGHT_COST`),IF(t.`FREIGHT_ESTIMATE`=0,NULL,t.`FREIGHT_ESTIMATE`),IF(t.`SHIPPING_CHARGES`=0,NULL,t.`SHIPPING_CHARGES`)), "
//            + "o.`SHIP_BY`=t.`CARRIER_SERVICE_CODE`, "
//            + "o.`RECPIENT_NAME`=t.`RECIPIENT_NAME`, "
//            + "o.`RECPIENT_COUNTRY`=t.`RECIPIENT_COUNTRY`, "
//            + "o.`STATUS`=t.`EXTERNAL_STATUS_STAGE`, "
//            + "o.`LAST_MODIFIED_DATE`=?, "
//            + "o.`PROGRAM_ID`=t.`PROGRAM_ID`, "
//            + "o.`SHIPMENT_ID`=t.`SHIPMENT_ID`, "
//            + "o.`FLAG`=1;";
//
//    private final String insertIntoErpOrder = "INSERT IGNORE INTO rm_erp_order "
//            + " SELECT NULL,RO_NO,RO_PRIME_LINE_NO,ORDER_NUMBER,PRIME_LINE_NO, "
//            + " ORDER_TYPE_IND,IFNULL(DATE_FORMAT(ORDER_ENTRY_DATE,'%Y-%m-%d'),NULL),PARENT_RO,IFNULL(DATE_FORMAT(PARENT_ORDER_ENTRY_DATE,'%Y-%m-%d %H:%i:%s'),NULL),LEFT(ITEM_ID, 12), "
//            + " IF(LENGTH(ITEM_ID)=15,ITEM_ID,NULL),ORDERED_QTY,IFNULL(DATE_FORMAT(PO_RELEASED_FOR_FULFILLMENT_DATE,'%Y-%m-%d'),NULL), "
//            + " IFNULL(DATE_FORMAT(LATEST_ESTIMATED_DELIVERY_DATE,'%Y-%m-%d'),NULL), "
//            + " IFNULL(DATE_FORMAT(REQ_DELIVERY_DATE,'%Y-%m-%d'),NULL), "
//            + " IFNULL(DATE_FORMAT(REVISED_AGREED_DELIVERY_DATE,'%Y-%m-%d'),NULL),ITEM_SUPPLIER_NAME,UNIT_PRICE, "
//            + " COALESCE(TOTAL_ACTUAL_FREIGHT_COST,FREIGHT_ESTIMATE,SHIPPING_CHARGES),CARRIER_SERVICE_CODE,RECIPIENT_NAME, "
//            + " RECIPIENT_COUNTRY,STATUS_NAME,1,?,1,NULL,PROGRAM_ID,SHIPMENT_ID "
//            + " FROM tmp_erp_order t ";
//
//    private final String updateErpShipmentTable = "UPDATE tmp_erp_shipment t "
//            + "LEFT JOIN rm_erp_shipment s ON t.`KN_SHIPMENT_NUMBER`=s.`KN_SHIPMENT_NO` "
//            + "AND t.`ORDER_NUMBER`=s.`ORDER_NO` "
//            + "AND t.`PRIME_LINE_NO`=s.`PRIME_LINE_NO` "
//            + "AND t.`BATCH_NO`=s.`BATCH_NO` "
//            + "SET s.`FLAG`=1, "
//            + "s.`LAST_MODIFIED_DATE`=?, "
//            + "s.`EXPIRY_DATE`=IFNULL(LEFT(t.`EXPIRATION_DATE`,10),NULL), "
//            + "s.`PROCUREMENT_UNIT_SKU_CODE`=t.`ITEM_ID`, "
//            + "s.`SHIPPED_QTY`=t.`SHIPPED_QUANTITY`, "
//            + "s.`DELIVERED_QTY`=t.`DELIVERED_QUANTITY`, "
//            + "s.`ACTUAL_SHIPMENT_DATE`=IFNULL(CONCAT(LEFT(t.`ACTUAL_SHIPMENT_DATE`,10),' ',REPLACE(MID(t.`ACTUAL_SHIPMENT_DATE`,12,8),'.',':')),NULL), "
//            + "s.`ACTUAL_DELIVERY_DATE`=IFNULL(CONCAT(LEFT(t.`ACTUAL_DELIVERY_DATE`,10),' ',REPLACE(MID(t.`ACTUAL_DELIVERY_DATE`,12,8),'.',':')),NULL), "
//            + "s.`STATUS`=t.`EXTERNAL_STATUS_STAGE`;";
//
//    private final String insertIntoErpShipmentTable = "INSERT IGNORE INTO rm_erp_shipment "
//            + "SELECT  NULL,NULL,KN_SHIPMENT_NUMBER,ORDER_NUMBER,PRIME_LINE_NO,BATCH_NO,IFNULL(DATE_FORMAT(EXPIRATION_DATE,'%Y-%m-%d %H:%i:%s'),NULL),ITEM_ID,SHIPPED_QUANTITY, "
//            + "DELIVERED_QUANTITY,IFNULL(DATE_FORMAT(ACTUAL_SHIPMENT_DATE,'%Y-%m-%d %H:%i:%s'),NULL),IFNULL(DATE_FORMAT(ACTUAL_DELIVERY_DATE,'%Y-%m-%d %H:%i:%s'),NULL),EXTERNAL_STATUS_STAGE,?,1 "
//            + "FROM  tmp_erp_shipment t;";
//
//    private final String getProgramId = "SELECT s.`PROGRAM_ID` FROM rm_shipment s WHERE s.`SHIPMENT_ID`=?;";
//
//    private final String getPlanningUnitId = "SELECT pu.`PLANNING_UNIT_ID` "
//            + "FROM rm_erp_order o "
//            + "LEFT JOIN rm_procurement_agent_planning_unit pu ON LEFT(pu.`SKU_CODE`,12)=o.`PLANNING_UNIT_SKU_CODE` AND pu.`PROCUREMENT_AGENT_ID`=1  "
//            + "WHERE o.`ERP_ORDER_ID`=?; ";
//
//    private final String isShipmentErpLinked = "SELECT st.`ERP_FLAG` FROM rm_shipment_trans st "
//            + " LEFT JOIN rm_shipment s ON s.`SHIPMENT_ID`=st.`SHIPMENT_ID` "
//            + " WHERE st.`SHIPMENT_ID`=? "
//            //+ " AND s.`PARENT_SHIPMENT_ID` IS NULL "
//            + " ORDER BY st.`VERSION_ID` DESC "
//            + " LIMIT 1;";
//
//    private final String hasChildShipments = "SELECT COUNT(*) FROM rm_shipment_trans st "
//            + " LEFT JOIN rm_shipment s ON s.`SHIPMENT_ID`=st.`SHIPMENT_ID` "
//            + " AND st.`VERSION_ID`=(SELECT MAX(stt.`VERSION_ID`) FROM rm_shipment_trans stt WHERE stt.`SHIPMENT_ID`=st.`SHIPMENT_ID`) "
//            + " WHERE s.`PARENT_SHIPMENT_ID`=? AND st.`ACTIVE` AND st.`ORDER_NO`=? AND st.`PRIME_LINE_NO`=?;";
//
//    private final String getVersionIdOrderNoPrimeLineNo = "SELECT MAX(stt.`VERSION_ID`) FROM rm_shipment_trans stt WHERE stt.`ORDER_NO`=? AND stt.`PRIME_LINE_NO`=?;";
//    private final String getMaxVersionFromShipmentTable = "SELECT s.`MAX_VERSION_ID` FROM rm_shipment s WHERE s.`SHIPMENT_ID`=?;";
//    private final String updateShipmentBasedOnOnderNoPrimeLineNo = "UPDATE rm_shipment_trans st "
//            + "LEFT JOIN rm_erp_order eo ON eo.`ORDER_NO`=st.`ORDER_NO`  AND st.`PRIME_LINE_NO`=eo.`PRIME_LINE_NO` "
//            + "LEFT JOIN rm_shipment_status_mapping sm ON sm.`EXTERNAL_STATUS_STAGE`=eo.`STATUS`"
//            + "LEFT JOIN rm_shipment s ON s.`SHIPMENT_ID`=st.`SHIPMENT_ID` "
//            + "LEFT JOIN rm_erp_shipment es ON es.`ERP_ORDER_ID`=eo.`ERP_ORDER_ID` "
//            + "SET "
//            + "st.`EXPECTED_DELIVERY_DATE`=eo.`CURRENT_ESTIMATED_DELIVERY_DATE`, "
//            + "st.`SHIPMENT_QTY`=eo.`QTY`,st.`RATE`=eo.`PRICE`, "
//            + "st.`PRODUCT_COST`=(eo.`QTY`*eo.`PRICE`),st.`SHIPMENT_MODE`=eo.`SHIP_BY`, "
//            + "st.`FREIGHT_COST`=eo.`SHIPPING_COST`, "
//            + "st.`SUBMITTED_DATE`=COALESCE(eo.`PARENT_CREATED_DATE`,eo.`ORDER_ENTRY_DATE`), "
//            + "st.`APPROVED_DATE`=eo.`ORDERD_DATE`, "
//            + "st.`SHIPPED_DATE`=MIN(es.`ACTUAL_SHIPMENT_DATE`), "
//            + "st.`ARRIVED_DATE`=IF(sm.`SHIPMENT_STATUS_ID`=21,?,NULL), "
//            + "st.`RECEIVED_DATE`=MIN(es.`ACTUAL_DELIVERY_DATE`), "
//            + "st.`LAST_MODIFIED_BY`=1, "
//            + "st.`LAST_MODIFIED_DATE`=?, "
//            + "st.`SHIPMENT_STATUS_ID`=sm.`SHIPMENT_STATUS_ID` "
//            + "WHERE st.`ORDER_NO`=? AND st.`PRIME_LINE_NO`=? AND s.`PARENT_SHIPMENT_ID`=? AND st.`VERSION_ID`=?;";
//
//    private final String createNewEntryInShipmentTable = "INSERT INTO  rm_shipment "
//            + "SELECT  NULL,s.`PROGRAM_ID`,:qty,s.`CURRENCY_ID`,s.`CONVERSION_RATE_TO_USD`, "
//            + "s.`SHIPMENT_ID`,1,:createdDate,1,:lastModifiedDate,s.`MAX_VERSION_ID`,NULL "
//            + "FROM rm_shipment s "
//            + "WHERE s.`SHIPMENT_ID`=:shipmentId;";
//
//    private final String createNewEntryIntoShipmentTrans = "INSERT INTO rm_shipment_trans "
//            + "SELECT NULL,:shipmentId,st.`PLANNING_UNIT_ID`,st.`PROCUREMENT_AGENT_ID`,st.`FUNDING_SOURCE_ID`,st.`BUDGET_ID`, "
//            + "COALESCE(eo.`CURRENT_ESTIMATED_DELIVERY_DATE`,st.`EXPECTED_DELIVERY_DATE`),st.`PROCUREMENT_UNIT_ID` ,st.`SUPPLIER_ID`,eo.`QTY`,eo.`PRICE`,(eo.`QTY`*eo.`PRICE`),eo.`SHIP_BY`,eo.`SHIPPING_COST`, "
//            + "st.`PLANNED_DATE`,COALESCE(eo.`PARENT_CREATED_DATE`,eo.`ORDER_ENTRY_DATE`,st.`SUBMITTED_DATE`),COALESCE(eo.`ORDERD_DATE`,st.`APPROVED_DATE`),IFNULL(MIN(es.`ACTUAL_SHIPMENT_DATE`),NULL),IF(sm.`SHIPMENT_STATUS_ID`=21,:CURDATE,NULL),IFNULL(MIN(es.`ACTUAL_DELIVERY_DATE`),NULL),sm.`SHIPMENT_STATUS_ID`,NULL,1, "
//            + "eo.`ORDER_NO`,eo.`PRIME_LINE_NO`,st.`ACCOUNT_FLAG`,st.`EMERGENCY_ORDER`,st.`LOCAL_PROCUREMENT`,1,st.`DATA_SOURCE_ID`,:CURDATE1,s.`MAX_VERSION_ID`,1 "
//            + "FROM rm_shipment_trans st "
//            + "LEFT JOIN rm_shipment s ON s.`SHIPMENT_ID`=st.`SHIPMENT_ID` "
//            + "LEFT JOIN rm_erp_order eo ON eo.`ORDER_NO`=:orderNo AND eo.`PRIME_LINE_NO`=:primeLineNo "
//            + "LEFT JOIN rm_shipment_status_mapping sm ON sm.`EXTERNAL_STATUS_STAGE`=eo.`STATUS` "
//            + "LEFT JOIN rm_erp_shipment es ON es.`ERP_ORDER_ID`=eo.`ERP_ORDER_ID` "
//            + "WHERE st.`SHIPMENT_ID`=:shipmentId1 "
//            + "ORDER BY st.`SHIPMENT_TRANS_ID` DESC LIMIT 1;";
//
//    private final String createNewEntryInShipmentTableDiff = "INSERT INTO  rm_shipment "
//            + "SELECT  NULL,s.`PROGRAM_ID`,s.`SUGGESTED_QTY`,s.`CURRENCY_ID`,s.`CONVERSION_RATE_TO_USD`, "
//            + "s.`SHIPMENT_ID`,1,:createdDate,1,:lastModifiedDate,s.`MAX_VERSION_ID`,NULL "
//            + "FROM rm_shipment s "
//            + "WHERE s.`SHIPMENT_ID`=:shipmentId;";
//    //Shipped qty
//    private final String insertIntoShipmentTransBatchInfo = "INSERT INTO rm_shipment_trans_batch_info SELECT NULL,?,b.`BATCH_ID`,SUM(s.`DELIVERED_QTY`) FROM rm_erp_shipment s "
//            + "LEFT JOIN rm_batch_info b ON b.`BATCH_NO`=s.`BATCH_NO` AND b.`PROGRAM_ID`=? AND b.`PLANNING_UNIT_ID`=? "
//            + "WHERE s.`FLAG`=1 AND s.`ERP_ORDER_ID`=? "
//            + "GROUP BY s.`BATCH_NO`;";
//
//    private final String updateShipmentTransManuallyTagged = "UPDATE rm_shipment_trans st "
//            + "LEFT JOIN rm_erp_order eo ON eo.`ORDER_NO`=st.`ORDER_NO`  AND st.`PRIME_LINE_NO`=eo.`PRIME_LINE_NO` "
//            + "LEFT JOIN rm_shipment_status_mapping sm ON sm.`EXTERNAL_STATUS_STAGE`=eo.`STATUS` "
//            + "SET "
//            + "st.`EXPECTED_DELIVERY_DATE`=eo.`CURRENT_ESTIMATED_DELIVERY_DATE`, "
//            + "st.`SHIPMENT_QTY`=eo.`QTY`,st.`RATE`=eo.`PRICE`, "
//            + "st.`PRODUCT_COST`=(eo.`QTY`*eo.`PRICE`),st.`SHIPMENT_MODE`=eo.`SHIP_BY`, "
//            + "st.`FREIGHT_COST`=eo.`SHIPPING_COST`, "
//            + "st.`SUBMITTED_DATE`=COALESCE(eo.`PARENT_CREATED_DATE`,eo.`ORDER_ENTRY_DATE`), "
//            + "st.`APPROVED_DATE`=eo.`ORDERD_DATE`, "
//            + "st.`SHIPPED_DATE`=MIN(es.`ACTUAL_SHIPMENT_DATE`), "
//            + "st.`ARRIVED_DATE`=IF(sm.`SHIPMENT_STATUS_ID`=21,?,NULL), "
//            + "st.`RECEIVED_DATE`=MIN(es.`ACTUAL_DELIVERY_DATE`), "
//            + "st.`LAST_MODIFIED_BY`=1, "
//            + "st.`LAST_MODIFIED_DATE`=?, "
//            + "st.`SHIPMENT_STATUS_ID`=sm.`SHIPMENT_STATUS_ID` "
//            + "WHERE st.`ORDER_NO`=? AND st.`PRIME_LINE_NO`=? AND st.`SHIPMENT_ID`=?;";
//
//    private final String deactivateParentAndMarkErpLinked = "UPDATE rm_shipment_trans st "
//            + "SET st.`ERP_FLAG`=1,st.`ACTIVE`=0 "
//            + "WHERE st.`SHIPMENT_ID`=? AND st.`VERSION_ID`=?";
//
//    @Override
//    @Transactional
//    public void importOrderAndShipmentData(File orderFile) throws ParserConfigurationException, SAXException, IOException, FileNotFoundException {
//        String sql;
//        int rows;
//        String curDate = DateUtils.getCurrentDateString(DateUtils.EST, DateUtils.YMDHMS);
//        int shipmentTransId = 0, shipmentId = 0, found, planningUnitId, programId;
//        boolean isErpLinked;
//
//        KeyHolder keyHolder1 = new GeneratedKeyHolder();
//        KeyHolder keyHolder2 = new GeneratedKeyHolder();
//        MapSqlParameterSource params1 = new MapSqlParameterSource();
//        File fXmlFile = orderFile;
////        File fXmlFile1 = new File(fileList1.trim());
//        String sqlString = "DROP TEMPORARY TABLE IF EXISTS tmp_erp_order";
//        this.jdbcTemplate.execute(sqlString);
//        // Create ERO oder table
//        sqlString = "CREATE TEMPORARY TABLE `tmp_erp_order` ( "
//                + "  `TEMP_ID` int(11) NOT NULL AUTO_INCREMENT, "
//                + "  `RO_NO` varchar(45) COLLATE utf8_bin NOT NULL, "
//                + "  `RO_PRIME_LINE_NO` int(11) NOT NULL, "
//                + "  `ORDER_NUMBER` varchar(45) COLLATE utf8_bin NOT NULL, "
//                + "  `PRIME_LINE_NO` int(11) DEFAULT NULL, "
//                + "  `ORDER_TYPE_IND` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//                + "  `ORDER_ENTRY_DATE` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//                + "  `PARENT_RO` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//                + "  `PARENT_ORDER_ENTRY_DATE` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//                + "  `ITEM_ID` varchar(100) COLLATE utf8_bin DEFAULT NULL, "
//                + "  `ORDERED_QTY` int(11) DEFAULT NULL, "
//                + "  `PO_RELEASED_FOR_FULFILLMENT_DATE` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//                + "  `LATEST_ESTIMATED_DELIVERY_DATE` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//                + "  `REQ_DELIVERY_DATE` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//                + "  `REVISED_AGREED_DELIVERY_DATE` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//                + "  `ITEM_SUPPLIER_NAME` varchar(100) COLLATE utf8_bin DEFAULT NULL, "
//                + "  `UNIT_PRICE` double DEFAULT NULL, "
//                + "  `STATUS_NAME` varchar(100) COLLATE utf8_bin DEFAULT NULL, "
//                + "  `EXTERNAL_STATUS_STAGE` varchar(100) COLLATE utf8_bin DEFAULT NULL, "
//                + "  `SHIPPING_CHARGES` double DEFAULT NULL, "
//                + "  `FREIGHT_ESTIMATE` double DEFAULT NULL, "
//                + "  `TOTAL_ACTUAL_FREIGHT_COST` double DEFAULT NULL, "
//                + "  `CARRIER_SERVICE_CODE` varchar(45) COLLATE utf8_bin DEFAULT NULL, "
//                + "  `RECIPIENT_NAME` varchar(100) COLLATE utf8_bin DEFAULT NULL, "
//                + "  `RECIPIENT_COUNTRY` varchar(100) COLLATE utf8_bin DEFAULT NULL, "
//                + "  `STATUS` int(11) DEFAULT NULL, "
//                + "  `PROGRAM_ID` int(11) DEFAULT NULL, "
//                + "  `SHIPMENT_ID` int(11) DEFAULT NULL, "
//                + "  PRIMARY KEY (`TEMP_ID`), "
//                + "  UNIQUE KEY `TEMP_ID_UNIQUE` (`TEMP_ID`) "
//                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin";
//        this.jdbcTemplate.execute(sqlString);
//        
//
//        //----------------Read order xml--------------------start----------------
//        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//        FileReader fr = new FileReader(fXmlFile1);
//        Document doc;
//        NodeList nList1;
//        MapSqlParameterSource[] batchParams;
//        Map<String, Object> map = new HashedMap<String, Object>();
//        int[] rows1;
//        int x;
//
//        if (fr.read() == -1) {
//            //file is empty
//            logger.info("Order file is empty");
//        } else {
//            doc = dBuilder.parse(fXmlFile);
//            doc.getDocumentElement().normalize();
//
//            nList1 = doc.getElementsByTagName("orderdata");
//            batchParams = new MapSqlParameterSource[nList1.getLength()];
//            x = 0;
//
//            SimpleJdbcInsert si = new SimpleJdbcInsert(jdbcTemplate).withTableName("tmp_erp_order");
//            for (int temp2 = 0; temp2 < nList1.getLength(); temp2++) {
//                Node nNode1 = nList1.item(temp2);
//                if (nNode1.getNodeType() == Node.ELEMENT_NODE) {
//                    Element dataRecordElement = (Element) nNode1;
//                    map.put("RO_NO", dataRecordElement.getElementsByTagName("ro_number").item(0).getTextContent());
//                    map.put("RO_PRIME_LINE_NO", dataRecordElement.getElementsByTagName("ro_prime_line_no").item(0).getTextContent());
//                    map.put("ORDER_NO", dataRecordElement.getElementsByTagName("order_number").item(0).getTextContent());
//                    map.put("PRIME_LINE_NO", dataRecordElement.getElementsByTagName("prime_line_no").item(0).getTextContent());
//                    map.put("ORDER_TYPE_IN", dataRecordElement.getElementsByTagName("order_type_ind").item(0).getTextContent());
//                    map.put("ORDER_ENTRY_DATE", dataRecordElement.getElementsByTagName("order_entry_date").item(0).getTextContent());
//                    map.put("PARENT_RO", dataRecordElement.getElementsByTagName("parent_ro").item(0).getTextContent());
//                    map.put("PARENT_ORDER_ENTRY_DATE", dataRecordElement.getElementsByTagName("parent_ro_entry_date").item(0).getTextContent());
//                    map.put("ITEM_ID", dataRecordElement.getElementsByTagName("item_id").item(0).getTextContent());
//                    map.put("ORDERED_QTY", dataRecordElement.getElementsByTagName("ordered_qty").item(0).getTextContent());
//                    map.put("PO_RELEASED_FOR_FULFILLMENT_DATE", dataRecordElement.getElementsByTagName("po_released_for_fulfillment_date").item(0).getTextContent());
//                    map.put("LATEST_ESTIMATED_DELIVERY_DATE", dataRecordElement.getElementsByTagName("latest_estimated_delivery_date").item(0).getTextContent());
//                    map.put("REQ_DELIVERT_DATE", dataRecordElement.getElementsByTagName("req_delivery_date").item(0).getTextContent());
//                    map.put("REVISED_AGREED_DELIVERY_DATE", dataRecordElement.getElementsByTagName("revised_agreed_delivery_date").item(0).getTextContent());
//                    map.put("ITEM_SUPPLIER_NAME", dataRecordElement.getElementsByTagName("item_supplier_name").item(0).getTextContent());
//                    map.put("UNIT_PRICE", dataRecordElement.getElementsByTagName("unit_price").item(0).getTextContent());
//                    map.put("STATUS_NAME", dataRecordElement.getElementsByTagName("status_name").item(0).getTextContent());
//                    map.put("EXTERNAL_STATUS_STAGE", dataRecordElement.getElementsByTagName("external_status_stage").item(0).getTextContent());
//                    String shippingCharges = dataRecordElement.getElementsByTagName("shipping_charges").item(0).getTextContent();
//                    map.put("SHIPPING_CHARGES", (shippingCharges != null && shippingCharges != "" ? shippingCharges : null));
//                    String freightEstimate = dataRecordElement.getElementsByTagName("freight_estimate").item(0).getTextContent();
//                    map.put("FREIGHT_ESTIMATE", (freightEstimate != null && freightEstimate != "" ? freightEstimate : null));
//                    String totalActualFreightCost = dataRecordElement.getElementsByTagName("total_actual_freight_cost").item(0).getTextContent();
//                    map.put("TOTAL_ACTUAL_FREIGHT_COST", (totalActualFreightCost != null && totalActualFreightCost != "" ? totalActualFreightCost : null));
//                    map.put("CARRIER_SERVICE_CODE", dataRecordElement.getElementsByTagName("carrier_service_code").item(0).getTextContent());
//                    map.put("RECIPIENT_NAME", dataRecordElement.getElementsByTagName("recipient_name").item(0).getTextContent());
//                    map.put("RECIPIENT_COUNTRY", dataRecordElement.getElementsByTagName("recipient_country").item(0).getTextContent());
//                    map.put("PROGRAM_ID", (dataRecordElement.getElementsByTagName("qat_program_id").item(0).getTextContent() != null && dataRecordElement.getElementsByTagName("qat_program_id").item(0).getTextContent() != "" ? dataRecordElement.getElementsByTagName("qat_program_id").item(0).getTextContent() : null));
//                    map.put("SHIPMENT_ID", (dataRecordElement.getElementsByTagName("qat_shipment_id").item(0).getTextContent() != null && dataRecordElement.getElementsByTagName("qat_shipment_id").item(0).getTextContent() != "" ? dataRecordElement.getElementsByTagName("qat_shipment_id").item(0).getTextContent() : null));
//                    batchParams[x] = new MapSqlParameterSource(map);
//                    x++;
//                }
//            }
//            rows1 = si.executeBatch(batchParams);
//            logger.info("Successfully inserted into tmp_erp_order records---" + rows1.length);
////---------------------End-----------------------------------------------
//        }
//        sql = "SELECT COUNT(*) FROM tmp_erp_order;";
//
//        logger.info(
//                "Total rows inserted in tmp_erp_order---" + this.jdbcTemplate.queryForObject(sql, Integer.class
//                ));
//
//        sql = "UPDATE tmp_erp_order teo SET teo.LATEST_ESTIMATED_DELIVERY_DATE = NULL WHERE teo.LATEST_ESTIMATED_DELIVERY_DATE='';";
//
//        this.jdbcTemplate.update(sql);
//
//        sql = "UPDATE tmp_erp_order teo SET teo.ORDER_ENTRY_DATE = NULL WHERE teo.ORDER_ENTRY_DATE='';";
//
//        this.jdbcTemplate.update(sql);
//
//        sql = "UPDATE tmp_erp_order teo SET teo.PARENT_ORDER_ENTRY_DATE = NULL WHERE teo.PARENT_ORDER_ENTRY_DATE='';";
//
//        this.jdbcTemplate.update(sql);
//
//        sql = "UPDATE tmp_erp_order teo SET teo.PO_RELEASED_FOR_FULFILLMENT_DATE = NULL WHERE teo.PO_RELEASED_FOR_FULFILLMENT_DATE='';";
//
//        this.jdbcTemplate.update(sql);
//
//        sql = "UPDATE tmp_erp_order teo SET teo.REQ_DELIVERY_DATE = NULL WHERE teo.REQ_DELIVERY_DATE='';";
//
//        this.jdbcTemplate.update(sql);
//
//        sql = "UPDATE tmp_erp_order teo SET teo.REVISED_AGREED_DELIVERY_DATE = NULL WHERE teo.REVISED_AGREED_DELIVERY_DATE='';";
//
//        this.jdbcTemplate.update(sql);
//
//        // Delete erp shipment table
//        this.jdbcTemplate.execute(deleteErpShipmentTable);
//
//        // Create erp shipment table
//        this.jdbcTemplate.execute(createErpShipmentTable);
//
//        //----------------Read shipment xml--------------------start----------------
//        dbFactory = DocumentBuilderFactory.newInstance();
//        dBuilder = dbFactory.newDocumentBuilder();
//        fr = new FileReader(fXmlFile1);
//
//        if (fr.read()
//                == -1) {
//            //file is empty
//            logger.info("Shipment file is empty");
//        } else {
//            doc = dBuilder.parse(fXmlFile1);
//            doc.getDocumentElement().normalize();
//            System.out.println("fXmlFile1 name---" + fXmlFile1.getName());
//            System.out.println("fXmlFile1 path---" + fXmlFile1.getPath());
//            NodeList nList2 = doc.getElementsByTagName("shipmentdata");
//            batchParams = new MapSqlParameterSource[nList2.getLength()];
//            map.clear();
//            x = 0;
//            System.out.println("list length---" + nList2.getLength());
//            sql = "INSERT INTO tmp_erp_shipment VALUES(null,:knShipmentNo,:orderNo,:primeLineNo,:batchNo,:itemId,"
//                    + ":expirationDate,:shippedQty,:deliveredQty,"
//                    + ":statusName,:externalStatusStage,:actualShipmentDate,:actualDeliveryDate,null)";
//            for (int temp2 = 0; temp2 < nList2.getLength(); temp2++) {
//                Node nNode1 = nList2.item(temp2);
//                if (nNode1.getNodeType() == Node.ELEMENT_NODE) {
//                    Element dataRecordElement = (Element) nNode1;
//                    System.out.println("dataRecordElement---" + dataRecordElement);
//                    map.put("knShipmentNo", dataRecordElement.getElementsByTagName("kn_shipment_number").item(0).getTextContent());
//                    System.out.println("order no shipment---" + dataRecordElement.getElementsByTagName("order_number").item(0).getTextContent());
//                    map.put("orderNo", dataRecordElement.getElementsByTagName("order_number").item(0).getTextContent());
//                    map.put("primeLineNo", dataRecordElement.getElementsByTagName("prime_line_no").item(0).getTextContent());
//                    map.put("batchNo", dataRecordElement.getElementsByTagName("batch_no").item(0).getTextContent());
//                    map.put("itemId", dataRecordElement.getElementsByTagName("item_id").item(0).getTextContent());
//                    map.put("expirationDate", dataRecordElement.getElementsByTagName("expiration_date").item(0).getTextContent());
//                    map.put("shippedQty", dataRecordElement.getElementsByTagName("shipped_quantity").item(0).getTextContent());
//                    String deliveredQty = dataRecordElement.getElementsByTagName("delivered_quantity").item(0).getTextContent();
//                    map.put("deliveredQty", (deliveredQty != null && deliveredQty != "" ? deliveredQty : 0));
//                    map.put("statusName", dataRecordElement.getElementsByTagName("status_name").item(0).getTextContent());
//                    map.put("externalStatusStage", dataRecordElement.getElementsByTagName("external_status_stage").item(0).getTextContent());
//                    map.put("actualShipmentDate", dataRecordElement.getElementsByTagName("actual_shipment_date").item(0).getTextContent());
//                    map.put("actualDeliveryDate", dataRecordElement.getElementsByTagName("actual_delivery_date").item(0).getTextContent());
//                    batchParams[x] = new MapSqlParameterSource(map);
//                    x++;
//                }
//            }
//            rows1 = namedParameterJdbcTemplate.batchUpdate(sql, batchParams);
//            logger.info("Successfully inserted into tmp_erp_shipment records---" + rows1.length);
//            //---------------------End-----------------------------------------------
//        }
//        sql = "SELECT COUNT(*) FROM tmp_erp_shipment;";
//
//        logger.info(
//                "Total rows inserted in tmp_erp_shipment---" + this.jdbcTemplate.queryForObject(sql, Integer.class
//                ));
//
//        sql = "UPDATE rm_erp_order o SET o.`FLAG`=0";
//
//        this.jdbcTemplate.update(sql);
//
//        sql = "SELECT COUNT(*) FROM rm_erp_order;";
//
//        logger.info(
//                "Total rows present in rm_erp_order---" + this.jdbcTemplate.queryForObject(sql, Integer.class
//                ));
//
//        // Update erp order table
//        rows = this.jdbcTemplate.update(updateErpOrderTable, curDate);
//
//        logger.info(
//                "No of rows updated in rm_erp_order---" + rows);
//
//        //Insert into erp order table
//        rows = this.jdbcTemplate.update(insertIntoErpOrder, curDate);
//
//        logger.info(
//                "No of rows inserted into rm_erp_order---" + rows);
//
//        sql = "UPDATE tmp_erp_shipment teo SET teo.EXPIRATION_DATE = NULL WHERE teo.EXPIRATION_DATE='';";
//
//        this.jdbcTemplate.update(sql);
//
//        sql = "UPDATE tmp_erp_shipment teo SET teo.ACTUAL_SHIPMENT_DATE = NULL WHERE teo.ACTUAL_SHIPMENT_DATE='';";
//
//        this.jdbcTemplate.update(sql);
//
//        sql = "UPDATE tmp_erp_shipment teo SET teo.ACTUAL_DELIVERY_DATE = NULL WHERE teo.ACTUAL_DELIVERY_DATE='';";
//
//        this.jdbcTemplate.update(sql);
//
//        sql = "SELECT COUNT(*) FROM rm_erp_shipment;";
//
//        logger.info(
//                "Total rows present in rm_erp_shipment---" + this.jdbcTemplate.queryForObject(sql, Integer.class
//                ));
//
//        // Update erp shipment table
//        rows = this.jdbcTemplate.update(updateErpShipmentTable, curDate);
//
//        logger.info(
//                "No of rows updated in  rm_erp_shipment---" + rows);
//
//        //Insert into erp shipment table
//        rows = this.jdbcTemplate.update(insertIntoErpShipmentTable, curDate);
//
//        logger.info(
//                "No of rows inserted into rm_erp_shipment---" + rows);
//
//        sql = "UPDATE rm_erp_shipment t "
//                + "LEFT JOIN rm_erp_order o ON o.`ORDER_NO`=t.`ORDER_NO` AND o.`PRIME_LINE_NO`=t.`PRIME_LINE_NO` "
//                + "SET t.`ERP_ORDER_ID`=o.`ERP_ORDER_ID`;";
//        rows = this.jdbcTemplate.update(sql);
//
//        logger.info(
//                "update erp order id in erp shipment table---" + rows);
//
//        sql = "SELECT COUNT(*) FROM rm_erp_shipment where erp_order_id is null and flag=1;";
//
//        logger.info(
//                "Total rows without erp_order_id in rm_erp_shipment---" + this.jdbcTemplate.queryForObject(sql, Integer.class
//                ));
//
//        sql = "SELECT e.*,l.`LABEL_ID`,IF(l.`LABEL_EN` IS NOT NULL,l.`LABEL_EN`,'') AS LABEL_EN,l.`LABEL_FR`,l.`LABEL_PR`,l.`LABEL_SP` FROM rm_erp_order e "
//                + " LEFT JOIN rm_procurement_agent_planning_unit pu ON LEFT(pu.`SKU_CODE`,12)=e.`PLANNING_UNIT_SKU_CODE` "
//                + " AND pu.`PROCUREMENT_AGENT_ID`=1 "
//                + " LEFT JOIN rm_planning_unit p ON p.`PLANNING_UNIT_ID`=pu.`PLANNING_UNIT_ID` "
//                + " LEFT JOIN ap_label l ON l.`LABEL_ID`=p.`LABEL_ID` "
//                + "WHERE e.`FLAG`=1;";
//        List<ErpOrderDTO> erpOrderDTOList = this.jdbcTemplate.query(sql, new ErpOrderDTORowMapper());
//        int versionId = 0;
//
//        logger.info(
//                "erpOrderDTO---" + erpOrderDTOList.size());
//        for (ErpOrderDTO erpOrderDTO : erpOrderDTOList) {
//            try {
//                // Shipment id found in file
//                if (erpOrderDTO.getShipmentId() != 0) {
//                    System.out.println("shipment id found in file---" + erpOrderDTO.getShipmentId());
//                    // Get program id
//                    programId = this.jdbcTemplate.queryForObject(getProgramId, Integer.class, erpOrderDTO.getShipmentId());
//
//                    // Get planning unit id
//                    planningUnitId = this.jdbcTemplate.queryForObject(getPlanningUnitId, Integer.class, erpOrderDTO.getErpOrderId());
//
//                    // Batch info
//                    // Autogenerated 
//                    // 1-QAT generated 
//                    // 0-Others
//                    this.jdbcTemplate.update(insertIntoBatch, programId, planningUnitId, curDate, erpOrderDTO.getErpOrderId());
//
//                    //Check ERP linked
//                    isErpLinked = this.jdbcTemplate.queryForObject(isShipmentErpLinked, Boolean.class, erpOrderDTO.getShipmentId());
//                    if (isErpLinked) {
//                        // Check if contains child shipments
//                        found = this.jdbcTemplate.queryForObject(hasChildShipments, Integer.class, erpOrderDTO.getShipmentId(), erpOrderDTO.getOrderNo(), erpOrderDTO.getPrimeLineNo());
//                        if (found > 0) {
//                            //Found Update shipment
//                            versionId = this.jdbcTemplate.queryForObject(getVersionIdOrderNoPrimeLineNo, Integer.class, erpOrderDTO.getOrderNo(), erpOrderDTO.getPrimeLineNo());
//                            this.jdbcTemplate.update(updateShipmentBasedOnOnderNoPrimeLineNo, curDate, curDate, erpOrderDTO.getOrderNo(), erpOrderDTO.getPrimeLineNo(), erpOrderDTO.getShipmentId(), versionId);
//                            this.programDataDaoImpl.getNewSupplyPlanList(programId, versionId, true);
//                        } else {
//                            //Insert into shipment table
//                            params1 = new MapSqlParameterSource();
////                                    params1.addValue("qty", erpOrderDTO.getQuantity());
//                            params1.addValue("createdDate", curDate);
//                            params1.addValue("lastModifiedDate", curDate);
//                            params1.addValue("shipmentId", erpOrderDTO.getShipmentId());
//                            namedParameterJdbcTemplate.update(createNewEntryInShipmentTableDiff, params1, keyHolder1);
//                            if (keyHolder1.getKey() != null) {
//                                shipmentId = keyHolder1.getKey().intValue();
//                            }
//
//                            //Shipment Trans
//                            params1 = new MapSqlParameterSource();
//                            params1.addValue("shipmentId1", erpOrderDTO.getShipmentId());
//                            params1.addValue("shipmentId", shipmentId);
//                            params1.addValue("CURDATE", curDate);
//                            params1.addValue("CURDATE1", curDate);
//                            params1.addValue("orderNo", erpOrderDTO.getOrderNo());
//                            params1.addValue("primeLineNo", erpOrderDTO.getPrimeLineNo());
//                            namedParameterJdbcTemplate.update(createNewEntryIntoShipmentTrans, params1, keyHolder2);
//                            if (keyHolder2.getKey() != null) {
//                                shipmentTransId = keyHolder2.getKey().intValue();
//                            }
//
//                            //Shipment trans batch info
//                            this.jdbcTemplate.update(insertIntoShipmentTransBatchInfo, shipmentTransId, programId, planningUnitId, erpOrderDTO.getErpOrderId());
//                            versionId = this.jdbcTemplate.queryForObject(getMaxVersionFromShipmentTable, Integer.class, shipmentId);
//                            this.programDataDaoImpl.getNewSupplyPlanList(programId, versionId, true);
//                        }
//
//                    } else {
//                        // Not erp linked
//                        sql = "SELECT MAX(st1.`VERSION_ID`) FROM rm_shipment_trans st1 WHERE st1.`SHIPMENT_ID`=?";
//                        versionId = this.jdbcTemplate.queryForObject(sql, Integer.class, erpOrderDTO.getShipmentId());
//                        System.out.println("version id---" + versionId);
//
//                        this.jdbcTemplate.update(deactivateParentAndMarkErpLinked, erpOrderDTO.getShipmentId(), versionId);
//                        //Shipment Table
//                        params1.addValue("createdDate", curDate);
//                        params1.addValue("lastModifiedDate", curDate);
//                        params1.addValue("shipmentId", erpOrderDTO.getShipmentId());
//                        namedParameterJdbcTemplate.update(createNewEntryInShipmentTableDiff, params1, keyHolder1);
//                        if (keyHolder1.getKey() != null) {
//                            shipmentId = keyHolder1.getKey().intValue();
//                        }
//                        //Shipment Trans
//                        params1 = new MapSqlParameterSource();
//                        params1.addValue("shipmentId", shipmentId);
//                        params1.addValue("shipmentId1", erpOrderDTO.getShipmentId());
//                        params1.addValue("CURDATE", curDate);
//                        params1.addValue("CURDATE1", curDate);
//                        params1.addValue("orderNo", erpOrderDTO.getOrderNo());
//                        params1.addValue("primeLineNo", erpOrderDTO.getPrimeLineNo());
//                        namedParameterJdbcTemplate.update(createNewEntryIntoShipmentTrans, params1, keyHolder2);
//                        if (keyHolder2.getKey() != null) {
//                            shipmentTransId = keyHolder2.getKey().intValue();
//                        }
//                        //insert Into Shipment Trans Batch Info
//                        this.jdbcTemplate.update(insertIntoShipmentTransBatchInfo, shipmentTransId, programId, planningUnitId, erpOrderDTO.getErpOrderId());
//                        this.programDataDaoImpl.getNewSupplyPlanList(programId, versionId, true);
//                    }
//                } // manual tagging--------------------------------------------------------------
//                else {
//                    // Find manually tagged shipment id
//                    logger.info("Going to check manual tagging---Order No" + erpOrderDTO.getOrderNo() + " Prime line no---" + erpOrderDTO.getPrimeLineNo());
//                    sql = "SELECT m.`SHIPMENT_ID` FROM rm_manual_tagging m WHERE m.`ORDER_NO`=? AND m.`PRIME_LINE_NO`=? AND m.`ACTIVE`=1;";
//                    try {
//                        shipmentId = this.jdbcTemplate.queryForObject(sql, Integer.class, erpOrderDTO.getOrderNo(), erpOrderDTO.getPrimeLineNo());
//                    } catch (Exception e) {
//                        shipmentId = 0;
//                    }
//                    logger.info("Manual tagging shipment id---" + shipmentId);
//                    if (shipmentId != 0) {
//                        // get program id
//                        programId = this.jdbcTemplate.queryForObject(getProgramId, Integer.class, shipmentId);
//                        //Get planning unit
//                        planningUnitId = this.jdbcTemplate.queryForObject(getPlanningUnitId, Integer.class, erpOrderDTO.getErpOrderId());
//
//                        // Insert into batch
//                        this.jdbcTemplate.update(insertIntoBatch, programId, planningUnitId, curDate, erpOrderDTO.getErpOrderId());
//                        // Is ERP linked
//                        isErpLinked = this.jdbcTemplate.queryForObject(isShipmentErpLinked, Boolean.class, shipmentId);
//                        if (isErpLinked) {
////                                    sql = "SELECT COUNT(*) FROM rm_shipment_trans st "
////                                            + "LEFT JOIN rm_shipment s ON s.`SHIPMENT_ID`=st.`SHIPMENT_ID` "
////                                            + "WHERE s.`PARENT_SHIPMENT_ID`=? AND st.`ACTIVE` AND st.`ORDER_NO`=? AND st.`PRIME_LINE_NO`=?;";
//                            found = this.jdbcTemplate.queryForObject(hasChildShipments, Integer.class, erpOrderDTO.getShipmentId(), erpOrderDTO.getOrderNo(), erpOrderDTO.getPrimeLineNo());
//                            if (found > 0) {
//                                //Found Update shipment
//                                // Update shipment trans
//                                versionId = this.jdbcTemplate.queryForObject(getVersionIdOrderNoPrimeLineNo, Integer.class, shipmentId);
//                                this.jdbcTemplate.update(updateShipmentBasedOnOnderNoPrimeLineNo, curDate, erpOrderDTO.getOrderNo(), erpOrderDTO.getPrimeLineNo(), shipmentId, versionId);
//                                this.programDataDaoImpl.getNewSupplyPlanList(programId, versionId, true);
//                            } else {
//                                //Not found-Insert shipment
//                                //Insert into shipment table
//                                params1 = new MapSqlParameterSource();
////                                    params1.addValue("qty", erpOrderDTO.getQuantity());
//                                params1.addValue("createdDate", curDate);
//                                params1.addValue("lastModifiedDate", curDate);
//                                params1.addValue("shipmentId", shipmentId);
//                                namedParameterJdbcTemplate.update(createNewEntryInShipmentTableDiff, params1, keyHolder1);
//                                int newShipId = 0;
//                                if (keyHolder1.getKey() != null) {
//                                    newShipId = keyHolder1.getKey().intValue();
//                                }
//
//                                //Shipment Trans
//                                params1 = new MapSqlParameterSource();
//                                params1.addValue("shipmentId", newShipId);
//                                params1.addValue("shipmentId1", shipmentId);
//                                params1.addValue("CURDATE", curDate);
//                                params1.addValue("CURDATE1", curDate);
//                                params1.addValue("orderNo", erpOrderDTO.getOrderNo());
//                                params1.addValue("primeLineNo", erpOrderDTO.getPrimeLineNo());
//                                namedParameterJdbcTemplate.update(createNewEntryIntoShipmentTrans, params1, keyHolder2);
//                                if (keyHolder2.getKey() != null) {
//                                    shipmentTransId = keyHolder2.getKey().intValue();
//                                }
//
//                                //Insert Into Shipment Trans Batch Info
//                                this.jdbcTemplate.update(insertIntoShipmentTransBatchInfo, shipmentTransId, programId, planningUnitId, erpOrderDTO.getErpOrderId());
//                                versionId = this.jdbcTemplate.queryForObject(getMaxVersionFromShipmentTable, Integer.class, shipmentId);
//                                this.programDataDaoImpl.getNewSupplyPlanList(programId, versionId, true);
//                            }
//                        } else {
//                            sql = "SELECT MAX(st1.`VERSION_ID`) FROM rm_shipment_trans st1 WHERE st1.`SHIPMENT_ID`=?";
//                            versionId = this.jdbcTemplate.queryForObject(sql, Integer.class, erpOrderDTO.getShipmentId());
//                            this.jdbcTemplate.update(deactivateParentAndMarkErpLinked, shipmentId, versionId);
//                            //Shipment Table
//                            params1 = new MapSqlParameterSource();
////                                    params1.addValue("qty", erpOrderDTO.getQuantity());
//                            params1.addValue("createdDate", curDate);
//                            params1.addValue("lastModifiedDate", curDate);
//                            params1.addValue("shipmentId", shipmentId);
//                            System.out.println("params1 manual tagging ---" + params1);
//                            System.out.println("createNewEntryInShipmentTable manual tagging ---" + createNewEntryInShipmentTable);
//                            namedParameterJdbcTemplate.update(createNewEntryInShipmentTableDiff, params1, keyHolder1);
//                            int newShipid = 0;
//                            if (keyHolder1.getKey() != null) {
//                                System.out.println("inside mt shipment");
//                                newShipid = keyHolder1.getKey().intValue();
//                            }
//                            System.out.println("my manual tagging shipment id---" + shipmentId);
//                            //Shipment Trans
//                            params1 = new MapSqlParameterSource();
//                            params1.addValue("shipmentId", newShipid);
//                            params1.addValue("shipmentId1", shipmentId);
//                            params1.addValue("CURDATE", curDate);
//                            params1.addValue("CURDATE1", curDate);
//                            params1.addValue("orderNo", erpOrderDTO.getOrderNo());
//                            params1.addValue("primeLineNo", erpOrderDTO.getPrimeLineNo());
//                            System.out.println("params1---------" + params1);
//                            namedParameterJdbcTemplate.update(createNewEntryIntoShipmentTrans, params1, keyHolder2);
//                            if (keyHolder2.getKey() != null) {
//                                shipmentTransId = keyHolder2.getKey().intValue();
//                            }
//                            System.out.println("manual tagging shipment trans---" + shipmentTransId);
//                            //insert Into Shipment Trans Batch Info
//                            this.jdbcTemplate.update(insertIntoShipmentTransBatchInfo, shipmentTransId, programId, planningUnitId, erpOrderDTO.getErpOrderId());
//                            versionId = this.jdbcTemplate.queryForObject(getMaxVersionFromShipmentTable, Integer.class, newShipid);
//                            this.programDataDaoImpl.getNewSupplyPlanList(programId, versionId, true);
//                        }
//                    }
//
//                }
//            } catch (Exception e) {
//                logger.info("Error occured while creating shipment---" + e);
//                e.printStackTrace();
//            }
//
//        }
//
//        logger.info(
//                "Order/Shipment file imported successfully");
//        File directory = new File(QAT_FILE_PATH + BKP_CATALOG_FILE_PATH);
//
//        if (directory.isDirectory()) {
//            fXmlFile.renameTo(new File(QAT_FILE_PATH + BKP_CATALOG_FILE_PATH + fXmlFile.getName()));
//            fXmlFile1.renameTo(new File(QAT_FILE_PATH + BKP_CATALOG_FILE_PATH + fXmlFile1.getName()));
//            logger.info("Order/Shipment files moved into processed folder");
//        } else {
//            subjectParam = new String[]{"Order/Shipment", "Backup directory does not exists"};
//            bodyParam = new String[]{"Order/Shipment", date, "Backup directory does not exists", "Backup directory does not exists"};
//            emailer = this.emailService.buildEmail(emailTemplate.getEmailTemplateId(), toList, ccList, subjectParam, bodyParam);
//            int emailerId = this.emailService.saveEmail(emailer);
//            emailer.setEmailerId(emailerId);
//            this.emailService.sendMail(emailer);
//            logger.error("Backup directory does not exists");
//        }
//    }
//}
//}
//
////    SELECT * FROM rm_shipment_trans st 
////LEFT JOIN rm_shipment s ON s.`SHIPMENT_ID`=st.`SHIPMENT_ID`
////WHERE s.`PROGRAM_ID`=1 AND st.`PLANNING_UNIT_ID`=1191
////ORDER BY st.`SHIPMENT_TRANS_ID` DESC LIMIT 1
//}