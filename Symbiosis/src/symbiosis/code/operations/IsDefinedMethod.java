/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import symbiosis.code.Field;
import symbiosis.code.ImportType;
import symbiosis.code.IndentedList;
import symbiosis.code.Language;
import symbiosis.meta.classrelations.QualifierRelation;
import symbiosis.meta.classrelations.Relation;
import symbiosis.meta.objectmodel.BaseType;
import symbiosis.meta.objectmodel.ObjectType;
import symbiosis.meta.objectmodel.Role;
import symbiosis.meta.requirements.RuleRequirement;
import symbiosis.util.Naming;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class IsDefinedMethod extends Method implements IBooleanOperation, IRelationalOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;

    public IsDefinedMethod(Relation relation, ObjectType ot) {
        super(ot, "isDefined" + Naming.withCapital(relation.name()), null, ot.getCodeClass());
        this.relation = relation;

        List<Role> qualifiers = relation.qualifierRoles();
        List<Param> params = new ArrayList<>();
        for (Role role : qualifiers) {
            params.add(new Param(role.detectRoleName(), role.getSubstitutionType(), new QualifierRelation(ot, role)));
        }
        setParams(params);

        setReturnType(new ReturnType(BaseType.BOOLEAN));
    }

    @Override
    public void initSpec() {

        returnType.setSpec("true if " + relation.name() + " has a defined value"
                + " otherwise false");
    }

    @Override
    public RuleRequirement getRuleRequirement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String returnValue() {
        return returnType.getSpec();
    }

    @Override
    public Set<ImportType> getImports() {
        return new HashSet();
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        list.addLineAtCurrentIndentation(l.returnStatement(relation.fieldName() + "Defined"));
        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
    }

    @Override
    public Relation getRelation() {
        return relation;
    }

    @Override
    public Field getField() {
        final boolean AUTO_INCR = true;
        return new Field(BaseType.BOOLEAN, getName(), !AUTO_INCR);
    }
    
    
     @Override
    public BooleanCall call() {
        return new BooleanCall(this, false);
    }
}
