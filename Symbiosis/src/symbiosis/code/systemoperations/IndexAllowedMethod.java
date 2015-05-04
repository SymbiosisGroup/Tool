/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.systemoperations;

import java.util.ArrayList;
import java.util.List;

import symbiosis.code.CodeClass;
import symbiosis.code.ImportType;
import symbiosis.code.IndentedList;
import symbiosis.code.Language;
import symbiosis.code.operations.IBooleanOperation;
import symbiosis.code.operations.Method;
import symbiosis.code.operations.Operator;
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
public class IndexAllowedMethod extends Method implements IBooleanOperation {

    public static final long serialVersionUID = 1L;

    public IndexAllowedMethod(ObjectModel om, ModelElement source) {
        super(om, "indexAllowed", null, source);
        List<Param> params = new ArrayList<>();
        params.add(new Param("index", BaseType.NATURAL, null));
        params.add(new Param("size", BaseType.NATURAL, null));
        setParams(params);

        returnType = new ReturnType(BaseType.BOOLEAN);
    }

    @Override
    public void initSpec() {
        returnType.setSpec("1 <= index <= size");
    }

    @Override
    public Set<ImportType> getImports() {
        Set<ImportType> imports = new HashSet<>();
        imports.add(ImportType.Utility);
        return imports;
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        list.addLinesAtCurrentIndentation(l.operationHeader(this));
        list.addLineAtCurrentIndentation(l.returnStatement("1" + l.operator(Operator.SMALLER_OR_EQUAL) + getParams().get(0).getName() + l.and() + getParams().get(0).getName() + l.operator(Operator.SMALLER_OR_EQUAL) + getParams().get(1).getName()));
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
