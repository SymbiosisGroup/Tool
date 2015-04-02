/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import java.io.Serializable;

import equa.code.IndentedList;
import equa.code.Language;

/**
 *
 * @author frankpeeters
 */
public class Algorithm implements  Serializable {

    private static final long serialVersionUID = 1L;

    private Language language;
    private IndentedList api;
    private IndentedList code;
    private boolean removable;
    

    public Algorithm() {
        language = null;
        api = new IndentedList();
        code = new IndentedList();
        removable = false;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
    
    public IndentedList getAPI(){
        return api;
    }

    public void setAPI(IndentedList api){
        this.api = api;
    }
    
    public boolean isRemovable() {
        return removable;
    }
    
    public boolean isEmpty() {
        return code==null ||  code.isEmpty();
    }

    public void setRemovable(boolean removable) {
        this.removable = removable;
    }

    public IndentedList getCode() {
        return code;
    }

    public void setCode(IndentedList code) {
        this.code = code;
    }

}
