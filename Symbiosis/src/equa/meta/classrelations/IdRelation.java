/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.classrelations;

import static equa.code.CodeNames.AUTO_INCR_NEXT;
import equa.meta.objectmodel.BaseValueRole;
import equa.meta.objectmodel.RoleEvent;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.meta.objectmodel.SubstitutionType;
import equa.util.Naming;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author frankpeeters
 */
public class IdRelation extends Relation {

    private static final long serialVersionUID = 1L;

    public IdRelation(ObjectType owner, Role role) {
        super(owner, role);
    }

    @Override
    public String multiplicity() {
        return "1";
    }

    @Override
    public boolean isCreational() {
        return false;
    }

    @Override
    public boolean isMandatory() {
        return true;
    }

    @Override
    public boolean hasMultipleTarget() {
        return false;
    }

    @Override
    public boolean isComposition() {
        return false;
    }

    @Override
    public String name() {
        return role.detectRoleName();
    }

    @Override
    public boolean isNavigable() {
        return true;
    }

    @Override
    public SubstitutionType targetType() {
        SubstitutionType st = role.getSubstitutionType();
        if (st instanceof ObjectType) {
            ObjectType ot = (ObjectType) st;
            if (ot.isAbstract()) {
                ObjectType concreteSubType = ot.concreteSubType();
                if (concreteSubType != null) {
                    st = concreteSubType;
                }
            }
        }
        return st;
    }

//    @Override
//    public boolean isAnchorRelation() {
//        Relation inverse = inverse();
//        if (inverse == null) {
//            return false;
//        } else {
//            return inverse.multiplicity().equals("1");
//        }
//    }
    @Override
    public Relation inverse() {
        if (role instanceof BaseValueRole) {
            return null;
        } else {
            return new ObjectTypeRelation((ObjectType) role.getSubstitutionType(), role);
        }
    }

    @Override
    public String asAttribute() {
        return "- " + name() + " : " + targetType().getName();
    }

    @Override
    public boolean isAdjustable() {
        return false;
    }

    @Override
    public boolean isSettable() {
        return false;
    }

    @Override
    public boolean isAddable() {
        return false;
    }

    @Override
    public boolean isInsertable() {
        return false;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @Override
    public boolean isPartOfId() {
        return true;
    }

    @Override
    public boolean hasNoDefaultValue() {
        return true;
    }

    @Override
    public String getDefaultValueString() {
        return null;
    }

    @Override
    public String fieldName() {
        {
            if (role.hasDefaultName()) {
                return Naming.withoutCapital(role.getSubstitutionType().getName());
            } else {
                return role.getRoleName();
            }
        }
    }

    @Override
    public boolean isSeqRelation() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return !role.getParent().isMutable();
    }

    @Override
    public boolean isMapRelation() {
        return false;
    }

    @Override
    public List<RoleEvent> getEvents() {
        return new ArrayList<>();
    }

    @Override
    public int compareTo(Relation relation) {
        if (equals(relation)) {
            return 0;
        } else if (relation instanceof IdRelation) {
            if (name().equals(relation.name())) {
                return hashCode() - relation.hashCode();
            } else {
                return name().compareTo(relation.name());
            }
        } else {
            return -1;
        }
    }

    @Override
    public String getAutoIncrFieldName() {
        Role autoIncrRole = role.getParent().getAutoIncrRole();
        if (autoIncrRole == null) {
            return null;
        } else {
            return autoIncrRole.detectRoleName() + "Next" + getOwner().getName();
        }

    }

}
