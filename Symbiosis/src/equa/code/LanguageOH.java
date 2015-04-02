package equa.code;

import equa.code.operations.JavaType;
import equa.code.operations.OperationWithParams;
import equa.code.operations.Param;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LanguageOH extends OperationHeader {

    private static final long serialVersionUID = 1L;
    private final String retrn, name, access;
    private final List<String> modifiers;
    private final List<String> paramTypes;
    private final List<String> paramNames;
    private final List<String> exceptions;
    private final Language l;

    public LanguageOH(String access, List<String> modifiers, String retrn, String name, List<String> params, List<String> exceptions, Language l) {
        this.access = access;
        this.modifiers = modifiers;
        this.retrn = retrn;
        this.name = name;
        paramTypes = new ArrayList<>();
        paramNames = new ArrayList<>();
        for (String s : params) {
            if (s.isEmpty()) {
                continue;
            }
            String[] split = s.split(" ");
            paramTypes.add(split[0]);
            paramNames.add(split[1]);
        }
        this.exceptions = exceptions;
        this.l = l;
    }

    @Override
    public String getName(Language l) {
        return name;
    }

    @Override
    public List<String> getParamTypes(Language l) {
        return Collections.unmodifiableList(paramTypes);
    }

    @Override
    public List<String> getParamNames(Language l) {
        return Collections.unmodifiableList(paramNames);
    }

    @Override
    public String getReturn(Language l) {
        return retrn;
    }

    @Override
    public String getAccessModifier(Language l) {
        return access;
    }

    public List<String> getModifiers() {
        return Collections.unmodifiableList(modifiers);
    }

    @Override
    public List<String> getExceptions(Language l) {
        return Collections.unmodifiableList(exceptions);
    }

    public Language getLanguage() {
        return l;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof OperationHeader) {
            OperationHeader other = (OperationHeader) obj;

            if (!getName(l).equals(other.getName(l))) {
                return false;
            }
            if (getParamTypes(l) == null) {
                return other.getParamTypes(l) == null;
            }
            return getParamTypes(l).equals(other.getParamTypes(l));
        } else {
            return false;
        }
    }

    @Override
    public List<Param> getParams(Language l) {
        List<Param> params = new ArrayList<>();
        for (int i = 0; i < paramTypes.size(); i++) {
            // temporary language dependent solution:
            params.add(new Param(paramNames.get(i), new JavaType(paramTypes.get(i)), null));
        }
        return params;
    }

    @Override
    public boolean isClassMethod() {
        // temporary solution: 
        if (modifiers == null) {
            return false;
        }

        for (String modifier : modifiers) {
            if (modifier.equalsIgnoreCase("static")) {
                return true;
            }
        }
        return false;
    }

    @Override
    boolean sameParamTypes(OperationWithParams o) {
        List<Param> otherParams = o.getParams();
        if (paramTypes.size() != otherParams.size()) {
            return false;
        }
        for (int i = 0; i < paramTypes.size(); i++) {
            if (!paramTypes.get(i).equals(l.type(otherParams.get(i).getType()))) {
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
