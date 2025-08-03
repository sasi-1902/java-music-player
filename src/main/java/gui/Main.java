package gui;

import database.DbManager;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Screen;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        DbManager.createSchemaIfNeeded();

        primaryStage.setTitle("MAD Music Player - Select Role");

        Button adminBtn = new Button("Admin");
        Button userBtn = new Button("User");

        adminBtn.setOnAction(e -> {
            saveAndStartLogin(primaryStage, "admin");
        });

        userBtn.setOnAction(e -> {
            saveAndStartLogin(primaryStage, "user");
        });

        VBox layout = new VBox(20, adminBtn, userBtn);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 400, 200);
        primaryStage.setMaximized(true); 
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void saveAndStartLogin(Stage stage, String role) {
        double width = stage.getWidth();
        double height = stage.getHeight();
        double x = stage.getX();
        double y = stage.getY();

        LoginScreen login = new LoginScreen(role);
        login.start(stage);

        // Restore dimensions and position
        stage.setX(x);
        stage.setY(y);
        stage.setWidth(width);
        stage.setHeight(height);
    }

    public static void main(String[] args) {
        launch(args);
    }
}