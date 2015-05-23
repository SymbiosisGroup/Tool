/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.requirementsGui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import equa.meta.requirements.Requirement;
import equa.meta.requirements.RequirementFilter;

/**
 *
 * @author frankpeeters
 */
public class FilterTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private final String tableName;
    private List<RequirementFilter> filters;
    private Boolean[] selected;
    private final RequirementConfigurator requirementConfigurator;
    private int selectedItems;

    public FilterTableModel(RequirementConfigurator requirementConfigurator, String tableName, List<RequirementFilter> filters) {
        this.requirementConfigurator = requirementConfigurator;
        this.tableName = tableName;
        this.filters = filters;
        initSelected(filters.size());
    }

    @Override
    public int getRowCount() {
        return filters.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 0;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column == 0) {
            return Boolean.class;
        } else {
            return RequirementFilter.class;
        }
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "";
        } else {
            return tableName;
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) {
            return selected[row];
        } else {
            return filters.get(row);
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        if (column == 0) {
            Boolean selection = (Boolean) value;
            if (selection.equals(selected[row])) {
                return;
            }
            if (selection) {
                selectedItems++;
            } else {
                selectedItems--;
            }
            selected[row] = selection;
            fireTableCellUpdated(row, column);
            requirementConfigurator.refresh();
        }
    }

    private void initSelected(int size) {
        selected = new Boolean[size];
        for (int i = 0; i < size; i++) {
            selected[i] = false;
        }
        selectedItems = 0;
    }

    public boolean accepts(Requirement req) {
        if (selectedItems == 0) {
            //  none of the checkboxes are selected, holds as: all checkboxes are selected
            return true;
        }
        for (int i = 0; i < selected.length; i++) {
            if (selected[i] && filters.get(i).acccepts(req)) {
                return true;
            }
        }
        return false;
    }

}
