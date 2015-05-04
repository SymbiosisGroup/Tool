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
package symbiosis.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.google.jhsheets.filtered.FilteredTableView;
import symbiosis.gui.dialogs.AddRequirementDialog;
import symbiosis.gui.dialogs.FilterDialog;
import symbiosis.gui.resources.ButtonsCell;
import symbiosis.meta.requirements.ActionRequirement;
import symbiosis.meta.requirements.Requirement;
import symbiosis.meta.requirements.RequirementModel;
import symbiosis.meta.traceability.Category;
import symbiosis.meta.traceability.ExternalInput;
import symbiosis.project.Project;

/**
 *
 * @author Jeroen Berkvens
 * @company EQUA
 * @project Symbiosis
 */
public class MainScreen extends Application {

    @FXML
    private TableView requitementsTable;
    @FXML
    private TableColumn readyColumn, nameColumn, typeColumn, stateColumn, reviewColumn, textColumn;
    @FXML
    private ContextMenu readyColumnContextMenu, nameColumnContextMenu, typeColumnContextMenu, stateColumnContextMenu, reviewColumnContextMenu, textColumnContextMenu;
    @FXML
    private MenuItem AddNewRequirementMenuItem;
    @FXML
    private MenuItem filterReadyColumnMenuItem, filterNameColumnMenuItem, filterTypeColumnMenuItem, filterStateColumnMenuItem, filterReviewColumnMenuItem, filterTextColumnMenuItem;

    private ObservableList<Requirement> data = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("resources/MainScreen.fxml"));
        fxmlLoader.setController(this);
        try {
            Parent root = (Parent) fxmlLoader.load();
            Scene scene = new Scene(root, 1200, 600);
            primaryStage.setResizable(true);
            primaryStage.setTitle("Symbiosis");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        SplashScreen flashScreen = new SplashScreen();
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage.getScene().getWindow());
        flashScreen.start(stage);
        testData();
        fillTable();
        setup();
        ScreenManager.setMainScreen(this);
    }

    private void testData() {
        RequirementModel requirementModel = new RequirementModel();
        Category cat = new Category("te", "Tester", Project.getProject());
        data.add(requirementModel.addActionRequirement(cat, "test", new ExternalInput("", cat.getOwner())));
        data.add(requirementModel.addActionRequirement(cat, "X", new ExternalInput("", cat.getOwner())));
        data.add(requirementModel.addActionRequirement(cat, "henk", new ExternalInput("", cat.getOwner())));
        data.add(requirementModel.addActionRequirement(cat, "test", new ExternalInput("", cat.getOwner())));
        data.add(requirementModel.addActionRequirement(cat, "henk", new ExternalInput("", cat.getOwner())));
        data.add(requirementModel.addActionRequirement(cat, "Bla", new ExternalInput("", cat.getOwner())));
//        data.add(requirementModel.addActionRequirement(new Category("te", "Tester", Project.getProject()), "Bla", null));
//        data.add(requirementModel.addActionRequirement(new Category("te", "Tester", Project.getProject()), "X", null));
//        data.add(requirementModel.addActionRequirement(new Category("te", "Tester", Project.getProject()), "Henk", null));
        ActionRequirement addActionRequirement = requirementModel.addActionRequirement(cat, "test", new ExternalInput("", cat.getOwner()));
        addActionRequirement.isRealized();
    }

    private void fillTable() {
        requitementsTable.setItems(data);
        readyColumn.setCellValueFactory(
                new PropertyValueFactory<>("Realized")
        );
        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("Name")
        );
        typeColumn.setCellValueFactory(
                new PropertyValueFactory<>("Type")
        );
        stateColumn.setCellValueFactory(
                new PropertyValueFactory<>("State")
        );
        reviewColumn.setCellValueFactory(
                new PropertyValueFactory<>("Text")
        );
        reviewColumn.setSortable(false);
        reviewColumn.setMinWidth(120);
        reviewColumn.setCellFactory(new Callback<TableColumn<Object, String>, TableCell<Object, String>>() {
                    @Override
                    public TableCell<Object, String> call(TableColumn<Object, String> param) {
                        return new ButtonsCell(param);
                    }
                });

        textColumn.setCellValueFactory(
                new PropertyValueFactory<>("Text")
        );
    }

    private void setup() {
        setupMenuListeners();
    }
    
    private void setupMenuListeners(){
        //Main Menu
        AddNewRequirementMenuItem.setOnAction((ActionEvent event) -> {
            new AddRequirementDialog().start();
        });
        //Context Menu
        filterTextColumnMenuItem.setOnAction((ActionEvent event) -> {
            List<String> values = new ArrayList<>();
            for (Object object : requitementsTable.getItems()) {
                TableCell tableCell = (TableCell) object;
                //TODO Something is broken here.
                if (tableCell.getTableColumn().equals(textColumn) && !values.contains(tableCell.toString())) {
                    values.add(tableCell.toString());
                }
            }
            System.out.println(values);
            new FilterDialog().start(values);
        });
    }

    public void refresh() {
        //TODO
        FilteredTableView filteredTableView = new FilteredTableView();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
