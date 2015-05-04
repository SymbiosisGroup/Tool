/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.operations;

import symbiosis.code.CodeClass;
import symbiosis.code.ImportType;
import symbiosis.code.IndentedList;
import symbiosis.code.Language;
import symbiosis.code.OperationHeader;
import symbiosis.meta.objectmodel.Algorithm;
import symbiosis.meta.objectmodel.ObjectType;
import symbiosis.meta.traceability.ModelElement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author frankpeeters
 */
public class ManuallyAddedMethod extends Operation {

    private static final long serialVersionUID = 1L;
    private final OperationHeader oh;
    private final Algorithm alg;
    private boolean classMethod;

    public ManuallyAddedMethod(ObjectType parent, OperationHeader oh, Algorithm alg,
        boolean classMethod, ModelElement source) {
        super(parent, source);
        this.oh = oh;
        this.alg = alg;
        this.classMethod = classMethod;
    }

    @Override
    public CodeClass getCodeClass() {
        return (CodeClass) getParent();
    }

    @Override
    public void initSpec() {

    }

    public boolean isClassMethod() {
        return classMethod;
    }

    public void setClassMethod(boolean classMethod) {
        this.classMethod = classMethod;
    }

    @Override
    public String getName() {
        return oh.getName(Language.JAVA);
    }

    @Override
    public String getNameParamTypesAndReturnType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rename(String newName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<ImportType> getImports() {
        return new HashSet<ImportType>();
    }

    @Override
    public int order() {
        if (isClassMethod()) {
            return 4;
        } else {
            return 3;
        }
    }

    @Override
    public IndentedList getCode(Language l) {
        if (alg == null || alg.getCode() == null) {
            return new IndentedList();
        } else {
            return alg.getCode();
        }
    }

    @Override
    public boolean adaptName(CodeClass codeClass) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean equals(Object object) {
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
    public int compareTo(Operation o) {
        if (order() != o.order()) {
            return order() - o.order();
        }

        if (!getName().equals(o.getName())) {
            return getName().compareTo(o.getName());
        }

        if (o instanceof ManuallyAddedMethod) {
            OperationHeader oh2 = ((ManuallyAddedMethod) o).oh;
            return oh.getParamTypes(Language.JAVA).toString().compareTo(oh2.getParamTypes(Language.JAVA).toString());
        } else {
            return 1;
        }
    }



    protected String paramList(boolean withNameAndType) {
        if (oh.getParams(Language.JAVA) == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        Iterator<Param> it = oh.getParams(Language.JAVA).iterator();
        if (it.hasNext()) {
            if (withNameAndType) {
                sb.append(it.next().toString());
            } else {
                sb.append(it.next().getType().getName());
            }
        }
        while (it.hasNext()) {
            sb.append(", ");
            if (withNameAndType) {
                sb.append(it.next().toString());
            } else {
                sb.append(it.next().getType().getName());
            }
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getAccess().getAbbreviation());
        sb.append(" ");
        sb.append(getName());

        sb.append(paramList(true));

        Iterator<Exception> exceptions = getExceptions();
        if (exceptions.hasNext()) {
            sb.append(" throws ");
            while (exceptions.hasNext()) {
                sb.append(exceptions.next().getName());
                sb.append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());
        }

        return sb.toString();
    }

}
