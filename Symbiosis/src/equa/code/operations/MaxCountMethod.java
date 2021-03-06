/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.ImportType.ObjectEquals;

import java.util.ArrayList;
import java.util.List;

import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.Constraint;
import equa.meta.objectmodel.ObjectType;
import equa.meta.requirements.RuleRequirement;
import equa.util.Naming;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class MaxCountMethod extends Method implements IBooleanOperation, IRelationalOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;
    private final int max;

    public MaxCountMethod(Relation relation, ObjectType ot, int max, Constraint constraint) {
        super(ot, "maxCount" + Naming.withCapital(relation.name()),
                new ArrayList<Param>(), constraint);
        this.relation = relation;
        this.max = max;
        List<Param> params = new ArrayList<>();
        addQualifiers(params, relation);
        setParams(params);
        returnType = new ReturnType(BaseType.BOOLEAN);
    }

    @Override
    public void initSpec() {
        Operation count = getCodeClass().getOperation("count", relation);
        IPredicate returnSpec = new InformalPredicate("@return = (" + count.callString() + " = " + max + ")");
        returnType.setSpec(returnSpec.toString());
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        Operation count = getCodeClass().getOperation("count", relation);
        list.addLineAtCurrentIndentation(l.returnStatement(l.equalsStatement(l.callMethod("", count.getName()), max + "")));
        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
    }

    @Override
    public Set<ImportType> getImports() {
        Set<ImportType> list = new HashSet<>();
        list.add(ObjectEquals);
        return list;
    }

    @Override
    public RuleRequirement getRuleRequirement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Relation getRelation() {
        return relation;
    }

    @Override
    public Field getField() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
