/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.meta.objectmodel;

import symbiosis.code.MetaOH;
import symbiosis.code.OperationHeader;
import symbiosis.meta.requirements.Requirement;
import java.util.ArrayList;

/**
 *
 * @author frankpeeters
 */
public class Initializer extends Constraint {

    private static final long serialVersionUID = 1L;

    Initializer(Requirement rule, ObjectType source) {
        super(source, rule);
    }

    public ObjectType getObjectType() {
        return (ObjectType) getParent();
    }

    @Override
    public String getAbbreviationCode() {
        return ((Requirement) creationSource()).getId();
    }

    @Override
    public boolean isRealized() {
        ObjectType ot = getObjectType();
        OperationHeader oh = new MetaOH("init", null, false, false, null, new ArrayList<>());
        Algorithm alg = ot.getAlgorithm(oh);
        return alg != null && !alg.isEmpty();
    }

    @Override
    public String getName() {
         return "Init " + getAbbreviationCode();  }

    @Override
    public boolean equals(Object object) {
       if (object instanceof Initializer){
           Initializer initializer = (Initializer) object;
           return getObjectType().equals(initializer.getObjectType());
       } else {
           return false;
       }
    }

    @Override
    public FactType getFactType() {
      return getObjectType().getFactType();
    }

    @Override
    public String getRequirementText() {
        String text
            = "As soon as " + getObjectType().getName() + " is created "
            + " then " + "its init-method" + " will be executed.";
        return text;
    }
}
