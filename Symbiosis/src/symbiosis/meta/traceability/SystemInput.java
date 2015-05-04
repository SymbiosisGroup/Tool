/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.meta.traceability;

import javax.persistence.Entity;

/**
 *
 * @author frankpeeters
 */
@Entity
public class SystemInput extends ExternalInput {

    private static final long serialVersionUID = 1L;

    public SystemInput() {
    }

    public SystemInput(String justification) {
        super(justification, symbiosis.project.System.SINGLETON);
    }
}
