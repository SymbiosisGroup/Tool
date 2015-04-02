/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BaseTypeDialog.java
 *
 * Created on 4-nov-2012, 13:58:40
 */
package equa.configurator;

import java.util.List;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author frankpeeters
 */
public class BaseTypePlusDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 1L;

    /**
     * Creates new form BaseTypeDialog
     *
     * @param parent
     */
    public BaseTypePlusDialog(java.awt.Frame parent, boolean modal, String[] types, String basetype) {
        super(parent, modal);
        initComponents();
        cbBaseTypePlus.setModel(new DefaultComboBoxModel(types));
        cbBaseTypePlus.setSelectedItem(basetype);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbBaseTypePlus = new javax.swing.JComboBox();
        btCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(BaseTypePlusDialog.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        cbBaseTypePlus.setName("cbBaseTypePlus"); // NOI18N
        cbBaseTypePlus.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbBaseTypePlusItemStateChanged(evt);
            }
        });

        btCancel.setText(resourceMap.getString("btCancel.text")); // NOI18N
        btCancel.setName("btCancel"); // NOI18N
        btCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(26, 26, 26)
                .add(cbBaseTypePlus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(29, 29, 29)
                .add(btCancel)
                .addContainerGap(122, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(btCancel)
                    .add(cbBaseTypePlus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(102, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cbBaseTypePlusItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbBaseTypePlusItemStateChanged
        setVisible(false);
    }//GEN-LAST:event_cbBaseTypePlusItemStateChanged

    private void btCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btCancelActionPerformed

    public String getBaseTypePlus() {
        if (cbBaseTypePlus.getSelectedItem() != null) {
            return cbBaseTypePlus.getSelectedItem().toString();
        } else {
            return null;
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCancel;
    private javax.swing.JComboBox cbBaseTypePlus;
    // End of variables declaration//GEN-END:variables
}
