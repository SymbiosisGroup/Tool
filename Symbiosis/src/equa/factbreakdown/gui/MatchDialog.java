/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MatchDialog.java
 *
 * Created on 14-mei-2011, 11:57:25
 */
package equa.factbreakdown.gui;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.Role;
import equa.meta.objectmodel.TypeExpression;
import equa.meta.traceability.ExternalInput;
import equa.project.ProjectRole;
import java.awt.GridLayout;

/**
 *
 * @author FrankP
 */
public class MatchDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 1L;
    private boolean match;
    private final String originalExpression;
    private final int[] originalSequence;
    private final TypeExpression te;
    private final int size;
    private final List<String> constants;
    private final ConstantMatchPanel head;
    private final boolean objectExpression;
    private final ProjectRole currentUser;

    public MatchDialog(FactType ft, TypeExpression te, String expression,
        int mismatchPosition, boolean objectExpression, ProjectRole currentUser, Frame parent) {
        super(parent, true);
        initComponents();
        match = false;

        this.objectExpression = objectExpression;
        this.currentUser = currentUser;

        originalExpression = expression;

        this.te = te;

        Iterator<String> itConstants = te.constants();
        //Iterator<Role> itRoles = ft.roles();

        size = ft.size();
        constants = new ArrayList<>();

        ConstantMatchPanel pnConstant;
        SubstitutionMatchPanel pnSubstitution;

        String constant = itConstants.next();
        int correctConstantsToGo = mismatchPosition;
        String remainingExpression = expression;
        if (te.isParsable() && correctConstantsToGo > 0) {
            pnConstant = new ConstantMatchPanel(constant, null);
            correctConstantsToGo--;
            remainingExpression = remainingExpression.substring(constant.length());
        } else {
            pnConstant = new ConstantMatchPanel(expression, null);
            remainingExpression = "";
        }

        head = pnConstant;
        constants.add(constant);
        pnExpression.add(pnConstant);

        originalSequence = new int[size];

        for (int i = 0; i < ft.size(); i++) {
            int roleNumber = te.getRoleNumber(i);
            originalSequence[i] = roleNumber;
            Role role = ft.getRole(roleNumber);
            pnSubstitution = new SubstitutionMatchPanel(roleNumber, role.getRoleName(),
                role.getSubstitutionType().getName(), pnConstant);
            if (size > 1) {
                pnSubstitution.setSwapVisible();
            }
            pnExpression.add(pnSubstitution);
            pnConstant.setNext(pnSubstitution);
            constant = itConstants.next();
            if (te.isParsable() && correctConstantsToGo > 0) {
                int j = remainingExpression.indexOf(constant);
                correctConstantsToGo--;
                pnSubstitution.appendFrontEnd(remainingExpression.substring(0, j));
                pnConstant = new ConstantMatchPanel(constant, pnSubstitution);
                remainingExpression = remainingExpression.substring(j + constant.length());
            } else {
                pnSubstitution.appendFrontEnd(remainingExpression);
                remainingExpression = "";
                pnConstant = new ConstantMatchPanel("", pnSubstitution);
            }

            constants.add(constant);
            pnSubstitution.setNext(pnConstant);
            pnExpression.add(pnSubstitution);
            pnExpression.add(pnConstant);

        }
        pnExpression.setLayout(new GridLayout(constants.size() * 2 - 1, 1));
        getContentPane().validate();

        setSize(500, 100 + (2 * size + 1) * head.getHeight());
        setTitle(ft.getName());
    }

    public boolean match() {
        return match;
    }

    public boolean areRoleNamesChanged() {
        SubstitutionMatchPanel panel = (SubstitutionMatchPanel) head.getNext();
        while (panel != null) {
            if (panel.isRoleNameChanged()) {
                return true;
            }
            panel = (SubstitutionMatchPanel) panel.getNext().getNext();
        }
        return false;
    }

    /**
     *
     * @return null if constants didn't change, else the changed constants
     */
    public List<String> getConstants() {
        ArrayList<String> newConstants = new ArrayList<>();
        boolean changed = false;
        MatchPanel panel = head;
        String newConstant = panel.getText();
        if (objectExpression && !newConstant.isEmpty()) {
            newConstant = newConstant.substring(0, 1).toLowerCase()
                + newConstant.substring(1);
        }
        newConstants.add(newConstant);
        if (!newConstant.equals(constants.get(0))) {
            changed = true;
        }
        panel = panel.getNext();
        int i = 1;
        while (panel != null) {
            panel = panel.getNext();
            newConstant = panel.getText();
            newConstants.add(newConstant);
            if (!newConstant.equals(constants.get(i))) {
                changed = true;
            }
            panel = panel.getNext();
            i++;
        }
        if (changed) {
            return newConstants;
        } else {
            return null;
        }
    }

    /**
     *
     * @return the rolenames
     */
    public List<String> getRoleNames() {
        List<String> roleNames = new ArrayList<>();
        SubstitutionMatchPanel panel = (SubstitutionMatchPanel) head.getNext();
        while (panel != null) {
            roleNames.add(panel.getRoleName());
            panel = (SubstitutionMatchPanel) panel.getNext().getNext();
        }
        return roleNames;
    }

    /**
     *
     * @return the sequence in which the roles are used in respect to the
     * concerning type expression
     */
    public List<Integer> getSubstitutionSequence() {
        List<Integer> substitutionSequence = new ArrayList<>(size);
        SubstitutionMatchPanel panel = (SubstitutionMatchPanel) head.getNext();
        while (panel != null) {
            substitutionSequence.add(panel.getRoleNumber());
            panel = (SubstitutionMatchPanel) panel.getNext().getNext();

        }
        return substitutionSequence;
    }

    public String getExpression() {

        StringBuilder expression = new StringBuilder();
        MatchPanel panel = head;
        expression.append(panel.getText());

        while (panel.getNext() != null) {
            panel = panel.getNext();
            expression.append(panel.getText());
        }
        return expression.toString().trim();

    }

    public List<String> getSubstitutionStrings() {
        List<String> substitutionParts = new ArrayList<>();
        MatchPanel panel = head;

        while (panel.getNext() != null) {
            panel = panel.getNext();
            substitutionParts.add(panel.getText());
            panel = panel.getNext();
        }
        return substitutionParts;
    }

    public List<String> getExpressionParts() {
        List<String> expressionParts = new ArrayList<>();
        MatchPanel panel = head;
        expressionParts.add(panel.getText());
        while (panel.getNext() != null) {
            panel = panel.getNext();
            expressionParts.add(panel.getText());
        }
        return expressionParts;
    }

    public void setSubstitutionStrings(List<String> substitutionStrings) {

        MatchPanel panel = head.getNext();

        for (int index = 0; index < substitutionStrings.size(); index++) {
            ((SubstitutionMatchPanel) panel).setText(substitutionStrings.get(index));
            panel = panel.getNext();
            panel = panel.getNext();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnTypeExpression = new javax.swing.JPanel();
        pnExpression = new javax.swing.JPanel();
        btCancel = new javax.swing.JButton();
        btOk = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(MatchDialog.class);
        pnTypeExpression.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnTypeExpression.border.title"))); // NOI18N
        pnTypeExpression.setName("pnTypeExpression"); // NOI18N

        pnExpression.setBackground(resourceMap.getColor("pnExpression.background")); // NOI18N
        pnExpression.setName("pnExpression"); // NOI18N
        pnExpression.setPreferredSize(new java.awt.Dimension(350, 200));

        btCancel.setText(resourceMap.getString("btCancel.text")); // NOI18N
        btCancel.setName("btCancel"); // NOI18N
        btCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelActionPerformed(evt);
            }
        });

        btOk.setText(resourceMap.getString("btOk.text")); // NOI18N
        btOk.setToolTipText(resourceMap.getString("btOk.toolTipText")); // NOI18N
        btOk.setName("btOk"); // NOI18N
        btOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btOkActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnTypeExpressionLayout = new javax.swing.GroupLayout(pnTypeExpression);
        pnTypeExpression.setLayout(pnTypeExpressionLayout);
        pnTypeExpressionLayout.setHorizontalGroup(
            pnTypeExpressionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnTypeExpressionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnTypeExpressionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnExpression, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
                    .addGroup(pnTypeExpressionLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btOk)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btCancel)))
                .addContainerGap())
        );
        pnTypeExpressionLayout.setVerticalGroup(
            pnTypeExpressionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnTypeExpressionLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(pnExpression, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnTypeExpressionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btOk)
                    .addComponent(btCancel)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnTypeExpression, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnTypeExpression, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(37, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelActionPerformed
        match = false;
        setVisible(false);
    }//GEN-LAST:event_btCancelActionPerformed

    private void btOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btOkActionPerformed
        /*if (checkMatchingConstants())*/ {
            try {
                te.setRoleNumbers(getSubstitutionSequence());
                te.setRoleNames(getRoleNames());
                List<String> newConstants = getConstants();
                if (newConstants != null) {
                    ExternalInput input = new ExternalInput("modification caused by textual mismatch with other fact", currentUser);
                    te.setConstants(newConstants, input);
                }

                System.out.println("Match " + te.getParent().getName() + " : " + te.toString());
                match = true;
                setVisible(false);

            } catch (DuplicateException | ChangeNotAllowedException ex) {
                JOptionPane.showMessageDialog(getParent(), ex.getMessage());
            }
        }
    }//GEN-LAST:event_btOkActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCancel;
    private javax.swing.JButton btOk;
    private javax.swing.JPanel pnExpression;
    private javax.swing.JPanel pnTypeExpression;
    // End of variables declaration//GEN-END:variables

    boolean expressionChanged() {
        return !getExpression().equals(originalExpression);
    }

//    private boolean checkMatchingConstants() {
//        ConstantMatchPanel panel = head;
//        boolean allRight = true;
//        while (panel != null) {
//
//            if (panel.getNext() == null) {
//                panel = null;
//            } else {
//                panel = (ConstantMatchPanel) panel.getNext().getNext();
//            }
//        }
//        return allRight;
//    }
}
