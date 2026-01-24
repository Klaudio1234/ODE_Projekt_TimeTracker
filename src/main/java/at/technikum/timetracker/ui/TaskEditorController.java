package at.technikum.timetracker.ui;

import at.technikum.timetracker.model.*;
import at.technikum.timetracker.network.Client;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.UUID;
import java.util.function.Consumer;

public class TaskEditorController {
    private final VBox root = new VBox(8);

    private final TimeManager manager;
    private final Consumer<String> log;
    private final Runnable onChanged;

    private final Client client;

    private final ComboBox<String> type = new ComboBox<>();
    private final TextField name = new TextField();
    private final TextArea desc = new TextArea();
    private final TextField editTaskId = new TextField();


    public TaskEditorController(TimeManager manager, Consumer<String> log, Client client, Runnable onChanged) {
        this.manager = manager;
        this.log = log == null ? (s -> {}) : log;
        this.client = client;
        this.onChanged = onChanged == null ? () -> {} : onChanged;
        build();
    }

    public Parent getRoot() { return root; }

    private void build() {
        Label lbl = new Label("Create / Edit Task");

        type.getItems().addAll("PROGRAMMING", "DESIGN", "ACCOUNTING");
        type.getSelectionModel().selectFirst();

        name.setPromptText("Task name");
        desc.setPromptText("Description");
        desc.setPrefRowCount(3);

        editTaskId.setPromptText("Task ID to edit");
        editTaskId.setEditable(true);

        Button btnCreate = new Button("Create Task");
        btnCreate.setOnAction(e -> {

            if (!AdminAuth.requireAdmin("Create Task")) return;

            try {
                Task t = createTask(UUID.randomUUID(), type.getValue(), name.getText(), desc.getText());
                manager.addTask(t);
                log.accept("Task created: " + t.getName() + " (" + t.getType() + ")");


                if (client != null) {
                    client.sendLineAsync("TASK|" + t.getId()
                            + "|" + safe(t.getType())
                            + "|" + safe(t.getName())
                            + "|" + safe(t.getDescription()));
                }

                clearFields();
                onChanged.run();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        Button btnLoadForEdit = new Button("Load by ID");
        btnLoadForEdit.setOnAction(e -> {
            try {
                UUID id = UUID.fromString(editTaskId.getText().trim());
                Task t = manager.findTask(id).orElse(null);
                if (t == null) { showError("Task not found"); return; }

                type.setValue(t.getType());
                name.setText(t.getName());
                desc.setText(t.getDescription());

                log.accept("Loaded for edit: " + t.getName() + " (" + t.getType() + ")");
            } catch (Exception ex) {
                showError("Invalid ID");
            }
        });

        Button btnApplyEdit = new Button("Apply Edit (by ID)");
        btnApplyEdit.setOnAction(e -> {

            if (!AdminAuth.requireAdmin("Edit Task")) return;

            try {
                UUID id = UUID.fromString(editTaskId.getText().trim());
                Task t = manager.findTask(id).orElse(null);
                if (t == null) { showError("Task not found"); return; }

                String newType = type.getValue();


                if (!t.getType().equals(newType)) {
                    Task replacement = createTask(t.getId(), newType, name.getText(), desc.getText());
                    manager.deleteTask(t);
                    manager.addTask(replacement);
                    log.accept("Task type changed and replaced: " + replacement.getName() + " (" + replacement.getType() + ")");


                    if (client != null) {
                        client.sendLineAsync("DELETE_TASK|" + t.getId());
                        client.sendLineAsync("TASK|" + replacement.getId()
                                + "|" + safe(replacement.getType())
                                + "|" + safe(replacement.getName())
                                + "|" + safe(replacement.getDescription()));
                    }
                } else {
                    manager.updateTask(t, name.getText(), desc.getText());
                    log.accept("Task updated: " + t.getName() + " (" + t.getType() + ")");


                    if (client != null) {
                        client.sendLineAsync("UPDATE_TASK|" + t.getId()
                                + "|" + safe(name.getText())
                                + "|" + safe(desc.getText()));
                    }
                }

                onChanged.run();
            } catch (Exception ex) {
                showError("Edit failed: " + ex.getMessage());
            }
        });

        HBox row1 = new HBox(8, new Label("Type:"), type);
        HBox row2 = new HBox(8, new Label("ID:"), editTaskId, btnLoadForEdit);
        HBox buttons = new HBox(8, btnCreate, btnApplyEdit);

        root.setPadding(new Insets(10));
        root.getChildren().addAll(lbl, row1, name, desc, row2, buttons);
        root.setStyle("-fx-border-color: #444; -fx-border-radius: 6; -fx-background-radius: 6;");
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


    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("|", " ").replace("\n", " ").trim();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Task");
        a.setHeaderText("Task");
        a.setContentText(msg);
        a.showAndWait();
    }
}
