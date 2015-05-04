/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.operations;

import symbiosis.code.CodeClass;
import static symbiosis.code.CodeNames.RESULT;
import symbiosis.code.ImportType;
import static symbiosis.code.ImportType.Set;
import static symbiosis.code.ImportType.SortedSet;
import symbiosis.code.IndentedList;
import symbiosis.code.Language;
import symbiosis.meta.objectmodel.BaseType;
import symbiosis.meta.objectmodel.ObjectType;
import symbiosis.meta.traceability.ModelElement;
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
