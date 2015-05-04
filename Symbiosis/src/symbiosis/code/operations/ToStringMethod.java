/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.operations;

import symbiosis.code.CodeClass;
import static symbiosis.code.CodeNames.RESULT;
import symbiosis.code.ImportType;
import symbiosis.code.IndentedList;
import symbiosis.code.Language;
import symbiosis.meta.objectmodel.BaseType;
import symbiosis.meta.objectmodel.ObjectType;
import symbiosis.meta.objectmodel.TypeExpression;
import symbiosis.meta.traceability.ModelElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class ToStringMethod extends Method {

    private static final long serialVersionUID = 1L;

    public ToStringMethod(ObjectType parent, CodeClass source) {
        super(parent, "toString", new ArrayList<Param>(), source);
        ReturnType returnType = new ReturnType(BaseType.STRING);
        setReturnType(returnType);
        setOverrideMethod(true);
    }

    @Override
    public IndentedList getCode(Language l) {
        TypeExpression ote = ((ObjectType) getParent()).getOTE();
        IndentedList list = new IndentedList();
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        //get constants
        Iterator<String> constants = ote.constants();
        list.addLineAtCurrentIndentation(l.declarationAndAssignment(BaseType.STRING, RESULT, l.stringSymbol() + l.stringSymbol()));
        int i = 0;
        //add the first constant 
        String const1 = constants.next();
        list.addLineAtCurrentIndentation(l.assignment(RESULT, l.concatenate(RESULT, l.stringSymbol() + const1 + l.stringSymbol())));
        while (constants.hasNext()) {
            //for each other constant, first add the role then add the constant.
            String constant = constants.next();
            String s = l.getProperty(ote.getParent().getRole(ote.getRoleNumber(i)).relatedRoleName((ObjectType) getParent()));
            s = l.concatenate(s, l.stringSymbol() + constant + l.stringSymbol());
            s = l.concatenate(RESULT, s);
            list.addLineAtCurrentIndentation(l.assignment(RESULT, s));
            i++;
        }
        //return the created string
        list.addLineAtCurrentIndentation(l.returnStatement(RESULT));
        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
    }

    @Override
    public void initSpec() {
        returnType.setSpec(((ObjectType) getParent()).getOTE().toString());

    }

    @Override
    public Set<ImportType> getImports() {
        return new HashSet();
    }
}
