package at.technikum.timetracker.ui;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.Optional;

public final class AdminAuth {

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";

    private AdminAuth() {}

    public static boolean requireAdmin(String actionName) {
        Dialog<LoginData> dialog = new Dialog<>();
        dialog.setTitle("Admin required");
        dialog.setHeaderText("Admin login required for: " + actionName);

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        TextField userField = new TextField();
        userField.setPromptText("admin");

        PasswordField passField = new PasswordField();
        passField.setPromptText("password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Username:"), 0, 0);
        grid.add(userField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passField, 1, 1);

        GridPane.setHgrow(userField, Priority.ALWAYS);
        GridPane.setHgrow(passField, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == loginButtonType) {
                return new LoginData(userField.getText(), passField.getText());
            }
            return null;
        });

        Optional<LoginData> result = dialog.showAndWait();
        if (result.isEmpty()) return false;

        LoginData data = result.get();
        boolean ok = ADMIN_USER.equals(data.user) && ADMIN_PASS.equals(data.pass);

        if (!ok) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Access denied");
            a.setHeaderText("Access denied");
            a.setContentText("Wrong username or password.");
            a.showAndWait();
        }
        return ok;
    }

    private static final class LoginData {
        final String user;
        final String pass;

        LoginData(String user, String pass) {
            this.user = user == null ? "" : user.trim();
            this.pass = pass == null ? "" : pass;
        }
    }
}
