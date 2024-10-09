ALTER TABLE `fasp`.`rm_procurement_agent` CHANGE `SUBMITTED_TO_APPROVED_LEAD_TIME` `SUBMITTED_TO_APPROVED_LEAD_TIME` DECIMAL(4,2) UNSIGNED NULL COMMENT 'No of days for an Order to move from Submitted to Approved status, this will be used only in the case the Procurement Agent is TBD', CHANGE `APPROVED_TO_SHIPPED_LEAD_TIME` `APPROVED_TO_SHIPPED_LEAD_TIME` DECIMAL(4,2) UNSIGNED NULL ; 