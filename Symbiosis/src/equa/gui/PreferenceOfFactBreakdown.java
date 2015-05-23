/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.gui;

import equa.gui.PreferenceOfAspect;
import java.awt.Color;

/**
 *
 * @author frankpeeters
 */
public class PreferenceOfFactBreakdown extends PreferenceOfAspect {
    private Color ready;
    private Color editable;
    private Color inherits;
    private Color collection;

    public PreferenceOfFactBreakdown(Color ready, Color editable, Color inherits, Color collection, int fontSize, Color fontColor, Color background) {
        super(fontSize, fontColor, background);
        this.ready = ready;
        this.editable = editable;
        this.inherits = inherits;
        this.collection = collection;
    }
    
    public PreferenceOfFactBreakdown(){
        this.ready = Color.green;
        this.editable = Color.yellow;
        this.inherits = Color.blue;
        this.collection = Color.black;
        setFontSize(16);
    }

    public Color getCollection() {
        return collection;
    }

    public void setCollection(Color collection) {
        this.collection = collection;
    }

    public Color getReady() {
        return ready;
    }

    public void setReady(Color ready) {
        this.ready = ready;
    }

    public Color getEditable() {
        return editable;
    }

    public void setEditable(Color editable) {
        this.editable = editable;
    }

    public Color getInherits() {
        return inherits;
    }

    public void setInherits(Color inherits) {
        this.inherits = inherits;
    }
    
}
