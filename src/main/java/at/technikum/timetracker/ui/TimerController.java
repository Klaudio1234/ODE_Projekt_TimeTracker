package at.technikum.timetracker.ui;

import at.technikum.timetracker.model.Task;
import at.technikum.timetracker.model.TimeManager;
import at.technikum.timetracker.network.Client;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

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


    private void stopIfRunning() {
        if (tickJob != null) tickJob.cancel(true);
        tickJob = null;
        startTime = null;
    }

}