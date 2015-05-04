/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.operations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author frankpeeters
 */
public class GetOrSet implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean present;
    private AccessModifier access;
    private IFormalPredicate preSpec;
    private IPredicate postSpec;
    private Escape escape;

    /**
     * Constructor. present=true and the access modifier is public by default.
     *
     * @param relation
     */
    public GetOrSet() {
        this.present = true;
        this.access = AccessModifier.PUBLIC;
        preSpec = null;
        postSpec = null;
        escape = null;
    }

    /**
     *
     * @return the precondition with respect to the state of the corresponding
     * object and given parameters before execution of this behavior
     */
    public IFormalPredicate getPreSpec() {
        return this.preSpec;
    }

    /**
     * changing of the precondition
     *
     * @param preSpec
     */
    public void setPreSpec(IFormalPredicate preSpec) {
        this.preSpec = preSpec;
    }

    public void setEscape(IFormalPredicate condition, IPredicate result) {
        escape = new Escape(condition, result);
    }

    /**
     *
     * @return the postcondition after an abnormal execution (including
     * condition when escape raises)
     */
    public Escape getEscape() {
        return escape;
    }

    /**
     *
     * @return the postcondition concerning the state of the corresponding
     * object after a normal execution
     */
    public IPredicate getPostSpec() {
        return this.postSpec;
    }

    /**
     * changing the (postcondition after a normal execution)
     *
     * @param postSpec
     */
    public void setPostSpec(IPredicate postSpec) {
        this.postSpec = postSpec;
        // publisher.inform(this, null, ("NULL"), this);
    }

    public String getSpec() {
        StringBuilder sb = new StringBuilder();

        if (preSpec != null && preSpec.operands().hasNext()) {
            sb.append("Pre:\t");
            sb.append(getPreSpec().returnValue());
            sb.append(System.lineSeparator());
        }

        if (postSpec != null) {
            sb.append("Post:\t");
            if (escape == null) {
                if (postSpec != null) {
                    sb.append(getPostSpec().returnValue());
                }
            } else {
                IFormalPredicate condition = escape.getCondition();
                sb.append("IF ");
                if (condition.isNegated()) {
                    String withoutNegation = ((BooleanCall) condition).withoutNegationString();
                    sb.append(withoutNegation);
                    sb.append(System.lineSeparator());
                    sb.append("\tTHEN ");
                    if (postSpec != null) {
                        sb.append(getPostSpec().returnValue());
                    }
                    sb.append(System.lineSeparator());
                    sb.append("\tELSE ");
                    sb.append(escape.getResult().returnValue());

                } else {
                    sb.append(condition.returnValue());
                    sb.append(System.lineSeparator());
                    sb.append("\tTHEN ");
                    sb.append(escape.getResult().returnValue());
                    sb.append(System.lineSeparator());
                    sb.append("\tELSE ");
                    if (postSpec != null) {
                        sb.append(getPostSpec().returnValue());
                    }
                }
            }
            sb.append(System.lineSeparator());
        }

        return sb.toString();

    }

    /**
     * if present=true then GetOrSet is active otherwise inactive
     *
     * @param present
     */
    public void setPresent(boolean present) {
        this.present = present;
    }

    /**
     *
     * @param access modifier, see: {@link AccessModifier}.
     */
    public void setAccessModifier(AccessModifier access) {
        this.access = access;
    }

    /**
     *
     * @return if the GetOrSet is active (true) or inactive (false).
     */
    public boolean isPresent() {
        return present;
    }

    /**
     *
     * @return the access modifier, see: {@link AccessModifier}.
     */
    public AccessModifier getAccessModifier() {
        return access;
    }

    /**
     *
     * @return the abbreviation of the access modifier
     */
    public String getAccessString() {
        return access.getAbbreviation();
    }

    /**
     *
     * @return if the access modifier is not public, the abbreviation of the
     * access modifier is returned.
     */
    public String getAccessStringNonPublic() {
        if (access.equals(AccessModifier.PUBLIC)) {
            return "";
        } else {
            return access.getAbbreviation();
        }
    }

}
