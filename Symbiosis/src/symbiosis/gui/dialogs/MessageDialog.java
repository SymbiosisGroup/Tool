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

import java.util.Arrays;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
 * @author Jeroen Berkvens
 * @company EQUA
 * @project Symbiosis
 */
public class MessageDialog extends Application {

    private String message;
    
    private MessageDialog(){
    }

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        Label messageLabel = new Label(message);
        root.getChildren().add(messageLabel);
        //Scene en Stage Setup
        Scene scene = new Scene(root, 400, 200);
        primaryStage.setTitle("Message");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void show(String message) {
        MessageDialog instance = new MessageDialog();
        instance.message = message;
        instance.start(new Stage());
    }
}
