/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.operations;

import equa.code.systemoperations.IndexAllowedMethod;
import java.util.ArrayList;
import java.util.List;

import equa.meta.classrelations.QualifierRelation;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ConstrainedBaseType;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Role;
import equa.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class IndexedProperty extends Property {

    private static final long serialVersionUID = 1L;
    private List<Param> qualifiers;

    public IndexedProperty(Relation relation, ObjectType ot) {
        super(relation, ot);
        qualifiers = new ArrayList<>();
        List<Role> qualifierRoles = relation.qualifierRoles();
        for (Role role : qualifierRoles) {
            qualifiers.add(new Param(role.detectRoleName(), role.getSubstitutionType(), new QualifierRelation(ot, role)));
        }
//        if (relation.isMapRelation()) {
//            setter.setPresent(false);
//        }

    }

    @Override
    public void initSpec() {
        StringBuilder returnSpec = new StringBuilder();
        if (relation.hasMultipleQualifiedTarget()) {
            if (relation.getMinFreq() == relation.getMaxFreq()) {
                returnSpec.append("a collection with the ").append(Integer.toString(relation.getMinFreq())).
                    append(" ").append(callString()).append(" of ").append(Naming.withoutCapital(getParent().getName()));
            } else {
                returnSpec.append("a collection with all ").append(callString()).append(" of ").append(Naming.withoutCapital(getParent().getName()));
            }
        } else {
            returnSpec.append("the ").append(callString()).append(" of this ").append(Naming.withoutCapital(getParent().getName()));
        }

        //StringBuilder postSpec = new StringBuilder();
        //postSpec.append("The ").append(getName()).append(" of this ").append(Naming.withoutCapital(parent.getName()));
        if (relation.isDerivable()) {
            returnSpec.append("; the value is derived conform the rule: \"").append(relation.getDerivableText()).append("\"");
        }

        if (!relation.isMandatory() && !relation.hasMultipleQualifiedTarget()) {
            if (relation.targetType().getUndefinedString() == null && !relation.targetType().equals(BaseType.BOOLEAN)) {
                IBooleanOperation isdefined = (IBooleanOperation) ((ObjectType) getParent()).getCodeClass().getOperation("isDefined", relation);
                BooleanCall undefinedCall = ((BooleanCall) isdefined.call()).withNegation();
                setPreSpec(undefinedCall);
            }
        }

        List<Role> qRoles = relation.qualifierRoles();
        IFormalPredicate escapeCondition = null;
        if (qRoles.get(0).isSeqNr() && !(qRoles.get(0).getSubstitutionType() instanceof ConstrainedBaseType)) {
            IndexAllowedMethod indexAllowed = getObjectModel().getIndexAllowedMethod();
            List<ActualParam> actualParams = new ArrayList<>();
            actualParams.add(qualifiers.get(0));
            ActualParam size = null;
            if (relation.getMinFreq() == relation.getMaxFreq()) {
                size = new ValueString(relation.getMaxFreq() + "", BaseType.NATURAL);
            } else {
                Operation count = ((ObjectType) getParent()).getCodeClass().getOperation("count", relation);
                size = count.call();
            }
            actualParams.add(size);
            escapeCondition = new BooleanCall(indexAllowed, actualParams, true);

            IPredicate escapeResult = new InformalPredicate(relation.targetType().getUndefinedString() + " will be returned!");
            for (int i = 1; i < qualifiers.size(); i++) {
                actualParams = new ArrayList<>();
                Param param = qualifiers.get(i);
                actualParams.add(param);
                actualParams.add(size);
                escapeCondition.conjunctionWith(new BooleanCall(indexAllowed, actualParams, true));
            }

            setEscape(escapeCondition, escapeResult);

            returnSpec.append("; @result could be undefined, " + "in that case ").append(relation.targetType().getUndefinedString()).append(" will be returned");
        }

        returnType.setSpec(returnSpec.toString());

        if (isSetter()) {
            IFormalPredicate setterEscapeCondition=null;
            if (escapeCondition != null) {
                setterEscapeCondition = new BooleanCall((BooleanCall) escapeCondition);
                setter.setEscape(setterEscapeCondition, new InformalPredicate(self() + " stays unchanged"));
            }

            List<ActualParam> actualParams = new ArrayList<>();
            actualParams.addAll(setterParams());
            if (relation.targetType() instanceof ConstrainedBaseType) {
                ConstrainedBaseType cbt = (ConstrainedBaseType) relation.targetType();
                IBooleanOperation correctValue = (IBooleanOperation) cbt.getCodeClass().getOperation("isCorrectValue");
                BooleanCall bc = new BooleanCall(correctValue, actualParams, true);
                bc.setCalled((ObjectType) cbt.getCodeClass().getParent());
                if (setterEscapeCondition == null) {
                    setterEscapeCondition = bc;
                } else {
                    setterEscapeCondition.conjunctionWith(bc);
                }
                setter.setEscape(setterEscapeCondition, new InformalPredicate(self() + " stays unchanged"));
            } else if (relation.targetType() instanceof BaseType) {
                BaseType bt = (BaseType) relation.targetType();
                if (bt.equals(BaseType.NATURAL)) {
                    BooleanCall isNatural = new BooleanCall(getObjectModel().getIsNaturalMethod(), actualParams, true);
                    if (setterEscapeCondition == null) {
                        setterEscapeCondition = isNatural;
                    } else {
                        setterEscapeCondition.conjunctionWith(isNatural);
                    }
                    setter.setEscape(setterEscapeCondition, new InformalPredicate(self() + " stays unchanged"));
                }
            }

            setter.setPostSpec(new InformalPredicate(relation.roleName() + qualifierlist() + " equals value of parameter " + relation.name()));
        }
    }

    String qualifierlist() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        String separator = "";
        for (Param param : qualifiers) {
            sb.append(separator);
            sb.append(param.getName());
            separator = ", ";
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public List<Param> getterParams() {
        return qualifiers;
    }

    public List<Param> getQualifiers() {
        return qualifiers;
    }

    @Override
    public List<Param> setterParams() {
        List<Param> params = new ArrayList<>(qualifiers);
        params.add(value);
        return params;
    }

    @Override
    public String callString(List<? extends ActualParam> actualParams) {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append("(");
        if (!actualParams.isEmpty()) {
            sb.append(actualParams.get(0).callString());
            for (int i = 1; i < actualParams.size(); i++) {
                sb.append(",");
                sb.append(actualParams.get(i).callString());
            }
        }
        sb.append(")");

        return sb.toString();
    }

    @Override
    public String callString() {
        return callString(qualifiers);
    }

    @Override
    public String toString() {
        StringBuilder propString = new StringBuilder();
        propString.append(getAccess().getAbbreviation());
        propString.append(" ");
        propString.append(getName());
        propString.append("(");
        propString.append(qualifiers.get(0).getName()).append(" : ").append(qualifiers.get(0).getType().getName());
        for (int i = 1; i < qualifiers.size(); i++) {
            propString.append(",");
            propString.append(qualifiers.get(i).getName()).append(" : ").append(qualifiers.get(i).getType().getName());
        }
        propString.append(") : ");
        propString.append(returnType.toString());

        if (getter.isPresent()) {
            if (setter.isPresent()) {
                propString.append(" {").append(getter.getAccessString()).append("get, ").append(setter.getAccessString()).append("set}");
            } else {
                propString.append(" {").append(getter.getAccessString()).append("get}");
            }
        } else if (setter.isPresent()) {
            propString.append(" {").append(setter.getAccessString()).append("set}");
        }

        return propString.toString();
    }

}
