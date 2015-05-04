/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.operations;

import symbiosis.code.CodeClass;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import symbiosis.factuse.ActorInputItem;
import symbiosis.code.Field;
import symbiosis.code.ImportType;
import symbiosis.code.IndentedList;
import symbiosis.code.Language;
import symbiosis.meta.classrelations.Relation;
import symbiosis.meta.objectmodel.BaseType;
import symbiosis.meta.objectmodel.ConstrainedBaseType;
import symbiosis.meta.objectmodel.RoleEvent;
import symbiosis.meta.objectmodel.ObjectType;
import symbiosis.util.Naming;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class AdjustMethod extends Method implements IActionOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;

    public AdjustMethod(Relation relation, ObjectType ot) {
        super(ot, "adjust" + Naming.withCapital(relation.roleName()), null, ot.getCodeClass());
        this.relation = relation;
        List<Param> params = new ArrayList<>();
        if (relation.targetType() instanceof ConstrainedBaseType) {
            ConstrainedBaseType cbt = (ConstrainedBaseType) relation.targetType();
            params.add(new Param("amount", cbt.getBaseType(), relation));
        } else {
            params.add(new Param("amount", relation.targetType(), relation));
        }
        addQualifiers(params, relation);
        setParams(params);
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        Relation r = getParams().get(0).getRelation();
        if (r.isMapRelation()) {
            // TODO Assumption only 1 qualifier in map relation
            list.addLineAtCurrentIndentation(l.adjustMap(relation, getParams().get(1).getName(), getParams().get(0).getName()));
        } else if (r.targetType() instanceof ConstrainedBaseType) {
            list.addLineAtCurrentIndentation(l.assignment(
                r.fieldName(),
                l.newInstance(r.targetType(), r.fieldName() + l.memberOperator() + l.getProperty("value") + " + "
                    + getParams().get(0).getName())));
        } else {
            list.addLineAtCurrentIndentation(l.assignment(relation.fieldName(), relation.fieldName() + " + " + getParams().get(0).getName()));
        }
        Relation inv = relation.inverse();
        if (inv != null && inv.isNavigable()) {

        }
        list.addLinesAtCurrentIndentation(l.postProcessing(this));
        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
    }

    @Override
    public Set<ImportType> getImports() {
        Set<ImportType> imports = new HashSet();
        imports.add(ImportType.Utility);
        return imports;
    }

    @Override
    public Relation getRelation() {
        return relation;
    }

    @Override
    public Field getField() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void initSpec() {
        Operation property = getCodeClass().getOperation(relation.name());
        BinaryExpression newValue = new BinaryExpression(property.call(qualifierParams(relation)), Operator.PLUS, getParams().get(0));

        IFormalPredicate preCondition = null;
        if (!relation.isMandatory()) {
            IBooleanOperation isDefined = (IBooleanOperation) getCodeClass().getOperation("isDefined", relation);
            if (isDefined == null) {
                ActualParam undefined = relation.targetType().getUndefined();
                IBooleanOperation isEqual = getObjectModel().getIsEqualMethod();
                List<ActualParam> actualParams = new ArrayList<>();
                Call propertyCall = property.call(qualifierParams(relation));
                actualParams.add(propertyCall);
                actualParams.add(undefined);
                preCondition = new BooleanCall(isEqual, actualParams, true);
            } else {
                preCondition = new BooleanCall(isDefined, qualifierParams(relation), false);
            }
        }

        BooleanCall indicesAllowed = qualifiersCondition(relation);
        if (indicesAllowed != null) {
            if (preCondition != null) {
                preCondition = preCondition.conjunctionWith(indicesAllowed);
            } else {
                preCondition = indicesAllowed;
            }
        }
        if (preCondition != null) {
            setPreSpec(preCondition);
        }

        List<ActualParam> actualParams = new ArrayList<>();
        actualParams.add(newValue);
        if (relation.targetType() instanceof ObjectType) {
            ConstrainedBaseType cbt = (ConstrainedBaseType) relation.targetType();
            IBooleanOperation correctValue = (IBooleanOperation) cbt.getCodeClass().getOperation("isCorrectValue");
            BooleanCall bc = new BooleanCall(correctValue, actualParams, true);
            bc.setCalled((ObjectType) cbt.getCodeClass().getParent());
            IFormalPredicate predicate = bc;
            setEscape(predicate, new InformalPredicate(self() + " stays unchanged"));
        } else {
            BaseType bt = (BaseType) relation.targetType();
            if (bt.equals(BaseType.NATURAL)) {
                IBooleanOperation isNatural = getObjectModel().getIsNaturalMethod();
                IFormalPredicate predicate = new BooleanCall(isNatural, actualParams, true);
                setEscape(predicate, new InformalPredicate(self() + " stays unchanged"));
            }
        }

        setPostSpec(new InformalPredicate(relation.roleName() + " = self@Pre." + newValue.callString()));
    }

    @Override
    public List<ActorInputItem> inputItems() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canTrigger(RoleEvent e) {
        return e.isNeededWhileUpdating();
    }

    @Override
    public String getIntendedValue() {
        CodeClass cc = ((ObjectType) getParent()).getCodeClass();
        Property p = cc.getProperty(relation);
        if (p == null) {
            return "";
        } else {
            return p.callString();
        }
    }
}
