package gui;

import backend.AuthManager;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginScreen {
    private final String role;

    public LoginScreen(String role) {
        this.role = role;
    }

    public void start(Stage stage) {
        // Save current window state
        double width = stage.getWidth();
        double height = stage.getHeight();
        double x = stage.getX();
        double y = stage.getY();

        // UI Elements
        Label passLabel = new Label("Enter password for " + role + ":");
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");
        Button backButton = new Button("Back");
        Label messageLabel = new Label();

        passwordField.setOnAction(e -> loginButton.fire());

        loginButton.setOnAction(e -> {
            String password = passwordField.getText().trim();
            boolean isValid = AuthManager.authenticate(role, password);

            if (isValid) {
                if ("admin".equals(role)) {
                    new AdminPanel().start(stage);
                } else {
                    new UserDashboard().start(stage);
                }
                restoreStage(stage, width, height, x, y);
            } else {
                messageLabel.setText("Incorrect password.");
            }
        });

        backButton.setOnAction(e -> {
            new Main().start(stage);
            restoreStage(stage, width, height, x, y);
        });

        VBox layout = new VBox(10, passLabel, passwordField, loginButton, backButton, messageLabel);
        layout.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(layout, width, height));
    }

    private void restoreStage(Stage stage, double width, double height, double x, double y) {
        stage.setX(x);
        stage.setY(y);
        stage.setWidth(width);
        stage.setHeight(height);
    }
}