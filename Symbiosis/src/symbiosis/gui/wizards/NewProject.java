package symbiosis.gui.wizards;

import java.io.File;
import java.io.FileNotFoundException;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import symbiosis.gui.SplashScreen;
import symbiosis.project.Project;

/**
 * This class displays a wizard for the setup of a new project.
 */
public class NewProject extends Application {

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(new NewProjectWizard(stage), 400, 250));
        stage.show();
    }
}

/**
 * basic wizard infrastructure class
 */
class Wizard extends StackPane {

    private static final int UNDEFINED = -1;
    private ObservableList<WizardPage> pages = FXCollections.observableArrayList();
    private Stack<Integer> history = new Stack<>();
    private int curPageIdx = UNDEFINED;

    Wizard(WizardPage... nodes) {
        pages.addAll(nodes);
        navTo(0);
        setStyle("-fx-padding: 10;");
    }

    void nextPage() {
        if (hasNextPage()) {
            navTo(curPageIdx + 1);
        }
    }

    void priorPage() {
        if (hasPriorPage()) {
            navTo(history.pop(), false);
        }
    }

    boolean hasNextPage() {
        return (curPageIdx < pages.size() - 1);
    }

    boolean hasPriorPage() {
        return !history.isEmpty();
    }

    void navTo(int nextPageIdx, boolean pushHistory) {
        if (nextPageIdx < 0 || nextPageIdx >= pages.size()) {
            return;
        }
        if (curPageIdx != UNDEFINED) {
            if (pushHistory) {
                history.push(curPageIdx);
            }
        }

        WizardPage nextPage = pages.get(nextPageIdx);
        curPageIdx = nextPageIdx;
        getChildren().clear();
        getChildren().add(nextPage);
        nextPage.manageButtons();
    }

    void navTo(int nextPageIdx) {
        navTo(nextPageIdx, true);
    }

    void navTo(String id) {
        Node page = lookup("#" + id);
        if (page != null) {
            int nextPageIdx = pages.indexOf(page);
            if (nextPageIdx != UNDEFINED) {
                navTo(nextPageIdx);
            }
        }
    }

    public void finish() {
    }

    public void cancel() {
    }
}

/**
 * basic wizard page class
 */
abstract class WizardPage extends VBox {

    Button priorButton = new Button("_Previous");
    Button nextButton = new Button("N_ext");
    Button cancelButton = new Button("Cancel");
    Button finishButton = new Button("_Finish");

    WizardPage(String title) {
        getChildren().add(LabelBuilder.create().text(title).style("-fx-font-weight: bold; -fx-padding: 0 0 5 0;").build());
        setId(title);
        setSpacing(5);
        //setStyle("-fx-padding:10; -fx-background-color: honeydew; -fx-border-color: derive(honeydew, -30%); -fx-border-width: 3;");

        Region spring = new Region();
        VBox.setVgrow(spring, Priority.ALWAYS);
        getChildren().addAll(getContent(), spring, getButtons());

        priorButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                priorPage();
            }
        });
        nextButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                nextPage();
            }
        });
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                getWizard().cancel();
            }
        });
        finishButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                getWizard().finish();
            }
        });
    }

    HBox getButtons() {
        Region spring = new Region();
        HBox.setHgrow(spring, Priority.ALWAYS);
        HBox buttonBar = new HBox(5);
        cancelButton.setCancelButton(true);
        buttonBar.getChildren().addAll(spring, priorButton, nextButton, cancelButton, finishButton);
        return buttonBar;
    }

    abstract Parent getContent();

    boolean hasNextPage() {
        return getWizard().hasNextPage();
    }

    boolean hasPriorPage() {
        return getWizard().hasPriorPage();
    }

    void nextPage() {
        getWizard().nextPage();
    }

    void priorPage() {
        getWizard().priorPage();
    }

    void navTo(String id) {
        getWizard().navTo(id);
    }

    Wizard getWizard() {
        return (Wizard) getParent();
    }

    public void manageButtons() {
        if (!hasPriorPage()) {
            priorButton.setDisable(true);
        }

        if (!hasNextPage()) {
            nextButton.setDisable(true);
        }
    }
}

/**
 * This class shows a satisfaction survey
 */
class NewProjectWizard extends Wizard {

    Stage owner;

    public NewProjectWizard(Stage owner) {
        super(new SetupPage(), new StakeholderPage(), new ProjectMemberPage());
        this.owner = owner;
        this.owner.setOnCloseRequest((WindowEvent event) -> {
            cancel();
            event.consume();
        });
    }

    @Override
    public void finish() {
        String projectName = WizardData.instance.projectName.get();
        String fileLocation = WizardData.instance.fileLocation.get();
        String stakeholderName = WizardData.instance.stakeholderName.get();
        String stakeholderRole = WizardData.instance.stakeholderRole.get();
        String projectMemberName = WizardData.instance.projectMemberName.get();
        String projectMemberRole = WizardData.instance.projectMemberRole.get();
        Project.setup(projectName, stakeholderName, stakeholderRole);
        try {
            Project.getProject().save(new File(fileLocation));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NewProjectWizard.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!projectMemberName.isEmpty()) {
            Project.getProject().getParticipants().addProjectMember(projectMemberName, projectMemberRole);
        }
        owner.close();
    }

    @Override
    public void cancel() {
        System.out.println("Cancelled");
        SplashScreen flashScreen = new SplashScreen();
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(owner.getOwner().getScene().getWindow());
            flashScreen.start(stage);
        } catch (Exception ex) {
            Logger.getLogger(SplashScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
        owner.close();
    }
}

/**
 * Simple placeholder class for the customer entered survey response.
 */
class WizardData {

    //SetupPage
    StringProperty projectName = new SimpleStringProperty();
    StringProperty fileLocation = new SimpleStringProperty();
    //StakeholderPage
    StringProperty stakeholderName = new SimpleStringProperty();
    StringProperty stakeholderRole = new SimpleStringProperty();
    StringProperty projectMemberName = new SimpleStringProperty();
    StringProperty projectMemberRole = new SimpleStringProperty();
    BooleanProperty addProjectMemberProperty = new SimpleBooleanProperty();
    //ProjectMemberPage
    static WizardData instance = new WizardData();
}

/**
 * In this class the user specifies the parameters of the project.
 */
class SetupPage extends WizardPage {

    String path;

    public SetupPage() {
        super("About Project");

        nextButton.setDisable(true);
        nextButton.setDefaultButton(true);
        finishButton.setDisable(true);
    }

    @Override
    Parent getContent() {
        //Root
        VBox root = new VBox();
        root.setSpacing(5);
        //Project Name Label
        root.getChildren().add(new Label("Project Name:"));
        //Project Name TextField
        TextField projectNameTextField = new TextField();
        root.getChildren().add(projectNameTextField);
        WizardData.instance.projectName.bind(projectNameTextField.textProperty());
        //File Location
        HBox fileLocation = new HBox();
        fileLocation.setSpacing(10);
        //File Location Label
        root.getChildren().add(new Label("File Location:"));
        //File Location TextField
        path = System.getProperty("user.dir") + "/";
        TextField fileLocationTextField = new TextField(path);
        fileLocationTextField.setPrefWidth(300);
        WizardData.instance.fileLocation.bind(fileLocationTextField.textProperty());
        fileLocation.getChildren().add(fileLocationTextField);
        //File Location Browse Button
        Button fileLocationBrowseButton = new Button("Browse");
        fileLocation.getChildren().add(fileLocationBrowseButton);
        root.getChildren().add(fileLocation);
        //Listeners
        projectNameTextField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            nextButton.setDisable(projectNameTextField.getText().isEmpty());
            fileLocationTextField.setText(path + projectNameTextField.getText() + ".sym");
        });
        fileLocationBrowseButton.setOnAction((ActionEvent event) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save");
            File startDirectory = new File(fileLocationTextField.getText());
            fileChooser.setInitialDirectory(startDirectory.getParentFile());
            File saveFile = fileChooser.showSaveDialog(((NewProjectWizard) super.getWizard()).owner);
            if (saveFile != null) {
                path = saveFile.getParent() + "/";
                if (saveFile.getName().endsWith(".sym")) {
                    fileLocationTextField.setText(path + saveFile.getName());
                } else {
                    fileLocationTextField.setText(path + saveFile.getName() + ".sym");
                }
            }
        });
        return root;
    }

    @Override
    void nextPage() {
        super.nextPage();
    }
}

/**
 * This page gathers more information about the stakeholder. Including Name and
 * Role in the company.
 */
class StakeholderPage extends WizardPage {

    TextField stakeholderNameTextField;
    TextField stakeholderRoleTextField;

    public StakeholderPage() {
        super("About the Stakeholder");
        nextButton.setDisable(true);
        finishButton.setDisable(true);
    }

    @Override
    Parent getContent() {
        //root
        VBox root = new VBox();
        root.setSpacing(5);
        //Stakeholder Name Label
        root.getChildren().add(new Label("Stakeholder Name:"));
        //Stakeholder Name TextField
        stakeholderNameTextField = new TextField();
        stakeholderNameTextField.setPromptText("John Doe");
        WizardData.instance.stakeholderName.bind(stakeholderNameTextField.textProperty());
        root.getChildren().add(stakeholderNameTextField);
        //Stakeholder Role Label
        root.getChildren().add(new Label("Stakeholder Role:"));
        //Stakeholder Role TextField
        stakeholderRoleTextField = new TextField();
        stakeholderRoleTextField.setPromptText("CEO");
        WizardData.instance.stakeholderRole.bind(stakeholderRoleTextField.textProperty());
        root.getChildren().add(stakeholderRoleTextField);
        //Add Project Member
        RadioButton noRadioButton = new RadioButton("No");
        RadioButton yesRadioButton = new RadioButton("Yes");
        ToggleGroup addProjectMemberGroup = new ToggleGroup();
        noRadioButton.setToggleGroup(addProjectMemberGroup);
        noRadioButton.setSelected(true);
        yesRadioButton.setToggleGroup(addProjectMemberGroup);
        WizardData.instance.addProjectMemberProperty.bind(yesRadioButton.selectedProperty());
        root.getChildren().add(new Label("Add Project Member?"));
        root.getChildren().add(yesRadioButton);
        root.getChildren().add(noRadioButton);
        //Listeners
        stakeholderNameTextField.textProperty().addListener((ObservableValue<? extends String> observableValue, String oldValue, String newValue) -> {
            valueChanged();
        });
        stakeholderRoleTextField.textProperty().addListener((ObservableValue<? extends String> observableValue, String oldValue, String newValue) -> {
            valueChanged();
        });
        addProjectMemberGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
            valueChanged();
        });
        return root;
    }

    void valueChanged() {
        if (WizardData.instance.addProjectMemberProperty.get()) {
            nextButton.setDisable(stakeholderNameTextField.getText().isEmpty() || stakeholderRoleTextField.getText().isEmpty());
            nextButton.setDefaultButton(true);
            finishButton.setDisable(true);
            finishButton.setDefaultButton(false);
        } else {
            nextButton.setDisable(true);
            nextButton.setDefaultButton(false);
            finishButton.setDisable(stakeholderNameTextField.getText().isEmpty() || stakeholderRoleTextField.getText().isEmpty());
            finishButton.setDefaultButton(true);
        }
    }
}

/**
 * This page thanks the user for taking the survey
 */
class ProjectMemberPage extends WizardPage {

    TextField projectMemberNameTextField;
    TextField projectMemberRoleTextField;

    public ProjectMemberPage() {
        super("About the Project Member");
        nextButton.setDisable(true);
        finishButton.setDisable(true);
        finishButton.setDefaultButton(true);
    }

    @Override
    Parent getContent() {
        //root
        VBox root = new VBox();
        root.setSpacing(5);
        //Project Member Name Label
        root.getChildren().add(new Label("Project Member Name:"));
        //Project Member Name TextField
        projectMemberNameTextField = new TextField();
        projectMemberNameTextField.setPromptText("John Doe");
        WizardData.instance.projectMemberName.bind(projectMemberNameTextField.textProperty());
        root.getChildren().add(projectMemberNameTextField);
        //Project Member Role Label
        root.getChildren().add(new Label("Project Member Role:"));
        //Project Member Role TextField
        projectMemberRoleTextField = new TextField();
        projectMemberRoleTextField.setPromptText("Software Engineer");
        WizardData.instance.projectMemberRole.bind(projectMemberRoleTextField.textProperty());
        root.getChildren().add(projectMemberRoleTextField);
        //Listeners
        projectMemberNameTextField.textProperty().addListener((ObservableValue<? extends String> observableValue, String oldValue, String newValue) -> {
            finishButton.setDisable(projectMemberNameTextField.getText().isEmpty() || projectMemberRoleTextField.getText().isEmpty());
        });
        projectMemberRoleTextField.textProperty().addListener((ObservableValue<? extends String> observableValue, String oldValue, String newValue) -> {
            finishButton.setDisable(projectMemberNameTextField.getText().isEmpty() || projectMemberRoleTextField.getText().isEmpty());
        });
        return root;
    }
}
