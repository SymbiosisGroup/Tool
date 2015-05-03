/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.gui;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author jeroen
 */
public class MainScreen extends Application {

    @FXML
    private AnchorPane projectNavigator;

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
        FlashScreen flashScreen = new FlashScreen();
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage.getScene().getWindow());
        flashScreen.start(stage);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
