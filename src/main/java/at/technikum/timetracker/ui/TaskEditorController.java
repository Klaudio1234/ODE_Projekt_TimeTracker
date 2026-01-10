package at.technikum.timetracker.ui;

import at.technikum.timetracker.model.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.UUID;
import java.util.function.Consumer;

public class TaskEditorController {
    private final VBox root = new VBox(8);

    private final TimeManager manager;
    private final Consumer<String> log;
    private final Runnable onChanged;

    private final ComboBox<String> type = new ComboBox<>();
    private final TextField name = new TextField();
    private final TextArea desc = new TextArea();
    private final TextField editTaskId = new TextField();

    public TaskEditorController(TimeManager manager, Consumer<String> log, Runnable onChanged) {
        this.manager = manager;
        this.log = log == null ? (s -> {}) : log;
        this.onChanged = onChanged == null ? () -> {} : onChanged;

    }
}