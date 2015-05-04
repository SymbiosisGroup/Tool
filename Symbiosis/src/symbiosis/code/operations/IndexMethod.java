/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.operations;

import symbiosis.code.CodeClass;
import symbiosis.code.Field;
import symbiosis.code.ImportType;
import symbiosis.code.IndentedList;
import symbiosis.code.Language;
import symbiosis.meta.classrelations.Relation;
import symbiosis.meta.objectmodel.BaseType;
import symbiosis.meta.objectmodel.ObjectType;
import symbiosis.meta.objectmodel.SubstitutionType;
import symbiosis.util.Naming;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class IndexMethod extends Method implements IRelationalOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;

    public IndexMethod(Relation relation, ObjectType ot) {
        super(ot, Naming.withoutCapital(relation.name()), null, ot.getCodeClass());
        this.relation = relation;
        List<Param> params = new ArrayList<>();
        SubstitutionType indexType;
        if (relation.isSeqRelation()) {
            indexType = relation.qualifierRoles().get(0).getSubstitutionType();
        } else {
            indexType = BaseType.NATURAL;
        }
        Param param = new Param("index", indexType, relation);
        params.add(param);
        setParams(params);
        returnType = new ReturnType(relation.targetType());
    }

    @Override
    public IndentedList getCode(Language l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        Operation count = getCodeClass().getOperation("count", relation);
        IBooleanOperation indexAllowed = getObjectModel().getIndexAllowedMethod();
        List<ActualParam> actualParams = new ArrayList<>();
        actualParams.add(getParams().get(0));
        actualParams.add(count.call());
        BooleanCall call = new BooleanCall(indexAllowed, actualParams, false);
        setPreSpec(call);

        returnType.setSpec("the value of " + relation.name() + " in " + relation.getPluralName() + "at index");
    }

    @Override
    public Set<ImportType> getImports() {
         return new HashSet<ImportType>();}

    @Override
    public String getIntendedValue() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
