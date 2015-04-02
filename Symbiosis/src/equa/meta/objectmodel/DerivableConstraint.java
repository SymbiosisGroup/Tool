package equa.meta.objectmodel;

import equa.code.DerivableOH;
import equa.code.IndentedList;
import equa.code.Language;
import equa.code.MetaOH;
import equa.code.OperationHeader;
import equa.code.operations.AccessModifier;
import equa.code.operations.CT;
import equa.code.operations.CollectionKind;
import equa.code.operations.Param;
import equa.code.operations.STorCT;
import equa.meta.ChangeNotAllowedException;
import equa.meta.classrelations.BooleanRelation;
import equa.meta.classrelations.FactTypeRelation;
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
        if (text.equals(getText())) {
        } else {
            getFactType().getDerivableConstraint().setText(source, text);
        }
    }

    public String getText() {
        return ((Requirement) this.creationSource()).getText();

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
        api.addLineAtCurrentIndentation(Language.JAVA.docLine("Returns:\t" + getText()));
        api.addLineAtCurrentIndentation(Language.JAVA.docEnd());

        oh = new DerivableOH(ft);
        IndentedList code;
        if (role.getSubstitutionType().isAbstract()) {
            code = null;
        } else {
            code = new IndentedList();
        }
//        if (oh != null) {
//            if (!oh.equals(oh2)) {
//                code = ot.getAlgorithm(oh).getCode();
//                ot.removeAlgorithm(oh);
//            }
//        } 
        ot.addAlgorithm(oh, code, api);
        //oh = oh2;
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
        return getText();
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
