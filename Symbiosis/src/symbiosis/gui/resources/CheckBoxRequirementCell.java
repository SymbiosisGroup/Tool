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

/**
 * A CheckBoxRequirementCell is a cell in a TableView that displays the ready
 * CheckBox of a Requirement.
 *
 * @author Jeroen Berkvens
 * @company EQUA
 * @project Symbiosis
 */
public class CheckBoxRequirementCell extends TableCell<Requirement, Object> {

    final CheckBox ready = new CheckBox();

    private final TableColumn tableColumn;

    /**
     * Default Constructor initializing the default states.
     *
     * @param param the TableComumn this cell is part of.
     */
    public CheckBoxRequirementCell(Object param) {
        tableColumn = (TableColumn) param;
        tableColumn.setSortable(false);
        ready.setDisable(true);
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
            //Get Requirement
            final TableRow<Requirement> tableRow = getTableRow();
            final Requirement rowItem = tableRow == null ? null : tableRow.getItem();
            if (rowItem != null) {
                //Set isRealized
                ready.setSelected(rowItem.isRealized());
            }
            setGraphic(ready);
        } else {
            setGraphic(null);
        }
    }
}
