/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.meta.requirements;

/**
 *
 * @author frankpeeters
 */
public enum Urgency {

    UNDEFINED,
    /**
     * we do not need this to be implemented quickly
     */
    LOW,
    /**
     * we need this to be implemented 
     */
    MEDIUM,
    /**
     * we need this to be implemented quickly
     */
    HIGH;
    
    @Override
    public String toString(){
        return this.name().substring(0,1) + this.name().substring(1).toLowerCase();
    }
}
