/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.model.report;

import cc.altius.FASP.model.SimpleObject;
import java.io.Serializable;

/**
 *
 * @author akil
 */
public class StockOverTimeOutput implements Serializable {

    private String dt;
    private SimpleObject program;
    private SimpleObject planningUnit;
    private int stock;
    private int consumptionQty;
    private double amc;
    private int amcMonthCount;
    private double mos;

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public SimpleObject getProgram() {
        return program;
    }

    public void setProgram(SimpleObject program) {
        this.program = program;
    }

    public SimpleObject getPlanningUnit() {
        return planningUnit;
    }

    public void setPlanningUnit(SimpleObject planningUnit) {
        this.planningUnit = planningUnit;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getConsumptionQty() {
        return consumptionQty;
    }

    public void setConsumptionQty(int consumptionQty) {
        this.consumptionQty = consumptionQty;
    }

    public double getAmc() {
        return amc;
    }

    public void setAmc(double amc) {
        this.amc = amc;
    }

    public int getAmcMonthCount() {
        return amcMonthCount;
    }

    public void setAmcMonthCount(int amcMonthCount) {
        this.amcMonthCount = amcMonthCount;
    }

    public double getMos() {
        return mos;
    }

    public void setMos(double mos) {
        this.mos = mos;
    }
    
    
}
