/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import equa.meta.ChangeNotAllowedException;
import equa.meta.MismatchException;
import equa.meta.requirements.RequirementModel;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.Category;
import equa.meta.traceability.ExternalInput;
import equa.meta.traceability.ModelElement;
import equa.meta.traceability.Source;
import equa.project.ProjectRole;
import equa.util.Naming;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 *
 * @author frankpeeters
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class CBTRole extends ObjectRole {

    private static final long serialVersionUID = 1L;
    

    public CBTRole() {
    }

    public CBTRole(ConstrainedBaseType ot, FactType parent) {
        super(ot, parent);

    }

    @Override
    public boolean isAddable() {
        return false;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @Override
    public boolean isNavigable() {
        if (this.isQualifier()) {
            return false;
        }
        Role counterpart = getParent().counterpart(this);
        if (counterpart == null) {
            return false;
        } else if (counterpart.getSubstitutionType().isValueType()) {
            return super.isNavigable();
        }
        return false;
    }

    @Override
    public boolean isComposition() {
        return false;
    }

    @Override
    public boolean setComposition(boolean composition) {
        return false;
    }

    @Override
    public boolean isSettable() {
        return false;
    }

    /**
     * removable constraint will be added; concerning rule requirement made by
     * active project member will be added to
     *
     * @param justification made by active project member
     */
    @Override
    public void addRemovable(String justification) {
    }

   

    public void setDefaultValue(String value) throws MismatchException, ChangeNotAllowedException {
        if (this.defaultValue != null && this.defaultValue.getValue().equalsIgnoreCase(value)) {
            return;
        }

        ConstrainedBaseType cbt = (ConstrainedBaseType) this.getSubstitutionType();
        cbt.checkSyntaxis(value);

        String rolename = getRoleName();
        if (rolename.isEmpty()) {
            rolename = Naming.withoutCapital(cbt.getName());
        }

        if (this.defaultValue != null) {
            this.defaultValue.remove();
        }

        ObjectModel om = (ObjectModel) getParent().getParent();
        RequirementModel rm = om.getProject().getRequirementModel();
        ProjectRole projectRole = om.getProject().getCurrentUser();
        Category cat = getParent().getCategory();

        RuleRequirement rule = rm.addRuleRequirement(cat,
                value + " is the default value of <" + rolename
                + "> in " + getParent().getFactTypeString(),
                new ExternalInput("", projectRole));

        this.defaultValue = new DefaultValueConstraint(this, value, rule);
    }
    

    @Override
    public boolean isSeqNr() {
        return false; //isQualifier() && (((ConstrainedBaseType) getSubstitutionType()).isSuitableAsIndex());
    }

}
