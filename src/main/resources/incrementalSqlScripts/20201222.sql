USE `fasp`;
    DROP procedure IF EXISTS `stockStatusReportVertical`;

DELIMITER $$
USE `fasp`$$
CREATE DEFINER=`faspUser`@`%` PROCEDURE `stockStatusReportVertical`(VAR_START_DATE DATE, VAR_STOP_DATE DATE, VAR_PROGRAM_ID INT(10), VAR_VERSION_ID INT, VAR_PLANNING_UNIT_ID INT(10))
BEGIN
	-- %%%%%%%%%%%%%%%%%%%%%
    -- Report no 16
	-- %%%%%%%%%%%%%%%%%%%%%
	
        SET @startDate = VAR_START_DATE;
	SET @stopDate = VAR_STOP_DATE;
	SET @programId = VAR_PROGRAM_ID;
	SET @versionId = VAR_VERSION_ID;
	SET @planningUnitId = VAR_PLANNING_UNIT_ID;

	IF @versionId = -1 THEN 
		SELECT MAX(pv.VERSION_ID) INTO @versionId FROM rm_program_version pv WHERE pv.PROGRAM_ID=@programId;
	END IF;
    
	SELECT 
        s2.`TRANS_DATE`, 
        COALESCE(s2.`FINAL_OPENING_BALANCE`, @prvMonthClosingBal) `FINAL_OPENING_BALANCE`,
        s2.`ACTUAL_CONSUMPTION_QTY`, s2.`FORECASTED_CONSUMPTION_QTY`, 
        IF(s2.`ACTUAL`, s2.`ACTUAL_CONSUMPTION_QTY`,s2.`FORECASTED_CONSUMPTION_QTY`) `FINAL_CONSUMPTION_QTY`,
        s2.`ACTUAL`,
        s2.`SQTY` ,
        s2.`ADJUSTMENT`,
        s2.EXPIRED_STOCK,
        COALESCE(s2.`FINAL_CLOSING_BALANCE`, @prvMonthClosingBal) `FINAL_CLOSING_BALANCE`,
        s2.AMC,
        s2.`MoS`,
        s2.`MIN_MONTHS_OF_STOCK`,
        s2.`MAX_MONTHS_OF_STOCK`,
        s2.`SHIPMENT_ID`, s2.`SHIPMENT_QTY`, 
        s2.`FUNDING_SOURCE_ID`, s2.`FUNDING_SOURCE_CODE`, s2.`FUNDING_SOURCE_LABEL_ID`, s2.`FUNDING_SOURCE_LABEL_EN`, s2.`FUNDING_SOURCE_LABEL_FR`, s2.`FUNDING_SOURCE_LABEL_SP`, s2.`FUNDING_SOURCE_LABEL_PR`, 
        s2.PROCUREMENT_AGENT_ID, s2.PROCUREMENT_AGENT_CODE, s2.`PROCUREMENT_AGENT_LABEL_ID`, s2.`PROCUREMENT_AGENT_LABEL_EN`, s2.`PROCUREMENT_AGENT_LABEL_FR`, s2.`PROCUREMENT_AGENT_LABEL_SP`, s2.`PROCUREMENT_AGENT_LABEL_PR`, 
        s2.SHIPMENT_STATUS_ID, s2.`SHIPMENT_STATUS_LABEL_ID`, s2.`SHIPMENT_STATUS_LABEL_EN`, s2.`SHIPMENT_STATUS_LABEL_FR`, s2.`SHIPMENT_STATUS_LABEL_SP`, s2.`SHIPMENT_STATUS_LABEL_PR`,
        @prvMonthClosingBal:=COALESCE(s2.`FINAL_CLOSING_BALANCE`, s2.`FINAL_OPENING_BALANCE`, @prvMonthClosingBal) `PRV_CLOSING_BAL`
    FROM (
	SELECT 
        mn.MONTH `TRANS_DATE`, 
        sma.OPENING_BALANCE `FINAL_OPENING_BALANCE`, 
        sma.ACTUAL_CONSUMPTION_QTY, sma.FORECASTED_CONSUMPTION_QTY, 
        sma.ACTUAL,
        sma.SHIPMENT_QTY SQTY,
        sma.ADJUSTMENT_MULTIPLIED_QTY `ADJUSTMENT`,
        sma.EXPIRED_STOCK,
        sma.CLOSING_BALANCE `FINAL_CLOSING_BALANCE`,
        sma.AMC,
        sma.MOS `MoS`,
        IF(ppu.MIN_MONTHS_OF_STOCK<r.MIN_MOS_MIN_GAURDRAIL, r.MIN_MOS_MIN_GAURDRAIL, ppu.MIN_MONTHS_OF_STOCK) `MIN_MONTHS_OF_STOCK`, 
        IF(
            IF(ppu.MIN_MONTHS_OF_STOCK<r.MIN_MOS_MIN_GAURDRAIL, r.MIN_MOS_MIN_GAURDRAIL, ppu.MIN_MONTHS_OF_STOCK)+ppu.REORDER_FREQUENCY_IN_MONTHS<r.MIN_MOS_MAX_GAURDRAIL, 
            r.MIN_MOS_MAX_GAURDRAIL, 
            IF(
                IF(ppu.MIN_MONTHS_OF_STOCK<r.MIN_MOS_MIN_GAURDRAIL, r.MIN_MOS_MIN_GAURDRAIL, ppu.MIN_MONTHS_OF_STOCK)+ppu.REORDER_FREQUENCY_IN_MONTHS>r.MAX_MOS_MAX_GAURDRAIL,
                r.MAX_MOS_MAX_GAURDRAIL,
                IF(ppu.MIN_MONTHS_OF_STOCK<r.MIN_MOS_MIN_GAURDRAIL, r.MIN_MOS_MIN_GAURDRAIL, ppu.MIN_MONTHS_OF_STOCK)+ppu.REORDER_FREQUENCY_IN_MONTHS
            )
        ) `MAX_MONTHS_OF_STOCK`,
        sh.SHIPMENT_ID, sh.SHIPMENT_QTY, 
        fs.FUNDING_SOURCE_ID, fs.FUNDING_SOURCE_CODE, fs.LABEL_ID `FUNDING_SOURCE_LABEL_ID`, fs.LABEL_EN `FUNDING_SOURCE_LABEL_EN`, fs.LABEL_FR `FUNDING_SOURCE_LABEL_FR`, fs.LABEL_SP `FUNDING_SOURCE_LABEL_SP`, fs.LABEL_PR `FUNDING_SOURCE_LABEL_PR`, 
        pa.PROCUREMENT_AGENT_ID, pa.PROCUREMENT_AGENT_CODE, pa.LABEL_ID `PROCUREMENT_AGENT_LABEL_ID`, pa.LABEL_EN `PROCUREMENT_AGENT_LABEL_EN`, pa.LABEL_FR `PROCUREMENT_AGENT_LABEL_FR`, pa.LABEL_SP `PROCUREMENT_AGENT_LABEL_SP`, pa.LABEL_PR `PROCUREMENT_AGENT_LABEL_PR`, 
        ss.SHIPMENT_STATUS_ID, ss.LABEL_ID `SHIPMENT_STATUS_LABEL_ID`, ss.LABEL_EN `SHIPMENT_STATUS_LABEL_EN`, ss.LABEL_Fr `SHIPMENT_STATUS_LABEL_FR`, ss.LABEL_SP `SHIPMENT_STATUS_LABEL_SP`, ss.LABEL_PR `SHIPMENT_STATUS_LABEL_PR`
    FROM
        mn 
        LEFT JOIN rm_supply_plan_amc sma ON 
            mn.MONTH=sma.TRANS_DATE 
            AND sma.PROGRAM_ID = @programId
            AND sma.VERSION_ID = @versionId
            AND sma.PLANNING_UNIT_ID = @planningUnitId
        LEFT JOIN 
            (
            SELECT COALESCE(st.RECEIVED_DATE, st.EXPECTED_DELIVERY_DATE) `EDD`, s.SHIPMENT_ID, st.SHIPMENT_QTY , st.FUNDING_SOURCE_ID, st.PROCUREMENT_AGENT_ID, st.SHIPMENT_STATUS_ID
            FROM 
                (
                SELECT s.SHIPMENT_ID, MAX(st.VERSION_ID) MAX_VERSION_ID FROM rm_shipment s LEFT JOIN rm_shipment_trans st ON s.SHIPMENT_ID=st.SHIPMENT_ID WHERE s.PROGRAM_ID=@programId AND st.VERSION_ID<=@versionId AND st.SHIPMENT_TRANS_ID IS NOT NULL GROUP BY s.SHIPMENT_ID 
            ) AS s 
            LEFT JOIN rm_shipment_trans st ON s.SHIPMENT_ID=st.SHIPMENT_ID AND s.MAX_VERSION_ID=st.VERSION_ID 
            WHERE 
                st.ACTIVE 
                AND st.SHIPMENT_STATUS_ID != 8 
                AND st.ACCOUNT_FLAG
                AND COALESCE(st.RECEIVED_DATE, st.EXPECTED_DELIVERY_DATE) BETWEEN @startDate AND @stopDate 
                AND st.PLANNING_UNIT_ID =@planningUnitId
        ) sh ON LEFT(sma.TRANS_DATE,7)=LEFT(sh.EDD,7)
        LEFT JOIN vw_funding_source fs ON sh.FUNDING_SOURCE_ID=fs.FUNDING_SOURCE_ID
        LEFT JOIN vw_procurement_agent pa ON sh.PROCUREMENT_AGENT_ID=pa.PROCUREMENT_AGENT_ID
        LEFT JOIN vw_shipment_status ss ON sh.SHIPMENT_STATUS_ID=ss.SHIPMENT_STATUS_ID
        LEFT JOIN rm_program_planning_unit ppu ON ppu.PROGRAM_ID=@programId AND ppu.PLANNING_UNIT_ID=@planningUnitId
        LEFT JOIN rm_program p ON p.PROGRAM_ID=@programId
        LEFT JOIN rm_realm_country rc ON p.REALM_COUNTRY_ID=rc.REALM_COUNTRY_ID
        LEFT JOIN rm_realm r ON rc.REALM_ID=r.REALM_ID
    WHERE
        mn.MONTH BETWEEN @startDate AND @stopDate
    ORDER BY mn.MONTH) AS s2;
    
END$$

DELIMITER ;




USE `fasp`;
DROP procedure IF EXISTS `buildNewSupplyPlanRegion`;

DELIMITER $$
USE `fasp`$$
CREATE DEFINER=`faspUser`@`%` PROCEDURE `buildNewSupplyPlanRegion`(VAR_PROGRAM_ID INT(10), VAR_VERSION_ID INT(10))
BEGIN
    SET @programId = VAR_PROGRAM_ID;
    SET @versionId = VAR_VERSION_ID;
    
    SELECT COUNT(*) INTO @currentCount FROM rm_supply_plan_amc spa WHERE spa.PROGRAM_ID=@programId AND spa.VERSION_ID=@versionId;
    -- Get the Region count for this Program
    SELECT count(*) INTO @regionCount FROM rm_program_region pr WHERE pr.PROGRAM_ID=@programId;
        
    DELETE tn.* FROM tmp_nsp tn WHERE tn.PROGRAM_ID=@programId AND tn.VERSION_ID=@versionId;
        
    -- DELETE nsps.* FROM rm_nsp_summary nsps WHERE nsps.PROGRAM_ID=@programId AND nsps.VERSION_ID=@versionId;
    -- DELETE nspr.* FROM rm_nsp_region nspr WHERE nspr.PROGRAM_ID=@programId AND nspr.VERSION_ID=@versionId;
       
    -- Populate the nsp_region table with all the raw data that we have for Consumption, Inventory and Shipment per Region
    INSERT INTO tmp_nsp (
        PROGRAM_ID, VERSION_ID, PLANNING_UNIT_ID, TRANS_DATE, REGION_ID, 
        FORECASTED_CONSUMPTION, ACTUAL_CONSUMPTION, ADJUSTMENT, STOCK, REGION_COUNT, 
        MANUAL_PLANNED_SHIPMENT, MANUAL_SUBMITTED_SHIPMENT, MANUAL_APPROVED_SHIPMENT, MANUAL_SHIPPED_SHIPMENT, MANUAL_RECEIVED_SHIPMENT, MANUAL_ONHOLD_SHIPMENT, 
        ERP_PLANNED_SHIPMENT, ERP_SUBMITTED_SHIPMENT, ERP_APPROVED_SHIPMENT, ERP_SHIPPED_SHIPMENT, ERP_RECEIVED_SHIPMENT, ERP_ONHOLD_SHIPMENT 
    )
    SELECT 
        @programId `PROGRAM_ID`, @versionId, m.`PLANNING_UNIT_ID`, m.`TRANS_DATE`, o.`REGION_ID`, 
        SUM(o.`FORECASTED_CONSUMPTION`), SUM(o.`ACTUAL_CONSUMPTION`), SUM(o.`ADJUSTMENT`), SUM(o.`STOCK`), @regionCount, 
        SUM(o.`MANUAL_PLANNED_SHIPMENT`), SUM(o.`MANUAL_SUBMITTED_SHIPMENT`), SUM(o.`MANUAL_APPROVED_SHIPMENT`), SUM(o.`MANUAL_SHIPPED_SHIPMENT`), SUM(o.`MANUAL_RECEIVED_SHIPMENT`), SUM(o.`MANUAL_ONHOLD_SHIPMENT`), 
        SUM(o.`ERP_PLANNED_SHIPMENT`), SUM(o.`ERP_SUBMITTED_SHIPMENT`), SUM(o.`ERP_APPROVED_SHIPMENT`), SUM(o.`ERP_SHIPPED_SHIPMENT`), SUM(o.`ERP_RECEIVED_SHIPMENT`), SUM(o.`ERP_ONHOLD_SHIPMENT`)
    FROM (SELECT a3.PLANNING_UNIT_ID, mn.MONTH `TRANS_DATE` FROM (
    SELECT a2.PLANNING_UNIT_ID, MIN(a2.TRANS_DATE) `MIN_TRANS_DATE`, MAX(a2.TRANS_DATE) `MAX_TRANS_DATE` FROM (
        SELECT 
            tc.`PLANNING_UNIT_ID`, `TRANS_DATE`
        FROM (
            SELECT 
                ct.PLANNING_UNIT_ID, LEFT(ct.`CONSUMPTION_DATE`,7) `TRANS_DATE`
            FROM (
                SELECT c.`CONSUMPTION_ID`, MAX(ct.`VERSION_ID`) `MAX_VERSION_ID` FROM rm_consumption c LEFT JOIN rm_consumption_trans ct ON c.`CONSUMPTION_ID`=ct.`CONSUMPTION_ID` WHERE c.`PROGRAM_ID`=@programId AND ct.`VERSION_ID`<=@versionId AND ct.`CONSUMPTION_TRANS_ID` IS NOT NULL GROUP BY c.`CONSUMPTION_ID`
            ) tc
            LEFT JOIN rm_consumption c ON c.`CONSUMPTION_ID`=tc.`CONSUMPTION_ID`
            LEFT JOIN rm_consumption_trans ct ON c.`CONSUMPTION_ID`=ct.`CONSUMPTION_ID` AND tc.`MAX_VERSION_ID`=ct.`VERSION_ID`
            WHERE ct.`ACTIVE`
            GROUP BY c.`PROGRAM_ID`, ct.`PLANNING_UNIT_ID`, ct.`CONSUMPTION_DATE`, ct.`REGION_ID`
        ) tc 
        GROUP BY tc.`PLANNING_UNIT_ID`, tc.`TRANS_DATE`

        UNION

        SELECT 
            st.PLANNING_UNIT_ID, LEFT(COALESCE(st.`RECEIVED_DATE`, st.`EXPECTED_DELIVERY_DATE`),7) `TRANS_DATE`
        FROM (
            SELECT s.PROGRAM_ID, s.SHIPMENT_ID, MAX(st.VERSION_ID) MAX_VERSION_ID FROM rm_shipment s LEFT JOIN rm_shipment_trans st ON s.SHIPMENT_ID=st.SHIPMENT_ID WHERE s.PROGRAM_ID=@programId AND st.VERSION_ID<=@versionId AND st.SHIPMENT_TRANS_ID IS NOT NULL GROUP BY s.SHIPMENT_ID
        ) ts
        LEFT JOIN rm_shipment s ON s.SHIPMENT_ID=ts.SHIPMENT_ID
        LEFT JOIN rm_shipment_trans st ON s.SHIPMENT_ID=st.SHIPMENT_ID AND ts.MAX_VERSION_ID=st.VERSION_ID
        WHERE st.ACTIVE AND st.ACCOUNT_FLAG AND st.SHIPMENT_STATUS_ID!=8 
        GROUP BY st.PLANNING_UNIT_ID, COALESCE(st.RECEIVED_DATE, st.EXPECTED_DELIVERY_DATE)

        UNION

        SELECT 
            rcpu.PLANNING_UNIT_ID, LEFT(it.INVENTORY_DATE,7) `TRANS_DATE`
        FROM (
            SELECT i.PROGRAM_ID, i.INVENTORY_ID, MAX(it.VERSION_ID) MAX_VERSION_ID FROM rm_inventory i LEFT JOIN rm_inventory_trans it ON i.INVENTORY_ID=it.INVENTORY_ID WHERE i.PROGRAM_ID=@programId AND it.VERSION_ID<=@versionId AND it.INVENTORY_TRANS_ID IS NOT NULL GROUP BY i.INVENTORY_ID
        ) ti
        LEFT JOIN rm_inventory i ON i.INVENTORY_ID=ti.INVENTORY_ID
        LEFT JOIN rm_inventory_trans it ON i.INVENTORY_ID=it.INVENTORY_ID AND ti.MAX_VERSION_ID=it.VERSION_ID
        LEFT JOIN rm_realm_country_planning_unit rcpu ON it.REALM_COUNTRY_PLANNING_UNIT_ID=rcpu.REALM_COUNTRY_PLANNING_UNIT_ID
        WHERE it.ACTIVE
        GROUP BY rcpu.PLANNING_UNIT_ID, it.INVENTORY_DATE) as a2 GROUP BY a2.PLANNING_UNIT_ID) as a3 LEFT JOIN mn ON LEFT(mn.MONTH,7) between a3.MIN_TRANS_DATE AND a3.MAX_TRANS_DATE) AS m LEFT JOIN 
        (
            SELECT 
                tc.`PROGRAM_ID`, tc.`PLANNING_UNIT_ID`, LEFT(tc.`CONSUMPTION_DATE`, 7) `TRANS_DATE`, tc.`REGION_ID`, 
                SUM(tc.`FORECASTED_CONSUMPTION`) `FORECASTED_CONSUMPTION`, SUM(tc.`ACTUAL_CONSUMPTION`) `ACTUAL_CONSUMPTION`, null `ADJUSTMENT`, null `STOCK`, 
                null `MANUAL_PLANNED_SHIPMENT`, null `MANUAL_SUBMITTED_SHIPMENT`, null `MANUAL_APPROVED_SHIPMENT`, null `MANUAL_SHIPPED_SHIPMENT`, null `MANUAL_RECEIVED_SHIPMENT`, null `MANUAL_ONHOLD_SHIPMENT`, 
                null `ERP_PLANNED_SHIPMENT`, null `ERP_SUBMITTED_SHIPMENT`, null `ERP_APPROVED_SHIPMENT`, null `ERP_SHIPPED_SHIPMENT`, null `ERP_RECEIVED_SHIPMENT`, null `ERP_ONHOLD_SHIPMENT`
            FROM (
                SELECT 
                    c.`PROGRAM_ID`, ct.`PLANNING_UNIT_ID`, ct.`CONSUMPTION_DATE`, ct.`REGION_ID`, 
                    ct.`ACTIVE`, 
                    SUM(IF(ct.`ACTUAL_FLAG`=0, ct.`CONSUMPTION_QTY`, null)) `FORECASTED_CONSUMPTION`,
                    SUM(IF(ct.`ACTUAL_FLAG`=1, ct.`CONSUMPTION_QTY`, null)) `ACTUAL_CONSUMPTION`
                FROM (
                    SELECT c.`CONSUMPTION_ID`, MAX(ct.`VERSION_ID`) `MAX_VERSION_ID` FROM rm_consumption c LEFT JOIN rm_consumption_trans ct ON c.`CONSUMPTION_ID`=ct.`CONSUMPTION_ID` WHERE c.`PROGRAM_ID`=@programId AND ct.`VERSION_ID`<=@versionId AND ct.`CONSUMPTION_TRANS_ID` IS NOT NULL GROUP BY c.`CONSUMPTION_ID`
                ) tc
                LEFT JOIN rm_consumption c ON c.`CONSUMPTION_ID`=tc.`CONSUMPTION_ID`
                LEFT JOIN rm_consumption_trans ct ON c.`CONSUMPTION_ID`=ct.`CONSUMPTION_ID` AND tc.`MAX_VERSION_ID`=ct.`VERSION_ID`
                WHERE ct.`ACTIVE`
                GROUP BY c.`PROGRAM_ID`, ct.`PLANNING_UNIT_ID`, ct.`CONSUMPTION_DATE`, ct.`REGION_ID`
            ) tc 
            GROUP BY tc.`PROGRAM_ID`, tc.`PLANNING_UNIT_ID`, tc.`CONSUMPTION_DATE`, tc.`REGION_ID`

            UNION

            SELECT 
                s.`PROGRAM_ID`, st.`PLANNING_UNIT_ID`, LEFT(COALESCE(st.`RECEIVED_DATE`, st.`EXPECTED_DELIVERY_DATE`),7) `TRANS_DATE`, null `REGION_ID`,
                null `FORECASTED_CONSUMPTION`, null `ACTUAL_CONSUMPTION`, null `ADJUSTMENT`, null `STOCK`,
                SUM(IF((st.ERP_FLAG IS NULL OR st.ERP_FLAG=0) AND st.`SHIPMENT_STATUS_ID`=1, st.`SHIPMENT_QTY`, null )) `MANUAL_PLANNED_SHIPMENT`, 
                SUM(IF((st.ERP_FLAG IS NULL OR st.ERP_FLAG=0) AND st.`SHIPMENT_STATUS_ID`=3, st.`SHIPMENT_QTY`, null )) `MANUAL_SUBMITTED_SHIPMENT`, 
                SUM(IF((st.ERP_FLAG IS NULL OR st.ERP_FLAG=0) AND st.`SHIPMENT_STATUS_ID`=4, st.`SHIPMENT_QTY`, null )) `MANUAL_APPROVED_SHIPMENT`, 
                SUM(IF((st.ERP_FLAG IS NULL OR st.ERP_FLAG=0) AND st.`SHIPMENT_STATUS_ID` IN (5,6), st.`SHIPMENT_QTY`, null )) `MANUAL_SHIPPED_SHIPMENT`, 
                SUM(IF((st.ERP_FLAG IS NULL OR st.ERP_FLAG=0) AND st.`SHIPMENT_STATUS_ID`=7, st.`SHIPMENT_QTY`, null )) `MANUAL_RECEIVED_SHIPMENT`, 
                SUM(IF((st.ERP_FLAG IS NULL OR st.ERP_FLAG=0) AND st.`SHIPMENT_STATUS_ID`=9, st.`SHIPMENT_QTY`, null )) `MANUAL_ONHOLD_SHIPMENT`, 
                SUM(IF(st.`ERP_FLAG`=1 AND st.`SHIPMENT_STATUS_ID`=1, st.`SHIPMENT_QTY`, null )) `ERP_PLANNED_SHIPMENT`, 
                SUM(IF(st.`ERP_FLAG`=1 AND st.`SHIPMENT_STATUS_ID`=3, st.`SHIPMENT_QTY`, null )) `ERP_SUBMITTED_SHIPMENT`, 
                SUM(IF(st.`ERP_FLAG`=1 AND st.`SHIPMENT_STATUS_ID`=4, st.`SHIPMENT_QTY`, null )) `ERP_APPROVED_SHIPMENT`, 
                SUM(IF(st.`ERP_FLAG`=1 AND st.`SHIPMENT_STATUS_ID` IN (5,6), st.`SHIPMENT_QTY`, null )) `ERP_SHIPPED_SHIPMENT`, 
                SUM(IF(st.`ERP_FLAG`=1 AND st.`SHIPMENT_STATUS_ID` = 7, st.`SHIPMENT_QTY`, null )) `ERP_RECEIVED_SHIPMENT`, 
                SUM(IF(st.`ERP_FLAG`=1 AND st.`SHIPMENT_STATUS_ID`=9, st.`SHIPMENT_QTY`, null )) `ERP_ONHOLD_SHIPMENT`
            FROM (
                SELECT s.PROGRAM_ID, s.SHIPMENT_ID, MAX(st.VERSION_ID) MAX_VERSION_ID FROM rm_shipment s LEFT JOIN rm_shipment_trans st ON s.SHIPMENT_ID=st.SHIPMENT_ID WHERE s.PROGRAM_ID=@programId AND st.VERSION_ID<=@versionId AND st.SHIPMENT_TRANS_ID IS NOT NULL GROUP BY s.SHIPMENT_ID
            ) ts
            LEFT JOIN rm_shipment s ON s.SHIPMENT_ID=ts.SHIPMENT_ID
            LEFT JOIN rm_shipment_trans st ON s.SHIPMENT_ID=st.SHIPMENT_ID AND ts.MAX_VERSION_ID=st.VERSION_ID
            WHERE st.ACTIVE AND st.ACCOUNT_FLAG AND st.SHIPMENT_STATUS_ID!=8 
            GROUP BY s.PROGRAM_ID, st.PLANNING_UNIT_ID, COALESCE(st.RECEIVED_DATE, st.EXPECTED_DELIVERY_DATE)

            UNION

            SELECT 
                i.PROGRAM_ID, rcpu.PLANNING_UNIT_ID, LEFT(it.INVENTORY_DATE,7) `TRANS_DATE`, it.REGION_ID,
                null `FORECASTED_CONSUMPTION`, null `ACTUAL_CONSUMPTION`, SUM(it.ADJUSTMENT_QTY*rcpu.MULTIPLIER) `ADJUSTMENT`,  SUM(it.ACTUAL_QTY*rcpu.MULTIPLIER) `STOCK`,
                null `MANUAL_PLANNED_SHIPMENT`, null `MANUAL_SUBMITTED_SHIPMENT`, null `MANUAL_APPROVED_SHIPMENT`, null `MANUAL_SHIPPED_SHIPMENT`, null `MANUAL_RECEIVED_SHIPMENT`, null `MANUAL_ONHOLD_SHIPMENT`, 
                null `ERP_PLANNED_SHIPMENT`, null `ERP_SUBMITTED_SHIPMENT`, null `ERP_APPROVED_SHIPMENT`, null `ERP_SHIPPED_SHIPMENT`, null `ERP_RECEIVED_SHIPMENT`, null `ERP_ONHOLD_SHIPMENT`
            FROM (
                SELECT i.PROGRAM_ID, i.INVENTORY_ID, MAX(it.VERSION_ID) MAX_VERSION_ID FROM rm_inventory i LEFT JOIN rm_inventory_trans it ON i.INVENTORY_ID=it.INVENTORY_ID WHERE i.PROGRAM_ID=@programId AND it.VERSION_ID<=@versionId AND it.INVENTORY_TRANS_ID IS NOT NULL GROUP BY i.INVENTORY_ID
            ) ti
            LEFT JOIN rm_inventory i ON i.INVENTORY_ID=ti.INVENTORY_ID
            LEFT JOIN rm_inventory_trans it ON i.INVENTORY_ID=it.INVENTORY_ID AND ti.MAX_VERSION_ID=it.VERSION_ID
            LEFT JOIN rm_realm_country_planning_unit rcpu ON it.REALM_COUNTRY_PLANNING_UNIT_ID=rcpu.REALM_COUNTRY_PLANNING_UNIT_ID
            WHERE it.ACTIVE
            GROUP BY i.PROGRAM_ID, rcpu.PLANNING_UNIT_ID, it.INVENTORY_DATE, it.REGION_ID
        ) AS o ON m.PLANNING_UNIT_ID=o.PLANNING_UNIT_ID AND LEFT(m.TRANS_DATE,7)=o.TRANS_DATE GROUP BY m.PLANNING_UNIT_ID, m.TRANS_DATE, o.REGION_ID;
           
    -- Update the UseActualConsumption field = 1 
    -- IF All Regions have reported Consumption or if Sum(ActualConsumption)>Sum(ForecastedConsumption)
    -- ELSE UseActualConsumption field = 0
    UPDATE tmp_nsp tn LEFT JOIN (SELECT tn.PLANNING_UNIT_ID, tn.TRANS_DATE, SUM(IF(tn.ACTUAL_CONSUMPTION IS NOT NULL, 1,0)) `COUNT_OF_ACTUAL_CONSUMPTION`, SUM(tn.ACTUAL_CONSUMPTION) `TOTAL_ACTUAL_CONSUMPTION`, SUM(tn.FORECASTED_CONSUMPTION) `TOTAL_FORECASTED_CONSUMPTION` FROM tmp_nsp tn WHERE tn.PROGRAM_ID=@programId AND tn.VERSION_ID=@versionId AND tn.REGION_ID IS NOT NULL GROUP BY tn.PLANNING_UNIT_ID, tn.TRANS_DATE) rcount ON tn.PLANNING_UNIT_ID=rcount.PLANNING_UNIT_ID AND tn.TRANS_DATE=rcount.TRANS_DATE SET tn.USE_ACTUAL_CONSUMPTION=IF(rcount.COUNT_OF_ACTUAL_CONSUMPTION=@regionCount, 1, IF(rcount.TOTAL_ACTUAL_CONSUMPTION>rcount.TOTAL_FORECASTED_CONSUMPTION, 1, 0)) WHERE tn.PROGRAM_ID=@programId AND tn.VERSION_ID=@versionId AND tn.REGION_ID IS NOT NULL;
        
    -- Update the RegionStockCount field based on the number of Regions that have reported Stock
    UPDATE tmp_nsp tn LEFT JOIN (SELECT tn.PLANNING_UNIT_ID, tn.TRANS_DATE, COUNT(tn.STOCK) CNT FROM tmp_nsp tn WHERE tn.PROGRAM_ID=@programId AND tn.VERSION_ID=@versionId AND tn.REGION_ID IS NOT NULL GROUP BY tn.PLANNING_UNIT_ID, tn.TRANS_DATE, tn.REGION_ID) rcount ON tn.PLANNING_UNIT_ID=rcount.PLANNING_UNIT_ID AND tn.TRANS_DATE=rcount.TRANS_DATE SET tn.REGION_STOCK_COUNT = rcount.CNT WHERE tn.PROGRAM_ID=@programId AND tn.VERSION_ID=@versionId AND tn.REGION_ID IS NOT NULL;
        
    -- To get the range for AMC calculations
    -- SELECT MIN(sp.TRANS_DATE), ADDDATE(MAX(sp.TRANS_DATE), INTERVAL ppu.MONTHS_IN_PAST_FOR_AMC MONTH) INTO @startMonth, @stopMonth  FROM rm_supply_plan sp LEFT JOIN rm_program_planning_unit ppu ON sp.PROGRAM_ID=ppu.PROGRAM_ID AND sp.PLANNING_UNIT_ID=ppu.PLANNING_UNIT_ID WHERE sp.PROGRAM_ID=@programId and sp.VERSION_ID=@versionId;
    
    SELECT 
        tn.PLANNING_UNIT_ID, tn.TRANS_DATE, IFNULL(ppu.SHELF_LIFE, 24) SHELF_LIFE, tn.REGION_ID, tn.FORECASTED_CONSUMPTION, tn.ACTUAL_CONSUMPTION,
        tn.USE_ACTUAL_CONSUMPTION, tn.ADJUSTMENT, tn.STOCK, tn.REGION_STOCK_COUNT, tn.REGION_COUNT,
        tn.MANUAL_PLANNED_SHIPMENT, tn.MANUAL_SUBMITTED_SHIPMENT, tn.MANUAL_APPROVED_SHIPMENT, tn.MANUAL_SHIPPED_SHIPMENT, tn.MANUAL_RECEIVED_SHIPMENT, tn.MANUAL_ONHOLD_SHIPMENT, 
        tn.ERP_PLANNED_SHIPMENT, tn.ERP_SUBMITTED_SHIPMENT, tn.ERP_APPROVED_SHIPMENT, tn.ERP_SHIPPED_SHIPMENT, tn.ERP_RECEIVED_SHIPMENT, tn.ERP_ONHOLD_SHIPMENT
    FROM tmp_nsp tn LEFT JOIN rm_program_planning_unit ppu ON tn.PROGRAM_ID=ppu.PROGRAM_ID AND tn.PLANNING_UNIT_ID=ppu.PLANNING_UNIT_ID WHERE tn.PROGRAM_ID=@programId AND tn.VERSION_ID=@versionId -- AND tn.PLANNING_UNIT_ID=8293
    ;

END$$

DELIMITER ;



USE `fasp`;
DROP procedure IF EXISTS `shipmentDetails`;

DELIMITER $$
USE `fasp`$$
CREATE DEFINER=`faspUser`@`%` PROCEDURE `shipmentDetails`(VAR_START_DATE DATE, VAR_STOP_DATE DATE, VAR_PROGRAM_ID INT(10), VAR_VERSION_ID INT, VAR_PLANNING_UNIT_IDS TEXT)
BEGIN

	-- %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    -- Report no 19
    -- %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    
    -- Only Month and Year will be considered for StartDate and StopDate
    -- Only a single ProgramId can be selected
    -- VersionId can be a valid Version Id for the Program or -1 for last submitted VersionId
    -- PlanningUnitIds is the list of Planning Units you want to run the report for. 
    -- Empty PlanningUnitIds means you want to run the report for all the Planning Units in that Program

	SET @startDate = VAR_START_DATE;
	SET @stopDate = VAR_STOP_DATE;
	SET @programId = VAR_PROGRAM_ID;
	SET @versionId = VAR_VERSION_ID;
    
    IF @versionId = -1 THEN
		SELECT MAX(pv.VERSION_ID) INTO @versionId FROM rm_program_version pv WHERE pv.PROGRAM_ID=@programId;
	END IF;
    
	SET @sqlString = "";
        SET @sqlString = CONCAT(@sqlString, "SELECT ");
	SET @sqlString = CONCAT(@sqlString, "	pu.PLANNING_UNIT_ID, pu.LABEL_ID `PLANNING_UNIT_LABEL_ID`, pu.LABEL_EN `PLANNING_UNIT_LABEL_EN`, pu.LABEL_FR `PLANNING_UNIT_LABEL_FR`, pu.LABEL_SP `PLANNING_UNIT_LABEL_SP`, pu.LABEL_PR `PLANNING_UNIT_LABEL_PR`, ");
	SET @sqlString = CONCAT(@sqlString, "	fu.FORECASTING_UNIT_ID, fu.LABEL_ID `FORECASTING_UNIT_LABEL_ID`, fu.LABEL_EN `FORECASTING_UNIT_LABEL_EN`, fu.LABEL_FR `FORECASTING_UNIT_LABEL_FR`, fu.LABEL_SP `FORECASTING_UNIT_LABEL_SP`, fu.LABEL_PR `FORECASTING_UNIT_LABEL_PR`, ");
	SET @sqlString = CONCAT(@sqlString, "	pu.MULTIPLIER, ");
	SET @sqlString = CONCAT(@sqlString, "	s.SHIPMENT_ID, ");
	SET @sqlString = CONCAT(@sqlString, "	pa.PROCUREMENT_AGENT_ID, pa.PROCUREMENT_AGENT_CODE, pa.LABEL_ID `PROCUREMENT_AGENT_LABEL_ID`, pa.LABEL_EN `PROCUREMENT_AGENT_LABEL_EN`, pa.LABEL_FR `PROCUREMENT_AGENT_LABEL_FR`, pa.LABEL_SP `PROCUREMENT_AGENT_LABEL_SP`, pa.LABEL_PR `PROCUREMENT_AGENT_LABEL_PR`, ");
	SET @sqlString = CONCAT(@sqlString, "	fs.FUNDING_SOURCE_ID, fs.FUNDING_SOURCE_CODE, fs.LABEL_ID `FUNDING_SOURCE_LABEL_ID`, fs.LABEL_EN `FUNDING_SOURCE_LABEL_EN`, fs.LABEL_FR `FUNDING_SOURCE_LABEL_FR`, fs.LABEL_SP `FUNDING_SOURCE_LABEL_SP`, fs.LABEL_PR `FUNDING_SOURCE_LABEL_PR`, ");
	SET @sqlString = CONCAT(@sqlString, "	ss.SHIPMENT_STATUS_ID, ss.LABEL_ID `SHIPMENT_STATUS_LABEL_ID`, ss.LABEL_EN `SHIPMENT_STATUS_LABEL_EN`, ss.LABEL_FR `SHIPMENT_STATUS_LABEL_FR`, ss.LABEL_SP `SHIPMENT_STATUS_LABEL_SP`, ss.LABEL_PR `SHIPMENT_STATUS_LABEL_PR`, ");
	SET @sqlString = CONCAT(@sqlString, "	st.SHIPMENT_QTY, st.ORDER_NO, st.LOCAL_PROCUREMENT, st.ERP_FLAG, st.EMERGENCY_ORDER, ");
	SET @sqlString = CONCAT(@sqlString, "	COALESCE(st.RECEIVED_DATE, st.EXPECTED_DELIVERY_DATE) `EDD`, ");
	SET @sqlString = CONCAT(@sqlString, "	(IFNULL(st.PRODUCT_COST,0) * s.CONVERSION_RATE_TO_USD) `PRODUCT_COST`, ");
	SET @sqlString = CONCAT(@sqlString, "	(IFNULL(st.FREIGHT_COST,0) * s.CONVERSION_RATE_TO_USD) `FREIGHT_COST`, ");
	SET @sqlString = CONCAT(@sqlString, "	(IFNULL(st.PRODUCT_COST,0) * s.CONVERSION_RATE_TO_USD + IFNULL(st.FREIGHT_COST,0) * s.CONVERSION_RATE_TO_USD) `TOTAL_COST`, ");
	SET @sqlString = CONCAT(@sqlString, "	st.NOTES ");
	SET @sqlString = CONCAT(@sqlString, "FROM ");
	SET @sqlString = CONCAT(@sqlString, "	( ");
	SET @sqlString = CONCAT(@sqlString, "	SELECT ");
	SET @sqlString = CONCAT(@sqlString, "		s.SHIPMENT_ID, MAX(st.VERSION_ID) MAX_VERSION_ID, s.CONVERSION_RATE_TO_USD ");
	SET @sqlString = CONCAT(@sqlString, "	FROM rm_shipment s ");
	SET @sqlString = CONCAT(@sqlString, "	LEFT JOIN rm_shipment_trans st ON s.SHIPMENT_ID=st.SHIPMENT_ID ");
	SET @sqlString = CONCAT(@sqlString, "	WHERE ");
	SET @sqlString = CONCAT(@sqlString, "		s.PROGRAM_ID=@programId ");
	SET @sqlString = CONCAT(@sqlString, "		AND st.VERSION_ID<=@versionId ");
	SET @sqlString = CONCAT(@sqlString, "		AND st.SHIPMENT_TRANS_ID IS NOT NULL ");
	SET @sqlString = CONCAT(@sqlString, "	GROUP BY s.SHIPMENT_ID ");
	SET @sqlString = CONCAT(@sqlString, ") AS s ");
	SET @sqlString = CONCAT(@sqlString, "LEFT JOIN rm_shipment_trans st ON s.SHIPMENT_ID=st.SHIPMENT_ID AND s.MAX_VERSION_ID=st.VERSION_ID ");
	SET @sqlString = CONCAT(@sqlString, "LEFT JOIN vw_shipment_status ss on st.SHIPMENT_STATUS_ID=ss.SHIPMENT_STATUS_ID ");
	SET @sqlString = CONCAT(@sqlString, "LEFT JOIN vw_procurement_agent pa on st.PROCUREMENT_AGENT_ID=pa.PROCUREMENT_AGENT_ID ");
	SET @sqlString = CONCAT(@sqlString, "LEFT JOIN vw_funding_source fs ON st.FUNDING_SOURCE_ID=fs.FUNDING_SOURCE_ID ");
	SET @sqlString = CONCAT(@sqlString, "LEFT JOIN vw_planning_unit pu ON st.PLANNING_UNIT_ID=pu.PLANNING_UNIT_ID ");
        SET @sqlString = CONCAT(@sqlString, "LEFT JOIN rm_program_planning_unit ppu ON ppu.PROGRAM_ID=@programId AND st.PLANNING_UNIT_ID=ppu.PLANNING_UNIT_ID ");
	SET @sqlString = CONCAT(@sqlString, "LEFT JOIN vw_forecasting_unit fu ON pu.FORECASTING_UNIT_ID=fu.FORECASTING_UNIT_ID ");
	SET @sqlString = CONCAT(@sqlString, "WHERE ");
	SET @sqlString = CONCAT(@sqlString, "	st.ACTIVE AND ppu.ACTIVE AND pu.ACTIVE AND st.SHIPMENT_STATUS_ID!=8 ");
	SET @sqlString = CONCAT(@sqlString, "	AND COALESCE(st.RECEIVED_DATE, st.EXPECTED_DELIVERY_DATE) BETWEEN @startDate AND @stopDate ");
	IF LENGTH(VAR_PLANNING_UNIT_IDS) > 0 THEN
		SET @sqlString = CONCAT(@sqlString, "	AND (st.PLANNING_UNIT_ID in (",VAR_PLANNING_UNIT_IDS,")) ");
        END IF;
	SET @sqlString = CONCAT(@sqlString, "GROUP BY s.SHIPMENT_ID");
    
    PREPARE S1 FROM @sqlString;
    EXECUTE S1;
END$$

DELIMITER ;




USE `fasp`;
DROP procedure IF EXISTS `shipmentDetailsFundingSource`;

DELIMITER $$
USE `fasp`$$
CREATE DEFINER=`faspUser`@`%` PROCEDURE `shipmentDetailsFundingSource`(VAR_START_DATE DATE, VAR_STOP_DATE DATE, VAR_PROGRAM_ID INT(10), VAR_VERSION_ID INT, VAR_PLANNING_UNIT_IDS TEXT, VAR_REPORT_VIEW INT(10))
BEGIN

	-- %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    -- Report no 19 b
    -- %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    
    -- Only Month and Year will be considered for StartDate and StopDate
    -- Only a single ProgramId can be selected
    -- VersionId can be a valid Version Id for the Program or -1 for last submitted VersionId
    -- PlanningUnitIds is the list of Planning Units you want to run the report for. 
    -- Empty PlanningUnitIds means you want to run the report for all the Planning Units in that Program

	SET @startDate = VAR_START_DATE;
	SET @stopDate = VAR_STOP_DATE;
	SET @programId = VAR_PROGRAM_ID;
	SET @versionId = VAR_VERSION_ID;
    SET @reportView = VAR_REPORT_VIEW;
    
    IF @versionId = -1 THEN
		SELECT MAX(pv.VERSION_ID) INTO @versionId FROM rm_program_version pv WHERE pv.PROGRAM_ID=@programId;
	END IF;
    
	SET @sqlString = "";
    SET @sqlString = CONCAT(@sqlString, "SELECT ");
	SET @sqlString = CONCAT(@sqlString, "	fs.FUNDING_SOURCE_ID, fs.FUNDING_SOURCE_CODE, fs.LABEL_ID `FUNDING_SOURCE_LABEL_ID`, fs.LABEL_EN `FUNDING_SOURCE_LABEL_EN`, fs.LABEL_FR `FUNDING_SOURCE_LABEL_FR`, fs.LABEL_SP `FUNDING_SOURCE_LABEL_SP`, fs.LABEL_PR `FUNDING_SOURCE_LABEL_PR`, ");
	SET @sqlString = CONCAT(@sqlString, "	COUNT(st.SHIPMENT_ID) `ORDER_COUNT`, ");
    SET @sqlString = CONCAT(@sqlString, "	IF(@reportView=1, SUM(st.SHIPMENT_QTY), SUM(st.SHIPMENT_QTY*pu.MULTIPLIER)) `QUANTITY`, ");
    SET @sqlString = CONCAT(@sqlString, "	SUM((IFNULL(st.PRODUCT_COST,0) + IFNULL(st.FREIGHT_COST,0)) * s.CONVERSION_RATE_TO_USD) `COST` ");
	SET @sqlString = CONCAT(@sqlString, "FROM ");
	SET @sqlString = CONCAT(@sqlString, "	( ");
	SET @sqlString = CONCAT(@sqlString, "	SELECT ");
	SET @sqlString = CONCAT(@sqlString, "		s.SHIPMENT_ID, MAX(st.VERSION_ID) MAX_VERSION_ID, s.CONVERSION_RATE_TO_USD ");
	SET @sqlString = CONCAT(@sqlString, "	FROM rm_shipment s ");
	SET @sqlString = CONCAT(@sqlString, "	LEFT JOIN rm_shipment_trans st ON s.SHIPMENT_ID=st.SHIPMENT_ID ");
	SET @sqlString = CONCAT(@sqlString, "	WHERE ");
	SET @sqlString = CONCAT(@sqlString, "		s.PROGRAM_ID=@programId ");
	SET @sqlString = CONCAT(@sqlString, "		AND st.VERSION_ID<=@versionId ");
	SET @sqlString = CONCAT(@sqlString, "		AND st.SHIPMENT_TRANS_ID IS NOT NULL ");
	SET @sqlString = CONCAT(@sqlString, "	GROUP BY s.SHIPMENT_ID ");
	SET @sqlString = CONCAT(@sqlString, ") AS s ");
	SET @sqlString = CONCAT(@sqlString, "LEFT JOIN rm_shipment s1 ON s.SHIPMENT_ID=s1.SHIPMENT_ID ");
    SET @sqlString = CONCAT(@sqlString, "LEFT JOIN rm_shipment_trans st ON s.SHIPMENT_ID=st.SHIPMENT_ID AND s.MAX_VERSION_ID=st.VERSION_ID ");
	SET @sqlString = CONCAT(@sqlString, "LEFT JOIN vw_shipment_status ss on st.SHIPMENT_STATUS_ID=ss.SHIPMENT_STATUS_ID ");
	SET @sqlString = CONCAT(@sqlString, "LEFT JOIN vw_funding_source fs ON st.FUNDING_SOURCE_ID=fs.FUNDING_SOURCE_ID ");
	SET @sqlString = CONCAT(@sqlString, "LEFT JOIN vw_planning_unit pu ON st.PLANNING_UNIT_ID=pu.PLANNING_UNIT_ID ");
    SET @sqlString = CONCAT(@sqlString, "LEFT JOIN rm_program_planning_unit ppu ON s1.PROGRAM_ID=ppu.PROGRAM_ID AND st.PLANNING_UNIT_ID=ppu.PLANNING_UNIT_ID ");
	SET @sqlString = CONCAT(@sqlString, "WHERE ");
	SET @sqlString = CONCAT(@sqlString, "	st.ACTIVE AND ppu.ACTIVE AND pu.ACTIVE ");
	SET @sqlString = CONCAT(@sqlString, "	AND st.SHIPMENT_STATUS_ID!=8 ");
	SET @sqlString = CONCAT(@sqlString, "	AND COALESCE(st.RECEIVED_DATE, st.EXPECTED_DELIVERY_DATE) BETWEEN @startDate AND @stopDate ");
	IF LENGTH(VAR_PLANNING_UNIT_IDS) > 0 THEN
		SET @sqlString = CONCAT(@sqlString, "	AND (st.PLANNING_UNIT_ID in (",VAR_PLANNING_UNIT_IDS,")) ");
    END IF;
    SET @sqlString = CONCAT(@sqlString, " GROUP BY st.FUNDING_SOURCE_ID"); 
    
    PREPARE S1 FROM @sqlString;
    EXECUTE S1;
END$$

DELIMITER ;




USE `fasp`;
DROP procedure IF EXISTS `shipmentDetailsMonth`;

DELIMITER $$
USE `fasp`$$
CREATE DEFINER=`faspUser`@`%` PROCEDURE `shipmentDetailsMonth`(VAR_START_DATE DATE, VAR_STOP_DATE DATE, VAR_PROGRAM_ID INT(10), VAR_VERSION_ID INT, VAR_PLANNING_UNIT_IDS TEXT)
BEGIN

	-- %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    -- Report no 19 c
    -- %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    
    -- Only Month and Year will be considered for StartDate and StopDate
    -- Only a single ProgramId can be selected
    -- VersionId can be a valid Version Id for the Program or -1 for last submitted VersionId
    -- PlanningUnitIds is the list of Planning Units you want to run the report for. 
    -- Empty PlanningUnitIds means you want to run the report for all the Planning Units in that Program

	SET @startDate = VAR_START_DATE;
	SET @stopDate = VAR_STOP_DATE;
	SET @programId = VAR_PROGRAM_ID;
	SET @versionId = VAR_VERSION_ID; 
    
    IF @versionId = -1 THEN
		SELECT MAX(pv.VERSION_ID) INTO @versionId FROM rm_program_version pv WHERE pv.PROGRAM_ID=@programId;
	END IF;
    
    SET @sqlString = "";
    SET @sqlString = CONCAT(@sqlString, "SELECT ");
    SET @sqlString = CONCAT(@sqlString, "   mn.MONTH, ");
    SET @sqlString = CONCAT(@sqlString, "   SUM(IFNULL(s1.`PLANNED_COST`,0)) `PLANNED_COST`, ");
    SET @sqlString = CONCAT(@sqlString, "   SUM(IFNULL(s1.`SUBMITTED_COST`,0)) `SUBMITTED_COST`, ");
    SET @sqlString = CONCAT(@sqlString, "   SUM(IFNULL(s1.`APPROVED_COST`,0)) `APPROVED_COST`, ");
    SET @sqlString = CONCAT(@sqlString, "   SUM(IFNULL(s1.`SHIPPED_COST`,0)) `SHIPPED_COST`, ");
    SET @sqlString = CONCAT(@sqlString, "   SUM(IFNULL(s1.`ARRIVED_COST`,0)) `ARRIVED_COST`, ");
    SET @sqlString = CONCAT(@sqlString, "   SUM(IFNULL(s1.`RECEIVED_COST`,0)) `RECEIVED_COST`, ");
    SET @sqlString = CONCAT(@sqlString, "   SUM(IFNULL(s1.`ONHOLD_COST`,0)) `ONHOLD_COST` ");
    SET @sqlString = CONCAT(@sqlString, "FROM mn ");
    SET @sqlString = CONCAT(@sqlString, "LEFT JOIN ");
    SET @sqlString = CONCAT(@sqlString, "   ( ");
    SET @sqlString = CONCAT(@sqlString, "   SELECT ");
    SET @sqlString = CONCAT(@sqlString, "       CONCAT(LEFT(COALESCE(st.RECEIVED_DATE, st.EXPECTED_DELIVERY_DATE),7),'-01') `DT`, ");
    SET @sqlString = CONCAT(@sqlString, "       IF(st.SHIPMENT_STATUS_ID=1, (IFNULL(st.PRODUCT_COST,0) + IFNULL(st.FREIGHT_COST,0)) * s.CONVERSION_RATE_TO_USD, 0) `PLANNED_COST`, ");
    SET @sqlString = CONCAT(@sqlString, "       IF(st.SHIPMENT_STATUS_ID=3, (IFNULL(st.PRODUCT_COST,0) + IFNULL(st.FREIGHT_COST,0)) * s.CONVERSION_RATE_TO_USD, 0) `SUBMITTED_COST`, ");
    SET @sqlString = CONCAT(@sqlString, "       IF(st.SHIPMENT_STATUS_ID=4, (IFNULL(st.PRODUCT_COST,0) + IFNULL(st.FREIGHT_COST,0)) * s.CONVERSION_RATE_TO_USD, 0) `APPROVED_COST`, ");
    SET @sqlString = CONCAT(@sqlString, "       IF(st.SHIPMENT_STATUS_ID=5, (IFNULL(st.PRODUCT_COST,0) + IFNULL(st.FREIGHT_COST,0)) * s.CONVERSION_RATE_TO_USD, 0) `SHIPPED_COST`, ");
    SET @sqlString = CONCAT(@sqlString, "       IF(st.SHIPMENT_STATUS_ID=6, (IFNULL(st.PRODUCT_COST,0) + IFNULL(st.FREIGHT_COST,0)) * s.CONVERSION_RATE_TO_USD, 0) `ARRIVED_COST`, ");
    SET @sqlString = CONCAT(@sqlString, "       IF(st.SHIPMENT_STATUS_ID=7, (IFNULL(st.PRODUCT_COST,0) + IFNULL(st.FREIGHT_COST,0)) * s.CONVERSION_RATE_TO_USD, 0) `RECEIVED_COST`, ");
    SET @sqlString = CONCAT(@sqlString, "       IF(st.SHIPMENT_STATUS_ID=8, (IFNULL(st.PRODUCT_COST,0) + IFNULL(st.FREIGHT_COST,0)) * s.CONVERSION_RATE_TO_USD, 0) `ONHOLD_COST`, ");
    SET @sqlString = CONCAT(@sqlString, "       s1.SHIPMENT_ID ");
    SET @sqlString = CONCAT(@sqlString, "    FROM ");
    SET @sqlString = CONCAT(@sqlString, "        ( ");
    SET @sqlString = CONCAT(@sqlString, "        SELECT ");
    SET @sqlString = CONCAT(@sqlString, "            s.SHIPMENT_ID, MAX(st.VERSION_ID) MAX_VERSION_ID, s.CONVERSION_RATE_TO_USD ");
    SET @sqlString = CONCAT(@sqlString, "        FROM rm_shipment s ");
    SET @sqlString = CONCAT(@sqlString, "        LEFT JOIN rm_shipment_trans st ON s.SHIPMENT_ID=st.SHIPMENT_ID ");
    SET @sqlString = CONCAT(@sqlString, "        WHERE ");
    SET @sqlString = CONCAT(@sqlString, "            s.PROGRAM_ID=@programId ");
    SET @sqlString = CONCAT(@sqlString, "            AND st.VERSION_ID<=@versionId ");
    SET @sqlString = CONCAT(@sqlString, "            AND st.SHIPMENT_TRANS_ID IS NOT NULL ");
    SET @sqlString = CONCAT(@sqlString, "        GROUP BY s.SHIPMENT_ID ");
    SET @sqlString = CONCAT(@sqlString, "    ) AS s ");
    SET @sqlString = CONCAT(@sqlString, "    LEFT JOIN rm_shipment s1 ON s.SHIPMENT_ID=s1.SHIPMENT_ID ");
    SET @sqlString = CONCAT(@sqlString, "    LEFT JOIN rm_shipment_trans st ON s.SHIPMENT_ID=st.SHIPMENT_ID AND s.MAX_VERSION_ID=st.VERSION_ID ");
    SET @sqlString = CONCAT(@sqlString, "    LEFT JOIN vw_shipment_status ss on st.SHIPMENT_STATUS_ID=ss.SHIPMENT_STATUS_ID ");
    SET @sqlString = CONCAT(@sqlString, "    LEFT JOIN vw_funding_source fs ON st.FUNDING_SOURCE_ID=fs.FUNDING_SOURCE_ID ");
    SET @sqlString = CONCAT(@sqlString, "    LEFT JOIN vw_planning_unit pu ON st.PLANNING_UNIT_ID=pu.PLANNING_UNIT_ID ");
    SET @sqlString = CONCAT(@sqlString, "    LEFT JOIN rm_program_planning_unit ppu ON s1.PROGRAM_ID=ppu.PROGRAM_ID AND st.PLANNING_UNIT_ID=ppu.PLANNING_UNIT_ID ");
    SET @sqlString = CONCAT(@sqlString, "    WHERE ");
    SET @sqlString = CONCAT(@sqlString, "        st.ACTIVE  AND ppu.ACTIVE AND pu.ACTIVE ");
    SET @sqlString = CONCAT(@sqlString, "        AND st.SHIPMENT_STATUS_ID!=8 ");
    IF LENGTH(VAR_PLANNING_UNIT_IDS) > 0 THEN
		SET @sqlString = CONCAT(@sqlString, "	    AND (st.PLANNING_UNIT_ID in (",VAR_PLANNING_UNIT_IDS,")) ");
    END IF;
    SET @sqlString = CONCAT(@sqlString, "        AND COALESCE(st.RECEIVED_DATE, st.EXPECTED_DELIVERY_DATE) BETWEEN @startDate AND @stopDate ");
    SET @sqlString = CONCAT(@sqlString, ") AS s1 ON mn.MONTH =s1.DT ");
    SET @sqlString = CONCAT(@sqlString, "WHERE mn.MONTH BETWEEN @startDate AND @stopDate ");
    SET @sqlString = CONCAT(@sqlString, "GROUP BY mn.MONTH");
    
    PREPARE S1 FROM @sqlString;
    EXECUTE S1;
END$$

DELIMITER ;




UPDATE `rm_realm_problem` SET `ACTIVE`='0' , `LAST_MODIFIED_DATE`=now() WHERE `REALM_PROBLEM_ID`='5';
UPDATE `rm_realm_problem` SET `ACTIVE`='0' , `LAST_MODIFIED_DATE`=now() WHERE `REALM_PROBLEM_ID`='6';
UPDATE `rm_realm_problem` SET `ACTIVE`='0' , `LAST_MODIFIED_DATE`=now() WHERE `REALM_PROBLEM_ID`='7';
UPDATE `rm_realm_problem` SET `DATA3`='18,3',`LAST_MODIFIED_DATE`=now() WHERE `REALM_PROBLEM_ID`='11';
UPDATE `rm_realm_problem` SET `DATA3`='23' , `LAST_MODIFIED_DATE`=now() WHERE `REALM_PROBLEM_ID`='13';
UPDATE `rm_realm_problem` SET `DATA3`='26' , `LAST_MODIFIED_DATE`=now() WHERE `REALM_PROBLEM_ID`='14';

delete prt.* from rm_problem_report_trans prt where  prt.PROBLEM_REPORT_ID in (
select pr.PROBLEM_REPORT_ID from rm_problem_report pr
left join rm_realm_problem rrp on rrp.REALM_PROBLEM_ID=pr.REALM_PROBLEM_ID
left join ap_problem ap on ap.PROBLEM_ID=rrp.PROBLEM_ID
where pr.DATA4 in (select rst.SHIPMENT_ID from rm_shipment_trans rst where rst.SHIPMENT_STATUS_ID=8 or rst.ACTIVE=0 group by rst.SHIPMENT_ID)
and ap.PROBLEM_ID in (3,4));


delete pr.* from rm_problem_report pr
left join rm_realm_problem rrp on rrp.REALM_PROBLEM_ID=pr.REALM_PROBLEM_ID
left join ap_problem ap on ap.PROBLEM_ID=rrp.PROBLEM_ID
where pr.DATA4 in (select rst.SHIPMENT_ID from rm_shipment_trans rst where rst.SHIPMENT_STATUS_ID=8 or rst.ACTIVE=0 group by rst.SHIPMENT_ID)
and ap.PROBLEM_ID in (3,4);