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

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import symbiosis.gui.wizards.NewProject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextArea;
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
import symbiosis.gui.ScreenManager;
import symbiosis.gui.SplashScreen;
import symbiosis.meta.ChangeNotAllowedException;
import symbiosis.meta.requirements.ActionRequirement;
import symbiosis.meta.requirements.ChanceOfFailure;
import symbiosis.meta.requirements.MoSCoW;
import symbiosis.meta.requirements.Requirement;
import symbiosis.meta.requirements.Urgency;
import symbiosis.meta.requirements.VerifyMethod;
import symbiosis.meta.traceability.Category;
import symbiosis.meta.traceability.ExternalInput;
import symbiosis.meta.traceability.Impact;
import symbiosis.project.Project;

/**
 *
 * @author Jeroen Berkvens
 * @company EQUA
 * @project Symbiosis
 */
public class AddRequirementDialog extends Application {

    @FXML
    private TextArea requirementTextArea;
    @FXML
    private ComboBox typeComboBox, categoryComboBox, chanceOfFailureComboBox, impactComboBox, urgencyComboBox, moscowComboBox, verifyMethodComboBox;
    @FXML
    private Button cancelButton, addRequirementButton;

    private Stage primaryStage;

    public void start() {
        start(new Stage());
    }

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("resources/AddRequirementDialog.fxml"));
        fxmlLoader.setController(this);
        try {
            Parent root = (Parent) fxmlLoader.load();
            Scene scene = new Scene(root, 550, 450);
            primaryStage.setResizable(true);
            primaryStage.setTitle("Add Requirement");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.primaryStage = primaryStage;
        fillComboBoxes();
        addListeners();
    }

    private void fillComboBoxes() {
        //TypeOptions
        ObservableList<String> typeOptions = FXCollections.observableArrayList();
        typeOptions.add("Action");
        typeOptions.add("Fact");
        typeOptions.add("Rule");
        typeOptions.add("QualityAttributes");
        typeComboBox.getItems().addAll(typeOptions);
        typeComboBox.getSelectionModel().select(0);
        //CategoryOptions
        ObservableList<String> categoryOptions = FXCollections.observableArrayList();
        //categoryOptions.add(Category.SYSTEM.getName());
        Iterator<Category> categories = Project.getProject().getCategories();
        while (categories.hasNext()) {
            Category cat = categories.next();
            if (!cat.getCode().equalsIgnoreCase(Category.SYSTEM.getCode())) {
                categoryComboBox.getItems().add(cat);
            }
        }
        categoryComboBox.getItems().add(Category.SYSTEM);
        categoryComboBox.getItems().addAll(categoryOptions);
        categoryComboBox.getSelectionModel().select(0);
        //ChanceOfFaulureOptions
        ObservableList<ChanceOfFailure> chanceOfFailureOptions = FXCollections.observableArrayList();
        chanceOfFailureOptions.add(ChanceOfFailure.UNDEFINED);
        chanceOfFailureOptions.add(ChanceOfFailure.LOW);
        chanceOfFailureOptions.add(ChanceOfFailure.MEDIUM);
        chanceOfFailureOptions.add(ChanceOfFailure.HIGH);
        chanceOfFailureComboBox.getItems().addAll(chanceOfFailureOptions);
        chanceOfFailureComboBox.getSelectionModel().select(0);
        //ImpactOptions
        ObservableList<Impact> impactOptions = FXCollections.observableArrayList();
        impactOptions.add(Impact.UNDEFINED);
        impactOptions.add(Impact.NONE);
        impactOptions.add(Impact.LIGHT);
        impactOptions.add(Impact.NORMAL);
        impactOptions.add(Impact.SERIOUS);
        impactComboBox.getItems().addAll(impactOptions);
        impactComboBox.getSelectionModel().select(0);
        //UrgencyOptions
        ObservableList<Urgency> urgencyOptions = FXCollections.observableArrayList();
        urgencyOptions.add(Urgency.UNDEFINED);
        urgencyOptions.add(Urgency.LOW);
        urgencyOptions.add(Urgency.MEDIUM);
        urgencyOptions.add(Urgency.HIGH);
        urgencyComboBox.getItems().addAll(urgencyOptions);
        urgencyComboBox.getSelectionModel().select(0);
        //MoSCoWOptions
        ObservableList<MoSCoW> moscowOptions = FXCollections.observableArrayList();
        moscowOptions.add(MoSCoW.UNDEFINED);
        moscowOptions.add(MoSCoW.MUST);
        moscowOptions.add(MoSCoW.SHOULD);
        moscowOptions.add(MoSCoW.COULD);
        moscowOptions.add(MoSCoW.WONT);
        moscowComboBox.getItems().addAll(moscowOptions);
        moscowComboBox.getSelectionModel().select(0);
        //VerifyMethodOptions
        ObservableList<VerifyMethod> verifyMethodOptions = FXCollections.observableArrayList();
        verifyMethodOptions.add(VerifyMethod.UNDEFINED);
        verifyMethodOptions.add(VerifyMethod.NONE);
        verifyMethodOptions.add(VerifyMethod.ANALYSIS);
        verifyMethodOptions.add(VerifyMethod.DEMONSTRATION);
        verifyMethodOptions.add(VerifyMethod.INSPECTION);
        verifyMethodOptions.add(VerifyMethod.TEST);
        verifyMethodComboBox.getItems().addAll(verifyMethodOptions);
        verifyMethodComboBox.getSelectionModel().select(0);
    }

    private void addListeners() {
        addRequirementButton.setOnAction((ActionEvent event) -> {
            addRequirement();
        });
    }

    private void addRequirement() {
        Category category = (Category) categoryComboBox.getSelectionModel().getSelectedItem();
        ExternalInput externalInput = new ExternalInput("", Project.getProject().getCurrentUser());
        Requirement requirement = null;
        switch (typeComboBox.getSelectionModel().getSelectedIndex()) {
            case 0: {
                requirement = Project.getProject().getRequirementModel().addActionRequirement(category, requirementTextArea.getText(), externalInput);
                break;
            }
            case 1: {
                requirement = Project.getProject().getRequirementModel().addFactRequirement(category, requirementTextArea.getText(), externalInput);
                break;
            }
            case 2: {
                requirement = Project.getProject().getRequirementModel().addRuleRequirement(category, requirementTextArea.getText(), externalInput);
                break;
            }
            case 3: {
                requirement = Project.getProject().getRequirementModel().addQualityAttribute(category, requirementTextArea.getText(), externalInput);
                break;
            }
        }
        requirement.setImpact((Impact) impactComboBox.getSelectionModel().getSelectedItem());
        requirement.setChanceOfFailure((ChanceOfFailure) chanceOfFailureComboBox.getSelectionModel().getSelectedItem());
        requirement.setMoSCoW((MoSCoW) moscowComboBox.getSelectionModel().getSelectedItem());
        requirement.setUrgency((Urgency) urgencyComboBox.getSelectionModel().getSelectedItem());
        requirement.setVerifyMethod((VerifyMethod) verifyMethodComboBox.getSelectionModel().getSelectedItem());
        requirement.setManuallyCreated(true);
        if (requirement.getCategory().isOwner(Project.getProject().getCurrentUser())) {
            requirement.approve(new ExternalInput("added by owner of category", Project.getProject().getCurrentUser()));
        }
        this.primaryStage.close();
        ScreenManager.getMainScreen().refresh();
    }
}
