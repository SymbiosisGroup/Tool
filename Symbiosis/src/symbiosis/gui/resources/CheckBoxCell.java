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
 *
 * @author Jeroen Berkvens
 * @company EQUA
 * @project Symbiosis
 */
public class CheckBoxCell extends TableCell<Requirement, Object> {

    final CheckBox ready = new CheckBox();

    private final TableColumn tableColumn;

    public CheckBoxCell(Object param) {
        tableColumn = (TableColumn) param;
        tableColumn.setSortable(false);
        ready.setDisable(true);
    }

    @Override
    protected void updateItem(Object object, boolean empty) {
        super.updateItem(object, empty);
        if (!empty) {
            final TableRow<Requirement> tableRow = getTableRow();
            final Requirement rowItem = tableRow == null ? null : tableRow.getItem();
            if (rowItem != null) {
                ready.setSelected(rowItem.isRealized());
            }
            setGraphic(ready);
        }
    }
}
