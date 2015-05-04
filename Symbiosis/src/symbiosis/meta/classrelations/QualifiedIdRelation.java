/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.meta.classrelations;

import static symbiosis.code.CodeNames.AUTO_INCR_NEXT;
import java.util.List;

import symbiosis.meta.objectmodel.ObjectType;
import symbiosis.meta.objectmodel.Role;
import symbiosis.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class QualifiedIdRelation extends IdRelation {

    private static final long serialVersionUID = 1L;
    private List<Role> qualifiers;

    public QualifiedIdRelation(ObjectType owner, Role role, List<Role> qualifiers) {
        super(owner, role);
        this.qualifiers = qualifiers;
    }

    /**
     *
     * @return a list of concerning roles with respect to this qualified
     * relation
     */
    public List<Role> qualifiers() {
        return qualifiers;
    }

    @Override
    public boolean isSettable() {
        return false;
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

   

}
