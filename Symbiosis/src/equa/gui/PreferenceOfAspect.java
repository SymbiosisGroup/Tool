/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.gui;

import java.awt.Color;
import java.io.Serializable;

/**
 *
 * @author frankpeeters
 */
public class PreferenceOfAspect implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final int DEFAULT_SIZE = 12;
    public static final Color DEFAULT_BACKGROUND = Color.white;
    public static final Color DEFAULT_FOREGROUND = Color.black;
    private int fontSize;
    private Color fontColor;
    private Color background;

    public PreferenceOfAspect(int fontSize, Color fontColor, Color background) {
        this.fontSize = fontSize;
        this.fontColor = fontColor;
        this.background = background;
    }

    public PreferenceOfAspect() {
        this.fontSize = DEFAULT_SIZE;
        this.fontColor = DEFAULT_FOREGROUND;
        this.background = DEFAULT_BACKGROUND;
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public Color getFontColor() {
        return fontColor;
    }

    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
    }

}
