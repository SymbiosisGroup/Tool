package equa.meta.objectmodel;

import java.util.ArrayList;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import equa.meta.requirements.FactRequirement;
import equa.meta.requirements.Requirement;

/**
 *
 * @author FrankP
 */
@Entity
public class UnidentifiedObjectType extends ObjectType {

    private static final long serialVersionUID = 1L;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private UnidentifiedObject unidentifiedObject;

    public UnidentifiedObjectType() {
    }

    /**
     *
     * @param parent
     */
    UnidentifiedObjectType(FactType parent) {
        super(parent, ObjectType.UNIDENTIFIED_OBJECTTYPE);
        init(parent, new ArrayList<String>(), null);
        unidentifiedObject = new UnidentifiedObject(this);
    }

    @Override
    public Value parse(String expression, String separator, Requirement source) {
        String trimmed = expression.trim();
        if (trimmed.equals("<" + getName() + ">")) {
            return unidentifiedObject;
        } else {
            return null;
        }
    }

    @Override
    public String makeExpression(Value value) {
        return "<" + getName() + ">";
    }

//    @Override
//    public void deobjectify() {
//        // isn't allowed (importancy of use of pure value facts is unknown)
//    }
}
