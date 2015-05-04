/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.operations;

import symbiosis.code.CodeClass;
import symbiosis.code.ImportType;
import symbiosis.code.IndentedList;
import symbiosis.code.Language;
import symbiosis.meta.objectmodel.BaseType;
import symbiosis.meta.objectmodel.ObjectType;
import symbiosis.meta.traceability.ModelElement;
import symbiosis.util.Naming;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class CompareToMethod extends Method {

    private static final long serialVersionUID = 1L;

    public CompareToMethod(ObjectType parent, CodeClass source) {
        super(parent, "compareTo", null, source);
        List<Param> params = new ArrayList<>();
        params.add(new Param(Naming.withoutCapital(getName()), parent, null));
        setParams(params);
        setReturnType(new ReturnType(BaseType.INTEGER));
    }

    @Override
    public IndentedList getCode(Language l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initSpec() {
        String actualParam = getParams().get(0).callString();
        returnType.setSpec("if self equals " + actualParam
                + " 0 will be returned else if self is smaller than " + actualParam
                + " -1 will be returned else +1 will be returned");
    }

    @Override
    public Set<ImportType> getImports() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
