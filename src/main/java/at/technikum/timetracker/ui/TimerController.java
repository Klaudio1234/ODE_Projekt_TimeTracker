package at.technikum.timetracker.ui;

import at.technikum.timetracker.model.Task;
import at.technikum.timetracker.model.TimeEntry;
import at.technikum.timetracker.model.TimeManager;
import at.technikum.timetracker.network.Client;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class TimerController {
    private final VBox root = new VBox(8);

    private final TimeManager manager;
    private final Consumer<String> log;
    private final Client client;

    private final Runnable onEntryAdded;

    private final TextField userField = new TextField();
    private final ComboBox<Task> taskBox = new ComboBox<>();
    private final Label status = new Label("Timer: stopped");
    private final Label runningTime = new Label("00:00:00");

    private Instant startTime;
    private final ScheduledExecutorService ticker = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> tickJob;

    public TimerController(TimeManager manager, Consumer<String> log, Client client, Runnable onEntryAdded) {
        this.manager = manager;
        this.log = log == null ? (s -> {}) : log;
        this.client = client;
        this.onEntryAdded = onEntryAdded == null ? () -> {} : onEntryAdded;


    }

    public Parent getRoot() { return root; }

    private void build() {
        Label lbl = new Label("Timer (Start/Stop)");

        userField.setPromptText("Your name");
        taskBox.setPromptText("Select task");

        Button btnStart = new Button("Start");
        btnStart.setOnAction(e -> start());

        Button btnStop = new Button("Stop");
        btnStop.setOnAction(e -> stop());

        HBox row = new HBox(8, userField, taskBox, btnStart, btnStop);
        VBox.setVgrow(row, Priority.NEVER);

        root.setPadding(new Insets(10));
        root.getChildren().addAll(lbl, row, status, runningTime);
        root.setStyle("-fx-border-color: #444; -fx-border-radius: 6; -fx-background-radius: 6;");
    }

    public void refreshTasksAndFixSelection() {
        Platform.runLater(() -> {
            Task selected = taskBox.getValue();

            taskBox.getItems().setAll(manager.getTasks());

            if (selected != null) {
                boolean exists = manager.getTasks().stream()
                        .anyMatch(t -> t.getId().equals(selected.getId()));

                if (!exists) {
                    stopIfRunning();
                    taskBox.setValue(null);
                    status.setText("Timer: stopped");
                    runningTime.setText("00:00:00");
                    log.accept("Timer selection cleared (task was deleted).");
                }
            }
        });
    }


    private void start() {
        if (startTime != null) return;

        String user = safe(userField.getText());
        if (user.isEmpty()) {
            showError("Please enter your name");
            return;
        }

        Task t = taskBox.getValue();
        if (t == null) {
            showError("Select a task first");
            return;
        }

        boolean exists = manager.getTasks().stream().anyMatch(x -> x.getId().equals(t.getId()));
        if (!exists) {
            showError("Selected task does not exist anymore. (It was deleted)");
            taskBox.setValue(null);
            return;
        }

        startTime = Instant.now();
        status.setText("Timer: running for " + t.getName());

        userField.setDisable(true);
        taskBox.setDisable(true);

        tickJob = ticker.scheduleAtFixedRate(() -> {
            Instant now = Instant.now();
            long sec = Duration.between(startTime, now).getSeconds();
            Platform.runLater(() -> runningTime.setText(formatHMS(sec)));
        }, 0, 1, TimeUnit.SECONDS);

        log.accept("Timer started: " + t.getName() + " (User: " + user + ")");
    }
    private void stop() {
        if (startTime == null) return;

        String user = safe(userField.getText());
        if (user.isEmpty()) {
            showError("Please enter your name");
            return;
        }

        Task t = taskBox.getValue();
        if (t == null) {
            showError("No task selected");
            stopIfRunning();
            return;
        }

        boolean exists = manager.getTasks().stream().anyMatch(x -> x.getId().equals(t.getId()));
        if (!exists) {
            stopIfRunning();
            taskBox.setValue(null);
            status.setText("Timer: stopped");
            runningTime.setText("00:00:00");
            log.accept("Timer stopped (task was deleted). No entry created.");
            return;
        }

        Instant end = Instant.now();
        TimeEntry entry = new TimeEntry(t.getId(), startTime, end, user);
        manager.addEntry(entry);

        userField.setDisable(false);
        taskBox.setDisable(false); // optional

        stopIfRunning();

        status.setText("Timer: stopped");
        runningTime.setText("00:00:00");

        log.accept("Timer stopped: " + t.getName() + " by " + user + " (" + entry.getDurationSeconds() + "s)");

        String line = "ENTRY|" + t.getId()
                + "|" + entry.getStart().toString()
                + "|" + entry.getEnd().toString()
                + "|" + entry.getDurationSeconds()
                + "|" + safe(user)
                + "|" + safe(t.getType())
                + "|" + safe(t.getName());

        if (client != null) client.sendLineAsync(line);

        Platform.runLater(onEntryAdded);
    }


    private void stopIfRunning() {
        if (tickJob != null) tickJob.cancel(true);
        tickJob = null;
        startTime = null;
    }
    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("|", " ").replace("\n", " ").trim();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Timer");
        a.setHeaderText("Timer");
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