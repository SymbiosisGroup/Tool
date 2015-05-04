package symbiosis.meta.objectmodel;

import java.io.Serializable;

import symbiosis.meta.traceability.ModelElement;
import symbiosis.meta.traceability.ParentElement;
import symbiosis.meta.traceability.Source;

public abstract class Value extends ModelElement implements Serializable, Comparable<Value> {

    private static final long serialVersionUID = 1L;

    public Value() {
    }

    public Value(ParentElement parent, Source source) {
        super(parent, source);
    }

    /**
     *
     * @return the type of this value
     */
    public abstract Type getType();

    @Override
    public int compareTo(Value o) {
        return this.toString().compareTo(o.toString());
    }

    @Override
    public boolean isManuallyCreated() {
        return false;
    }

}
