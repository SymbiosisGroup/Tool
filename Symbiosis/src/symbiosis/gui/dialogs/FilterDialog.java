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
package symbiosis.gui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import symbiosis.gui.wizards.NewProject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import symbiosis.gui.ScreenManager;
import symbiosis.gui.resources.CheckBoxRequirementFilterCell;
import symbiosis.meta.requirements.Requirement;
import symbiosis.meta.requirements.RequirementFilter;

/**
 *
 * @author Jeroen Berkvens
 * @company EQUA
 * @project Symbiosis
 */
public class FilterDialog extends Application {

    List<RequirementFilter> values = new LinkedList<>();

    public void start(List<RequirementFilter> values) {
        this.values.addAll(values);
        start(new Stage());
    }

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);
        TableView<RequirementFilter> filterTableView = new TableView();
        filterTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        //SelectedFilterColumn
        TableColumn selectedFilterColumn = new TableColumn();
        selectedFilterColumn.setCellFactory(new Callback<TableColumn<RequirementFilter, Object>, TableCell<RequirementFilter, Object>>() {
            @Override
            public TableCell<RequirementFilter, Object> call(TableColumn<RequirementFilter, Object> param) {
                return new CheckBoxRequirementFilterCell(param);
            }
        });
        filterTableView.getColumns().add(selectedFilterColumn);
        //FilterNameColumn
        TableColumn filterNameColumn = new TableColumn();
        filterNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("Filter")
        );
        filterTableView.getColumns().add(filterNameColumn);
        filterTableView.getItems().addAll(values);
        vBox.getChildren().add(filterTableView);
        HBox hBox = new HBox();
        hBox.setSpacing(10);
        //SelectAll CheckBox
        CheckBox selectAllCheckBox = new CheckBox("SelectAll");
        selectAllCheckBox.setOnAction((ActionEvent event) -> {
            values.stream().forEach((filter) -> {
                filter.setSelected(selectAllCheckBox.isSelected());
            });
        });
        hBox.getChildren().add(selectAllCheckBox);
        //Apply Button
        Button applyFilterButton = new Button("Apply");
        applyFilterButton.setDefaultButton(true);
        applyFilterButton.setOnAction((ActionEvent event) -> {
            List<RequirementFilter> selectedFilters = new ArrayList<>();
            values.stream().filter((filter) -> (filter.isSelected())).forEach((filter) -> {
                selectedFilters.add(filter);
            });
            ScreenManager.getMainScreen().setFilter(selectedFilters);
            primaryStage.close();
        });
        hBox.getChildren().add(applyFilterButton);
        vBox.getChildren().add(hBox);
        root.getChildren().add(vBox);
        //Scene en Stage Setup
        Scene scene = new Scene(root, 500, 200);
        primaryStage.setTitle("Symbiosis");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
