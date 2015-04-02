/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author frankpeeters
 */
public class ImplFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        return f.getName().endsWith(CodeNames.TEMPLATE + ".java") ||
               f.getName().endsWith(CodeNames.TEMPLATE + ".jav") ||
               f.isDirectory(); 
            
    }

    @Override
    public String getDescription() {
        return "Only files which end with "+ CodeNames.TEMPLATE + ".jav" + "(a) are accepted.";
               
    }
    
}
