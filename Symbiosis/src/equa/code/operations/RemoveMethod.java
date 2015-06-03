package equa.code.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import equa.factuse.ActorInputItem;
import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.BooleanRelation;
import equa.meta.classrelations.BooleanSingletonRelation;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.RoleEvent;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.SubstitutionType;
import equa.util.Naming;
import java.util.HashSet;
import java.util.Set;

/**
 * @author frankpeeters§
 */
public class RemoveMethod extends Method implements IActionOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;
    private static final String NAME = "remove";

    public RemoveMethod(Relation relation, ObjectType ot) {
        super(ot, NAME, null, ot.getCodeClass());
        this.relation = relation;
        List<Param> params = new ArrayList<>();
        SubstitutionType st = relation.targetType();

        name = NAME + Naming.withCapital(relation.name());
        if (relation.isMapRelation()) {

            if (relation.hasMultipleQualifiedTarget()) {
                params.add(new Param(Naming.withoutCapital(relation.name()), st, relation));
                throw new UnsupportedOperationException("remove on map with multiple return value ");
            }
        } else {
            if (relation.hasMultipleTarget()) {
                params.add(new Param(Naming.withoutCapital(relation.name()), st, relation));
            } else {
                //  name = NAME + Naming.withCapital(relation.name());
            }
        }

        params.addAll(qualifierParams(relation));
        setParams(params);

        if (!relation.isRemovable()) {
            setAccessModifier(AccessModifier.NAMESPACE);
        }

        returnType = new ReturnType(BaseType.STRING);
    }

    @Override
    public Set<ImportType> getImports() {
        Set<ImportType> imports = new HashSet();
        imports.add(ImportType.Utility);
        return imports;
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        //operation header
        list.addLinesAtCurrentIndentation(l.operationHeader(this));

        Relation inv = relation.inverse();

        IndentedList trueStatement = new IndentedList();
        boolean withIf = false;

        // removing at inverse side
        if (relation.isCreational()) {
            ObjectType ot = (ObjectType) relation.targetType();
            if (ot.containsObjectFields()) {
                if (getParams().size() > 0) {
                    trueStatement.addLineAtCurrentIndentation(getParams().get(0).getName() + l.memberOperator() + "dispose();");
                } else {
                    withIf = true;
                    trueStatement.addLineAtCurrentIndentation(l.thisKeyword() + l.memberOperator() + relation.fieldName() + l.memberOperator() + "dispose();");
                }
            }
        } else if (inv != null && inv.isNavigable() && getAccess().equals(AccessModifier.PUBLIC)) {
            if (inv instanceof BooleanSingletonRelation) {
                withIf = true;
                String caller = relation.fieldName();
                String method = "set" + Naming.withCapital(inv.name()) + "(false)";
                trueStatement.addLineAtCurrentIndentation(caller + l.memberOperator() + method + l.endStatement());
            } else {
                String caller;
                if (getParams().isEmpty()) {
                    withIf = true;
                    caller = relation.fieldName();
                } else {
                    caller = getParams().get(0).getName();
                }
                List<ActualParam> actualParams = new ArrayList<>();
                if (inv.hasMultipleTarget()) {
                    actualParams.add(new This());
                }
                if (inv instanceof BooleanSingletonRelation) {
                    Operation booleanProp = inv.getOwner().getCodeClass().getProperty(inv);
                    trueStatement.addLineAtCurrentIndentation(caller + l.memberOperator() + booleanProp.callString() + l.endStatement());
                } else {
                    Operation remove = inv.getOwner().getCodeClass().getOperation("remove", inv);
                    trueStatement.addLineAtCurrentIndentation(caller + l.memberOperator() + remove.callString(actualParams) + l.endStatement());
                }
            }
        }

        // removing at this side
        if (!getParams().isEmpty()) {
            trueStatement.addLineAtCurrentIndentation(l.removeStatement(relation, getParams().get(0).getName()));
        } else {
            // else we either set the boolean of the defined property to false.
            if (relation.targetType() instanceof BaseType && ((BaseType) relation.targetType()).getUndefinedString() == null) {
                trueStatement.addLineAtCurrentIndentation(l.assignment(relation.fieldName() + "Defined", "false"));
                // or we set the field to null
            } else {
                trueStatement.addLineAtCurrentIndentation(l.assignment(l.thisKeyword() + l.memberOperator() + relation.fieldName(), "null"));
            }
        }

        if (withIf) {
            IndentedList ifStatement = l.ifStatement(l.thisKeyword() + l.memberOperator() + relation.fieldName() + " != null", trueStatement);
            list.addLinesAtCurrentIndentation(ifStatement);
        } else {
            list.addLinesAtCurrentIndentation(trueStatement);
        }

        list.addLinesAtCurrentIndentation(l.postProcessing(this));
        //We return null to indicate that the object is removed.
        if (returnType.getType() != null) {
            list.addLineAtCurrentIndentation(l.returnStatement("null"));
        }

        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
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
        StringBuilder postSpec = new StringBuilder();
        if (getParams().isEmpty()) {
            postSpec.append(relation.name()).append(" is removed at ").append(self());
        } else {
            postSpec.append(getParams().get(0).getName()).append(" is removed at ").append(self());
        }
        IFormalPredicate escapeCondition = null;

        IBooleanOperation minCount = (IBooleanOperation) getCodeClass().getOperation("minCount", relation);
        if (minCount != null) {
            escapeCondition = new BooleanCall(minCount, false);
        }

        if (relation.targetType() instanceof ObjectType) {
            ObjectType target = (ObjectType) relation.targetType();
            Operation isRemovable = target.getCodeClass().getOperation("isRemovable");

            if (isRemovable != null && relation.isCreational()/*&& inverse.isMultiple()*/) {
                List<ActualParam> actualParams1 = new ArrayList<>();
                Call isRemovableCall;
                ActualParam toRemove;
                if (getParams().isEmpty()) {
                    Operation property = getCodeClass().getOperation(relation.name(), relation);
                    toRemove = property.call();
                } else {
                    toRemove = getParams().get(0);
                }
                isRemovableCall = new Call(isRemovable, actualParams1).setCalled(toRemove);
                List<ActualParam> actualParams2 = new ArrayList<>();
                actualParams2.add(isRemovableCall);
                actualParams2.add(Null.NULL);
                IBooleanOperation isEqual = getObjectModel().getIsEqualMethod();
                BooleanCall isEqualCall = new BooleanCall(isEqual, actualParams2, true);
                if (escapeCondition != null) {
                    escapeCondition.disjunctionWith(isEqualCall);
                } else {
                    escapeCondition = isEqualCall;
                }
                getReturnType().setSpec("null if remove went well, otherwise the name of a property "
                    + " which refers currently to this child-object.");
                if (getParams().isEmpty()) {

                    Operation property = getCodeClass().getOperation(relation.name(), relation);
                    toRemove = property.call();
                    postSpec.append(" AND ").append(toRemove.callString()).append(" is removed from ").append(self());
                } else {
                    postSpec.append(" AND ").append(getParams().get(0).getName()).append(" is removed from ").append(self());
                }

            } else {
                returnType = new ReturnType(null);
            }
        } else {
            returnType = new ReturnType(null);
        }
        if (escapeCondition != null) {
            IPredicate escapeResult = new InformalPredicate(self() + " stays unchanged");
            setEscape(escapeCondition, escapeResult);
        }

        setPostSpec(
            new InformalPredicate(postSpec.toString()));
    }

    @Override
    public List<ActorInputItem> inputItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean canTrigger(RoleEvent e) {
        if (e.isNeededWhileRemoving()) {
            return true;
        } else {
            return false;
        }
    }
}
