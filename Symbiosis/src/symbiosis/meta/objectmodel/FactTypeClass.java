/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.meta.objectmodel;

import java.util.List;

import javax.persistence.Entity;

import symbiosis.meta.MismatchException;
import symbiosis.meta.NotParsableException;
import symbiosis.meta.requirements.FactRequirement;
import symbiosis.meta.requirements.Requirement;

/**
 *
 * @author frankpeeters
 */
@Entity
public class FactTypeClass extends ObjectType {

    

    private static final long serialVersionUID = 1L;

    public FactTypeClass(FactType ft) {
        super(ft);
    }

    @Override
    public TypeExpression getTypeExpression() {
        return getFactType().getFTE();
    }

    /**
     *
     * @return false (a pure facttype is never abstract)
     */
    @Override
    public boolean isAbstract() {
        return false;
    }

    /**
     *
     * @return false (a pure facttype is never ordered)
     */
    @Override
    public boolean isComparable() {
        return false;
    }

    @Override
    public void setAbstract(boolean isAbstract) {
    }

    @Override
    public void addSuperType(ObjectType supertype) {
    }

//    @Override
//    public void deobjectify() throws ChangeNotAllowedException {
//    }
    @Override
    public String makeExpression(Value value) {
        return getTypeExpression().makeExpression((Tuple) value);
    }

    @Override
    public Value parse(List<String> expressionParts, Requirement source)
            throws MismatchException {
        return getFactType().parse(expressionParts, getTypeExpression(), source);
    }

    @Override
    public Value parse(String expression, String separator, Requirement source)
            throws MismatchException {
        if (separator != null){
            throw new MismatchException(getTypeExpression(), "Facttype class expression is always a "
                    + "fact type expression, therefor separator should be null");
        }
        if (!getTypeExpression().isParsable()) {
            throw new NotParsableException(getTypeExpression(), "TE OF " + getName() + " NOT PARSABLE");
        }

        // delegating parsing to facttype, he knows the roles
        return getFactType().parse(expression.trim(), getTypeExpression(), separator,  source);
    }

    @Override
    public void setComparable(boolean comparable) {
    }

    @Override
    public void setValueType(boolean valueType) {
    }
}
