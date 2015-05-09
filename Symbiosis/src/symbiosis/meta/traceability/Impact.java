package symbiosis.meta.traceability;

import symbiosis.meta.requirements.Requirement;
import symbiosis.meta.requirements.RequirementFilter;

/**
 *
 * @author FrankP
 */
public enum Impact {

    UNDEFINED(-1),
    /**
     *
     */
    NONE(0),
    /**
     *
     */
    LIGHT(1),
    /**
     *
     */
    NORMAL(2),
    /**
     *
     */
    SERIOUS(3);

    private int order;

    private Impact(int order) {
        this.order = order;
    }

    /**
     *
     * @return the number of the constant
     */
    public int getOrder() {
        return order;
    }
    
    @Override
    public String toString(){
        return this.name().substring(0,1) + this.name().substring(1).toLowerCase();
    }

}
