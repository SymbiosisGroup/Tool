/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import equa.code.IndentedList;
import equa.code.Language;
import equa.code.MetaOH;
import equa.code.OperationHeader;
import equa.code.operations.AccessModifier;
import equa.code.operations.Param;
import equa.meta.ChangeNotAllowedException;
import equa.meta.requirements.Requirement;
import equa.meta.traceability.ParentElement;
import equa.util.Naming;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author frankpeeters
 */
public class RoleEvent extends Constraint {

    private static final long serialVersionUID = 1L;

    private final FactType source;
    private final FactType condition; // could be null which means: always
    private boolean negation;
    private boolean extending;
    private boolean updating;
    private boolean removing;
    private String handlerName;
    private List<Param> params;

    RoleEvent(Requirement rule, Role eventRole, FactType condition, boolean negation,
        boolean extending, boolean updating, boolean removing,
        String handlerName) throws ChangeNotAllowedException {
        super(eventRole, rule);
        this.source = eventRole.getParent();
        if (!source.getResponsibleRole().equals(eventRole)) {
            throw new ChangeNotAllowedException("Event on facttype "
                + source.getName()
                + " without a single responsible role.");
        }
        this.condition = condition;
        this.negation = negation;
        this.extending = extending;
        this.removing = removing;
        this.updating = updating;
        params = new ArrayList<>();
        setEventHandler(handlerName);
    }

    public FactType getEventSource() {
        return source;
    }

    public FactType getCondition() {
        return condition;
    }

    public String getNameOfHandler() {
        return handlerName;
    }

    public final void setEventHandler(String handlerName)
        throws ChangeNotAllowedException {
        IndentedList api;
        api = new IndentedList();
        api.addLineAtCurrentIndentation(Language.JAVA.docStart());
        // if (condition == null) {
        api.addLineAtCurrentIndentation(Language.JAVA.docLine("Pre:\t"));
//        } else {
//            CodeClass cc = getObjectType().getCodeClass();
//            Property conditionProperty = cc.getProperty(new FactTypeRelation(getObjectType(),
//                    getCondition().getResponsibleRole()));
//            if (negation) {
//                api.add(Language.JAVA.docLine("Pre:\t" + "NOT " + conditionProperty.getPostSpec()));
//            } else {
//                api.add(Language.JAVA.docLine("Pre:\t" + conditionProperty.getPostSpec()));
//            }
//        }
        api.addLineAtCurrentIndentation(Language.JAVA.docLine("Post:\t" + getText()));
        api.addLineAtCurrentIndentation(Language.JAVA.docEnd());

        ObjectType ot = getObjectType();
        if (ot == null) {
            throw new ChangeNotAllowedException("Event on facttype "
                + source.getName()
                + " without a single responsible role.");
        }

        IndentedList code;
        if (this.handlerName != null) {
            OperationHeader oh = new MetaOH(handlerName, AccessModifier.PRIVATE, false, false, null, new ArrayList<>());
            code = ot.getAlgorithm(oh).getCode();
            ot.removeAlgorithm(oh);
        } else {
            this.handlerName = handlerName;
            code = new IndentedList();
        }

        Role eventRole = (Role) getParent();
        if (eventRole.getParent().isObjectType()) {
            ObjectType type = eventRole.getParent().getObjectType();
            params.add(new Param(Naming.withoutCapital(type.getName()), type, null));
        } else {
            Role cp = eventRole.getParent().counterpart(eventRole);
            if (cp != null) {
                params.add(new Param(Naming.withoutCapital(cp.getRoleName()), cp.getSubstitutionType(), null));
            }
        }
        ot.addAlgorithm(handlerName, AccessModifier.PRIVATE, false, false, null, params, code, api);
    }

    public String getText() {
        return ((Requirement) this.creationSource()).getText();

    }

    public boolean isNegation() {
        return negation;
    }

    public boolean isExtending() {
        return extending;
    }

    public boolean isRemoving() {
        return removing;
    }

    public boolean isUpdating() {
        return updating;
    }

    public void setNegation(boolean negation) {
        this.negation = negation;
    }

    @Override
    public String getAbbreviationCode() {
        return ((Requirement) creationSource()).getId();
    }

    @Override
    public boolean isRealized() {

        ObjectType ot = getObjectType();
        if (ot == null) {
            return false;
        }
        OperationHeader oh = new MetaOH(handlerName, null, false, false, null, params);
        Algorithm alg = ot.getAlgorithm(oh);
        return alg != null && !alg.isEmpty();
    }

    public ObjectType getObjectType() {
        ObjectRole role = source.getResponsibleRole();
        if (role == null) {
            return null;
        }
        return role.getSubstitutionType();
    }

    @Override
    public String getName() {
        return "Event " + getAbbreviationCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof RoleEvent) {
            RoleEvent ev = (RoleEvent) object;
            return ev.source.equals(source) && ev.extending == extending
                && ev.updating == updating && ev.removing == removing
                && ev.negation == negation && ev.condition == condition;
        } else {
            return false;
        }
    }

    @Override
    public FactType getFactType() {
        return source;
    }

    @Override
    public String getRequirementText() {
        String holds = "holds";
        if (negation) {
            holds += " not";
        }
        String text
            = "As soon as " + source.getName() + " changes and if "
            + condition.getName() + holds + ""
            + " then " + handlerName + " will be executed.";
        return text;
    }

    @Override
    public void remove() {
        ObjectType ot = getFactType().getResponsibleRole().getSubstitutionType();
        OperationHeader oh = new MetaOH(this.handlerName, null, false, false, null, params);
        ot.removeAlgorithm(oh);
        super.remove();
    }

    public boolean isNeededWhileExtending() {
        return extending;
    }

    public boolean isNeededWhileRemoving() {
        return removing;
    }

    public boolean isNeededWhileUpdating() {
        return updating;
    }

}
