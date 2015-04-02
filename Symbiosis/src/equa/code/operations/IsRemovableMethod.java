/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.CodeNames.FOREACH;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import equa.code.Field;
import equa.code.ImportType;
import static equa.code.ImportType.ObjectEquals;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.BooleanRelation;
import equa.meta.classrelations.ObjectTypeRelation;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectType;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class IsRemovableMethod extends Method implements IRelationalOperation {

    public static final long serialVersionUID = 1L;

    private final Relation composition;
    public final static String NAME = "isRemovable";

    public IsRemovableMethod(Relation composition, ObjectType ot) {
        super(ot, NAME, null, ot.getCodeClass());
        this.composition = composition;
        List<Param> params = new ArrayList<Param>();
        // ObjectType parent = relation.getOwner();
        // params.add(new Param("parent",parent,relation));
        setParams(params);
        setReturnType(new ReturnType(BaseType.STRING));
        setAccessModifier(AccessModifier.NAMESPACE);
    }

    @Override
    public void initSpec() {
        returnType.setSpec("null if this object and all his compositional childs are removable,\n\t"
            + "otherwise the name of a property of a Fan-class,\n\t" + "except " + composition.getOwner().getName()
            + ", which equals or includes this " + self() + "\n\tor one his compositional childs now\n\t"
            + "NB: Fan of X ::= a class with a navigable association from Fan to X (Fan != X)");
    }

    @Override
    public Set<ImportType> getImports() {
        Set<ImportType> list = new HashSet<>();
        list.add(ObjectEquals);
        return list;
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        for (Relation fanRelation : ((ObjectType) getParent()).fans()) {
            if (!(fanRelation instanceof ObjectTypeRelation)) {
                IndentedList ifTrue = new IndentedList();
                ifTrue.addLineAtCurrentIndentation(l.returnStatement(l.stringSymbol() + fanRelation.getOwner().getName() + "." + fanRelation.name() + l.stringSymbol()));
                // We have to ignore the parent.
                if (!composition.getOwner().equals(fanRelation.getOwner().getResponsible())) {
                    String condition = "";
                    if (fanRelation.isCollectionReturnType()) {
                        // We need to do the contain check.
                        if (!fanRelation.isComposition()) {
                            List<ActualParam> params = new ArrayList<>();
                            params.add(new This());
                            Relation inverse = fanRelation.inverse();
                            if (inverse.isCollectionReturnType()) {
                                condition = l.negate(l.isEmpty(inverse.fieldName()));
                            } else {
                                Param otherObject = new Param(inverse.fieldName(), inverse.targetType(), inverse);

                                condition = new Call(fanRelation.getOperation(ContainsMethod.NAME), params).setCalled(otherObject)
                                    .expressIn(l);
                            }
                            list.addLinesAtCurrentIndentation(l.ifStatement(condition, ifTrue));
                        }
                    } else {

                        Relation inverse = fanRelation.inverse();
                        if (!inverse.isCollectionReturnType()) {
                            // We have a relation to a single OT so we check if it is
                            // null (or true)
                            if (inverse instanceof BooleanRelation) {
                                condition = l.equalsStatement(inverse.fieldName(), "true");
//                            ObjectType singleton = fanRelation.getOwner();
//                            Operation getSingleton = singleton.getCodeClass().getOperation("getSingleton");
//                            Property property = singleton.getCodeClass().getProperty(fanRelation);
//                            condition = l.negate(l.equalsStatement(singleton.getName() + l.memberOperator() + 
//                                getSingleton.callString()+l.memberOperator() + property.callString(),
//                                "null"));
                                ifTrue = new IndentedList();
                                ifTrue.addLineAtCurrentIndentation(l.returnStatement(l.stringSymbol() + fanRelation.getOwner().getName() + "." + inverse.name() + l.stringSymbol()));
                                list.addLinesAtCurrentIndentation(l.ifStatement(condition, ifTrue));
                            } else {

                                condition = l.negate(l.equalsStatement(inverse.fieldName(),
                                    "null"));
                                list.addLinesAtCurrentIndentation(l.ifStatement(condition, ifTrue));
                            }
                        } else {
                            // we check if size == 0
                            IndentedList forEachBody = new IndentedList();
                            condition = l.equalsStatement(FOREACH + l.memberOperator() + l.getProperty(fanRelation.fieldName()),
                                l.thisKeyword());
                            forEachBody.addLinesAtCurrentIndentation(l.ifStatement(condition, ifTrue));
                            list.addLinesAtCurrentIndentation(l.forEachLoop(inverse.targetType(), FOREACH, inverse.fieldName(), forEachBody));
                        }
                    }

                }
            }
        }
        // supertype contains isRemovable ?
        ObjectType ot = (ObjectType) getCodeClass().getParent();
        if (ot.supertypes().hasNext()) {
            ObjectType supertype = ot.supertypes().next();
            Operation isRemovable = supertype.getCodeClass().getOperation("isRemovable");
            if (isRemovable != null) {
                list.addLineAtCurrentIndentation(l.returnStatement(l.superCall(isRemovable, new ArrayList<Param>())));
            } else {// We return null to indicate it can be removed.
                list.addLineAtCurrentIndentation(l.returnStatement("null"));
            }
        } else {// We return null to indicate it can be removed.
            list.addLineAtCurrentIndentation(l.returnStatement("null"));
        }

        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
    }

    @Override
    public Relation getRelation() {
        return composition;
    }

    @Override
    public Field getField() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
