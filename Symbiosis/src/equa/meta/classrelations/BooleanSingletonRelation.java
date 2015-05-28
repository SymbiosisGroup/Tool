/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.classrelations;

import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectRole;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.meta.objectmodel.RoleEvent;
import equa.meta.objectmodel.SubstitutionType;
import equa.util.Naming;
import java.util.List;

/**
 *
 * @author frankpeeters
 */
public class BooleanSingletonRelation extends BooleanRelation {

    private static final long serialVersionUID = 1L;

    public BooleanSingletonRelation(ObjectType owner, Role role) {
        super(owner, role);
    }

    @Override
    public boolean isNavigable() {
        return role.isNavigable();
    }

    @Override
    public Relation inverse() {
        Role counterpart = role.getParent().counterpart(role);
        
        // if (counterpart != null && !role.isMandatory() && !counterpart.isCreational() && counterpart.getSubstitutionType().isSingleton()) {
        return new FactTypeRelation((ObjectType) counterpart.getSubstitutionType(), counterpart);

    }

}
