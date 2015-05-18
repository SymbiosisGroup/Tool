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

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.layout.HBox;
import symbiosis.gui.ScreenManager;
import symbiosis.meta.ChangeNotAllowedException;
import symbiosis.meta.requirements.Requirement;
import symbiosis.meta.traceability.ExternalInput;
import symbiosis.meta.traceability.ReviewState;
import symbiosis.project.Project;

/**
 * A ButtonRequirementCell is a cell in a TableView that displays one of the
 * action buttons for a Requirement.
 *
 * @author Jeroen Berkvens
 * @company EQUA
 * @project Symbiosis
 */
public class ButtonsRequirementCell extends TableCell<Requirement, Object> {

    final Button approveButton = new Button("App");
    final Button rejectButton = new Button("Rej");
    final Button removeButton = new Button("Rem");
    final HBox hBox = new HBox();

    private final TableColumn tableColumn;

    /**
     * This is the default constructor for the ButtonRequirement Cell.
     *
     * @param param is the TableColumn where this cell is part of.
     */
    public ButtonsRequirementCell(Object param) {
        //Get Column
        tableColumn = (TableColumn) param;
        //Set Button width and spacing
        approveButton.setMinWidth(50);
        rejectButton.setMinWidth(50);
        removeButton.setMinWidth(50);
        hBox.setSpacing(10);
        //Approve Action
        approveButton.setOnAction((ActionEvent t) -> {
            final TableRow<Requirement> tableRow = getTableRow();
            final Requirement rowItem = tableRow == null ? null : tableRow.getItem();
            if (rowItem != null) {
                rowItem.approve(new ExternalInput("Manually Approved", Project.getProject().getCurrentUser()));
            }
            ScreenManager.getMainScreen().refresh();
        });
        //Reject Action
        rejectButton.setOnAction((ActionEvent t) -> {
            final TableRow<Requirement> tableRow = getTableRow();
            final Requirement rowItem = tableRow == null ? null : tableRow.getItem();
            if (rowItem != null) {
                rowItem.reject(new ExternalInput("Manually Rejected", Project.getProject().getCurrentUser()));
            }
        });
        //Remove Action
        removeButton.setOnAction((ActionEvent t) -> {
            final TableRow<Requirement> tableRow = getTableRow();
            final Requirement rowItem = tableRow == null ? null : tableRow.getItem();
            if (rowItem != null) {
                rowItem.remove();
            }
        });
    }

    /**
     * The override of the GUI update function. In this function it returns the
     * correct buttons.
     *
     * @param object is used by the super function.
     * @param empty is true if the cell is empty.
     */
    @Override
    protected void updateItem(Object object, boolean empty) {
        //Super function
        super.updateItem(object, empty);
        if (!empty) {
            //Get ReviewState
            final TableRow<Requirement> tableRow = getTableRow();
            final Requirement rowItem = tableRow == null ? null : tableRow.getItem();
            ReviewState reviewState = rowItem == null ? null : rowItem.getReviewState();
            if (reviewState != null) {
                //Set Buttons
                if (reviewState == ReviewState.ADDED) {
                    hBox.getChildren().clear();
                    hBox.getChildren().add(approveButton);
                    hBox.getChildren().add(rejectButton);
                } else if (reviewState == ReviewState.APPROVED) {
                    hBox.getChildren().clear();
                    hBox.getChildren().add(removeButton);
                }
            }
            setGraphic(hBox);
        } else {
            setGraphic(null);
        }
    }
}
