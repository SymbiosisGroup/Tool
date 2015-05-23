package equa.requirementsGui;

import equa.meta.ChangeNotAllowedException;
import java.util.ArrayList;

import equa.meta.requirements.Requirement;
import equa.meta.traceability.*;
import equa.project.Project;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class RequirementsTableModel extends AbstractTableModel {

    public static final int COLUMN_NUMBER = 0;
    public static final int COLUMN_NAME = 1;
    private final String[] columnNames = new String[]{"Rlzd", "Name", "Type", "RevSt", "Appr", "Text"};
    private final ArrayList<Requirement> requirements;
    private final Project project;

    public RequirementsTableModel(ArrayList<Requirement> reqs, Project project) {
        requirements = reqs;
        this.project = project;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        Class clazz = String.class;
        switch (columnIndex) {
            case 0:
                clazz = Boolean.class;
                break;
            case 1:
                clazz = String.class;
                break;
            case 2:
                clazz = String.class;
                break;
            case 3:
                clazz = String.class;
                break;
            case 4:
                clazz = Boolean.class;
                break;
            case 5:
                clazz = String.class;
                break;
        }
        return clazz;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column != 4) {
            return false;
        }
        Requirement req = requirements.get(row);
        return req.isApprovable(project.getCurrentUser()) && req.getReviewState().needsApproval();
    }

    @Override
    public int getRowCount() {
        if (requirements == null) {
            return 0;
        }
        return requirements.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Requirement req = requirements.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return req.isRealized();
            case 1:
                return req.getName();
            case 2:
                return req.getReqType();
            case 3:
                return req.getReviewState().toString();
            case 4:
                return !req.getReviewState().needsApproval();
            case 5:
                return req.getText();
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        if (aValue instanceof Boolean && column == 4) {
            Requirement req = requirements.get(row);
            try {
                req.getReviewState().approve(new ExternalInput("", project.getCurrentUser()));
            } catch (ChangeNotAllowedException ex) {
                Logger.getLogger(RequirementsTableModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


    public Requirement getRequirementAt(int rowIndex) {
        try {
            return requirements.get(rowIndex);
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
