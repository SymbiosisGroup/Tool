/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.requirementsGui;

import equa.meta.objectmodel.RoleEvent;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.Initializer;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectRole;
import equa.meta.objectmodel.ObjectType;
import equa.meta.requirements.Requirement;
import equa.meta.requirements.RequirementModel;
import equa.meta.traceability.Category;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author frankpeeters
 */
public class InitializeDialog extends javax.swing.JDialog {

    private final ObjectModel om;
    private ObjectType ot;
    private Requirement rule;

    /**
     * Creates new form RuleAssignmentDialog
     *
     * @param parent
     * @param modal
     * @param om
     */
    public InitializeDialog(java.awt.Frame parent, ObjectModel om, Requirement rule) {
        super(parent, true);
        this.om = om;
        this.rule = rule;
        initComponents();
        btRemove.setEnabled(false);
        initFactTypes();
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Please Select Object Type");
    }

    void initControls() {
        FactType selected = (FactType) listFactTypes.getSelectedValue();
        if (selected != null) {
            btRemove.setEnabled(selected.getObjectType().getInitializer() != null);
        }
    }

    private void initFactTypes() {
        FactTypeListModel model = new FactTypeListModel();
        List<String> factTypes = new ArrayList<>();
        for (FactType ft : om.getFactTypes()) {
            if (ft.isSingleton() || ft.isValueType()) {
                factTypes.add(ft.getName());
            }
        }
        if (factTypes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "The OM contains no suitable object type;"
                + "Only singletons and value types are eligible");
        }
        model.setFactTypes(factTypes);
        listFactTypes.setModel(model);
    }

    public ObjectType getObjectType() {
        String ftName = (String) listFactTypes.getSelectedValue();
        if (ftName != null) {
            return om.getObjectType(ftName);
        } else {
            return null;
        }
    }

    final class FactTypeListModel implements ListModel<String> {

        private List<String> factTypes;
        private final EventListenerList eventListenerList;

        public FactTypeListModel() {
            factTypes = new ArrayList<>();
            eventListenerList = new EventListenerList();
        }

        @Override
        public int getSize() {
            return factTypes.size();
        }

        @Override
        public String getElementAt(int i) {
            if (i < 0 || i >= factTypes.size()) {
                return null;
            } else {
                return factTypes.get(i);
            }
        }

        @Override
        public void addListDataListener(ListDataListener ll) {
            eventListenerList.add(ListDataListener.class, ll);
        }

        @Override
        public void removeListDataListener(ListDataListener ll) {
            eventListenerList.remove(ListDataListener.class, ll);
        }

        public void fireListChanged() {
            EventListener[] listeners = eventListenerList.getListeners(ListDataListener.class);
            for (EventListener l : listeners) {
                ((ListDataListener) l).contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0));
            }
        }

        public void setFactTypes(List<String> ots) {
            factTypes = ots;
            fireListChanged();
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnEvent = new javax.swing.JPanel();
        pnButtons = new javax.swing.JPanel();
        btOk = new javax.swing.JButton();
        btCancel = new javax.swing.JButton();
        btRemove = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        listFactTypes = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnEvent.setName("pnEvent"); // NOI18N
        pnEvent.setLayout(new java.awt.BorderLayout());
        getContentPane().add(pnEvent, java.awt.BorderLayout.NORTH);

        pnButtons.setName("pnButtons"); // NOI18N

        btOk.setText("Ok");
        btOk.setName("btOk"); // NOI18N
        btOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btOkActionPerformed(evt);
            }
        });

        btCancel.setText("Cancel");
        btCancel.setName("btCancel"); // NOI18N
        btCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelActionPerformed(evt);
            }
        });

        btRemove.setText("Remove");
        btRemove.setEnabled(false);
        btRemove.setName("btRemove"); // NOI18N
        btRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnButtonsLayout = new javax.swing.GroupLayout(pnButtons);
        pnButtons.setLayout(pnButtonsLayout);
        pnButtonsLayout.setHorizontalGroup(
            pnButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btRemove)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btOk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btCancel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnButtonsLayout.setVerticalGroup(
            pnButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnButtonsLayout.createSequentialGroup()
                .addGap(0, 6, Short.MAX_VALUE)
                .addGroup(pnButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btCancel)
                    .addComponent(btOk)
                    .addComponent(btRemove)))
        );

        getContentPane().add(pnButtons, java.awt.BorderLayout.SOUTH);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 100));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        listFactTypes.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        listFactTypes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listFactTypes.setMaximumSize(new java.awt.Dimension(200, 300));
        listFactTypes.setMinimumSize(new java.awt.Dimension(100, 100));
        listFactTypes.setName("listFactTypes"); // NOI18N
        listFactTypes.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listFactTypesValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(listFactTypes);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void listFactTypesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listFactTypesValueChanged
        initControls();
    }//GEN-LAST:event_listFactTypesValueChanged

    private void btCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelActionPerformed
        listFactTypes.getSelectionModel().clearSelection();
        setVisible(false);
    }//GEN-LAST:event_btCancelActionPerformed

    private void btOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btOkActionPerformed
        FactType selected = (FactType) listFactTypes.getSelectedValue();
        if (selected != null) {
            JOptionPane.showMessageDialog(getParent(), "Please select object type that needs an initializer.");
            return;
        }
        selected.getObjectType().setInitializer(rule);

        setVisible(false);
    }//GEN-LAST:event_btOkActionPerformed

    private void btRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRemoveActionPerformed
        FactType selected = (FactType) listFactTypes.getSelectedValue();
        selected.getObjectType().removeInitializer();
        btRemove.setEnabled(false);
    }//GEN-LAST:event_btRemoveActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCancel;
    private javax.swing.JButton btOk;
    private javax.swing.JButton btRemove;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList listFactTypes;
    private javax.swing.JPanel pnButtons;
    private javax.swing.JPanel pnEvent;
    // End of variables declaration//GEN-END:variables
}
