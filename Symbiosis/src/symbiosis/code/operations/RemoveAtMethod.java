/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.operations;

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
import symbiosis.meta.objectmodel.RoleEvent;
import symbiosis.meta.objectmodel.ObjectType;
import symbiosis.util.Naming;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class RemoveAtMethod extends Method implements IActionOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;

    public RemoveAtMethod(Relation relation, ObjectType ot) {
        super(ot, "remove" + Naming.withCapital(relation.name()), null, ot.getCodeClass());
        this.relation = relation;
        List<Param> params = new ArrayList<>();
        params.add(new Param("nr", BaseType.NATURAL, relation));
        setParams(params);
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        // standard operation header
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        // we call the removeAt, this is only defined for sequenceRelation.
        list.addLineAtCurrentIndentation(l.removeAtStatement(relation.fieldName(), getParams().get(0).getName()));
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initSpec() {
//    
//        Predicate postSpec = new FormalSpec(Operation.collectionCondition(relation, true, false));
//        setPostSpec(postSpec);
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

    @Override
    public String getIntendedValue() {
        return "";
    }
}
