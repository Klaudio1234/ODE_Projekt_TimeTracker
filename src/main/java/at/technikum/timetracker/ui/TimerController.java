package at.technikum.timetracker.ui;

import at.technikum.timetracker.model.Task;
import at.technikum.timetracker.model.TimeManager;
import at.technikum.timetracker.network.Client;
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
}