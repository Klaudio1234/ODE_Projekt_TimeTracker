package at.technikum.timetracker.ui;

import at.technikum.timetracker.exception.NetworkException;
import at.technikum.timetracker.exception.StorageException;
import at.technikum.timetracker.model.Task;
import at.technikum.timetracker.model.TimeEntry;
import at.technikum.timetracker.model.TimeManager;
import at.technikum.timetracker.network.Client;
import at.technikum.timetracker.storage.ClientLogger;
import at.technikum.timetracker.storage.FileStorage;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;

public class MainController {


    private final BorderPane root = new BorderPane();


    private final TextArea console = new TextArea();
    private final ListView<String> entryList = new ListView<>();


    private final FileStorage storage = FileStorage.defaultStorage();
    private final TimeManager manager;


    private final Client client;


    private final ClientLogger logger = ClientLogger.defaultLogger();


    private final ScheduledExecutorService autosave =
            Executors.newSingleThreadScheduledExecutor();


    private final TaskListController taskListController;
    private final TaskEditorController taskEditorController;
    private final TimerController timerController;

    public MainController() {
        this.manager = loadSafe();

        console.setEditable(false);
        console.setPrefRowCount(6);


        client = new Client(this::onClientLine);


        taskListController = new TaskListController(manager, this::logFx, client);

        timerController = new TimerController(
                manager,
                this::logFx,
                client,
                this::refreshEntriesForSelectedTask
        );

        taskListController.setOnTasksChanged(() -> {
            timerController.refreshTasksAndFixSelection();
            refreshEntriesForSelectedTask();
        });


        taskEditorController = new TaskEditorController(
                manager,
                this::logFx,
                client,
                () -> {
                    taskListController.refresh();
                    timerController.refreshTasksAndFixSelection();
                    refreshEntriesForSelectedTask();
                }
        );

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
        btnConnect.setOnAction(e -> {
            try {
                client.connect("127.0.0.1", 5555);
            } catch (NetworkException ex) {
                showError("Network", ex.getMessage());
                logFx("ERROR: " + ex.getMessage());
            }
        });

        HBox top = new HBox(10, title, new Region(), btnConnect, btnSave);
        HBox.setHgrow(top.getChildren().get(1), Priority.ALWAYS);
        top.setPadding(new Insets(10));

        SplitPane split = new SplitPane();
        split.setDividerPositions(0.33);

        VBox left = new VBox(10,
                taskListController.getRoot(),
                taskEditorController.getRoot()
        );
        left.setPadding(new Insets(10));

        VBox center = new VBox(10,
                timerController.getRoot(),
                buildEntriesPane()
        );
        center.setPadding(new Insets(10));

        split.getItems().addAll(left, center);

        VBox bottom = new VBox(6,
                new Label("Console / Server Messages"),
                console
        );
        bottom.setPadding(new Insets(10));

        root.setTop(top);
        root.setCenter(split);
        root.setBottom(bottom);
    }

    private Parent buildEntriesPane() {
        VBox box = new VBox(8);
        Label lbl = new Label("Time Entries (for selected task)");

        entryList.setPrefHeight(250);

        taskListController.setOnSelectionChanged(t -> refreshEntriesForSelectedTask());

        box.getChildren().addAll(lbl, entryList);
        return box;
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

    private void refreshEntriesForSelectedTask() {
        Task task = taskListController.getSelected();

        entryList.getItems().clear();
        if (task == null) return;

        manager.getEntriesForTask(task.getId())
                .forEach(e -> entryList.getItems().add(e.toString()));

        long total = manager.getTotalSecondsForTask(task.getId());
        logFx("Total for " + task.getName() + ": " + formatHMS(total));
    }


    private void onClientLine(String line) {

        logFx(line);


        if (line == null) return;
        if (line.startsWith("TASK|")
                || line.startsWith("UPDATE_TASK|")
                || line.startsWith("DELETE_TASK|")
                || line.startsWith("ENTRY|")) {
            applyEvent(line);
        }
    }


    private void applyEvent(String payload) {
        try {
            if (payload.startsWith("DELETE_TASK|")) {
                String[] p = payload.split("\\|", -1);
                UUID id = UUID.fromString(p[1]);

                manager.findTask(id).ifPresent(task -> {
                    manager.deleteTask(task);
                    Platform.runLater(this::refreshAllUi);
                });
                return;
            }

            if (payload.startsWith("UPDATE_TASK|")) {
                String[] p = payload.split("\\|", -1);
                UUID id = UUID.fromString(p[1]);
                String newName = p.length >= 3 ? p[2] : "";
                String newDesc = p.length >= 4 ? p[3] : "";

                manager.findTask(id).ifPresent(task -> {
                    manager.updateTask(task, newName, newDesc);
                    Platform.runLater(this::refreshAllUi);
                });
                return;
            }

            if (payload.startsWith("TASK|")) {

                String[] p = payload.split("\\|", -1);
                UUID id = UUID.fromString(p[1]);
                String type = p.length >= 3 ? p[2] : "";
                String name = p.length >= 4 ? p[3] : "";
                String desc = p.length >= 5 ? p[4] : "";


                if (manager.findTask(id).isEmpty()) {
                    manager.addTask(Task.fromStorage(id, type, name, desc));
                    Platform.runLater(this::refreshAllUi);
                }
                return;
            }

            if (payload.startsWith("ENTRY|")) {


                String[] p = payload.split("\\|", -1);
                UUID taskId = UUID.fromString(p[1]);
                Instant start = Instant.parse(p[2]);
                Instant end = Instant.parse(p[3]);

                String user = (p.length >= 6) ? p[5] : "";


                boolean exists = manager.getEntries().stream().anyMatch(e ->
                        e.getTaskId().equals(taskId)
                                && e.getStart().equals(start)
                                && e.getEnd().equals(end)
                                && e.getUserName().equals(user)
                );

                if (!exists) {
                    manager.addEntry(new TimeEntry(taskId, start, end, user));
                    Platform.runLater(this::refreshAllUi);
                }
            }
        } catch (Exception ex) {
            logFx("APPLY_EVENT_ERROR: " + ex.getMessage());
        }
    }


    private void refreshAllUi() {
        taskListController.refresh();
        timerController.refreshTasksAndFixSelection();
        refreshEntriesForSelectedTask();
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

    public void shutdown() {
        saveSafe();
        autosave.shutdownNow();
        try { client.close(); } catch (Exception ignored) {}
        try { logger.close(); } catch (Exception ignored) {}
    }
}
