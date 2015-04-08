/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code;

import equa.code.operations.AccessModifier;
import equa.code.operations.CT;
import equa.code.operations.CollectionKind;
import equa.code.operations.JavaType;
import equa.code.operations.OperationWithParams;
import equa.code.operations.Param;
import equa.code.operations.STorCT;
import equa.meta.classrelations.BooleanRelation;
import equa.meta.classrelations.FactTypeRelation;
import equa.meta.classrelations.QualifierRelation;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectRole;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author frankpeeters
 */
public class DerivableOH extends OperationHeader {

    private static final long serialVersionUID = 1L;
    private FactType ft;

    public DerivableOH(FactType ft) {
        this.ft = ft;
    }

    @Override
    public boolean isClassMethod() {
        return false;
    }

    @Override
    public String getName(Language l) {
        ObjectRole role = ft.getNavigableRole();
        if (role.isQualified()) {
            return getName();
        } else {
            return l.propertyName(getName(), getReturnType());
        }
    }

    STorCT getReturnType() {
        ObjectRole role = ft.getNavigableRole();
        ObjectType ot = role.getSubstitutionType();
        Relation relation;
        if (ft.nonQualifierSize() == 1) {
            relation = new BooleanRelation(ot, role);
        } else {
            relation = new FactTypeRelation(ot, role);
        }
        if (relation.isCollectionReturnType()) {
            return new CT(CollectionKind.LIST, relation.targetType());
        } else {
            return relation.targetType();
        }
    }

    List<Param> getParams() {
        List<Param> params = new ArrayList<>();
        ObjectRole role = ft.getNavigableRole();
        if (role.isQualified()) {
            for (Role r : ft.qualifiersOf(role)) {
                params.add(new Param(r.detectRoleName(), r.getSubstitutionType(), new QualifierRelation(role.getSubstitutionType(), r)));
            }
        }
        return params;
    }

    @Override
    public List<String> getParamTypes(Language l) {
        List<String> paramTypes = new ArrayList<>();
        for (Param p : getParams()) {
            paramTypes.add(l.type(p.getType()));
        }
        return paramTypes;
    }

    @Override
    public List<String> getParamNames(Language l) {
        List<String> paramNames = new ArrayList<>();
        for (Param p : getParams()) {
            paramNames.add(p.getName());
        }
        return paramNames;
    }

    @Override
    public String getReturn(Language l) {
        return l.type(getReturnType());
    }

    @Override
    public String getAccessModifier(Language l) {
        return l.accessModifier(AccessModifier.PUBLIC);
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
            Language l = ((ObjectModel) ft.getParent()).getProject().getLastUsedLanguage();
            if (l == null) {
                l = Language.JAVA;
            }
            if (!getName(l).equals(other.getName(l))) {
                return false;
            }
            return getParams(l).equals(other.getParams(l));
        } else {
            return false;
        }
    }

    @Override
    public List<Param> getParams(Language l) {
        List<Param> params = new ArrayList<>();
        List<String> paramNames = getParamNames(l);
        List<String> paramTypes = getParamTypes(l);
        for (int i = 0; i < paramTypes.size(); i++) {
            // temporary language dependent solution:
            params.add(new Param(paramNames.get(i), new JavaType(paramTypes.get(i)), null));
        }
        return params;
    }

    @Override
    boolean sameParamTypes(OperationWithParams o) {
        List<Param> otherParams = o.getParams();
        List<Param> params = getParams();
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
        ObjectRole role = ft.getNavigableRole();
        ObjectType ot = role.getSubstitutionType();
        Relation relation;
        if (ft.nonQualifierSize() == 1) {
            relation = new BooleanRelation(ot, role);
        } else {
            relation = new FactTypeRelation(ot, role);
        }
        if (relation.isCollectionReturnType()) {
            return relation.getPluralName();
        } else {
            return relation.name();
        }
    }

}
