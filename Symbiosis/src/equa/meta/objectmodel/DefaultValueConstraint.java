/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.ParentElement;
import java.io.Serializable;

/**
 *
 * @author frankpeeters
 */
public class DefaultValueConstraint extends StaticConstraint implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String stringvalue;
    private final Value value;

    public DefaultValueConstraint(ParentElement parent, String defaultValue, RuleRequirement rule) {
        super(parent, rule);
        this.stringvalue = defaultValue;
        this.value = null;
    }

    public DefaultValueConstraint(ParentElement parent, Value defaultValue, RuleRequirement rule) {
        super(parent, rule);
        this.stringvalue = null;
        this.value = defaultValue;
    }

    @Override
    public String getAbbreviationCode() {
        return "def";
    }

    public String getValue() {
        if (stringvalue == null) {
            return value.toString();
        } else {
            return stringvalue;
        }
    }

    @Override
    public boolean isRealized() {
        return true;
    }

    @Override
    public FactType getFactType() {
        if (getParent() instanceof Role) {
            return ((Role) getParent()).getParent();
        } else {
            return (FactType) getParent();
        }
    }

    @Override
    public String getRequirementText() {
        if (getParent() instanceof Role) {
            return "The default value on <" + ((Role) getParent()).detectRoleName() + "> within " + getFactType().getFactTypeString()
                + " is " + getValue() + ".";
        } else {
            return "The default value on <" + getFactType().getName()
                + "> is " + getValue() + ".";
        }
    }

}
