/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import equa.factuse.ActorInputItem;
import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
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
public class AddBaseTypeMethod extends Method implements IActionOperation {

    private static final long serialVersionUID = 1L;
    private Relation relation;

    public AddBaseTypeMethod(Relation relation, ObjectType ot) {
        super(ot, "add" + Naming.withCapital(relation.name()), null, ot.getCodeClass());
        this.relation = relation;
        List<Param> params = new ArrayList<>();
        params.add(new Param(relation.name(), relation.targetType(), relation));
        setParams(params);
    }

    @Override
    public IndentedList getCode(Language l) {
    	IndentedList list = new IndentedList();
    	list.addLinesAtCurrentIndentation(l.operationHeader(this));
    	list.addLineAtCurrentIndentation(l.addCollection(relation.fieldName(), relation.collectionType().getKind(), getParams().get(0).getName()) + l.endStatement());
    	list.addLinesAtCurrentIndentation(l.postProcessing(this));
         list.addLinesAtCurrentIndentation(l.bodyClosure());
    	return list;
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
        String param = getParams().get(0).getName();

        StringBuilder sb = new StringBuilder();
        sb.append(param);
        sb.append(" is added to self.");
        sb.append(relation.roleName());
        sb.append("s");
        setPostSpec(new InformalPredicate(sb.toString()));

        BooleanCall containsCall = new BooleanCall((IBooleanOperation) getCodeClass().getOperation("contains"), true);
        IPredicate escapeResult = new InformalPredicate(self() + " stays unchanged");
        IBooleanOperation maxCount = (IBooleanOperation) getCodeClass().getOperation("maxCount", relation);
        if (maxCount != null) {
            setEscape(new DisjunctionCall(containsCall, new BooleanCall(maxCount, true)), escapeResult);
        } else {
            setEscape(containsCall, escapeResult);
        }
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
      if (e.isNeededWhileExtending()) return true;
      else return false;
    }


}
