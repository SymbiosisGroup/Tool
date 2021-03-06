/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SupertypeDialog.java
 *
 * Created on 5-jan-2012, 16:08:11
 */
package equa.configurator;

import java.util.Set;

import javax.swing.JOptionPane;

/**
 *
 * @author frankpeeters
 */
public class SupertypeDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 1L;

    /**
     * Creates new form SupertypeDialog
     */
    public SupertypeDialog(java.awt.Frame parent, Set<String> subtypeNames) {
        super(parent, true);
        if (subtypeNames.isEmpty()) {
            setVisible(false);
        }
        initComponents();
        for (String subtypeName : subtypeNames) {
            comboSubtypes.addItem(subtypeName);
        }
        tfSupertype.setText("");
    }

    public String getSupertypeName() {
        if (tfSupertype.getText().isEmpty()) {
            return null;
        } else {
            return tfSupertype.getText();
        }
    }

    public String getSubtypeName() {
        return (String) comboSubtypes.getSelectedItem();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btOK = new javax.swing.JButton();
        btCancel = new javax.swing.JButton();
        pnSupertype = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tfSupertype = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        comboSubtypes = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(SupertypeDialog.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N
        setResizable(false);

        btOK.setText(resourceMap.getString("btOK.text")); // NOI18N
        btOK.setToolTipText(resourceMap.getString("btOK.toolTipText")); // NOI18N
        btOK.setName("btOK"); // NOI18N
        btOK.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                btOKActionPerformed(evt);
            }
        });

        btCancel.setText(resourceMap.getString("btCancel.text")); // NOI18N
        btCancel.setToolTipText(resourceMap.getString("btCancel.toolTipText")); // NOI18N
        btCancel.setName("btCancel"); // NOI18N
        btCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelActionPerformed(evt);
            }
        });

        pnSupertype.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnSupertype.border.title"))); // NOI18N
        pnSupertype.setName("pnSupertype"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        tfSupertype.setText(resourceMap.getString("tfSupertype.text")); // NOI18N
        tfSupertype.setName("tfSupertype"); // NOI18N
        tfSupertype.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfSupertypeActionPerformed(evt);
            }
        });

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        comboSubtypes.setName("comboSubtypes"); // NOI18N

        org.jdesktop.layout.GroupLayout pnSupertypeLayout = new org.jdesktop.layout.GroupLayout(pnSupertype);
        pnSupertype.setLayout(pnSupertypeLayout);
        pnSupertypeLayout.setHorizontalGroup(
            pnSupertypeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnSupertypeLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnSupertypeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pnSupertypeLayout.createSequentialGroup()
                        .add(jLabel1)
                        .addContainerGap(91, Short.MAX_VALUE))
                    .add(pnSupertypeLayout.createSequentialGroup()
                        .add(jLabel2)
                        .addContainerGap(37, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, pnSupertypeLayout.createSequentialGroup()
                        .add(pnSupertypeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, comboSubtypes, 0, 200, Short.MAX_VALUE)
                            .add(tfSupertype, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        pnSupertypeLayout.setVerticalGroup(
            pnSupertypeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnSupertypeLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tfSupertype, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(comboSubtypes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(124, 124, 124)
                        .add(btOK)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btCancel))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(pnSupertype, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(pnSupertype, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btCancel)
                    .add(btOK))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btOKActionPerformed
        if (tfSupertype.getText().isEmpty() || comboSubtypes.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(getOwner(), "Please enter a type name");
        } else {
            setVisible(false);
        }
    }//GEN-LAST:event_btOKActionPerformed

    private void btCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelActionPerformed
        tfSupertype.setText("");
        setVisible(false);
    }//GEN-LAST:event_btCancelActionPerformed

    private void tfSupertypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfSupertypeActionPerformed
        btOKActionPerformed(evt);
    }//GEN-LAST:event_tfSupertypeActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCancel;
    private javax.swing.JButton btOK;
    private javax.swing.JComboBox<String> comboSubtypes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel pnSupertype;
    private javax.swing.JTextField tfSupertype;
    // End of variables declaration//GEN-END:variables
}
