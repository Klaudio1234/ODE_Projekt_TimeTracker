package at.technikum.timetracker.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
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

    public MainController() {
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
}
