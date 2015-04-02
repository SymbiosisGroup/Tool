/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.CodeNames.FOREACH;
import static equa.code.CodeNames.TEMP1;
import static equa.code.ImportType.ObjectEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import equa.factuse.ActorInputItem;
import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.IdRelation;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.SubstitutionType;
import equa.util.Naming;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class SearchMethod extends Method implements IActionOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;


    public SearchMethod(Relation relation, ObjectType concreteObjectType, ObjectType parent) {
        super(parent, "get" + Naming.withCapital(relation.name()), null, parent.getCodeClass());
        this.relation = relation;
        List<Param> params = new ArrayList<>();
        for (Relation idrelation : concreteObjectType.getFactType().identifyingRelations()) {

            SubstitutionType st = idrelation.targetType();
            if (st instanceof ObjectType) {
                ObjectType idtarget = (ObjectType) st;
                if ((idtarget.equals(parent) && idrelation.inverse().isResponsible())
                    || idtarget.isSingleton()) {

                } else if (concreteObjectType.creates(idtarget)) {
                    Param parentParam = new Param(idrelation.name(), idrelation.targetType(), idrelation);
                    for (Relation targetIdrelation : idtarget.getFactType().identifyingRelations()) {
                        SubstitutionType stIndirect = targetIdrelation.targetType();
                        if (stIndirect instanceof ObjectType) {
                            ObjectType idtargetIndirect = (ObjectType) stIndirect;
                            if ((idtargetIndirect.equals(parent) && targetIdrelation.inverse().isResponsible())
                                || idtargetIndirect.isSingleton()) {
                            } else {
                                params.add(new SubParam(targetIdrelation, parentParam));
                            }
                        } else {
                            params.add(new SubParam(targetIdrelation, parentParam));
                        }
                    }
                } else {
                    params.add(new Param(idrelation.name(), idrelation.targetType(), idrelation));
                }

            } else {
                params.add(new Param(idrelation.name(), idrelation.targetType(), idrelation));
            }
        }

        setParams(params);
        returnType = new ReturnType(concreteObjectType);

    }

    @Override
    public void initSpec() {
        StringBuilder returnSb = new StringBuilder();
        returnSb.append("let selection : Set(").append(getParent().getName()).append(") = ");
        returnSb.append(relation.name());
        returnSb.append("->select(");
        returnSb.append(paramCallList());
        returnSb.append(") AND \n\tif selection->isEmpty() \n\t"
            + "then @result = null else @result = selection->asSequence()->first()\n\t"
            + "endif\"");

        returnType.setSpec(returnSb.toString());

    }

    @Override
    public IndentedList getCode(Language l) {
        boolean supertype = false;
        IndentedList list = new IndentedList();
        // operation header
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        // we create a body for the if statement which will be in the foreach
        // loop.
        // this is just to return the found object
        IndentedList bodyIf = new IndentedList();
        IndentedList bodyIf2 = new IndentedList();
        IndentedList bodyForEach = new IndentedList();
        String sReturn = FOREACH;
        // if we have a supertype in the collection, we need to cast.
        if (getReturnType().getType() instanceof ObjectType) {
            ObjectType ot = (ObjectType) getReturnType().getType();
            if (ot.supertypes().hasNext()) {
                supertype = true;
                sReturn = TEMP1;
            }
        }
        // now we have to make the condition for the if statement
        // we add all params to it and check if they are equal
        Iterator<Param> params = getParams().iterator();
        String statement = "";
        while (params.hasNext()) {
            Param p = params.next();
            String leftOperand = sReturn + l.memberOperator() + p.propertyCalls(l);
            statement += l.equalsStatement(leftOperand, p.getName());
            if (params.hasNext()) {
                statement += l.and();
            }
        }

        bodyIf.addLineAtCurrentIndentation(l.returnStatement(sReturn));
        //statement if2 checks if instance is the same as the one we want to return
        bodyIf2.addLineAtCurrentIndentation(l.cast(getReturnType().getType(), TEMP1, FOREACH));
        //then we can check the values and return the variable
        bodyIf2.addLinesAtCurrentIndentation(l.ifStatement(statement, bodyIf));
        //if we have a supertype we need to check instance etc.
        // else we can just check the values and return
        if (supertype) {
            bodyForEach.addLinesAtCurrentIndentation(l.ifStatement(l.checkType(FOREACH, getReturnType().getType()), bodyIf2));
        } else {
            bodyForEach.addLinesAtCurrentIndentation(l.ifStatement(statement, bodyIf));
        }
        //here we loop over the elements in the collection, it may be a supertype of the one we want to return
        list.addLinesAtCurrentIndentation(l.forEachLoop(getRelation().targetType(), FOREACH, getRelation().fieldName(), bodyForEach));
        list.addLineAtCurrentIndentation(l.returnStatement("null"));
        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;

        /*
         * foreach loop {
         * 		if2 (only when supertype exists) {
         * 			if {
         * 			
         * 			}
         * 		}
         * }
         */
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
    public Set<ImportType> getImports() {
        Set<ImportType> list = new HashSet<>();
        list.add(ObjectEquals);
        return list;
    }

    @Override
    public List<ActorInputItem> inputItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
