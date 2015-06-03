/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.util.ArrayList;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.BooleanRelation;
import equa.meta.classrelations.BooleanSingletonRelation;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.RoleEvent;
import equa.meta.objectmodel.ObjectType;
import equa.util.Naming;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class Strip extends Method {

    private static final long serialVersionUID = 1L;

    public Strip(ObjectType ot) {
        super(ot, "dispose", new ArrayList<Param>(), ot.getCodeClass());
        setAccessModifier(AccessModifier.NAMESPACE);
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.addLinesAtCurrentIndentation(l.operationHeader(this));

        ObjectType ot = (ObjectType) this.getParent();
        for (Relation relation : ot.relations(true, false)) {
            Relation inverse = relation.inverse();
            String caller;
            ObjectType responsible = ot.getResponsible();
            if (inverse != null && (inverse.isNavigable() || relation.getOwner().equals(inverse.getOwner()))
                && responsible != relation.targetType() /*&& !inverse.isMandatory()*/) {
                String removableObject = "";
                if (inverse.isSeqRelation() || inverse.isSetRelation()) {
                    removableObject = l.thisKeyword();
                }

                String method = null;
                if (inverse.isMandatory()) {
                    if (inverse.getOwner().containsObjectFields()) {
                        method = "dispose()";
                    }
                } else {

                    ObjectType targetResponsible = inverse.getOwner().getResponsible();
                    if (targetResponsible == relation.getOwner()) {
                        if (inverse.getOwner().containsObjectFields()) {
                            method = "dispose()";
                        }
                    } else if (inverse.isNavigable()) {
                        method = "remove" + Naming.withCapital(inverse.name()) + "(" + removableObject + ")";
//                        else {
//                                method = "remove" + Naming.withCapital(relation.name()) + "(" + removableObject + ")";
//                            }
                    }

                }

                if (method != null) {

                    if (relation.isSetRelation() || relation.isSeqRelation()) {
                        IndentedList body = new IndentedList();
                        caller = relation.name();

                        body.addLineAtCurrentIndentation(caller + l.memberOperator() + method + l.endStatement());
                        list.addLinesAtCurrentIndentation(
                            l.forEachLoop(relation.targetType(), caller,
                                l.thisKeyword() + l.memberOperator() + relation.fieldName(),
                                body));
                        // list.addLineAtCurrentIndentation(l.clear(relation));

                    } else if (relation.isMapRelation()) {
                        throw new UnsupportedOperationException("todo: removing of map content");
                    } else {
                        // removing old value at the inverse side
                        IndentedList trueStatement = new IndentedList();
                        caller = l.thisKeyword() + l.memberOperator() + relation.fieldName();
                        String finalValue;
                        if (relation instanceof BooleanSingletonRelation) {
                            caller = inverse.getOwner().getName() + l.memberOperator() + "getSingleton()";
                            finalValue = "false";
                        } else {
                            finalValue = "null";
                        }
                        trueStatement.addLineAtCurrentIndentation(caller + l.memberOperator() + method + l.endStatement());
                        //  trueStatement.addLineAtCurrentIndentation(l.assignment(relation.fieldName(), finalValue));
                        if (relation.isMandatory()) {
                            list.addLinesAtCurrentIndentation(trueStatement);
                        } else {
                            IndentedList ifStatement = l.ifStatement(l.negate(l.equalsStatement(relation.fieldName(), finalValue)), trueStatement);
                            list.addLinesAtCurrentIndentation(ifStatement);
                        }

                    }
                }
            }

        }

        supercall(ot, list, l);

        //list.addLinesAtCurrentIndendation(l.postProcessing(this));
        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
    }

    private void supercall(ObjectType ot, IndentedList list, Language l) {
        boolean supercall = false;
        while (ot.supertypes().hasNext()) {
            ot = ot.supertypes().next();
            if (ot.containsObjectFields()) {
                supercall = true;
            }
        }
        if (supercall) {
            list.addLineAtCurrentIndentation(l.superCall(this, this.getParams()) + l.endStatement());
        }
    }

    private String removeExternalStatement(Language l, String caller, Relation inverse) {
        String removable = "todo";
        String result = caller + l.memberOperator();
//        ObjectType ot = inverse.getOwner();
//        Operation remove = ot.getCodeClass().getOperation("remove", inverse);
        String methodName = "remove" + Naming.withCapital(inverse.name());
        if (inverse.isSetRelation() || inverse.isSeqRelation()) {
            result += methodName + "(" + l.thisKeyword() + ");";
        } else if (inverse.isMapRelation()) {
            throw new UnsupportedOperationException(inverse.name() + " is a map-relation; removing is difficult because of difficult  retrieval of its key");
        } else {
            removable = "";
        }
        return result += methodName + "(" + removable + ");";
    }

    @Override
    public Set<ImportType> getImports() {
        Set<ImportType> imports = new HashSet();
        imports.add(ImportType.ObjectEquals);
        return imports;
    }

    @Override
    public void initSpec() {

//        Relation inverse = relation.inverse();
//        StringBuilder postSpec = new StringBuilder();
//        postSpec.append("self->collect(").append(relation.name()).append(")->isEmpty()");
//
//        IFormalPredicate escapeCondition = null;
//
//        IBooleanOperation minCount = (IBooleanOperation) getCodeClass().getOperation("minCount", relation);
//        if (minCount != null) {
//            escapeCondition = new BooleanCall(minCount, false);
//        }
//
//        Operation isRemovable = null;
//        if (relation.targetType() instanceof ObjectType) {
//            ObjectType target = (ObjectType) relation.targetType();
//            isRemovable = target.getCodeClass().getOperation("isRemovableFrom");
//        }
//
//        if (isRemovable != null && inverse.hasMultipleTarget()) {
////            List<ActualParam> actualParams1 = new ArrayList<>();
////            actualParams1.add(new This());
////            Call isRemovableCall = new Call(isRemovable, actualParams1);
////            List<ActualParam> actualParams2 = new ArrayList<>();
////            actualParams2.add(isRemovableCall);
////            actualParams2.add(new Null());
////            IBooleanOperation isEqual = getObjectModel().getIsEqualMethod();
////            BooleanCall isEqualCall = new BooleanCall(isEqual, actualParams2, true);
////            if (escapeCondition != null) {
////                escapeCondition.disjunctionWith(isEqualCall);
////            } else {
////                escapeCondition = isEqualCall;
////            }
////            getReturnType().setSpec("null if remove went well, otherwise the name of a property "
////                    + " which refers (indirectly) at this moment to this child-object.");
////
////            postSpec.append(" AND ").append(self()).append(" is removed from ").append(getParams().get(0).getName());
//
//        } else {
//            returnType = new ReturnType(null);
//        }
//        if (escapeCondition != null) {
//            IPredicate escapeResult = new InformalPredicate(self() + " stays unchanged");
//            setEscape(escapeCondition, escapeResult);
//        }
//        setPostSpec(new InformalPredicate(postSpec.toString()));
    }

    @Override
    public boolean canTrigger(RoleEvent e) {
        if (e.isNeededWhileRemoving()) {
            return false;
        } else {
            return false;
        }
    }

    @Override
    public boolean isFinal() {
        ObjectType parent = (ObjectType) getParent();
        return !parent.subtypes().hasNext();
    }
}
