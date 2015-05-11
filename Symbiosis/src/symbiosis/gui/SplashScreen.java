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
import symbiosis.gui.wizards.NewProject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import symbiosis.project.Project;

/**
 *
 * @author Jeroen Berkvens
 * @company EQUA
 * @project Symbiosis
 */
public class SplashScreen extends Application {

    private boolean closing = false;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        HBox imageTextSplit = new HBox();
        //Image (left)
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/symbiosis/gui/resources/logo.png")));
        imageTextSplit.getChildren().add(imageView);
        //Text (right)
        VBox textBox = new VBox();
        textBox.setPadding(new Insets(10));
        //Title
        Label titlLabel = new Label("Symbiosis");
        titlLabel.setFont(Font.font("System Regular", FontWeight.BOLD, 18));
        textBox.getChildren().add(titlLabel);
        //Subtitle
        Label subtitleLabel = new Label("Fact-Orientated Software Factory");
        textBox.getChildren().add(subtitleLabel);
        //Details
        GridPane textTable = new GridPane();
        textTable.setPadding(new Insets(5, 0, 0, 0));
        textTable.setHgap(5);
        //Version
        Label versionLabel = new Label("Version:");
        versionLabel.setFont(Font.font("System Regular", FontWeight.BOLD, 13));
        textTable.add(versionLabel, 0, 0);
        Label versionNrLabel = new Label(SplashScreen.class.getPackage().getImplementationVersion());
        textTable.add(versionNrLabel, 1, 0);
        //Vendor
        Label vendorLabel = new Label("Vendor:");
        vendorLabel.setFont(Font.font("System Regular", FontWeight.BOLD, 13));
        textTable.add(vendorLabel, 0, 1);
        Label vendorNameLabel = new Label(SplashScreen.class.getPackage().getImplementationVendor());
        textTable.add(vendorNameLabel, 1, 1);
        //Homepage
        Label homepageLabel = new Label("Homepage:");
        homepageLabel.setFont(Font.font("System Regular", FontWeight.BOLD, 13));
        textTable.add(homepageLabel, 0, 2);
        Label homepageURLLabel = new Label("http://symbiosis.moridrin.com");
        textTable.add(homepageURLLabel, 1, 2);
        textBox.getChildren().add(textTable);
        imageTextSplit.getChildren().add(textBox);
        //Root Setup
        StackPane root = new StackPane();
        root.getChildren().add(imageTextSplit);
        //Control
        VBox controlBox = new VBox();
        controlBox.setAlignment(Pos.BOTTOM_RIGHT);
        controlBox.setSpacing(10);
        //NewProjectButton
        Button newProjecButton = new Button("New Project");
        newProjecButton.setPrefWidth(380);
        newProjecButton.setOnMouseClicked((MouseEvent event) -> {
            //New Project
            NewProject newProjectWizard = new NewProject(true);
            try {
                Stage stage = new Stage();
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(this.primaryStage.getOwner().getScene().getWindow());
                newProjectWizard.start(stage);
            } catch (Exception ex) {
                Logger.getLogger(SplashScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
            //This can not be done currently because JAVA will crash!
//            primaryStage.hide();
            closing = true;
            this.primaryStage.close();
        });
        controlBox.getChildren().add(newProjecButton);
        //OpenProjectButton
        Button openProjecButton = new Button("Open Project");
        openProjecButton.setPrefWidth(380);
        openProjecButton.setOnAction((ActionEvent event) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Symbiosis Projects", "*.sym"));
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            File openFile = fileChooser.showOpenDialog(this.primaryStage);
            if (openFile != null) {
                try {
                    Project.getProject(openFile);
                    closing = true;
                    this.primaryStage.close();
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(SplashScreen.class.getName()).log(Level.SEVERE, null, ex);
                }
                ScreenManager.getMainScreen().refresh();
            }
        });
        controlBox.getChildren().add(openProjecButton);
        //QuitButton
        Button quitButton = new Button("Quit Symbiosis");
        quitButton.setPrefWidth(380);
        quitButton.setOnAction((ActionEvent event) -> {
            System.exit(0);
        });
        controlBox.getChildren().add(quitButton);

        controlBox.setPadding(new Insets(10));
        root.getChildren().add(controlBox);
        StackPane.setAlignment(controlBox, Pos.BOTTOM_RIGHT);
        //Scene en Stage Setup
        Scene scene = new Scene(root, 800, 300);
        this.primaryStage.setTitle("Symbiosis");
        this.primaryStage.setScene(scene);
        this.primaryStage.setOnCloseRequest((WindowEvent event) -> {
            if (!closing) {
                System.exit(0);
            }
        });
        this.primaryStage.show();
    }

    public void show() {
        this.primaryStage.show();
    }
}
