/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown.gui;

import java.awt.Container;
import javax.swing.JPanel;

/**
 *
 * @author FrankP
 */
public abstract class MatchPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    abstract MatchPanel getNext();

    abstract MatchPanel getPrevious();

    abstract void appendFrontEnd(String toMove);

    abstract void appendBackEnd(String toMove);

    abstract String getText();

    void refreshLayout() {
        Container parent = getParent();
        if (parent != null) {
            parent = parent.getParent();
            if (parent != null) {
                getParent().getParent().setVisible(false);
                getParent().getParent().setVisible(true);
            }
        }
    }
}
