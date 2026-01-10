package at.technikum.timetracker.ui;

import at.technikum.timetracker.exception.StorageException;
import at.technikum.timetracker.model.TimeManager;
import at.technikum.timetracker.storage.ClientLogger;
import at.technikum.timetracker.storage.FileStorage;
import javafx.application.Platform;
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

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainController {

    private final BorderPane root = new BorderPane();

    private final TextArea console = new TextArea();

    private final FileStorage storage = FileStorage.defaultStorage();

    private final TimeManager manager;

    private final ClientLogger logger = ClientLogger.defaultLogger();

    private final ScheduledExecutorService autosave =
            Executors.newSingleThreadScheduledExecutor();

    public MainController() {
        this.manager = loadSafe();

        console.setEditable(false);
        console.setPrefRowCount(6);

        buildUi();
        startBackgroundThings();
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

    private void startBackgroundThings() {
        autosave.scheduleAtFixedRate(() -> {
            try {
                storage.save(manager);
                logFx("Auto-saved at " + Instant.now());
            } catch (StorageException ex) {
                logFx("Auto-save failed: " + ex.getMessage());
            }
        }, 20, 20, TimeUnit.SECONDS);
    }

    private TimeManager loadSafe() {
        try {
            TimeManager m = storage.load();
            logFx("Loaded data from: " + storage.getDataFile());
            return m;
        } catch (StorageException ex) {
            logFx("Load failed: " + ex.getMessage());
            return new TimeManager();
        }
    }

    private void saveSafe() {
        try {
            storage.save(manager);
            logFx("Saved to: " + storage.getDataFile());
        } catch (StorageException ex) {
            showError("Storage", ex.getMessage());
            logFx("ERROR: " + ex.getMessage());
        }
    }

    private void logFx(String msg) {
        logger.logAsync(msg);
        Platform.runLater(() -> console.appendText(msg + "\n"));
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
