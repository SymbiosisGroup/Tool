/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import equa.code.CodeClass;
import static equa.code.CodeNames.RESULT;
import equa.code.ImportType;
import static equa.code.ImportType.Set;
import static equa.code.ImportType.SortedSet;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.traceability.ModelElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class PropertiesMethod extends Method {

    private static final long serialVersionUID = 1L;

    public PropertiesMethod(ObjectType parent, CodeClass source) {
        super(parent, "properties", new ArrayList<Param>(), source);
        returnType = new ReturnType(new CT(CollectionKind.SET, BaseType.STRING));
        setClassMethod(true);
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        // standard operation header
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        // we create a Set as defined in the specifications
        list.addLineAtCurrentIndentation(l.declarationAndAssignment(returnType.getType(), RESULT, l.assignCollection(returnType.getType())));
        // every field that is public will be added as a string to this set.

        for (String r : ((ObjectType) getParent()).publicProperties()) {
            list.addLineAtCurrentIndentation(l.addCollection(RESULT, ((CT) returnType.getType()).getKind(), l.stringSymbol() + r + l.stringSymbol()) );
        }
        // return the set (it is a new one every time so we can give a direct reference)
        list.addLineAtCurrentIndentation(l.returnStatement(RESULT));
        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
    }

    @Override
    public void initSpec() {
        returnType.setSpec(
                "the set with names of all public "
                + "properties with getter-functionality belonging to this class "
                + "or an (indirect) generalization of this class");

    }

    @Override
    public Set<ImportType> getImports() {
        Set<ImportType> list = new HashSet<>();
        list.add(Set);
        list.add(SortedSet);
        return list;
    }
}
