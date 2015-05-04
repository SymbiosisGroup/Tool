/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code;

import java.io.Serializable;

import symbiosis.code.operations.AccessModifier;
import symbiosis.code.operations.CT;
import symbiosis.code.operations.STorCT;
import symbiosis.meta.classrelations.Relation;

/**
 *
 * @author frankpeeters
 */
public class Field implements Comparable<Field>, Serializable {

    private static final long serialVersionUID = 1L;
    private AccessModifier accessModifier;
    private final STorCT type;
    private final String name;
    private boolean classField, isFinal;
    private Relation relation;
    private boolean incr;

    public Relation getRelation() {
        return relation;
    }

    public Field(Relation r) {
        if (r.hasMultipleTarget()) {
            this.type = r.collectionType();
        } else {
            this.type = r.targetType();
        }
        this.name = r.fieldName();
        this.isFinal = (type instanceof CT) || r.isFinal();
        this.relation = r;
        this.accessModifier = AccessModifier.PRIVATE;
        this.incr = false;
    }

    public Field(STorCT type, String name, boolean incr) {
        this.type = type;
        this.name = name;
        this.accessModifier = AccessModifier.PRIVATE;
        this.incr = incr;
    }

    public boolean isAutoIncr() {
        return incr;
    }

    public STorCT getType() {
        return type;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public String getName() {
        return name;
    }

    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    public void setAccessModifier(AccessModifier accessModifier) {
        this.accessModifier = accessModifier;
    }

    public boolean isClassField() {
        return classField;
    }

    public void setClassField(boolean classField) {
        this.classField = classField;
    }

    public IndentedList getCode(Language language, boolean withOrm, boolean isEditable) {
//        if (isEditable && relation != null && !relation.isFinal()) {
//            AccessModifier old = getAccessModifier();
//            setAccessModifier(AccessModifier.PROTECTED);
//            IndentedList result = language.field(this, withOrm);
//            setAccessModifier(old);
//            return result;
//        }
        return language.field(this, withOrm);

    }

    @Override
    public int compareTo(Field o) {
        return name.compareTo(o.name);
    }
}
