package symbiosis.meta.requirements;

/**
 *
 * @author FrankP
 */
public enum ChanceOfFailure {

    UNDEFINED,
    /**
     * we expect a low risk in realisation of this requirement
     */
    LOW,
    /**
     * we expect a medium risk in realisation of this requirement
     */
    MEDIUM,
    /**
     * we expect a high risk in realisation of this requirement
     */
    HIGH;
    
    @Override
    public String toString(){
        return this.name().substring(0,1) + this.name().substring(1).toLowerCase();
    }
}
