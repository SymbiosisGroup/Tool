/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.systemoperations;

import static symbiosis.code.ImportType.ObjectEquals;

import java.util.ArrayList;
import java.util.List;

import symbiosis.code.CodeClass;
import symbiosis.code.ImportType;
import symbiosis.code.IndentedList;
import symbiosis.code.Language;
import symbiosis.code.operations.IBooleanOperation;
import symbiosis.code.operations.Method;
import symbiosis.code.operations.Param;
import symbiosis.code.operations.ReturnType;
import symbiosis.meta.objectmodel.BaseType;
import symbiosis.meta.objectmodel.ObjectModel;
import symbiosis.meta.requirements.RuleRequirement;
import symbiosis.meta.traceability.ModelElement;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class IsEqualMethod extends Method implements IBooleanOperation {

    private static final long serialVersionUID = 1L;

    public IsEqualMethod(ObjectModel om, ModelElement source) {
        super(om, "isEqual", null, source);
        List<Param> params = new ArrayList<>();
        params.add(new Param("x", BaseType.OBJECT, null));
        params.add(new Param("y", BaseType.OBJECT, null));
        setParams(params);
        returnType = new ReturnType(BaseType.BOOLEAN);
    }

    @Override
    public void initSpec() {
        returnType.setSpec("value of x equals value of y");
    }

    @Override
    public Set<ImportType> getImports() {
        Set<ImportType> list = new HashSet<>();
        list.add(ObjectEquals);
        list.add(ImportType.Utility);
        return list;
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        list.addLineAtCurrentIndentation(l.returnStatement(l.equalsStatement(getParams().get(0).getName(), getParams().get(1).getName())));
        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
    }

    @Override
    public RuleRequirement getRuleRequirement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CodeClass getCodeClass() {
        return ((ObjectModel) getParent()).getCodeClass();
    }
}
