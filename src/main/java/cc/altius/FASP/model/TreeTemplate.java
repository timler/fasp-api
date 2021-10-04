/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.model;

import cc.altius.utils.TreeUtils.Tree;
import com.fasterxml.jackson.annotation.JsonView;
import java.io.Serializable;

/**
 *
 * @author akil
 */
public class TreeTemplate extends BaseModel implements Serializable {

    @JsonView(Views.InternalView.class)
    private int treeTemplateId;
    @JsonView(Views.InternalView.class)
    private SimpleCodeObject realm;
    @JsonView(Views.InternalView.class)
    private Label label;
    @JsonView(Views.InternalView.class)
    private SimpleObjectWithType forecastMethod;
    @JsonView(Views.InternalView.class)
    private Tree<TreeNode> tree;
    

    public TreeTemplate() {
    }

    public TreeTemplate(int treeTemplateId) {
        this.treeTemplateId = treeTemplateId;
    }

    public int getTreeTemplateId() {
        return treeTemplateId;
    }

    public void setTreeTemplateId(int treeTemplateId) {
        this.treeTemplateId = treeTemplateId;
    }

    public SimpleCodeObject getRealm() {
        return realm;
    }

    public void setRealm(SimpleCodeObject realm) {
        this.realm = realm;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public SimpleObjectWithType getForecastMethod() {
        return forecastMethod;
    }

    public void setForecastMethod(SimpleObjectWithType forecastMethod) {
        this.forecastMethod = forecastMethod;
    }

    public Tree<TreeNode> getTree() {
        return tree;
    }

    public void setTree(Tree<TreeNode> tree) {
        this.tree = tree;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.treeTemplateId;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TreeTemplate other = (TreeTemplate) obj;
        if (this.treeTemplateId != other.treeTemplateId) {
            return false;
        }
        return true;
    }

}
