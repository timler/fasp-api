/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.model;

/**
 *
 * @author akil
 */
public class SimpleUnitObjectWithMultiplier extends SimpleUnitObject {

    private double multiplier;

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public SimpleUnitObjectWithMultiplier() {
    }

    public SimpleUnitObjectWithMultiplier(SimpleCodeObject unit, Integer id, Label label, double multiplier) {
        super(unit, id, label);
        this.multiplier = multiplier;
    }

}
