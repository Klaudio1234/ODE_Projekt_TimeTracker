package at.technikum.timetracker.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        MainController controller = new MainController();
        Scene scene = new Scene(controller.getRoot(), 1100, 650);
        stage.setTitle("ODE TimeTracker");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(e -> controller.shutdown());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
