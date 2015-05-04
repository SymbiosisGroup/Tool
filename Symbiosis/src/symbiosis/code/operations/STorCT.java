/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.operations;

import symbiosis.meta.objectmodel.Type;
import java.util.List;

/**
 *
 * @author frankpeeters
 */
public interface STorCT extends Type {

    public List<Param> transformToBaseTypes(Param param);
}
