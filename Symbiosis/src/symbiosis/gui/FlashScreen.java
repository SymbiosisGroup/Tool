/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.gui;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author jeroen
 */
public class FlashScreen extends Application {

    private boolean closing = false;

    @Override
    public void start(Stage primaryStage) {
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
        Label versionNrLabel = new Label(FlashScreen.class.getPackage().getImplementationVersion());
        textTable.add(versionNrLabel, 1, 0);
        //Vendor
        Label vendorLabel = new Label("Vendor:");
        vendorLabel.setFont(Font.font("System Regular", FontWeight.BOLD, 13));
        textTable.add(vendorLabel, 0, 1);
        Label vendorNameLabel = new Label(FlashScreen.class.getPackage().getImplementationVendor());
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
        //NewProjectButton
        Button newProjecButton = new Button("New Project");
        newProjecButton.setDefaultButton(true);
        newProjecButton.setOnMouseClicked((MouseEvent event) -> {
            //New Project
            Survey survey = new Survey();
            try {
                Stage stage = new Stage();
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(primaryStage.getOwner().getScene().getWindow());
                survey.start(stage);
            } catch (Exception ex) {
                Logger.getLogger(FlashScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
            closeScreen(primaryStage);
        });
        controlBox.getChildren().add(newProjecButton);
        //OpenProjectButton
        Button openProjecButton = new Button("Open Project");
        openProjecButton.setOnAction((ActionEvent event) -> {
            //Open Project
            closeScreen(primaryStage);
        });
        controlBox.getChildren().add(openProjecButton);
        //QuitButton
        Button quitButton = new Button("Quit Symbiosis");
        quitButton.setOnAction((ActionEvent event) -> {
        });
        controlBox.getChildren().add(quitButton);

        controlBox.setPadding(new Insets(10));
        root.getChildren().add(controlBox);
        StackPane.setAlignment(controlBox, Pos.BOTTOM_RIGHT);
        //Scene en Stage Setup
        Scene scene = new Scene(root, 800, 300);
        primaryStage.setTitle("Symbiosis");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            if (!closing) {
                event.consume();
            }
        });
        primaryStage.show();
    }

    private void closeScreen(Stage primaryStage) {
        closing = true;
        primaryStage.close();
    }
}
