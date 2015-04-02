/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import static equa.code.CodeNames.TEMP1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import equa.factuse.ActorInputItem;
import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.code.systemoperations.UnknownMethod;
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
public class AddObjectTypeMethod extends Method implements IActionOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;
    private final ObjectType concreteOT;
    private boolean autoIncr = false;

    public AddObjectTypeMethod(Relation relation, ObjectType concreteOT, ObjectType parent) {
        super(parent, "add" + concreteOT.getName(), null, parent.getCodeClass());
        this.relation = relation;
        this.concreteOT = concreteOT;
        List<Param> params = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<Param> candidates = concreteOT.getCodeClass().getConstructor().getParams();
        String paramName;
        //ObjectType target = (ObjectType) relation.targetType();
        for (Param candidate : candidates) {
            if (/**
                 * !target.isSingleton() && *
                 */
                !candidate.getRelation().isAutoIncr()
                && !candidate.getRelation().equals(relation.inverse())) {
                paramName = detectUniqueName(candidate.getName(), names);
                params.add(new Param(paramName, candidate.getType(), candidate.getRelation()));
            } else if (candidate.getRelation().isAutoIncr()) {
                autoIncr = true;
            }
        }
        setParams(params);
        returnType = new ReturnType(concreteOT);
    }

    @Override
    public void initSpec() {
        Relation inverse = relation.inverse();
        StringBuilder sb = new StringBuilder();
        sb.append("@result is added to ").append(self()).append(".");
        sb.append(relation.getPluralName());
        if (inverse != null && inverse.isNavigable()) {
            if (inverse.hasMultipleTarget()) {
                sb.append(" AND @result.contains(").append(self()).append(")");
            } else {
                sb.append(" AND @result knows ").append(self());
            }
        }
        setPostSpec(new InformalPredicate(sb.toString()));

        IFormalPredicate escapeCondition = null;
        //List<Param> idParams = getSearchParams();
        List<Param> idParams = idParams();
        if (relation.isAddable()) {
            if (!idParams.isEmpty()) {
                //Method search = (Method) getCodeClass().getOperation("get" + concreteOT.getName(), relation);
                Method search = (Method) getCodeClass().getOperation("get" + Naming.withCapital(relation.name()), relation);Call searchCall = search.call(idParams);
                UnknownMethod unknown = getObjectModel().getUnknownMethod();
                List<ActualParam> actualParams = new ArrayList<>();
                actualParams.add(searchCall);
                escapeCondition = new BooleanCall(unknown, actualParams, true);
            }

            IBooleanOperation maxCount = (IBooleanOperation) getCodeClass().getOperation("maxCount" + concreteOT.getName(), relation);
            if (maxCount != null) {
                if (escapeCondition != null) {
                    escapeCondition = escapeCondition.disjunctionWith(new BooleanCall(maxCount, false));
                }
            }
            if (escapeCondition != null) {
                IPredicate escapeResult = new InformalPredicate(self() + " stays unchanged");
                setEscape(escapeCondition, escapeResult);
            }

            /* callString() should be called with actualP{arsm; but there is a problem in 
             * in case of navigable composition relation 
             */
            String constructorCall = concreteOT.getCodeClass().getConstructor().callString();
            returnType.setSpec(constructorCall);
        }
    }

    private List<Param> idParams() {
        List<Param> idParams = new ArrayList<>();
        List<Relation> idRelations = concreteOT.identifyingRelations();
        for (Param param : getParams()) {
            if (idRelations.contains(param.getRelation())) {
                idParams.add(param);
            }
        }
        return idParams;
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        // create a new object from the params.
        List<String> constructorParams = new ArrayList<>();
        Iterator<Param> it = concreteOT.getCodeClass().constructorParams();
        while (it.hasNext()) {
            Param p = it.next();

            Relation inverse = p.getRelation().inverse();
            if (inverse != null && inverse.equals(getRelation())) {
                constructorParams.add(l.thisKeyword());
            } else if (p.isAutoIncr()) {
                String fieldName = p.getRelation().getAutoIncrFieldName();
                constructorParams.add(l.autoIncr(fieldName));
            } else {
                constructorParams.add(p.getName());
            }

//            String name = searchParamName(p);
//            if (name == null) {
//                if (p.getType().equals(getParent())) {
//                    name = l.thisKeyword();
//                } else {
//                    if (p.getRelation().isAutoIncr()) {
//                        name = l.thisKeyword() + l.memberOperator() + l.autoIncr(p.getRelation().fieldName());
//                    } else {
//                        throw new RuntimeException("I don't know what to do.");
//                    }
//                }
//            }
//            constructorParams.add(name);
        }
        list.addLineAtCurrentIndentation(l.createInstance(getReturnType().getType(), TEMP1, concreteOT.getName(), constructorParams.toArray(new String[0])));
        Relation inverse = relation.inverse();
        if (relation.isSeqRelation() || autoIncr) {
            list.addLineAtCurrentIndentation(l.addCollection(relation.fieldName(), relation.collectionType().getKind(), TEMP1));
            //if a value is added to the collection and the inverse relation is navigable, we have to register.
            
            if (inverse.isNavigable() && inverse.isCollectionReturnType()) {
                
                list.addLineAtCurrentIndentation(l.callMethod(TEMP1, inverse.getOperationName(RegisterMethod.NAME), l.thisKeyword()));
            }

        } else {
            //if a value is added to the collection and the inverse relation is navigable, we have to register.
            if (inverse.isNavigable() && inverse.isCollectionReturnType()) {
                list.addLineAtCurrentIndentation(l.callMethod(TEMP1, inverse.getOperationName(RegisterMethod.NAME), l.thisKeyword()));
            }
            //we add the item
            list.addLineAtCurrentIndentation(l.addCollection(relation.fieldName(), relation.collectionType().getKind(), TEMP1));

        }
        list.addLinesAtCurrentIndentation(l.postProcessing(this));
        list.addLineAtCurrentIndentation(l.returnStatement(TEMP1));
        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
    }

    private String searchParamName(Param p) {
        for (Param param : getParams()) {
            if (param.equals(p)) {
                return param.getName();
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
    public Set<ImportType> getImports() {
        Set<ImportType> imports = new HashSet();
        imports.add(ImportType.Utility);
        return imports;
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

    @Override
    public String getIntendedValue() {
        return TEMP1;
    }

}
