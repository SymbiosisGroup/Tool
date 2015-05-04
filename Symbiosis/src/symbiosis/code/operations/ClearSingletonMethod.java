/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.operations;

import symbiosis.code.CodeClass;
import symbiosis.code.ImportType;
import static symbiosis.code.ImportType.ObjectEquals;
import symbiosis.code.IndentedList;
import symbiosis.code.Language;
import symbiosis.meta.objectmodel.ObjectType;
import symbiosis.util.Naming;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class ClearSingletonMethod extends Method {

    private static final long serialVersionUID = 1L;
    public static final String NAME = "clear";

    public ClearSingletonMethod(ObjectType parent, CodeClass source) {
        super(parent, NAME, new ArrayList<Param>(), source);
        setClassMethod(true);
        setAccessModifier(AccessModifier.NAMESPACE);
    }

    @Override
    public IndentedList getCode(Language l) {
        // this is the field name
        String name = Naming.singletonName(getParent().getName());
        IndentedList list = new IndentedList();
        // standard operation header
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        // here we assign a new instance to the field name
        list.addLineAtCurrentIndentation(l.assignment(name, l.callConstructor(getParent().getName())));
    
        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
    }

    @Override
    public void initSpec() {
        String spec = "total clear of the singleton-object " + ((ObjectType) getParent()).getOTE().toString();
        this.setPostSpec(new InformalPredicate(spec));
       
    }

    @Override
    public Set<ImportType> getImports() {
        Set<ImportType> list = new HashSet<>();
        return list;
    }
}
