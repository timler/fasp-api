/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.model.report;

import cc.altius.FASP.framework.JsonDateDeserializer;
import cc.altius.FASP.framework.JsonDateSerializer;
import cc.altius.FASP.model.SimpleBatchQuantity;
import cc.altius.FASP.model.Views;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Date;

/**
 *
 * @author akil
 */
public class SimpleBatchQuantityWithTransHistory extends SimpleBatchQuantity {

    @JsonView({Views.ReportView.class})
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    private Date transDate;

    public SimpleBatchQuantityWithTransHistory() {
        super();
    }

    public SimpleBatchQuantityWithTransHistory(Date transDate, int batchId, String batchNo, Date expiryDate, boolean autoGenerated, double qty, double qtyWps, double expiredQty, double expiredQtyWps, Date createdDate, Double shipmentQty, Double shipmentQtyWps) {
        super(batchId, batchNo, expiryDate, autoGenerated, qty, qtyWps, expiredQty, expiredQtyWps, createdDate, shipmentQty, shipmentQtyWps);
        this.transDate = transDate;
    }

    public Date getTransDate() {
        return transDate;
    }

    public void setTransDate(Date transDate) {
        this.transDate = transDate;
    }

}
