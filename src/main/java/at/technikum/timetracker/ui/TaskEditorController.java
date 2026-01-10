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


    private Task createTask(UUID id, String type, String name, String desc) {
        return switch (type) {
            case "DESIGN" -> new DesignTask(id, name, desc);
            case "ACCOUNTING" -> new AccountingTask(id, name, desc);
            default -> new ProgrammingTask(id, name, desc);
        };
    }

    private void clearFields() {
        name.clear();
        desc.clear();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Task");
        a.setHeaderText("Task");
        a.setContentText(msg);
        a.showAndWait();
    }
}