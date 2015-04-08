/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import java.io.Serializable;

import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.traceability.ModelElement;
import equa.meta.traceability.ParentElement;
import equa.meta.traceability.Source;

/**
 *
 * @author frankpeeters
 */
public class Algorithm extends ModelElement implements Serializable {

    private static final long serialVersionUID = 1L;

    private Language language;
    private IndentedList api;
    private IndentedList code;
    private boolean removable;

    public Algorithm(ParentElement parent, Source source, Language l) {
        super(parent, source);
        language = l;
        api = new IndentedList();
        code = new IndentedList();
        removable = true;
    }

    public Language getLanguage() {
        return language;
    }

    public IndentedList getAPI() {
        return api;
    }

    public void setAPI(IndentedList api) {
        this.api = api;
    }

    public boolean isRemovable() {
        return removable;
    }

    public boolean isEmpty() {
        return code == null || code.isEmpty();
    }

    public void setRemovable(boolean removable) {
        this.removable = removable;
    }

    public IndentedList getCode() {
        return code;
    }

    public void setCode(IndentedList code, Source source) {
        Source oldSource = sources().get(0);
        addSource(source);
        removeSource(oldSource);
        this.code = code;
    }

    @Override
    public String getName() {
        return "algorithm of " + getParent().getName();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object instanceof Algorithm) {
            Algorithm other = (Algorithm) object;
            return language.equals(other.language)
                && code.equals(other.code);
        } else {
            return false;
        }
    }

    @Override
    public boolean isManuallyCreated() {
        return true;
    }

}
