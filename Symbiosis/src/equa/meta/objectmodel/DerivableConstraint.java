package equa.meta.objectmodel;

import equa.code.DerivableOH;
import equa.code.IndentedList;
import equa.code.Language;
import equa.code.MetaOH;
import equa.code.OperationHeader;
import equa.code.operations.AccessModifier;
import equa.code.operations.BooleanCall;
import equa.code.operations.CT;
import equa.code.operations.CollectionKind;
import equa.code.operations.IBooleanOperation;
import equa.code.operations.Param;
import equa.code.operations.Property;
import equa.code.operations.STorCT;
import equa.meta.ChangeNotAllowedException;
import equa.meta.classrelations.BooleanRelation;
import equa.meta.classrelations.BooleanSingletonRelation;
import equa.meta.classrelations.FactTypeRelation;
import equa.meta.classrelations.IdRelation;
import equa.meta.classrelations.ObjectTypeRelation;
import equa.meta.classrelations.QualifierRelation;
import equa.meta.classrelations.Relation;
import equa.meta.requirements.Requirement;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.ExternalInput;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

/**
 *
 * @author frankpeeters
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class DerivableConstraint extends StaticConstraint {

    private static final long serialVersionUID = 1L;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private OperationHeader oh;

    public DerivableConstraint() {
    }

    DerivableConstraint(FactType parent, RuleRequirement rule, String text) {
        super(parent, rule);
        initOH();
    }

    public void setText(ExternalInput source, String text) throws ChangeNotAllowedException {
        if (text.equals(getSpec())) {
        } else {
            ((Requirement) this.creationSource()).setText(source, text);
            initOH();
        }
    }

    public String getSpec() {
        ObjectRole role = getFactType().getResponsibleRole();
        if (role == null) {
            return "error (undefined navigable role)";
        }
        Relation relation;
        if (role.getParent().isClass()) {
            relation = new ObjectTypeRelation(role.getSubstitutionType(), role);
        } else if (role.getParent().nonQualifierSize() == 1) {
            relation = new BooleanRelation(role.getSubstitutionType(), role);
        } else {
            Role counterpart = role.getParent().counterpart(role);
            if (counterpart != null && !role.isMandatory() && !counterpart.isCreational() && counterpart.getSubstitutionType().isSingleton()) {
                relation = new BooleanSingletonRelation(role.getSubstitutionType(), role);
            } else {
                relation = new FactTypeRelation(role.getSubstitutionType(), role);
            }
        }

        Property property = role.getSubstitutionType().getCodeClass().getProperty(relation);
        if (property == null) {
            StringBuilder returnSpec = new StringBuilder();
            returnSpec.append(((Requirement) this.creationSource()).getText()).append("\"");
            if (!role.isMandatory() && !role.isMultiple()) {
                Role cp = getFactType().counterpart(role);
                if (cp != null) {
                    String undefined = cp.getSubstitutionType().getUndefinedString();
                    if (undefined != null) {
                        returnSpec.append("; the value could be undefined, " + "in that case ").append(undefined).append(" will be returned");
                    }
                }
            }
            return returnSpec.toString();
        } else {
            return property.getSpec();
        }
    }

    public OperationHeader getOperationHeader() {
        //checkOH();
        return oh;
    }

    private void initOH() {
        FactType ft = getFactType();

        ObjectRole role = ft.getNavigableRole();
        if (role == null) {
            if (ft.isObjectType()) {
                throw new UnsupportedOperationException("Factory is still missing");
            } else {
                // if role = null then there are no or too many navigable roles 
                throw new RuntimeException("there are no or too many navigable roles within derivable fact type " + ft.getName());
            }
        }
        ObjectType ot = role.getSubstitutionType();
        List<Role> qualifiers = ft.qualifiersOf(role);
        List<Param> params = new ArrayList<>();
        if (role.isQualified()) {
            for (Role r : qualifiers) {
                params.add(new Param(r.detectRoleName(), r.getSubstitutionType(), new QualifierRelation(ot, r)));
            }
        }
        String name;
        STorCT returnType;
        Relation relation;
        if (ft.nonQualifierSize() == 1) {
            relation = new BooleanRelation(ot, role);
        } else {
            relation = new FactTypeRelation(ot, role);
        }
        if (relation.isCollectionReturnType()) {
            name = relation.getPluralName();
            returnType = new CT(CollectionKind.LIST, relation.targetType());

        } else {
            name = relation.name();
            returnType = relation.targetType();

        }

        IndentedList api;
        api = new IndentedList();
        api.addLineAtCurrentIndentation(Language.JAVA.docStart());
        api.addLineAtCurrentIndentation(Language.JAVA.docLine(getSpec()));
        api.addLineAtCurrentIndentation(Language.JAVA.docEnd());

        oh = new DerivableOH(ft);
        IndentedList code;
        if (role.getSubstitutionType().isAbstract()) {
            code = null;
        } else {
            code = new IndentedList();
        }

        Algorithm alg = ot.addAlgorithm(oh, code, api, false, Language.JAVA, sources().get(0));

    }

    /**
     *
     */
    @Override
    public void remove() {
        FactType ft = getFactType();
        ObjectType ot = null;
        for (Role role : ft.roles) {
            if (role.isNavigable()) {
                ot = (ObjectType) role.getSubstitutionType();
            }
        }
        ot.removeAlgorithm(oh);

        super.remove();
    }

    @Override
    public String getAbbreviationCode() {
        return "der";
    }

    @Override
    public FactType getFactType() {
        return (FactType) getParent();
    }

    @Override
    public String getRequirementText() {
        return ((RuleRequirement) this.creationSource()).getOriginalText();
    }

    @Override
    public boolean isRealized() {
        ObjectRole responsible = getFactType().getNavigableRole();
        if (responsible == null || oh == null) {
            return false;
        } else {
            ObjectType ot = responsible.getSubstitutionType();
            Algorithm alg = ot.getAlgorithm(oh);
            return alg != null && !alg.isEmpty();
        }
    }
}
