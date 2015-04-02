package equa.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import equa.code.operations.AccessModifier;
import equa.code.operations.Operation;
import equa.code.operations.OperationWithParams;
import equa.code.operations.Param;
import equa.code.operations.STorCT;

public class MetaOH extends OperationHeader {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final AccessModifier access;
    private final boolean property;
    private final boolean classMethod;
    private final STorCT returnType;
    private final List<Param> params;

    public MetaOH(String name, AccessModifier access, boolean property, boolean classMethod, STorCT returnType, List<Param> params) {
        this.name = name;
        this.access = access;
        this.property = property;
        this.classMethod = classMethod;
        this.returnType = returnType;
        this.params = params;
    }

    @Override
    public boolean isClassMethod() {
        return classMethod;
    }

    @Override
    public String getName(Language l) {
        if (property) {
            return l.propertyName(name, returnType);
        } else {
            return name;
        }
    }

    @Override
    public List<String> getParamTypes(Language l) {
        List<String> paramTypes = new ArrayList<>();
        for (Param p : params) {
            paramTypes.add(l.type(p.getType()));
        }
        return paramTypes;
    }

    @Override
    public List<String> getParamNames(Language l) {
        List<String> paramNames = new ArrayList<>();
        for (Param p : params) {
            paramNames.add(p.getName());
        }
        return paramNames;
    }

    @Override
    public String getReturn(Language l) {
        return l.type(returnType);
    }

    @Override
    public String getAccessModifier(Language l) {
        return l.accessModifier(access);
    }

    @Override
    public List<String> getExceptions(Language l) {
        return Collections.emptyList();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof OperationHeader) {
            OperationHeader other = (OperationHeader) obj;
            Language l;
            if (other instanceof LanguageOH) {
                l = ((LanguageOH) other).getLanguage();
            } else {
                l = Language.JAVA;
            }
            if (!getName(l).equals(other.getName(l))) {
                return false;
            }
            if (getParamTypes(l) == null) {
                return other.getParamTypes(l) == null;
            } else {
                return getParamTypes(l).equals(other.getParamTypes(l));
            }
        } else {
            return false;
        }
    }

    @Override
    public List<Param> getParams(Language l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    boolean sameParamTypes(OperationWithParams o) {
        List<Param> otherParams = o.getParams();
        if (params.size() != otherParams.size()) {
            return false;
        }
        for (int i = 0; i < params.size(); i++) {
            if (!params.get(i).getType().equals(otherParams.get(i).getType())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

}
