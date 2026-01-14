package at.technikum.timetracker.ui;

import at.technikum.timetracker.model.Task;
import at.technikum.timetracker.model.TimeManager;
import at.technikum.timetracker.network.Client;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TaskListController {
    private final VBox root = new VBox(8);

    private final TimeManager manager;
    private final Consumer<String> log;

    private final Client client;

    private final TextField search = new TextField();
    private final ListView<Task> list = new ListView<>();

    private Consumer<Task> onSelectionChanged = t -> {};
    private Runnable onTasksChanged = () -> {};


    public TaskListController(TimeManager manager, Consumer<String> log, Client client) {
        this.manager = manager;
        this.log = log == null ? (s -> {}) : log;
        this.client = client;
        build();
        refresh();
    }

    public Parent getRoot() { return root; }

    public void setOnSelectionChanged(Consumer<Task> listener) {
        this.onSelectionChanged = listener == null ? (t -> {}) : listener;
    }

    public void setOnTasksChanged(Runnable r) {
        this.onTasksChanged = r == null ? () -> {} : r;
    }

    private void build() {
        Label lbl = new Label("Tasks");

        search.setPromptText("Search by name...");
        search.textProperty().addListener((obs, o, n) -> refresh());

        list.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> onSelectionChanged.accept(n));
        list.setPrefHeight(260);

        Button btnDelete = new Button("Delete selected");
        btnDelete.setOnAction(e -> {
            Task t = list.getSelectionModel().getSelectedItem();
            if (t == null) return;

            if (!AdminAuth.requireAdmin("Delete Task")) return;

            manager.deleteTask(t);
            log.accept("Task deleted: " + t.getName() + " (" + t.getType() + ")");


            if (client != null) client.sendLineAsync("DELETE_TASK|" + t.getId());

            refresh();
            onSelectionChanged.accept(null);
            onTasksChanged.run();
        });

        HBox actions = new HBox(8, btnDelete);

        root.setPadding(new Insets(10));
        root.getChildren().addAll(lbl, search, list, actions);
        root.setStyle("-fx-border-color: #444; -fx-border-radius: 6; -fx-background-radius: 6;");
    }

    public void refresh() {
        String q = search.getText() == null ? "" : search.getText().trim().toLowerCase();

        List<Task> filtered = manager.getTasks().stream()
                .filter(t -> q.isEmpty() || t.getName().toLowerCase().contains(q))
                .collect(Collectors.toList());

        list.setItems(FXCollections.observableArrayList(filtered));
    }

    public Task getSelected() {
        return list.getSelectionModel().getSelectedItem();
    }

    public void select(Task task) {
        list.getSelectionModel().select(task);
    }
}
