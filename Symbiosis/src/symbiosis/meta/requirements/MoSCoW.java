/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.meta.requirements;

/**
 *
 * @author frankpeeters
 */
public enum MoSCoW {

    UNDEFINED,
    /**
     * we need to implement this
     */
    MUST,
    /**
     * we should implemented this
     */
    SHOULD,
    /**
     * we could implemented this if there is time
     */
    COULD,
    /**
     * we will not implemented this
     */
    WONT;

    @Override
    public String toString() {
        if (this.equals(WONT)) {
            return "Won't";
        } else {
            return this.name().substring(0, 1) + this.name().substring(1).toLowerCase();
        }
    }
}
