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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;

/**
 *
 * @author Jeroen Berkvens
 * @company EQUA
 * @project Symbiosis
 */
public class ButtonsCell extends TableCell<Object, String> {

    final Button approveButton = new Button("App");
    final Button removeButton = new Button("Rem");
    final HBox approveAndRemove = new HBox(approveButton, removeButton);

    private TableColumn tableColumn;

    public ButtonsCell(Object param) {
        tableColumn = (TableColumn) param;
        approveButton.setMinWidth(50);
        removeButton.setMinWidth(50);
        approveAndRemove.setSpacing(10);
        approveButton.setOnAction((ActionEvent t) -> {
            approveButton.setText("APP");
        });
        removeButton.setOnAction((ActionEvent t) -> {
            removeButton.setText("REM");
        });
    }

    @Override
    protected void updateItem(String t, boolean empty) {
        super.updateItem(t, empty);
        if (!empty) {
            //TODO This is done by test data!
            if (tableColumn.getCellData(this.getIndex()).equals("test")) {
                setGraphic(approveButton);
            } else if (tableColumn.getCellData(this.getIndex()).equals("Bla")) {
                setGraphic(approveAndRemove);
            } else {
                setGraphic(removeButton);
            }
        }
    }
}
