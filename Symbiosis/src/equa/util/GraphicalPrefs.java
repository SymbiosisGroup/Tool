/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.util;

import java.awt.Color;
import java.util.HashMap;

/**
 *
 * @author frankpeeters
 */
public class GraphicalPrefs {

    public static final String[] ASPECTS = {"Requirements", "FactTypes", "Roles",
        "FactTypeOfRoles", "ClassHeader", "ClassOperations"};
    public static final GraphicalPrefs DEFAULT = new GraphicalPrefs();

    private String name;
    private HashMap<String, PreferenceOfAspect> preferences;

    public GraphicalPrefs(String name) {
        this.name = name;
        defaultPrefs();
    }

    public GraphicalPrefs() {
        this("default");
    }

    public void defaultPrefs() {
        preferences = new HashMap<>();
        preferences.put("Requirements", new PreferenceOfAspect());
        preferences.put("FactTypes", new PreferenceOfAspect(PreferenceOfAspect.DEFAULT_SIZE, Color.black, Color.white));
        preferences.put("Roles", new PreferenceOfAspect(PreferenceOfAspect.DEFAULT_SIZE + 2, Color.black, Color.lightGray));
        preferences.put("FactTypeOfRoles", new PreferenceOfAspect(PreferenceOfAspect.DEFAULT_SIZE + 4, Color.white, Color.black));
        preferences.put("ClassHeader", new PreferenceOfAspect(PreferenceOfAspect.DEFAULT_SIZE + 4, Color.blue, Color.white));
        preferences.put("ClassOperations", new PreferenceOfAspect(PreferenceOfAspect.DEFAULT_SIZE, Color.blue, Color.white));
        preferences.put("FactBreakdown", new PreferenceOfFactBreakdown());
    }

    public PreferenceOfAspect getPreference(String aspect) {
        return preferences.get(aspect);
    }

    public void setPreference(String aspect, PreferenceOfAspect pref) {
        preferences.put(aspect, pref);
        if (aspect.equals("Roles")) {
            preferences.put("FactTypeOfRoles", new PreferenceOfAspect(pref.getFontSize() + 2, Color.white, Color.black));
            preferences.put("ClassHeader", new PreferenceOfAspect(pref.getFontSize() + 2, Color.blue, Color.white));
        }
    }

}
