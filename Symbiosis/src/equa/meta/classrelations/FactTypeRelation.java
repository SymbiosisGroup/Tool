/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.classrelations;

import equa.code.operations.CT;
import equa.code.operations.CollectionKind;
import equa.meta.objectmodel.BaseValueRole;
import equa.meta.objectmodel.CollectionType;
import equa.meta.objectmodel.ElementsFactType;
import equa.meta.objectmodel.RoleEvent;
import equa.meta.objectmodel.ObjectRole;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.meta.objectmodel.SubstitutionType;
import equa.util.Naming;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author frankpeeters
 */
public class FactTypeRelation extends Relation {

    private static final long serialVersionUID = 1L;
    private final Role relatedRole;

    public FactTypeRelation(ObjectType owner, Role role) {
        super(owner, role);

        if (role.isQualifier()) {
            throw new RuntimeException("qualifier role is not allowed in fact type relation");
        } else {
            relatedRole = role.getParent().counterpart(role);
            if (relatedRole == null) {
                System.out.println("unknown related role at " + owner.getName() + " ; " + role.toString());
            }
        }
    }

    @Override
    public SubstitutionType targetType() {
        if (relatedRole == null) {
            return null;
        }
        SubstitutionType st = relatedRole.getSubstitutionType();
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

    @Override
    public String name() {
        return relatedRole.detectRoleName();
    }

    @Override
    public boolean hasDefaultName() {
        return relatedRole.hasDefaultName();
    }

    @Override
    public Relation inverse() {
        if (relatedRole instanceof BaseValueRole) {
            return null;
        } else {
            if (!relatedRole.isMandatory() && !role.isCreational() && role.getSubstitutionType().isSingleton()) {
                return new BooleanRelation((ObjectType) relatedRole.getSubstitutionType(), relatedRole);
            } else {
                return new FactTypeRelation((ObjectType) relatedRole.getSubstitutionType(), relatedRole);
            }
        }
    }

    @Override
    public boolean isFinal() {
        if (hasMultipleTarget()) {
            return true;
        }
        if (getParent().hasMutableRole()) {
            return false;
        }
        if (isMandatory()) {
            if (isSettable() || isAdjustable() || relatedRole.isSettable()) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    @Override
    public boolean isSettable() {
        return role.isSettable() && role.hasSingleTarget();
    }

    @Override
    public boolean isNavigable() {
        return role.isNavigable();
    }

    @Override
    public String asAttribute() {
        if (hasMultipleTarget()) {
            if (this.isSeqRelation()) {
                return "- " + Naming.withoutCapital(Naming.plural(name())) + " : " + new CT(CollectionKind.LIST, targetType());
            } else {
                return "- " + Naming.withoutCapital(Naming.plural(name())) + " : " + new CT(CollectionKind.SET, targetType());
            }
        } else {
            return "- " + name() + " : " + targetType();
        }
    }

    @Override
    public boolean hasNoDefaultValue() {
//        if (relatedRole == null) {
//            return true;
//        }
        return !relatedRole.hasDefaultValue();
    }

    @Override
    public boolean isAdjustable() {
        return role.isAdjustable();
    }

    @Override
    public boolean isPartOfId() {
        return (role.getParent() instanceof ElementsFactType)
            && (role.getSubstitutionType() instanceof CollectionType);
    }
    
    public boolean isEventSource(){
        return role.isEventSource();
    }

    @Override
    public String getDefaultValue() {
        if (hasNoDefaultValue()) {
            return null;
        } else {
            return relatedRole.getDefaultValue();
        }
    }

    @Override
    public String fieldName() {
        if (hasMultipleTarget()) {
            return relatedRole.getPluralName();
        } else {
            return relatedRole.detectRoleName();
        }
    }

    @Override
    public String getPluralName() {
        return relatedRole.getPluralName();
    }

    @Override
    public boolean isSeqRelation() {
        return role.getParent().isSeqRole(role);
    }

    @Override
    public boolean isSeqAutoIncrRelation() {
        if (isSeqRelation()) {

            for (Role role : qualifierRoles()) {
                if (role.isAutoIncr()) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public boolean hasMultipleQualifiedTarget() {
        if (qualifierRoles().isEmpty()) {
            return false;
        }

        return role.hasCommonUniquenessWith(relatedRole);
    }

    @Override
    public String getAutoIncrFieldName() {
//        if (isCreational()) 
//        {
//            if (isSeqRelation()) {
//                return role.getParent().getAutoIncrRole();
//            } else {
//                Role counterpart = role.getParent().counterpart(role);
//                if (counterpart != null && counterpart.getSubstitutionType() instanceof ObjectType) {
//                    ObjectType cp = (ObjectType) counterpart.getSubstitutionType();
//                    return cp.getFactType().getAutoIncrRole();
//                } else {
//                    return null;
//                }
//            }
//        }
        Iterator<Role> it = role.getParent().roles();
        while (it.hasNext()) {
            Role r = it.next();
            if (r.isAutoIncr()) {
                return r.getName() + "Next" + Naming.withCapital(targetType().getName());
            }
        }
        return null;
    }

    @Override
    public List<RoleEvent> getEvents() {
        ObjectRole objectRole = (ObjectRole) role;
        return objectRole.getEvents();
    }

}
