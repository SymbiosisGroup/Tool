/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.systemoperations;

import java.util.List;

import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.code.operations.IBooleanOperation;
import equa.code.operations.Method;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.ModelElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class IsLesserOrEqualMethod extends Method implements IBooleanOperation {

    public IsLesserOrEqualMethod(ObjectModel om, ModelElement source) {
        super(om, null, null, source);
        // TODO Auto-generated constructor stub
    }

    private static final long serialVersionUID = 1L;

    @Override
    public RuleRequirement getRuleRequirement() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void initSpec() {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<ImportType> getImports() {
        Set<ImportType> imports = new HashSet<>();
        imports.add(ImportType.Utility);
        return imports;
    }

    @Override
    public IndentedList getCode(Language l) {
        // TODO Auto-generated method stub
        return null;
    }
}
