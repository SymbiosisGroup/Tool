/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.systemoperations;

import java.util.ArrayList;
import java.util.List;

import symbiosis.code.ImportType;
import symbiosis.code.IndentedList;
import symbiosis.code.Language;
import symbiosis.code.operations.ActualParam;
import symbiosis.code.operations.Call;
import symbiosis.code.operations.IBooleanOperation;
import symbiosis.code.operations.Method;
import symbiosis.code.operations.Param;
import symbiosis.code.operations.ReturnType;
import symbiosis.meta.objectmodel.BaseType;
import symbiosis.meta.objectmodel.ObjectModel;
import symbiosis.meta.requirements.RuleRequirement;
import symbiosis.meta.traceability.ModelElement;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class UniversalQuantor extends Method implements IBooleanOperation {

    private static final long serialVersionUID = 1L;

    public UniversalQuantor(ObjectModel om, ModelElement source) {
        super(om, "universalQuantor", null, source);
        List<Param> params = new ArrayList<>();
        params.add(new Param("domain", BaseType.STRING, null));
        params.add(new Param("predicate", BaseType.STRING, null));
        setParams(params);
        returnType = new ReturnType(BaseType.BOOLEAN);
    }

    @Override
    public String returnValue() {
//        List<ActualParam> actualParams = new ArrayList<>();
//        actualParams.add(new Param("x",getParams().get(0).getType(),null));
//        return "All (x in " + domain.toString() + ") : " + new BooleanCall(operation,actualParams,false);
        return "Null";
    }

    @Override
    public RuleRequirement getRuleRequirement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Call call(List<? extends ActualParam> actualParams) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Call call() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String callString(List<? extends ActualParam> actualParams) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String callString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initSpec() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<ImportType> getImports() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IndentedList getCode(Language l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
