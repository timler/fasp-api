/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.model.report;

import cc.altius.FASP.framework.JsonDateDeserializer;
import cc.altius.FASP.framework.JsonDateSerializer;
import cc.altius.FASP.model.SimpleObject;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author akil
 */
public class StockStatusForProgramOutput implements Serializable {
    
    private SimpleObject planningUnit;
    private int minMos;
    private int maxMos;
    private Integer stock;
    private Double amc;
    private Double mos;
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    private Date lastStockCount;

    public SimpleObject getPlanningUnit() {
        return planningUnit;
    }

    public void setPlanningUnit(SimpleObject planningUnit) {
        this.planningUnit = planningUnit;
    }

    public int getMinMos() {
        return minMos;
    }

    public void setMinMos(int minMos) {
        this.minMos = minMos;
    }

    public int getMaxMos() {
        return maxMos;
    }

    public void setMaxMos(int maxMos) {
        this.maxMos = maxMos;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Double getAmc() {
        return amc;
    }

    public void setAmc(Double amc) {
        this.amc = amc;
    }

    public Double getMos() {
        return mos;
    }

    public void setMos(Double mos) {
        this.mos = mos;
    }

    public Date getLastStockCount() {
        return lastStockCount;
    }

    public void setLastStockCount(Date lastStockCount) {
        this.lastStockCount = lastStockCount;
    }
    
}