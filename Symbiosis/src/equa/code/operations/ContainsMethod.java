/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.util.ArrayList;
import java.util.List;

import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.SubstitutionType;
import equa.meta.requirements.RuleRequirement;
import equa.util.Naming;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class ContainsMethod extends Method implements IRelationalOperation, IBooleanOperation {

    private static final long serialVersionUID = 1L;
    public static final String NAME = "contains";
    private final Relation relation;

    public ContainsMethod(Relation relation, ObjectType ot, SubstitutionType st) {
        super(ot, NAME + Naming.withCapital(relation.name()), null, ot.getCodeClass());
        this.relation = relation;
        List<Param> params = new ArrayList<>();
        params.add(new Param(Naming.withoutCapital(relation.name()), st, relation));
        setParams(params);
        setReturnType(new ReturnType(BaseType.BOOLEAN));
    }

    @Override
    public IndentedList getCode(Language l) {
    	IndentedList list = new IndentedList();
    	list.addLinesAtCurrentIndentation(l.operationHeader(this));
    	//we use a contains method from the standard libraries
    	list.addLineAtCurrentIndentation(l.returnStatement(l.contains(relation, getParams().get(0).getName())));
    	list.addLinesAtCurrentIndentation(l.bodyClosure());
    	return list;
    }

    @Override
    public Relation getRelation() {
        return relation;
    }

    @Override
    public Field getField() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initSpec() {
        returnType.setSpec("true if " + getParams().get(0).getName() + " is registered at " + self()
                + " otherwise false");
    }

    @Override
    public Set<ImportType> getImports() {
        return new HashSet<ImportType>(); }

    @Override
    public RuleRequirement getRuleRequirement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
