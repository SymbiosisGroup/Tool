/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.requirementsGui;

import equa.meta.objectmodel.RoleEvent;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectRole;
import equa.meta.objectmodel.ObjectType;
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
public class EventRuleAssignmentDialog extends javax.swing.JDialog {

    public static final String ALWAYS = "--Always--";
    private final ObjectModel om;
    private ObjectRole responsibleRole;

    /**
     * Creates new form RuleAssignmentDialog
     *
     * @param parent
     * @param modal
     * @param om
     */
    public EventRuleAssignmentDialog(java.awt.Frame parent, boolean modal, ObjectModel om) {
        super(parent, modal);
        this.om = om;
        initComponents();
        initFactTypes();
        initControls();
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    private void initControls() {
        cbExtending.setVisible(false);
        cbExtending.setEnabled(false);
        cbRemoving.setVisible(false);
        cbRemoving.setEnabled(false);
        cbUpdating.setVisible(false);
        cbUpdating.setEnabled(false);

        responsibleRole = null;
        btRemove.setEnabled(false);

        if (null == listFactTypes.getSelectedValue()) {
            listBooleanFactTypes.setModel(new DefaultListModel());
            pnEventCondition.setEnabled(false);
        } else {
            pnEventCondition.setEnabled(true);
            FactType ft = om.getFactType((String) listFactTypes.getSelectedValue());

            FactTypeListModel model = new FactTypeListModel();
            List<String> unaryFactTypes = new ArrayList<>();
            responsibleRole = ft.getResponsibleRole();
            if (responsibleRole != null) {
                ObjectType ot = (ObjectType) responsibleRole.getSubstitutionType();
                unaryFactTypes.add(ALWAYS);
                for (FactType bft : ot.getBooleanFactTypes()) {
                    unaryFactTypes.add(bft.getName());
                }
                model.setFactTypes(unaryFactTypes);
                listBooleanFactTypes.setModel(model);
                if (responsibleRole.isMultiple()) {
                    if (responsibleRole.isAddable() || responsibleRole.isInsertable()) {
                        cbExtending.setEnabled(true);
                        cbExtending.setVisible(true);
                    }
                    if (responsibleRole.isRemovable()) {
                        cbRemoving.setEnabled(true);
                        cbRemoving.setVisible(true);
                    }
                } else {
                    if (responsibleRole.isSettable() || responsibleRole.isAdjustable()) {
                        cbUpdating.setEnabled(true);
                        cbUpdating.setVisible(true);
                    }
                    if (!responsibleRole.isMandatory() && responsibleRole.isRemovable()) {
                        cbRemoving.setEnabled(true);
                    }
                }
                if (unaryFactTypes.size() == 1) {
                    listBooleanFactTypes.setSelectedIndex(0);
                }
            }
        }
    }

    private void initFactTypes() {
        FactTypeListModel model = new FactTypeListModel();
        List<String> factTypes = new ArrayList<>();
        for (FactType ft : om.getFactTypes()) {
            if (ft.isMutable()) {
                factTypes.add(ft.getName());
            }
        }
        model.setFactTypes(factTypes);
        listFactTypes.setModel(model);
    }

    public FactType getEventCondition() {
        String ftName = (String) listBooleanFactTypes.getSelectedValue();
        if (ftName != null) {
            if (ftName.equals(ALWAYS)) {
                return null;
            } else {
                return om.getFactType(ftName);
            }
        } else {
            return null;
        }
    }

    public FactType getEventSource() {
        String ftName = (String) listFactTypes.getSelectedValue();
        if (ftName != null) {
            return om.getFactType(ftName);
        } else {
            return null;
        }
    }

    public boolean isNegated() {
        return cbNegation.isSelected();
    }

    public boolean checkExtendOrUpdate() {
        return cbExtending.isSelected();
    }

    public boolean checkRemove() {
        return cbRemoving.isSelected();
    }

    public ObjectRole getResponsibleRole() {
        return responsibleRole;
    }

    public String getEventHandler() {
        return tfEventHandler.getText().trim();
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
        pnEventSource = new javax.swing.JPanel();
        cbExtending = new javax.swing.JCheckBox();
        cbRemoving = new javax.swing.JCheckBox();
        cbUpdating = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        listFactTypes = new javax.swing.JList();
        pnEventCondition = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        listBooleanFactTypes = new javax.swing.JList();
        cbNegation = new javax.swing.JCheckBox();
        pnButtons = new javax.swing.JPanel();
        btOk = new javax.swing.JButton();
        btCancel = new javax.swing.JButton();
        btRemove = new javax.swing.JButton();
        pnEventHandler = new javax.swing.JPanel();
        tfEventHandler = new javax.swing.JTextField();
        lbEventHandler = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnEvent.setName("pnEvent"); // NOI18N
        pnEvent.setLayout(new java.awt.BorderLayout());

        pnEventSource.setBorder(javax.swing.BorderFactory.createTitledBorder("Select Event Source"));
        pnEventSource.setMinimumSize(new java.awt.Dimension(100, 100));
        pnEventSource.setName("pnEventSource"); // NOI18N
        pnEventSource.setPreferredSize(new java.awt.Dimension(200, 260));

        cbExtending.setText("add/insert");
        cbExtending.setName("cbExtending"); // NOI18N
        cbExtending.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbExtendingActionPerformed(evt);
            }
        });

        cbRemoving.setText("rem");
        cbRemoving.setName("cbRemoving"); // NOI18N
        cbRemoving.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbRemovingActionPerformed(evt);
            }
        });

        cbUpdating.setText("set/adj");
        cbUpdating.setName("cbUpdating"); // NOI18N

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

        javax.swing.GroupLayout pnEventSourceLayout = new javax.swing.GroupLayout(pnEventSource);
        pnEventSource.setLayout(pnEventSourceLayout);
        pnEventSourceLayout.setHorizontalGroup(
            pnEventSourceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnEventSourceLayout.createSequentialGroup()
                .addGroup(pnEventSourceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnEventSourceLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnEventSourceLayout.createSequentialGroup()
                        .addGroup(pnEventSourceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cbUpdating, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnEventSourceLayout.createSequentialGroup()
                                .addComponent(cbExtending)
                                .addGap(18, 18, 18)
                                .addComponent(cbRemoving)))
                        .addGap(0, 20, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnEventSourceLayout.setVerticalGroup(
            pnEventSourceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnEventSourceLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbUpdating)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnEventSourceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbExtending)
                    .addComponent(cbRemoving)))
        );

        pnEvent.add(pnEventSource, java.awt.BorderLayout.CENTER);

        pnEventCondition.setBorder(javax.swing.BorderFactory.createTitledBorder("Select Event Condition"));
        pnEventCondition.setMinimumSize(new java.awt.Dimension(100, 120));
        pnEventCondition.setName("pnEventCondition"); // NOI18N
        pnEventCondition.setPreferredSize(new java.awt.Dimension(200, 260));

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        listBooleanFactTypes.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        listBooleanFactTypes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listBooleanFactTypes.setName("listBooleanFactTypes"); // NOI18N
        listBooleanFactTypes.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listBooleanFactTypesValueChanged(evt);
            }
        });
        jScrollPane4.setViewportView(listBooleanFactTypes);

        cbNegation.setText("negated");
        cbNegation.setName("cbNegation"); // NOI18N

        javax.swing.GroupLayout pnEventConditionLayout = new javax.swing.GroupLayout(pnEventCondition);
        pnEventCondition.setLayout(pnEventConditionLayout);
        pnEventConditionLayout.setHorizontalGroup(
            pnEventConditionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnEventConditionLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cbNegation)
                .addContainerGap())
        );
        pnEventConditionLayout.setVerticalGroup(
            pnEventConditionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnEventConditionLayout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addComponent(cbNegation))
        );

        pnEvent.add(pnEventCondition, java.awt.BorderLayout.EAST);

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 144, Short.MAX_VALUE)
                .addComponent(btOk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btCancel))
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

        pnEventHandler.setName("pnEventHandler"); // NOI18N
        pnEventHandler.setPreferredSize(new java.awt.Dimension(220, 50));

        tfEventHandler.setName("tfEventHandler"); // NOI18N

        lbEventHandler.setText("Name of the Event Handler:");
        lbEventHandler.setName("lbEventHandler"); // NOI18N

        javax.swing.GroupLayout pnEventHandlerLayout = new javax.swing.GroupLayout(pnEventHandler);
        pnEventHandler.setLayout(pnEventHandlerLayout);
        pnEventHandlerLayout.setHorizontalGroup(
            pnEventHandlerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnEventHandlerLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lbEventHandler)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tfEventHandler, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pnEventHandlerLayout.setVerticalGroup(
            pnEventHandlerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnEventHandlerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnEventHandlerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfEventHandler, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbEventHandler, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(pnEventHandler, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void listFactTypesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listFactTypesValueChanged
        initControls();
    }//GEN-LAST:event_listFactTypesValueChanged

    private void btCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelActionPerformed
        listBooleanFactTypes.getSelectionModel().clearSelection();
        setVisible(false);
    }//GEN-LAST:event_btCancelActionPerformed

    private void btOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btOkActionPerformed

        if (listFactTypes.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(getParent(), "Please select event source (fact type).");
            return;
        }

//        if (listBooleanFactTypes.getModel().getSize() == 1) {
//            String ftName = (String) listFactTypes.getSelectedValue();
//
//            FactType ft = om.getFactType(ftName);
//            Role role = ft.getResponsibleRole();
//            if (role != null) {
//                JOptionPane.showMessageDialog(getParent(), "Perhaps you need to "
//                    + "enter an example fact "
//                    + "about the event condition of an object of "
//                    + role.getSubstitutionType().getName());
//            }
//        }
        if (listBooleanFactTypes.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(getParent(), "Please select the event condition.");
            return;
        }

        if (tfEventHandler.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(getParent(), "Please enter a name of the event handler.");
            return;
        }

        if (!cbExtending.isSelected() && !cbRemoving.isSelected() && !cbUpdating.isSelected()) {
            JOptionPane.showMessageDialog(getParent(), "Please select one (or more) checkboxes in the left panel about the event source.");
            return;
        }

        setVisible(false);
    }//GEN-LAST:event_btOkActionPerformed

    private void cbExtendingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbExtendingActionPerformed

    }//GEN-LAST:event_cbExtendingActionPerformed

    private void cbRemovingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbRemovingActionPerformed

    }//GEN-LAST:event_cbRemovingActionPerformed

    private void btRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRemoveActionPerformed

        if (listFactTypes.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(getParent(), "Please select event source (fact type).");
            return;
        }
        if (listBooleanFactTypes.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(getParent(), "Please select the event condition.");
            return;
        }

        String name = (String) listBooleanFactTypes.getSelectedValue();
        FactType condition;
        if (name.equals(ALWAYS)) {
            condition = null;
        } else {
            condition = om.getFactType(name);
        }

        boolean removed = responsibleRole.removeEvent(condition, cbNegation.isSelected(), cbExtending.isSelected(),
            cbRemoving.isSelected(), cbUpdating.isSelected());

        btRemove.setEnabled(!removed);
    }//GEN-LAST:event_btRemoveActionPerformed

    private void listBooleanFactTypesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listBooleanFactTypesValueChanged

        if (responsibleRole == null) {
            return;
        }

        cbNegation.setSelected(false);
        tfEventHandler.setText("");

        String selectedCondition = (String) listBooleanFactTypes.getSelectedValue();
        if (selectedCondition == null) {
            return;
        }

        for (RoleEvent event : responsibleRole.getEvents()) {
            FactType condition = event.getCondition();
            if (condition == null && selectedCondition.equals(ALWAYS)) {
                btRemove.setEnabled(true);
                cbNegation.setSelected(event.isNegation());
                tfEventHandler.setText(event.getNameOfHandler());
                return;
            }
            if (condition != null && condition.getName().equals(selectedCondition)) {
                btRemove.setEnabled(true);
                cbNegation.setSelected(event.isNegation());
                tfEventHandler.setText(event.getNameOfHandler());
                return;
            }

        }

        btRemove.setEnabled(false);
    }//GEN-LAST:event_listBooleanFactTypesValueChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCancel;
    private javax.swing.JButton btOk;
    private javax.swing.JButton btRemove;
    private javax.swing.JCheckBox cbExtending;
    private javax.swing.JCheckBox cbNegation;
    private javax.swing.JCheckBox cbRemoving;
    private javax.swing.JCheckBox cbUpdating;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lbEventHandler;
    private javax.swing.JList listBooleanFactTypes;
    private javax.swing.JList listFactTypes;
    private javax.swing.JPanel pnButtons;
    private javax.swing.JPanel pnEvent;
    private javax.swing.JPanel pnEventCondition;
    private javax.swing.JPanel pnEventHandler;
    private javax.swing.JPanel pnEventSource;
    private javax.swing.JTextField tfEventHandler;
    // End of variables declaration//GEN-END:variables
}
