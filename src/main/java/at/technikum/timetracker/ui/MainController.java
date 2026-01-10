package at.technikum.timetracker.ui;

import at.technikum.timetracker.exception.StorageException;
import at.technikum.timetracker.model.TimeManager;
import at.technikum.timetracker.storage.FileStorage;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class MainController {


    private final BorderPane root = new BorderPane();


    private final TextArea console = new TextArea();


    private final FileStorage storage = FileStorage.defaultStorage();
    private final TimeManager manager;

    public MainController() {
        this.manager = loadSafe();

        console.setEditable(false);
        console.setPrefRowCount(6);

        buildUi();
    }

    public Parent getRoot() {
        return root;
    }


    private void buildUi() {
        Label title = new Label("ODE TimeTracker (unreleased)");
        title.setFont(Font.font(18));

        Button btnSave = new Button("Save");
        btnSave.setOnAction(e -> saveSafe());

        Button btnConnect = new Button("Connect Client → localhost:5555");

        HBox top = new HBox(10, title, new Region(), btnConnect, btnSave);
        HBox.setHgrow(top.getChildren().get(1), Priority.ALWAYS);
        top.setPadding(new Insets(10));

        VBox bottom = new VBox(6,
                new Label("Console / Server Messages"),
                console
        );
        bottom.setPadding(new Insets(10));

        root.setTop(top);
        root.setBottom(bottom);
    }


    private TimeManager loadSafe() {
        try {
            TimeManager m = storage.load();
            console.appendText("Loaded data from: " + storage.getDataFile() + "\n");
            return m;
        } catch (StorageException ex) {
            console.appendText("Load failed: " + ex.getMessage() + "\n");
            return new TimeManager();
        }
    }

    private void saveSafe() {
        try {
            storage.save(manager);
            console.appendText("Saved to: " + storage.getDataFile() + "\n");
        } catch (StorageException ex) {
            showError("Storage", ex.getMessage());
            console.appendText("ERROR: " + ex.getMessage() + "\n");
        }
    }


    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }

    private static String formatHMS(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}