/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.classrelations;

import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.RoleEvent;
import equa.meta.objectmodel.ObjectRole;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.meta.objectmodel.SubstitutionType;
import equa.util.Naming;
import java.util.List;

/**
 *
 * @author frankpeeters
 */
public class BooleanRelation extends Relation {

    private static final long serialVersionUID = 1L;

    public BooleanRelation(ObjectType owner, Role role) {
        super(owner, role);
    }

    @Override
    public SubstitutionType targetType() {
        return BaseType.BOOLEAN;
    }

    @Override
    public boolean hasMultipleTarget() {
        return this.isSeqRelation() || this.isMapRelation();
    }

    @Override
    public String name() {
        return Naming.withoutCapital(role.getParent().getName().replace(getOwner().getName(), ""));
    }

    @Override
    public boolean isCreational() {
        return false;
    }

    @Override
    public boolean isNavigable() {
            return true;
    }

    @Override
    public Relation inverse() {
        return null;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public String asAttribute() {
        return "- " + name() + " : " + BaseType.BOOLEAN;
    }

    @Override
    public boolean isAdjustable() {
        return false;
    }

    @Override
    public boolean isPartOfId() {
        return false;
    }

    @Override
    public boolean hasNoDefaultValue() {
        return role.getParent().getDefaultValue() == null;
    }

    @Override
    public String getDefaultValueString() {
        return role.getParent().getDefaultValue().toString();
    }

    @Override
    public String fieldName() {
        String fieldName = name();// role.getParent().getName();
        if (fieldName.startsWith("is") || fieldName.startsWith("Is")) {
            return Naming.withoutCapital(fieldName);
        } else {
            return "is" + Naming.withCapital(fieldName);
        }
    }

    @Override
    public boolean isSeqRelation() {
        return false;
    }

    @Override
    public boolean isSettable() {
        return role.isSettable();
    }

    @Override
    public List<RoleEvent> getEvents() {
        return ((ObjectRole) role).getEvents();
    }

}
