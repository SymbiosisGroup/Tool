/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.operations;

import static symbiosis.code.ImportType.Collections;
import static symbiosis.code.ImportType.Linq;

import java.util.ArrayList;
import java.util.List;

import symbiosis.code.Field;
import symbiosis.code.ImportType;
import symbiosis.code.IndentedList;
import symbiosis.code.Language;
import symbiosis.meta.classrelations.Relation;
import symbiosis.meta.objectmodel.BaseType;
import symbiosis.meta.objectmodel.ObjectType;
import symbiosis.meta.objectmodel.SubstitutionType;
import symbiosis.util.Naming;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class SubsequenceMethod extends Method implements IRelationalOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;

    public SubsequenceMethod(Relation relation, ObjectType ot) {
        super(ot, "subseq" + Naming.withCapital(relation.name()), null, ot.getCodeClass());
        this.relation = relation;

        List<Param> params = new ArrayList<>();
        SubstitutionType indexType;
        if (relation.isSeqRelation()) {
            indexType = relation.qualifierRoles().get(0).getSubstitutionType();
        } else {
            indexType = BaseType.NATURAL;
        }
        params.add(new Param("i", indexType, null));
        params.add(new Param("j", indexType, null));
        setParams(params);
        returnType = new ReturnType(new CT(CollectionKind.LIST, relation.targetType()));
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        // we could have done checking of the parameters, but at least in Java,
        // this is handled by the subsequence method.
        list.addLineAtCurrentIndentation(l.returnStatement(l.unmodifiable(relation.collectionType(), l.subseq(relation, getParams().get(0).getName(), getParams().get(1).getName()))));
        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
    }

    @Override
    public Relation getRelation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Field getField() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initSpec() {
        String maxcount = null;
        if (getCodeClass().getOperation("count", relation) == null) {
            // frequency is constant
            maxcount = relation.getMaxFreq() + "";
        } else {
            maxcount = "count" + relation.getPluralName();
        }
        if (getParams().get(0).getType().equals(BaseType.NATURAL)) {
            returnType.setSpec("if 0 <= i and i <= j and j <= " + maxcount
                    + " then @result = the subsequence from i until j including, otherwise @result = null");
        } else {
            returnType.setSpec("if i <= j "
                    + " then @result = the subsequence from i until j including, otherwise @result = null");

        }

    }

    @Override
    public Set<ImportType> getImports() {
        Set<ImportType> list = new HashSet<>();
        list.add(ImportType.List);
        list.add(ImportType.ArrayList);
        list.add(Collections);
        list.add(Linq);
        return list;
    }
}
