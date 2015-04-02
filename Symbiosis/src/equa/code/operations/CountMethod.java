package equa.code.operations;

import java.util.ArrayList;
import java.util.List;

import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectType;
import equa.util.Naming;
import java.util.HashSet;
import java.util.Set;

public class CountMethod extends Method implements IRelationalOperation {

    private static final long serialVersionUID = 1L;
    private final Relation relation;

    public CountMethod(Relation relation, ObjectType ot) {
        super(ot, "count" + Naming.withCapital(relation.getPluralName()), null, ot.getCodeClass());
        this.relation = relation;
        List<Param> params = new ArrayList<>();

       

        setParams(params);
        returnType = new ReturnType(BaseType.NATURAL);

    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        list.addLineAtCurrentIndentation(l.returnStatement(relation.fieldName() + l.memberOperator() + l.size(relation.collectionType().getKind())));
        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
    }

    @Override
    public Relation getRelation() {
        return relation;
    }

    @Override
    public Field getField() {
        return null;
    }

    @Override
    public void initSpec() {
       // Property property = getCodeClass().getProperty(relation);
        returnType.setSpec("count of " + relation.name());
    }

    @Override
    public Set<ImportType> getImports() {
        return new HashSet<ImportType>();
    }

}
