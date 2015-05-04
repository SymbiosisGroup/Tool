/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.meta.objectmodel;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import symbiosis.meta.ChangeNotAllowedException;
import symbiosis.meta.MismatchException;
import symbiosis.meta.requirements.RequirementModel;
import symbiosis.meta.requirements.RuleRequirement;
import symbiosis.meta.traceability.Category;
import symbiosis.meta.traceability.ExternalInput;
import symbiosis.meta.traceability.ModelElement;
import symbiosis.project.ProjectRole;
import symbiosis.util.Naming;

/**
 *
 * @author frankpeeters
 */
@Entity
public class BaseValueRole extends Role {

    private static final long serialVersionUID = 1L;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private BaseType bt;
    @Column
    private DefaultValueConstraint defaultValue;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private AutoIncrConstraint autoIncr;
    @Transient
    private transient BaseType toDisconnect;

    public BaseValueRole() {
    }

    public BaseValueRole(BaseType bt, FactType parent) {
        super(parent);
        this.bt = bt;
        defaultValue = null;
        autoIncr = null;
        toDisconnect = null;
    }

    public void setAutoIncrement(boolean autoIncrement) throws ChangeNotAllowedException {
        if (isAutoIncr() == autoIncrement) {
            return;
        }

        if (autoIncrement) {
            if (!bt.equals(BaseType.NATURAL)) {
                throw new ChangeNotAllowedException(
                    "auto increment only allowed in case of natural number role");
            }
            if (getDefaultValue() != null) {
                defaultValue.remove();
            }

            ObjectModel om = (ObjectModel) getParent().getParent();
            RequirementModel rm = om.getProject().getRequirementModel();
            ProjectRole projectRole = om.getProject().getCurrentUser();

            RuleRequirement autoIncrRule = rm.addRuleRequirement(getParent().getCategory(),
                "auto increment",
                new ExternalInput("", projectRole));
            autoIncr = new AutoIncrConstraint(this, autoIncrRule);

        } else {
            autoIncr.remove();
            autoIncr = null;
        }

    }

    @Override
    public boolean isAutoIncr() {
        return autoIncr != null;
    }

    @Override
    public String getDefaultValue() {
        if (defaultValue == null) {
            return null;
        } else {
            return defaultValue.getValue();
        }
    }

    public void removeDefaultValue() {
        if (defaultValue != null) {

            defaultValue.remove();

        }
    }

    public void setDefaultValue(String value) throws MismatchException, ChangeNotAllowedException {
        if (defaultValue != null && defaultValue.getValue().equalsIgnoreCase(value)) {
            return;
        }
        Role cp = getParent().counterpart(this);
        if (cp != null && !cp.isMandatory()) {
            throw new ChangeNotAllowedException("Default value makes no sense if counterpart role is not mandatory.");
        }
        if (isAutoIncr()) {
            throw new ChangeNotAllowedException("Default value cannot be combined with auto increment");
        }
        bt.checkSyntaxis(value);

        String rolename = getRoleName();
        if (rolename.isEmpty()) {
            rolename = Naming.withoutCapital(bt.getName());
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
        rule.setManuallyCreated(false);
        this.defaultValue = new DefaultValueConstraint(this, value, rule);
    }

    void deleteDefaultValue() {
        if (defaultValue != null) {
            defaultValue = null;
        }
    }

    @Override
    public BaseType getSubstitutionType() {
        return bt;
    }

    @Override
    public boolean isNavigable() {
        return false;
    }

    @Override
    public void setNavigable(boolean navigable) {
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    SubstitutionType disconnect() {
        toDisconnect = bt;
        bt = null;
        return toDisconnect;
    }

    @Override
    void reconnect() {
        bt = toDisconnect;
        toDisconnect = null;
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
    public String getConstraintString() {
        Iterator<StaticConstraint> it = constraints();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            sb.append(it.next().getDescription()).append(" ");
        }
        if (isQualifier()) {
            if (isSeqNr()) {
                sb.append("seq ");
            } else {
                sb.append("map ");
            }
        }
        if (isHidden()) {
            sb.append("hid ");
        }
        if (hasDefaultValue()) {
            sb.append(defaultValue.getValue());
        }
        if (isAutoIncr()) {
            sb.append("incr");
        }

        return sb.toString().trim();

    }

    @Override
    public boolean isSettable() {
        return false;
    }

    @Override
    public boolean isAdjustable() {
        return false;
    }

    @Override
    public boolean isInsertable() {
        return false;
    }

    @Override
    public boolean isSeqNr() {
        return isQualifier() && bt.equals(BaseType.NATURAL);
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
    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    @Override
    public void remove(ModelElement member) {
        //try {
        if (member instanceof DefaultValueConstraint) {
            if (defaultValue != null) {
                defaultValue = null;
            }
        } else if (member instanceof AutoIncrConstraint) {
            if (autoIncr != null) {
                autoIncr = null;
            }
        } else {
            super.remove(member);
        }
    }

    public void setBaseType(BaseType bt) {
        if (bt != BaseType.NATURAL) {
            if (autoIncr != null) {
                autoIncr.remove();
                autoIncr = null;
            }

        }

        int roleNr = -1;
        List<Role> roles = getParent().roles;
        for (int i = 0; i < getParent().size(); i++) {
            if (roles.get(i) == this) {
                roleNr = i;
            }
        }
        getParent().getPopulation().changeBaseValueRole(roleNr, bt);
        this.bt = bt;

    }

    @Override
    public boolean isCandidateAutoIncr() {
        return bt.equals(BaseType.NATURAL);
    }

    @Override
    public boolean isCandidateAddable() {
        return false;
    }

    @Override
    public boolean isCandidateAdjustable() {
        return false;
    }

    @Override
    public boolean isCandidateSettable() {
        return false;
    }

    @Override
    public boolean isCandidateInsertable() {
        return false;
    }

    @Override
    public boolean isCandidateRemovable() {
        return false;
    }

    @Override
    public boolean isCandidateComposition() {
        return false;
    }

    @Override
    boolean isResponsibleForNonVT() {
        return false;
    }

    @Override
    public String getName() {
        return "role " + this.detectRoleName() + " of " + getParent().getName();
    }

    @Override
    public boolean equals(Object object) {
        return this == object;
    }

    @Override
    public void remove() {

        if (autoIncr != null) {
            autoIncr.remove();
            autoIncr = null;
            publisher.inform(this, "autoincr", null, isAutoIncr());
        }
        if (defaultValue != null) {
            defaultValue.remove();
            defaultValue = null;
        }

        super.remove();
    }

    @Override
    public void expandSubstitutionType(ObjectType ot) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isCreational() {
        return false;
    }

    @Override
    public boolean isMappingRole() {
        return false;
    }

    @Override
    boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    void removePermissions() {
    }

    @Override
    public boolean isResponsible() {
        return false;
    }

    @Override
    public boolean isEventSource() {
        return false;
    }

}
