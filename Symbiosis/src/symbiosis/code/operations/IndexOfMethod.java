/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
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
import symbiosis.meta.classrelations.Relation;
import symbiosis.meta.objectmodel.BaseType;
import symbiosis.meta.objectmodel.ConstrainedBaseType;
import symbiosis.meta.objectmodel.ObjectType;
import symbiosis.meta.objectmodel.SubstitutionType;
import symbiosis.util.Naming;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class IndexOfMethod extends Method implements IRelationalOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;
    public final static String NAME_PREFIX = "nrOf";

    public IndexOfMethod(Relation relation, ObjectType parent) {
        super(parent, NAME_PREFIX + Naming.withCapital(relation.name()), null, parent.getCodeClass());
        this.relation = relation;
        List<Param> params = new ArrayList<>();
        Param param = new Param(relation.name(), relation.targetType(), relation);
        params.add(param);
        setParams(params);
        if (relation.isSeqRelation()) {
            SubstitutionType indexType = relation.qualifierRoles().get(0).getSubstitutionType();
            if (indexType instanceof ConstrainedBaseType) {
                returnType = new ReturnType(indexType);
            } else {
                returnType = new ReturnType(BaseType.INTEGER);
            }
        } else {
            returnType = new ReturnType(BaseType.INTEGER);
        }
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        if (relation.qualifierRoles().get(0).getSubstitutionType().equals(BaseType.NATURAL)) {
            IndentedList falseStatement = new IndentedList();
            IndentedList trueStatement = new IndentedList();
            String result = l.indexOf(relation, getParams().get(0).getName());
            falseStatement.addLineAtCurrentIndentation(
                l.returnStatement(result + " + 1"));
            trueStatement.addLineAtCurrentIndentation(l.returnStatement(" -1"));
            list.addLinesAtCurrentIndentation(l.ifStatement(l.equalsStatement(result, "-1"), trueStatement, falseStatement));

        } else {
            list.addLineAtCurrentIndentation(l.returnStatement(l.indexOf(relation, getParams().get(0).getName())));
        }
        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
    }

    @Override
    public Set<ImportType> getImports() {
        Set<ImportType> imports = new HashSet();
        imports.add(ImportType.ObjectEquals);
        return imports;
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
        if (returnType.getType() == BaseType.INTEGER) {
            returnType.setSpec("if " + relation.getPluralName()
                + " includes " + getParams().get(0).getName()
                + " the index which holds: "
                + relation.getPluralName() + "(nr) = " + getParams().get(0).getName()
                + ", otherwise -1");
        } else {
            returnType.setSpec("if " + relation.getPluralName()
                + " includes " + getParams().get(0).getName()
                + " the index which holds: "
                + relation.getPluralName() + "(nr) = " + getParams().get(0).getName()
                + ", otherwise null");

        }

    }


}
