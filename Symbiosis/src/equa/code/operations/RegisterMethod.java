/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.CodeNames.TEMP1;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.BooleanRelation;
import equa.meta.classrelations.BooleanSingletonRelation;
import equa.meta.classrelations.FactTypeRelation;
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
public class RegisterMethod extends Method implements IRelationalOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;

    public static final String NAME = "register";

    public RegisterMethod(Relation relation, ObjectType parent) {
        super(parent, NAME + Naming.withCapital(relation.name()), null, parent.getCodeClass());
        this.relation = relation;
        List<Param> params = new ArrayList<>();
        ObjectType concreteObjectType = (ObjectType) relation.targetType();
        params.add(new Param(relation.name(), concreteObjectType, relation));
        addQualifiers(params, relation);
        setParams(params);
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        list.addLineAtCurrentIndentation(l.addCollection(relation.fieldName(), relation.collectionType().getKind(), getParams().get(0).getName()));
//        Relation inverse = relation.inverse();
//        if (inverse != null && inverse.isNavigable() && this.getAccess().equals(AccessModifier.PUBLIC)) {
//            ObjectType otInv = (ObjectType) relation.targetType();
//            if (inverse.isCollectionReturnType()) {
//
//                list.addLineAtCurrentIndentation(l.callMethod(getParams().get(0).getName(), inverse.getOperationName(RegisterMethod.NAME), l.thisKeyword()) + l.endStatement());
//            } else if (inverse instanceof BooleanSingletonRelation) {
//                // wrong : JAVA code, it's to specific
//                list.addLineAtCurrentIndentation(getParams().get(0).getName() + l.memberOperator()
//                    + "set" + Naming.withCapital(inverse.fieldName()) + "(true)" + l.endStatement());
//            } else {
//                // wrong : JAVA code, it's to specific
//                if (!relation.getOwner().isSingleton()) {
//                    list.addLineAtCurrentIndentation(getParams().get(0).getName() + l.memberOperator()
//                        + "set" + Naming.withCapital(inverse.fieldName()) + "(" + l.thisKeyword() + ")" + l.endStatement());
//                }
//            }
//
//        }
         if (getAccess().equals(AccessModifier.PUBLIC) && relation instanceof FactTypeRelation) {
            Relation inverse = relation.inverse();
            //if a value is added to the collection and the inverse relation is navigable, we have to register.
            if (inverse.isNavigable() || relation.targetType().equals(relation.getOwner())) {
                if (inverse.isSeqRelation() || inverse.isSetRelation()) {
                    list.addLineAtCurrentIndentation(l.callMethod(getParams().get(0).getName(), inverse.getOperationName(RegisterMethod.NAME), l.thisKeyword())
                        + l.endStatement());
                } else if (inverse.isMapRelation()) {
                    // TODO
                } else if (inverse instanceof BooleanSingletonRelation) {
                    // wrong : JAVA code, it's to specific
                    list.addLineAtCurrentIndentation(l.thisKeyword() + l.memberOperator() + relation.fieldName() + l.memberOperator()
                        + "set" + Naming.withCapital(inverse.fieldName()) + "(true)" + l.endStatement());
                } else {
                    if (!relation.getOwner().isSingleton()) {
                        list.addLineAtCurrentIndentation(getParams().get(0).getName() + l.memberOperator() + "set" + Naming.withCapital(inverse.name() + "(" + l.thisKeyword() + ")")
                            + l.endStatement());
                    }
                }
            }
        }
        
        
        
        
//        if (getParent().equals(relation.targetType())) {
//            if (inverse.isCollectionReturnType()) {
//                list.addLineAtCurrentIndentation(l.callMethod(getParams().get(0).getName(), getName(), l.thisKeyword()) + l.endStatement());
//            }
//        }

        list.addLinesAtCurrentIndentation(l.postProcessing(this));
        list.addLinesAtCurrentIndentation(l.bodyClosure());

        /*
         * void registerRole(ST role) {
         * 		collection.add(role);
         * } 
         */
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
        IFormalPredicate escapeCondition = null;

        IBooleanOperation contains = (IBooleanOperation) getCodeClass().getOperation("contains", relation);
        if (contains != null) {
            List<ActualParam> actualParams = new ArrayList<>();
            actualParams.add(getParams().get(0));
            escapeCondition = new BooleanCall(contains, actualParams, false);
        }

        IBooleanOperation maxCount = (IBooleanOperation) getCodeClass().getOperation("maxCount", relation);
        if (maxCount != null) {
            BooleanCall maxCall = new BooleanCall(maxCount, false);

            if (escapeCondition == null) {
                escapeCondition = maxCall;
            } else {
                escapeCondition.disjunctionWith(maxCall);
            }
        }

        if (escapeCondition != null) {
            IPredicate escapeResult = new InformalPredicate(self() + " stays unchanged");
            setEscape(escapeCondition, escapeResult);
        }

        IPredicate postSpec = new InformalPredicate("collect(" + relation.name() + ") = collect("
            + relation.name() + "@Pre)->including(" + relation.name() + ")");
        setPostSpec(postSpec);
    }

    @Override
    public boolean canTrigger(RoleEvent e) {
        if (getAccess().equals(AccessModifier.PUBLIC) && e.isNeededWhileExtending()) {
            return true;
        } else {
            return false;
        }
    }
}
