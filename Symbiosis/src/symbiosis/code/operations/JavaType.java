/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code.operations;

import symbiosis.meta.objectmodel.Type;
import java.util.List;

/**
 *
 * @author frankpeeters
 */
public class JavaType implements STorCT {

    private String name;
    
    public JavaType(String name){
        this.name = name;
    }
    @Override
    public List<Param> transformToBaseTypes(Param param) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        return name;}

    @Override
    public boolean isPureFactType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int compareTo(Type o) {
       if (o instanceof JavaType){
           JavaType other = (JavaType) o;
           return name.compareTo(other.name);
       }
       return 1;
    }
    
}
