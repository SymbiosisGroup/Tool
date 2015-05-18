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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import symbiosis.gui.dialogs.AddRequirementDialog;
import symbiosis.gui.dialogs.FilterDialog;
import symbiosis.gui.dialogs.MessageDialog;
import symbiosis.gui.resources.ButtonsRequirementCell;
import symbiosis.gui.resources.CheckBoxRequirementCell;
import symbiosis.gui.wizards.NewProject;
import symbiosis.meta.requirements.Requirement;
import symbiosis.meta.requirements.RequirementFilter;
import symbiosis.meta.requirements.RequirementFilter.FilterProperty;
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
    private MenuItem newProjectMenuItem, saveProjectMenuItem, openProjectMenuItem, addNewRequirementMenuItem;
    @FXML
    private MenuItem filterReadyColumnMenuItem, filterNameColumnMenuItem, filterTypeColumnMenuItem, filterStateColumnMenuItem, filterReviewColumnMenuItem, filterTextColumnMenuItem;

    private final List<Requirement> data = new ArrayList();
    private final ObservableList<Requirement> displaying = FXCollections.observableArrayList();

    private Stage primaryStage;

    /**
     * This is operation starts the MainScreen and initializes all of its
     * components.
     *
     * @param primaryStage is the Stage the SplashScreen is going to be
     * displayed on.
     */
    @Override
    public void start(Stage primaryStage) {
        //Load the FXML File
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

        //Open the SplashScreen
        SplashScreen splashScreen = new SplashScreen();
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage.getScene().getWindow());
        this.primaryStage = primaryStage;
        splashScreen.start(stage);
        //Setup
        setup();
    }

    private void setupTable() {
        requitementsTable.setItems(displaying);
        readyColumn.setSortable(false);
        readyColumn.setMinWidth(40);
        readyColumn.setCellFactory(new Callback<TableColumn<Requirement, Object>, TableCell<Requirement, Object>>() {
            @Override
            public TableCell<Requirement, Object> call(TableColumn<Requirement, Object> param) {
                return new CheckBoxRequirementCell(param);
            }
        });
        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("Name")
        );
        typeColumn.setCellValueFactory(
                new PropertyValueFactory<>("ReqType")
        );
        stateColumn.setCellValueFactory(
                new PropertyValueFactory<>("ReviewState")
        );
        reviewColumn.setCellValueFactory(
                new PropertyValueFactory<>("ReviewState")
        );
        reviewColumn.setSortable(false);
        reviewColumn.setMinWidth(120);
        reviewColumn.setCellFactory(new Callback<TableColumn<Requirement, Object>, TableCell<Requirement, Object>>() {
            @Override
            public TableCell<Requirement, Object> call(TableColumn<Requirement, Object> param) {
                return new ButtonsRequirementCell(param);
            }
        });

        textColumn.setCellValueFactory(
                new PropertyValueFactory<>("Text")
        );
        setRightColumnWidth();
    }

    private void setRightColumnWidth() {
        double tableWidth = requitementsTable.getWidth();
        double readyWidth = readyColumn.getWidth();
        double nameWidth = nameColumn.getWidth();
        double typeWidth = typeColumn.getWidth();
        double stateWidth = stateColumn.getWidth();
        double reviewWidth = reviewColumn.getWidth();
        //requitementsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        textColumn.setPrefWidth(tableWidth - readyWidth - nameWidth - typeWidth - stateWidth - reviewWidth - 50);
    }

    /**
     * Setup the Table fields, the Menu Listeners, and the ScreenManager.
     */
    private void setup() {
        setupTable();
        setupMenuListeners();
        //Set this in ScreemManager
        try {
            ScreenManager.setMainScreen(this);
        } catch (Exception ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
            MessageDialog.show("The Main Screen already exists this may have resulted in errors.");
        }

    }

    /**
     * Setup the Main Menu Listeners and the Right Click Menu Listeners.
     */
    private void setupMenuListeners() {
        setupMainMenuListeners();
        setupRightClickMenuListeners();
    }

    /**
     * Setup the Main Menu Listeners. Project Menu and Edit Menu.
     */
    private void setupMainMenuListeners() {
        setupProjectMenu();
        setupEditMenu();
    }

    /**
     * Setup the Project Menu Listeners.
     */
    private void setupProjectMenu() {
        //New Project
        newProjectMenuItem.setOnAction((ActionEvent event) -> {
            NewProject newProjectWizard = new NewProject(false);
            try {
                Stage stage = new Stage();
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(primaryStage.getScene().getWindow());
                newProjectWizard.start(stage);
            } catch (Exception ex) {
                Logger.getLogger(SplashScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        //Save Project
        saveProjectMenuItem.setOnAction((ActionEvent event) -> {
            Project.getProject().save();
        });
        //Open Project
        openProjectMenuItem.setOnAction((ActionEvent event) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Symbiosis Projects", "*.sym"));
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            File openFile = fileChooser.showOpenDialog(primaryStage);
            if (openFile != null) {
                try {
                    Project.getProject(openFile);
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(SplashScreen.class.getName()).log(Level.SEVERE, null, ex);
                }
                ScreenManager.getMainScreen().refresh();
            }
        });
    }

    /**
     * Setup the Edit Menu Listeners.
     */
    private void setupEditMenu() {
        //AddRequirement
        addNewRequirementMenuItem.setOnAction((ActionEvent event) -> {
            new AddRequirementDialog().start();
        });
    }

    /**
     * Setup the Right CLick Menu Listeners.
     */
    private void setupRightClickMenuListeners() {
        //Text Filter
        filterTextColumnMenuItem.setOnAction((ActionEvent event) -> {
            List<RequirementFilter> values = new ArrayList<>();
            data.stream().forEach((requirement) -> {
                RequirementFilter requirementFilter = new RequirementFilter(requirement, FilterProperty.TEXT);
                if (!values.contains(requirementFilter)) {
                    values.add(requirementFilter);
                }
            });
            new FilterDialog().start(values);
        });
        //Name Filter
        filterNameColumnMenuItem.setOnAction((ActionEvent event) -> {
            List<RequirementFilter> values = new ArrayList<>();
            data.stream().forEach((requirement) -> {
                RequirementFilter requirementFilter = new RequirementFilter(requirement, FilterProperty.NAME);
                if (!values.contains(requirementFilter)) {
                    values.add(requirementFilter);
                }
            });
            new FilterDialog().start(values);
        });
        //State Filter
        filterStateColumnMenuItem.setOnAction((ActionEvent event) -> {
            List<RequirementFilter> values = new ArrayList<>();
            data.stream().forEach((requirement) -> {
                RequirementFilter requirementFilter = new RequirementFilter(requirement, FilterProperty.STATE);
                if (!values.contains(requirementFilter)) {
                    values.add(requirementFilter);
                }
            });
            new FilterDialog().start(values);
        });
        //Type Filter
        filterTypeColumnMenuItem.setOnAction((ActionEvent event) -> {
            List<RequirementFilter> values = new ArrayList<>();
            data.stream().forEach((requirement) -> {
                RequirementFilter requirementFilter = new RequirementFilter(requirement, FilterProperty.TYPE);
                if (!values.contains(requirementFilter)) {
                    values.add(requirementFilter);
                }
            });
            new FilterDialog().start(values);
        });
    }

    /**
     * This operation refreshes the requirements being displayed. A refresh
     * removes all filters.
     */
    public void refresh() {
        data.clear();
        displaying.clear();
        Iterator<Requirement> requirements = Project.getProject().getRequirementModel().requirements();
        while (requirements.hasNext()) {
            Requirement nextRequirement = requirements.next();
            data.add(nextRequirement);
        }
        displaying.addAll(data);
    }

    /**
     * This operation refreshes only one requirement.
     *
     * @param requirement the requirement being refreshed.
     */
    public void refresh(Requirement requirement) {
        if (!data.contains(requirement)) {
            data.add(requirement);
            displaying.add(requirement);
        } else {
            //TODO: refresh an already existing requirement.
        }
    }

    /**
     * This operation filters the requirements view.
     *
     * @param selectedFilters the filter that is being applied.
     */
    public void setFilter(List<RequirementFilter> selectedFilters) {
        displaying.clear();
        selectedFilters.stream().forEach((filter) -> {
            displaying.add(filter.getRequirement());
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
