/*
 * Copyright (C) 2015 Jeroen Berkvens
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package symbiosis.gui.resources;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.layout.HBox;
import symbiosis.meta.requirements.Requirement;
import symbiosis.meta.requirements.RequirementFilter;

/**
 * A CheckBoxRequirementFilterCell is a cell in a TableView that displays one of
 * the action buttons for a RequirementFilter.
 *
 * @author Jeroen Berkvens
 * @company EQUA
 * @project Symbiosis
 */
public class CheckBoxRequirementFilterCell extends TableCell<RequirementFilter, Object> {

    final CheckBox selected = new CheckBox();

    private final TableColumn tableColumn;

    /**
     * This is the default constructor for the CheckBoxRequirementFilterCell.
     *
     * @param param is the TableColumn where this cell is part of.
     */
    public CheckBoxRequirementFilterCell(Object param) {
        tableColumn = (TableColumn) param;
        tableColumn.setSortable(false);
    }

    /**
     * The override of the GUI update function. In this function it returns the
     * CheckBox.
     *
     * @param object is used by the super function.
     * @param empty is true if the cell is empty.
     */
    @Override
    protected void updateItem(Object object, boolean empty) {
        //Super function
        super.updateItem(object, empty);
        if (!empty) {
            //Get RequirementFilter
            final TableRow<RequirementFilter> tableRow = getTableRow();
            final RequirementFilter rowItem = tableRow == null ? null : tableRow.getItem();
            if (rowItem != null) {
                //Bind CheckBoxSelectedProperty to RequirementFilterSelectedProperty
                rowItem.bindSelectedProperty(selected.selectedProperty());
            }
            setGraphic(selected);
        }
    }
}
