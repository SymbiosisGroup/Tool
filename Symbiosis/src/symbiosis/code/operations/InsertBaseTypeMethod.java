/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.operations;

import static symbiosis.code.CodeNames.TEMP1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import symbiosis.factuse.ActorInputItem;
import symbiosis.code.Field;
import symbiosis.code.ImportType;
import symbiosis.code.IndentedList;
import symbiosis.code.Language;
import symbiosis.meta.classrelations.Relation;
import symbiosis.meta.objectmodel.BaseType;
import symbiosis.meta.objectmodel.RoleEvent;
import symbiosis.meta.objectmodel.ObjectType;
import symbiosis.meta.objectmodel.SubstitutionType;
import symbiosis.util.Naming;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class InsertBaseTypeMethod extends Method implements IActionOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;

    public InsertBaseTypeMethod(Relation relation, ObjectType ot) {
        super(ot, "insert" + Naming.withCapital(relation.name()), null, ot.getCodeClass());
        this.relation = relation;
        List<Param> params = new ArrayList<>();
        addIndices(relation, params);
        params.add(new Param(relation.name(), relation.targetType(), relation));
        setParams(params);
        setReturnType(new ReturnType(null));
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.addLinesAtCurrentIndentation(l.operationHeader(this));

        StringBuilder sb = new StringBuilder();
        sb.append(relation.fieldName());
        sb.append(l.memberOperator());
        sb.append("add(");
        String separator = "";
        for (int i = 0; i < getParams().size(); i++) {
            sb.append(separator);
            sb.append(getParams().get(i).getName());
            separator = ", ";
        }
        sb.append(");");

        list.addLineAtCurrentIndentation(sb.toString());
        list.addLinesAtCurrentIndentation(l.postProcessing(this));
        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
    }

    private String searchParamName(Relation r) {
        for (Param p : getParams()) {
            if (p.getRelation().equals(r)) {
                return p.getName();
            }
        }
        return null;
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

        int size = getParams().size();
        List<Param> params = new ArrayList<>();
        String indices = addIndices(relation, params);
        SubstitutionType target = relation.targetType();
        Relation inverse = relation.inverse();
        StringBuilder sb = new StringBuilder();
        sb.append("@result is inserted in ").append(self()).append(".");
        sb.append(relation.getPluralName());
        sb.append(" at index ");

        sb.append(indices);
        if (inverse != null && inverse.isNavigable()) {
            if (inverse.hasMultipleTarget()) {
                sb.append(" AND @result.contains(").append(self()).append(")");
            } else {
                sb.append(" AND @result knows ").append(self());
            }
        }
        setPostSpec(new InformalPredicate(sb.toString()));
        IFormalPredicate escapeCondition;
        IBooleanOperation indexAllowed = getObjectModel().getIndexAllowedMethod();

        List<ActualParam> actualParams = new ArrayList<>(params);
        Operation count = getCodeClass().getOperation("count", relation);
        if (count != null) {
            BinaryExpression countIncr = new BinaryExpression(count.call(), Operator.PLUS, new ValueString("1", BaseType.INTEGER));
            actualParams.add(countIncr);
        }

        escapeCondition = new BooleanCall(indexAllowed, actualParams, true);
        IBooleanOperation maxCount = (IBooleanOperation) getCodeClass().getOperation("maxCount", relation);
        if (maxCount != null) {
            escapeCondition = escapeCondition.disjunctionWith(new BooleanCall(maxCount, false));
        }

        IPredicate escapeResult = new InformalPredicate(self() + " stays unchanged");
        setEscape(escapeCondition, escapeResult);

    }

    @Override
    public Set<ImportType> getImports() {
        return new HashSet<ImportType>();
    }

    @Override
    public List<ActorInputItem> inputItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean canTrigger(RoleEvent e) {
        if (e.isNeededWhileExtending()) {
            return true;
        } else {
            return false;
        }
    }
}
