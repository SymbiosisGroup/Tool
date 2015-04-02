/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown;

import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Type;
import equa.meta.objectmodel.Value;
import equa.meta.traceability.ParentElement;
import equa.meta.traceability.Source;

/**
 *
 * @author frankpeeters
 */
public class UnparsableValue extends Value {

    private static final long serialVersionUID = 1L;
    private final ObjectType type;
    private final String text;

    public UnparsableValue(ParentElement parent, Source source, ObjectType objecttype, String text) {
        super(parent, source);
        this.type = objecttype;
        this.text = text;
    }

    @Override
    public ObjectType getType() {
        return type;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public String getName() {
        return type.getName() + "_unparsable";
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof UnparsableValue) {
            return ((UnparsableValue) object).text.equals(text);
        } else {
            return false;
        }
    }

}
