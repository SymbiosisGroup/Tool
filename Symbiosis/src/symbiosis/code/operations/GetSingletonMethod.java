/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.operations;

import symbiosis.code.CodeClass;
import symbiosis.code.ImportType;
import static symbiosis.code.ImportType.ObjectEquals;
import symbiosis.code.IndentedList;
import symbiosis.code.Language;
import symbiosis.meta.objectmodel.ObjectType;
import symbiosis.meta.traceability.ModelElement;
import symbiosis.util.Naming;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class GetSingletonMethod extends Method {

    private static final long serialVersionUID = 1L;
    public static final String NAME = "getSingleton";

    public GetSingletonMethod(ObjectType parent, CodeClass source) {
        super(parent, NAME, new ArrayList<Param>(), source);
        setClassMethod(true);
        setReturnType(new ReturnType(parent));
    }

    @Override
    public IndentedList getCode(Language l) {
        // this is the field name
        String name = Naming.singletonName(getParent().getName());
        IndentedList list = new IndentedList();
        // standard operation header
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        // if the field is null we have to create it first
        IndentedList ifTrue = new IndentedList();
        // here we assign a new instance to the field name
        ifTrue.addLineAtCurrentIndentation(l.assignment(name, l.callConstructor(getParent().getName())));
        // if field name equals null is the condition
        list.addLinesAtCurrentIndentation(l.ifStatement(l.equalsStatement(name, "null"), ifTrue));
        // return the field
        list.addLineAtCurrentIndentation(l.returnStatement(name));
        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
    }

    @Override
    public void initSpec() {
        String spec = "the singleton-object " + ((ObjectType) getParent()).getOTE().toString();
        returnType.setSpec(spec);
    }

    @Override
    public Set<ImportType> getImports() {
        Set<ImportType> list = new HashSet<>();
        list.add(ObjectEquals);
        return list;
    }
}
