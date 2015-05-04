/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.meta.objectmodel;

import symbiosis.meta.requirements.ActionRequirement;
import java.io.Serializable;

/**
 *
 * @author frankpeeters
 */
public class MutablePermission extends ActionPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    MutablePermission(FactType ft, ActionRequirement source) {
        super(ft, source);
    }

    @Override
    public String getName() {
        return getParent().getName() + ".mut";
    }

    @Override
    public String getAbbreviationCode() {
        return "mut";
    }

    @Override
    public boolean equals(Object member) {
        if (member instanceof MutablePermission) {
            return getParent().equals(((MutablePermission) member).getParent());
        } else {
            return false;
        }
    }

    @Override
    public String getRequirementText() {
        String reqText;
        if (getFactType().isObjectType()) {
            reqText = "Some actor of the system must get the opportunity to change the id of a(n) <"
                + getParent().getName() + ">";

        } else {
            reqText
                = "It is allowed to change the facts of <" + getParent().getName() + "> "
                + "as a result of an event rule.";
        }
        return reqText;
    }

    @Override
    public boolean isRealized() {
        return true;
    }

    @Override
    public FactType getFactType() {
        return (FactType) getParent();
    }
}
