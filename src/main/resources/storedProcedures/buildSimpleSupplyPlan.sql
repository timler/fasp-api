CREATE DEFINER=`faspUser`@`localhost` PROCEDURE `buildSimpleSupplyPlan`(PROGRAM_ID INT(10), VERSION_ID INT(10))
BEGIN
    SET @programId = PROGRAM_ID;
    SET @versionId = VERSION_ID;
    SET @cb = 0;
   
    DELETE spbi.* FROM rm_supply_plan_batch_info spbi WHERE spbi.PROGRAM_ID=@programId AND spbi.VERSION_ID=@versionId;
    DELETE sp.* FROM rm_supply_plan sp WHERE sp.PROGRAM_ID=@programId AND sp.VERSION_ID=@versionId;
   
    INSERT INTO rm_supply_plan (
        SUPPLY_PLAN_ID, VERSION_ID, PROGRAM_ID, PLANNING_UNIT_ID, TRANS_DATE, 
        BATCH_ID, FORECASTED_CONSUMPTION_QTY, ACTUAL_CONSUMPTION_QTY, 
        MANUAL_PLANNED_SHIPMENT_QTY, MANUAL_SUBMITTED_SHIPMENT_QTY, MANUAL_APPROVED_SHIPMENT_QTY, MANUAL_SHIPPED_SHIPMENT_QTY, MANUAL_RECEIVED_SHIPMENT_QTY, MANUAL_ONHOLD_SHIPMENT_QTY,
        ERP_PLANNED_SHIPMENT_QTY, ERP_SUBMITTED_SHIPMENT_QTY, ERP_APPROVED_SHIPMENT_QTY, ERP_SHIPPED_SHIPMENT_QTY, ERP_RECEIVED_SHIPMENT_QTY, ERP_ONHOLD_SHIPMENT_QTY,
        SHIPMENT_QTY, ADJUSTMENT_MULTIPLIED_QTY, STOCK_MULTIPLIED_QTY
    )
    SELECT 
        null, @versionId, oc.PROGRAM_ID, oc.PLANNING_UNIT_ID, oc.TRANS_DATE, 
        oc.BATCH_ID, oc.FORECASTED_CONSUMPTION, oc.ACTUAL_CONSUMPTION, 
        oc.MANUAL_PLANNED_SHIPMENT_QTY, oc.MANUAL_SUBMITTED_SHIPMENT_QTY, oc.MANUAL_APPROVED_SHIPMENT_QTY, oc.MANUAL_SHIPPED_SHIPMENT_QTY, oc.MANUAL_RECEIVED_SHIPMENT_QTY, oc.MANUAL_ONHOLD_SHIPMENT_QTY,
        oc.ERP_PLANNED_SHIPMENT_QTY, oc.ERP_SUBMITTED_SHIPMENT_QTY, oc.ERP_APPROVED_SHIPMENT_QTY, oc.ERP_SHIPPED_SHIPMENT_QTY, oc.ERP_RECEIVED_SHIPMENT_QTY, oc.ERP_ONHOLD_SHIPMENT_QTY,
        oc.SHIPMENT_QTY, oc.ADJUSTMENT_MULTIPLIED_QTY, oc.STOCK_MULTIPLIED_QTY
    FROM (
        SELECT 
            o.PROGRAM_ID, o.PLANNING_UNIT_ID, DATE(CONCAT(o.TRANS_DATE,"-01")) `TRANS_DATE`, o.BATCH_ID, SUM(IFNULL(o.FORECASTED_CONSUMPTION,0)) `FORECASTED_CONSUMPTION`, SUM(IFNULL(o.ACTUAL_CONSUMPTION,0)) `ACTUAL_CONSUMPTION`, 
            SUM(IFNULL(o.MANUAL_PLANNED_SHIPMENT_QTY,0)) `MANUAL_PLANNED_SHIPMENT_QTY`, SUM(IFNULL(o.MANUAL_SUBMITTED_SHIPMENT_QTY,0)) `MANUAL_SUBMITTED_SHIPMENT_QTY`, SUM(IFNULL(o.MANUAL_APPROVED_SHIPMENT_QTY,0)) `MANUAL_APPROVED_SHIPMENT_QTY`, SUM(IFNULL(o.MANUAL_SHIPPED_SHIPMENT_QTY,0)) `MANUAL_SHIPPED_SHIPMENT_QTY`, SUM(IFNULL(o.MANUAL_RECEIVED_SHIPMENT_QTY,0)) `MANUAL_RECEIVED_SHIPMENT_QTY`, SUM(IFNULL(o.MANUAL_ONHOLD_SHIPMENT_QTY,0)) `MANUAL_ONHOLD_SHIPMENT_QTY`, 
            SUM(IFNULL(o.ERP_PLANNED_SHIPMENT_QTY,0)) `ERP_PLANNED_SHIPMENT_QTY`, SUM(IFNULL(o.ERP_SUBMITTED_SHIPMENT_QTY,0)) `ERP_SUBMITTED_SHIPMENT_QTY`, SUM(IFNULL(o.ERP_APPROVED_SHIPMENT_QTY,0)) `ERP_APPROVED_SHIPMENT_QTY`, SUM(IFNULL(o.ERP_SHIPPED_SHIPMENT_QTY,0)) `ERP_SHIPPED_SHIPMENT_QTY`, SUM(IFNULL(o.ERP_RECEIVED_SHIPMENT_QTY,0)) `ERP_RECEIVED_SHIPMENT_QTY`, SUM(IFNULL(o.ERP_ONHOLD_SHIPMENT_QTY,0)) `ERP_ONHOLD_SHIPMENT_QTY`, 
            SUM(IFNULL(o.SHIPMENT_QTY,0)) `SHIPMENT_QTY`, SUM(IFNULL(o.ADJUSTMENT_MULTIPLIED_QTY,0)) `ADJUSTMENT_MULTIPLIED_QTY`, SUM(IFNULL(o.STOCK_MULTIPLIED_QTY,0)) `STOCK_MULTIPLIED_QTY` 
        FROM (
            SELECT 
                '2' `TRANS_TYPE`, c1.PROGRAM_ID, c1.CONSUMPTION_ID `TRANS_ID`, c1.PLANNING_UNIT_ID, LEFT(c1.CONSUMPTION_DATE, 7) `TRANS_DATE`, 
                c1.BATCH_ID, c1.EXPIRY_DATE, SUM(FORECASTED_CONSUMPTION) `FORECASTED_CONSUMPTION`, SUM(ACTUAL_CONSUMPTION) `ACTUAL_CONSUMPTION`, 
                null `MANUAL_PLANNED_SHIPMENT_QTY`, null `MANUAL_SUBMITTED_SHIPMENT_QTY`, null `MANUAL_APPROVED_SHIPMENT_QTY`, null `MANUAL_SHIPPED_SHIPMENT_QTY`, null `MANUAL_RECEIVED_SHIPMENT_QTY`, null `MANUAL_ONHOLD_SHIPMENT_QTY`, 
                null `ERP_PLANNED_SHIPMENT_QTY`, null `ERP_SUBMITTED_SHIPMENT_QTY`, null `ERP_APPROVED_SHIPMENT_QTY`, null `ERP_SHIPPED_SHIPMENT_QTY`, null `ERP_RECEIVED_SHIPMENT_QTY`, null `ERP_ONHOLD_SHIPMENT_QTY`, 
                null `SHIPMENT_QTY`, null `ADJUSTMENT_MULTIPLIED_QTY`, null  `STOCK_MULTIPLIED_QTY` 
            FROM (
                SELECT 
                    c.PROGRAM_ID, c.CONSUMPTION_ID, ct.REGION_ID, ct.PLANNING_UNIT_ID, ct.CONSUMPTION_DATE, 
                    ifnull(ctbi.BATCH_ID,0) `BATCH_ID`, ifnull(bi.EXPIRY_DATE,@defaultExpDate) `EXPIRY_DATE`, ct.ACTIVE, SUM(IF(ct.ACTUAL_FLAG=1, COALESCE(ctbi.CONSUMPTION_QTY, ct.CONSUMPTION_QTY),null)) `ACTUAL_CONSUMPTION`, 
                    SUM(IF(ct.ACTUAL_FLAG=0, COALESCE(ctbi.CONSUMPTION_QTY, ct.CONSUMPTION_QTY),null)) `FORECASTED_CONSUMPTION`
                FROM (
                    SELECT c.CONSUMPTION_ID, MAX(ct.VERSION_ID) MAX_VERSION_ID 
                    FROM rm_consumption c
                    LEFT JOIN rm_consumption_trans ct ON c.CONSUMPTION_ID=ct.CONSUMPTION_ID
                    WHERE c.PROGRAM_ID=@programId AND ct.VERSION_ID<=@versionId AND ct.CONSUMPTION_TRANS_ID IS NOT NULL
                    GROUP BY c.CONSUMPTION_ID
                ) tc
                LEFT JOIN rm_consumption c ON c.CONSUMPTION_ID=tc.CONSUMPTION_ID
                LEFT JOIN rm_consumption_trans ct ON c.CONSUMPTION_ID=ct.CONSUMPTION_ID AND tc.MAX_VERSION_ID=ct.VERSION_ID
                LEFT JOIN rm_consumption_trans_batch_info ctbi ON ct.CONSUMPTION_TRANS_ID=ctbi.CONSUMPTION_TRANS_ID
                LEFT JOIN rm_batch_info bi ON ctbi.BATCH_ID=bi.BATCH_ID
                WHERE ct.ACTIVE
                GROUP BY c.PROGRAM_ID, ct.REGION_ID, ct.PLANNING_UNIT_ID, ct.CONSUMPTION_DATE, ifnull(ctbi.BATCH_ID,0)
            ) c1 
            GROUP BY c1.PROGRAM_ID, c1.PLANNING_UNIT_ID, c1.CONSUMPTION_DATE, c1.BATCH_ID

            UNION

            SELECT 
                '1' `TRANS_TYPE`, s.PROGRAM_ID, s.SHIPMENT_ID `TRANS_ID`, st.PLANNING_UNIT_ID, LEFT(COALESCE(st.RECEIVED_DATE, st.EXPECTED_DELIVERY_DATE),7) `TRANS_DATE`, 
                ifnull(stbi.BATCH_ID,0) `BATCH_ID`, ifnull(bi.EXPIRY_DATE, @defaultExpDate) `EXPIRY_DATE`, null `FORECASTED_CONSUMPTION`, null `ACTUAL_CONSUMPTION`, 
                SUM(IF((st.ERP_FLAG IS NULL OR st.ERP_FLAG=0) AND st.SHIPMENT_STATUS_ID = 1, COALESCE(stbi.BATCH_SHIPMENT_QTY ,st.SHIPMENT_QTY),0)) `MANUAL_PLANNED_SHIPMENT_QTY`, 
                SUM(IF((st.ERP_FLAG IS NULL OR st.ERP_FLAG=0) AND st.SHIPMENT_STATUS_ID = 3, COALESCE(stbi.BATCH_SHIPMENT_QTY ,st.SHIPMENT_QTY),0)) `MANUAL_SUBMITTED_SHIPMENT_QTY`, 
                SUM(IF((st.ERP_FLAG IS NULL OR st.ERP_FLAG=0) AND st.SHIPMENT_STATUS_ID = 4, COALESCE(stbi.BATCH_SHIPMENT_QTY ,st.SHIPMENT_QTY),0)) `MANUAL_APPROVED_SHIPMENT_QTY`, 
                SUM(IF((st.ERP_FLAG IS NULL OR st.ERP_FLAG=0) AND st.SHIPMENT_STATUS_ID IN (5,6), COALESCE(stbi.BATCH_SHIPMENT_QTY ,st.SHIPMENT_QTY),0)) `MANUAL_SHIPPED_SHIPMENT_QTY`, 
                SUM(IF((st.ERP_FLAG IS NULL OR st.ERP_FLAG=0) AND st.SHIPMENT_STATUS_ID = 7, COALESCE(stbi.BATCH_SHIPMENT_QTY ,st.SHIPMENT_QTY),0)) `MANUAL_RECEIVED_SHIPMENT_QTY`, 
                SUM(IF((st.ERP_FLAG IS NULL OR st.ERP_FLAG=0) AND st.SHIPMENT_STATUS_ID = 9, COALESCE(stbi.BATCH_SHIPMENT_QTY ,st.SHIPMENT_QTY),0)) `MANUAL_ONHOLD_SHIPMENT_QTY`, 
                SUM(IF(st.ERP_FLAG = 1 AND st.SHIPMENT_STATUS_ID = 1, COALESCE(stbi.BATCH_SHIPMENT_QTY ,st.SHIPMENT_QTY),0)) `ERP_PLANNED_SHIPMENT_QTY`, 
                SUM(IF(st.ERP_FLAG = 1 AND st.SHIPMENT_STATUS_ID = 3, COALESCE(stbi.BATCH_SHIPMENT_QTY ,st.SHIPMENT_QTY),0)) `ERP_SUBMITTED_SHIPMENT_QTY`, 
                SUM(IF(st.ERP_FLAG = 1 AND st.SHIPMENT_STATUS_ID = 4, COALESCE(stbi.BATCH_SHIPMENT_QTY ,st.SHIPMENT_QTY),0)) `ERP_APPROVED_SHIPMENT_QTY`, 
                SUM(IF(st.ERP_FLAG = 1 AND st.SHIPMENT_STATUS_ID IN (5,6), COALESCE(stbi.BATCH_SHIPMENT_QTY ,st.SHIPMENT_QTY),0)) `ERP_SHIPPED_SHIPMENT_QTY`, 
                SUM(IF(st.ERP_FLAG = 1 AND st.SHIPMENT_STATUS_ID = 7, COALESCE(stbi.BATCH_SHIPMENT_QTY ,st.SHIPMENT_QTY),0)) `ERP_RECEIVED_SHIPMENT_QTY`, 
                SUM(IF(st.ERP_FLAG = 1 AND st.SHIPMENT_STATUS_ID = 9, COALESCE(stbi.BATCH_SHIPMENT_QTY ,st.SHIPMENT_QTY),0)) `ERP_ONHOLD_SHIPMENT_QTY`, 
                SUM(COALESCE(stbi.BATCH_SHIPMENT_QTY ,st.SHIPMENT_QTY)) `SHIPMENT_QTY`, null  `ADJUSTMENT_MULTIPLIED_QTY`, null  `STOCK_MULTIPLIED_QTY`
            FROM (
                SELECT s.PROGRAM_ID, s.SHIPMENT_ID, MAX(st.VERSION_ID) MAX_VERSION_ID
                FROM rm_shipment s
                LEFT JOIN rm_shipment_trans st ON s.SHIPMENT_ID=st.SHIPMENT_ID
                WHERE s.PROGRAM_ID=@programId AND st.VERSION_ID<=@versionId AND st.SHIPMENT_TRANS_ID IS NOT NULL 
                GROUP BY s.SHIPMENT_ID
            ) ts
            LEFT JOIN rm_shipment s ON s.SHIPMENT_ID=ts.SHIPMENT_ID
            LEFT JOIN rm_shipment_trans st ON s.SHIPMENT_ID=st.SHIPMENT_ID AND ts.MAX_VERSION_ID=st.VERSION_ID
            LEFT JOIN rm_shipment_trans_batch_info stbi ON st.SHIPMENT_TRANS_ID=stbi.SHIPMENT_TRANS_ID
            LEFT JOIN rm_batch_info bi ON stbi.BATCH_ID=bi.BATCH_ID
            WHERE st.ACTIVE AND st.ACCOUNT_FLAG AND st.SHIPMENT_STATUS_ID!=8 
            GROUP BY s.PROGRAM_ID, st.PLANNING_UNIT_ID, COALESCE(st.RECEIVED_DATE, st.EXPECTED_DELIVERY_DATE), ifnull(stbi.BATCH_ID,0)

            UNION

            SELECT 
                '3' `TRANS_TYPE`, i.PROGRAM_ID, i.INVENTORY_ID `TRANS_ID`, rcpu.PLANNING_UNIT_ID, LEFT(it.INVENTORY_DATE,7) `TRANS_DATE`, 
                ifnull(itbi.BATCH_ID,0) `BATCH_ID`, IFNULL(bi.EXPIRY_DATE, @defaultExpDate) `EXPIRY_DATE`, null `FORECASTED_CONSUMPTION`, null `ACTUAL_CONSUMPTION`, 
                null `MANUAL_PLANNED_SHIPMENT_QTY`, null `MANUAL_SUBMITTED_SHIPMENT_QTY`, null `MANUAL_APPROVED_SHIPMENT_QTY`, null `MANUAL_SHIPPED_SHIPMENT_QTY`, null `MANUAL_RECEIVED_SHIPMENT_QTY`, null `MANUAL_ONHOLD_SHIPMENT_QTY`, 
                null `ERP_PLANNED_SHIPMENT_QTY`, null `ERP_SUBMITTED_SHIPMENT_QTY`, null `ERP_APPROVED_SHIPMENT_QTY`, null `ERP_SHIPPED_SHIPMENT_QTY`, null `ERP_RECEIVED_SHIPMENT_QTY`, null `ERP_ONHOLD_SHIPMENT_QTY`, 
                null `SHIPMENT_QTY`, SUM(COALESCE(itbi.ADJUSTMENT_QTY, it.ADJUSTMENT_QTY)*rcpu.MULTIPLIER) `ADJUSTMENT_MULTIPLIED_QTY`,  SUM(COALESCE(itbi.ACTUAL_QTY, it.ACTUAL_QTY)*rcpu.MULTIPLIER) `STOCK_MULTIPLIED_QTY`
            FROM (
                SELECT i.PROGRAM_ID, i.INVENTORY_ID, MAX(it.VERSION_ID) MAX_VERSION_ID
                FROM rm_inventory i
                LEFT JOIN rm_inventory_trans it ON i.INVENTORY_ID=it.INVENTORY_ID
                WHERE i.PROGRAM_ID=@programId AND it.VERSION_ID<=@versionId AND it.INVENTORY_TRANS_ID IS NOT NULL 
                GROUP BY i.INVENTORY_ID
            ) ti
            LEFT JOIN rm_inventory i ON i.INVENTORY_ID=ti.INVENTORY_ID
            LEFT JOIN rm_inventory_trans it ON i.INVENTORY_ID=it.INVENTORY_ID AND ti.MAX_VERSION_ID=it.VERSION_ID
            LEFT JOIN rm_inventory_trans_batch_info itbi ON it.INVENTORY_TRANS_ID=itbi.INVENTORY_TRANS_ID
            LEFT JOIN rm_batch_info bi ON itbi.BATCH_ID=bi.BATCH_ID
            LEFT JOIN rm_realm_country_planning_unit rcpu ON it.REALM_COUNTRY_PLANNING_UNIT_ID=rcpu.REALM_COUNTRY_PLANNING_UNIT_ID
            WHERE it.ACTIVE
            GROUP BY i.PROGRAM_ID, rcpu.PLANNING_UNIT_ID, it.INVENTORY_DATE, ifnull(itbi.BATCH_ID,0)
        ) AS o GROUP BY o.PROGRAM_ID, o.PLANNING_UNIT_ID, o.TRANS_DATE, o.BATCH_ID
    ) oc;
       
    -- Get the Region count for this Program
    SELECT count(*) into @regionCount FROM rm_program_region pr WHERE pr.PROGRAM_ID=@programId;
    
    -- Update if the Consumption that is to be used for the month is Actual or Forecasted
    UPDATE rm_supply_plan sp LEFT JOIN (SELECT PLANNING_UNIT_ID, CONSUMPTION_DATE, IF(@regionCount<=SUM(IF(ACTUAL_CONSUMPTION IS NOT NULL, 1,0)), 1 , IF(IFNULL(SUM(ACTUAL_CONSUMPTION),0)>IFNULL(SUM(FORECASTED_CONSUMPTION),0), 1, 0)) `ACTUAL` FROM (SELECT ct.PLANNING_UNIT_ID, ct.CONSUMPTION_DATE, ct.REGION_ID, SUM(IF(ct.ACTUAL_FLAG, ct.CONSUMPTION_QTY, null)) ACTUAL_CONSUMPTION, SUM(IF(ct.ACTUAL_FLAG=0, ct.CONSUMPTION_QTY, null)) FORECASTED_CONSUMPTION FROM (SELECT c.CONSUMPTION_ID, MAX(ct.VERSION_ID) MAX_VERSION_ID  FROM rm_consumption c LEFT JOIN rm_consumption_trans ct ON c.CONSUMPTION_ID=ct.CONSUMPTION_ID WHERE c.PROGRAM_ID=@programId AND ct.VERSION_ID<=@versionId AND ct.CONSUMPTION_TRANS_ID IS NOT NULL GROUP BY c.CONSUMPTION_ID) tc LEFT JOIN rm_consumption c ON c.CONSUMPTION_ID=tc.CONSUMPTION_ID LEFT JOIN rm_consumption_trans ct ON c.CONSUMPTION_ID=ct.CONSUMPTION_ID AND tc.MAX_VERSION_ID=ct.VERSION_ID WHERE ct.ACTIVE GROUP BY ct.PLANNING_UNIT_ID, ct.CONSUMPTION_DATE, ct.REGION_ID) c2 GROUP BY c2.PLANNING_UNIT_ID, CONSUMPTION_DATE) spa ON sp.PLANNING_UNIT_ID=spa.PLANNING_UNIT_ID AND sp.TRANS_DATE=spa.CONSUMPTION_DATE SET sp.ACTUAL=spa.ACTUAL;
     
    SELECT MIN(sp.TRANS_DATE), ADDDATE(MAX(sp.TRANS_DATE), INTERVAL ppu.MONTHS_IN_PAST_FOR_AMC MONTH) INTO @startMonth, @stopMonth  FROM rm_supply_plan sp LEFT JOIN rm_program_planning_unit ppu ON sp.PROGRAM_ID=ppu.PROGRAM_ID AND sp.PLANNING_UNIT_ID=ppu.PLANNING_UNIT_ID WHERE sp.PROGRAM_ID=@programId and sp.VERSION_ID=@versionId;

    INSERT INTO rm_supply_plan_batch_info (
        PROGRAM_ID, VERSION_ID, PLANNING_UNIT_ID, BATCH_ID, TRANS_DATE, EXPIRY_DATE, 
        MANUAL_PLANNED_SHIPMENT_QTY, MANUAL_SUBMITTED_SHIPMENT_QTY, MANUAL_APPROVED_SHIPMENT_QTY, MANUAL_SHIPPED_SHIPMENT_QTY, MANUAL_RECEIVED_SHIPMENT_QTY, MANUAL_ONHOLD_SHIPMENT_QTY, 
        ERP_PLANNED_SHIPMENT_QTY, ERP_SUBMITTED_SHIPMENT_QTY, ERP_APPROVED_SHIPMENT_QTY, ERP_SHIPPED_SHIPMENT_QTY, ERP_RECEIVED_SHIPMENT_QTY, ERP_ONHOLD_SHIPMENT_QTY, 
        SHIPMENT_QTY, ACTUAL, ACTUAL_CONSUMPTION_QTY, FORECASTED_CONSUMPTION_QTY, ADJUSTMENT_MULTIPLIED_QTY, STOCK_MULTIPLIED_QTY)
    SELECT
        @programId `PROGRAM_ID`, @versionId `VERSION_ID`, m3.PLANNING_UNIT_Id,  m3.BATCH_ID, m3.MONTH `TRANS_DATE`, IFNULL(bi.EXPIRY_DATE, '2099-12-31') `EXPIRY_DATE`, 
        IFNULL(sp.MANUAL_PLANNED_SHIPMENT_QTY,0) `MANUAL_PLANNED_SHIPMENT_QTY`, IFNULL(sp.MANUAL_SUBMITTED_SHIPMENT_QTY,0) `MANUAL_SUBMITTED_SHIPMENT_QTY`, IFNULL(sp.MANUAL_APPROVED_SHIPMENT_QTY,0) `MANUAL_APPROVED_SHIPMENT_QTY`,IFNULL(sp.MANUAL_SHIPPED_SHIPMENT_QTY,0) `MANUAL_SHIPPED_SHIPMENT_QTY`, IFNULL(sp.MANUAL_RECEIVED_SHIPMENT_QTY,0) `MANUAL_RECEIVED_SHIPMENT_QTY`, IFNULL(sp.MANUAL_ONHOLD_SHIPMENT_QTY,0) `MANUAL_ONHOLD_SHIPMENT_QTY`, 
        IFNULL(sp.ERP_PLANNED_SHIPMENT_QTY,0) `ERP_PLANNED_SHIPMENT_QTY`, IFNULL(sp.ERP_SUBMITTED_SHIPMENT_QTY,0) `ERP_SUBMITTED_SHIPMENT_QTY`, IFNULL(sp.ERP_APPROVED_SHIPMENT_QTY,0) `ERP_APPROVED_SHIPMENT_QTY`,IFNULL(sp.ERP_SHIPPED_SHIPMENT_QTY,0) `ERP_SHIPPED_SHIPMENT_QTY`, IFNULL(sp.ERP_RECEIVED_SHIPMENT_QTY,0) `ERP_RECEIVED_SHIPMENT_QTY`, IFNULL(sp.ERP_ONHOLD_SHIPMENT_QTY,0) `ERP_ONHOLD_SHIPMENT_QTY`, 
        IFNULL(sp.SHIPMENT_QTY,0) `SHIPMENT_QTY`, sp.ACTUAL, IFNULL(sp.ACTUAL_CONSUMPTION_QTY, 0) `ACTUAL_CONSUMPTION_QTY`, IFNULL(sp.FORECASTED_CONSUMPTION_QTY,0) `FORECASTED_CONSUMPTION_QTY`, IFNULL(sp.ADJUSTMENT_MULTIPLIED_QTY,0) `ADJUSTMENT_MULTIPLIED_QTY`, IFNULL(sp.STOCK_MULTIPLIED_QTY,0) `STOCK_MULTIPLIED_QTY`
    FROM (
        SELECT
            m.PLANNING_UNIT_ID, m.BATCH_ID, mn.MONTH
        FROM (
            SELECT
                sp.PLANNING_UNIT_ID, sp.BATCH_ID
            FROM rm_supply_plan sp
            WHERE sp.PROGRAM_ID=@programId and sp.VERSION_ID=@versionId
            GROUP BY sp.PLANNING_UNIT_ID, sp.BATCH_ID
        ) m JOIN mn ON mn.MONTH BETWEEN @startMonth AND @stopMonth
    ) m3
    LEFT JOIN rm_supply_plan sp ON m3.PLANNING_UNIT_ID=sp.PLANNING_UNIT_ID AND m3.BATCH_ID=sp.BATCH_ID AND m3.MONTH=sp.TRANS_DATE
    LEFT JOIN rm_batch_info bi ON m3.BATCH_ID=bi.BATCH_ID
    ORDER BY m3.PLANNING_UNIT_ID, `TRANS_DATE`, IF(m3.BATCH_ID=0,9999999999,m3.BATCH_ID), `SHIPMENT_QTY`;

--    UPDATE rm_supply_plan_batch_info spbi LEFT JOIN rm_supply_plan sp ON spbi.PLANNING_UNIT_ID=sp.PLANNING_UNIT_ID AND spbi.TRANS_DATE=sp.TRANS_DATE SET spbi.ACTUAL=sp.ACTUAL WHERE sp.ACTUAL IS NOT NULL;       
    
    SELECT
        spbi.`SUPPLY_PLAN_BATCH_INFO_ID`, spbi.`PROGRAM_ID`, spbi.`VERSION_ID`, spbi.`PLANNING_UNIT_ID`, spbi.`BATCH_ID`, spbi.`TRANS_DATE`, spbi.`EXPIRY_DATE`, 
        spbi.`MANUAL_PLANNED_SHIPMENT_QTY`, spbi.`MANUAL_SUBMITTED_SHIPMENT_QTY`, spbi.`MANUAL_APPROVED_SHIPMENT_QTY`, spbi.`MANUAL_SHIPPED_SHIPMENT_QTY`, spbi.`MANUAL_RECEIVED_SHIPMENT_QTY`, spbi.`MANUAL_ONHOLD_SHIPMENT_QTY`, 
        spbi.`ERP_PLANNED_SHIPMENT_QTY`, spbi.`ERP_SUBMITTED_SHIPMENT_QTY`, spbi.`ERP_APPROVED_SHIPMENT_QTY`, spbi.`ERP_SHIPPED_SHIPMENT_QTY`, spbi.`ERP_RECEIVED_SHIPMENT_QTY`, spbi.`ERP_ONHOLD_SHIPMENT_QTY`, 
        spbi.`SHIPMENT_QTY`, IF (spbi.`ACTUAL`=1, spbi.`ACTUAL_CONSUMPTION_QTY`, spbi.`FORECASTED_CONSUMPTION_QTY`) `CONSUMPTION`, spbi.`ADJUSTMENT_MULTIPLIED_QTY`, spbi.`STOCK_MULTIPLIED_QTY`
    FROM rm_supply_plan_batch_info spbi
    WHERE spbi.PROGRAM_ID=@programId AND spbi.VERSION_ID=@versionId
    ORDER BY spbi.PLANNING_UNIT_ID, spbi.TRANS_DATE, spbi.EXPIRY_DATE, IF(spbi.BATCH_ID=0, 9999999999,spbi.BATCH_ID);
    
END