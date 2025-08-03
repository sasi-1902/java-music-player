package gui;

import database.DbAssist;
import database.DbFinder;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Map;

public class AdminPanel {

    private VBox layout;

    public void start(Stage stage) {
        stage.setTitle("Admin Panel - MAD Music Player");

        layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        buildAdminMainView(stage);

        Scene scene = new Scene(layout, 800, 500);
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    private void buildAdminMainView(Stage stage) {
        layout.getChildren().clear();

        Button addSongBtn = new Button("➕ Add Songs");
        Button manageSongBtn = new Button("✏️🗑 Edit or Remove Song");
        Button backBtn = new Button("🔙 Back to Login");

        addSongBtn.setOnAction(e -> buildAddSongOptionsView(stage));
        manageSongBtn.setOnAction(e -> buildManageSongSearchView(stage));
        backBtn.setOnAction(e -> new LoginScreen("admin").start(stage));

        layout.getChildren().addAll(addSongBtn, manageSongBtn, backBtn);
    }

    private void buildAddSongOptionsView(Stage stage) {
        layout.getChildren().clear();

        Button bulkAddBtn = new Button("📄 Add in Bulk (CSV Import)");
        Button singleAddBtn = new Button("🎹 Add Single Song");
        Button backBtn = new Button("🔙 Back to Admin Panel");

        bulkAddBtn.setOnAction(e -> buildCsvImportView(stage));
        singleAddBtn.setOnAction(e -> buildSingleSongAddView(stage));
        backBtn.setOnAction(e -> buildAdminMainView(stage));

        layout.getChildren().addAll(bulkAddBtn, singleAddBtn, backBtn);
    }

    private void buildCsvImportView(Stage stage) {
        layout.getChildren().clear();

        Label instruction = new Label("Paste or browse path to your CSV or TXT file:");
        TextField pathField = new TextField();

        Button browseBtn = new Button("📁 Browse");
        Button importBtn = new Button("📥 Import");
        Button backBtn = new Button("🔙 Back");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV or TXT File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV/TXT Files", "*.csv", "*.txt"));

        browseBtn.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) pathField.setText(file.getAbsolutePath());
        });

        importBtn.setOnAction(e -> {
            int count = DbAssist.importFromCSV(pathField.getText().trim());
            showAlert("Import Complete", count + " songs added successfully!", stage);
            buildAddSongOptionsView(stage);
        });

        backBtn.setOnAction(e -> buildAddSongOptionsView(stage));
        layout.getChildren().addAll(instruction, pathField, browseBtn, importBtn, backBtn);
    }

    private void buildSingleSongAddView(Stage stage) {
        layout.getChildren().clear();

        TextField titleField = new TextField();
        TextField artistField = new TextField();
        TextField languageField = new TextField();
        TextField genreField = new TextField();
        TextField ratingField = new TextField();

        Button addBtn = new Button("✅ Add Song");
        Button backBtn = new Button("🔙 Back");

        addBtn.setOnAction(e -> {
            try {
                String title = capitalizeWords(titleField.getText().trim());
                String artist = capitalizeWords(artistField.getText().trim());
                String language = capitalizeWords(languageField.getText().trim());
                String genre = capitalizeWords(genreField.getText().trim());
                double rating = Double.parseDouble(ratingField.getText().trim());
                String fileName = "data/audiofiles/" + title + ".mp3";

                boolean success = DbAssist.addSong(title, artist, language, genre, rating, fileName);
                showAlert(success ? "Success" : "Failure", success ? "🎉 Song added!" : "❌ Failed to add.", stage);
            } catch (Exception ex) {
                showAlert("Error", "Invalid input: " + ex.getMessage(), stage);
            }
        });

        backBtn.setOnAction(e -> buildAddSongOptionsView(stage));

        layout.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Artist:"), artistField,
                new Label("Language:"), languageField,
                new Label("Genre:"), genreField,
                new Label("Rating:"), ratingField,
                addBtn, backBtn
        );
    }

    private void buildManageSongSearchView(Stage stage) {
        layout.getChildren().clear();

        TextField titleField = new TextField();
        titleField.setPromptText("Enter song title...");

        Button searchBtn = new Button("🔍 Search");
        Button backBtn = new Button("🔙 Back");

        TableView<Map<String, String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, String>, String> titleCol = new TableColumn<>("Title");
        TableColumn<Map<String, String>, String> artistCol = new TableColumn<>("Artist");
        TableColumn<Map<String, String>, String> langCol = new TableColumn<>("Language");
        TableColumn<Map<String, String>, String> genreCol = new TableColumn<>("Genre");
        TableColumn<Map<String, String>, String> ratingCol = new TableColumn<>("Rating");
        TableColumn<Map<String, String>, Void> editCol = new TableColumn<>("✏️");
        TableColumn<Map<String, String>, Void> deleteCol = new TableColumn<>("🗑");

        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("Title")));
        artistCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("Artist")));
        langCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("Language")));
        genreCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("Genre")));
        ratingCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("Rating")));

        editCol.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("Edit");
            {
                btn.setOnAction(e -> {
                    int index = getIndex();
                    Map<String, String> songData = getTableView().getItems().get(index);
                    int songId = Integer.parseInt(songData.get("ID"));

                    Dialog<Void> dialog = new Dialog<>();
                    dialog.initModality(Modality.WINDOW_MODAL);
                    dialog.initOwner(stage);
                    dialog.setTitle("Edit Song - " + songData.get("Title"));

                    TextField titleField = new TextField(songData.get("Title"));
                    TextField artistField = new TextField(songData.get("Artist"));
                    TextField languageField = new TextField(songData.get("Language"));
                    TextField genreField = new TextField(songData.get("Genre"));
                    TextField ratingField = new TextField(songData.get("Rating"));

                    VBox form = new VBox(10,
                            new Label("Title:"), titleField,
                            new Label("Artist:"), artistField,
                            new Label("Language:"), languageField,
                            new Label("Genre:"), genreField,
                            new Label("Rating:"), ratingField
                    );
                    form.setAlignment(Pos.CENTER_LEFT);
                    dialog.getDialogPane().setContent(form);

                    ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
                    dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

                    dialog.setResultConverter(btnType -> {
                        if (btnType == saveButton) {
                            try {
                                String title = capitalizeWords(titleField.getText().trim());
                                String artist = capitalizeWords(artistField.getText().trim());
                                String language = capitalizeWords(languageField.getText().trim());
                                String genre = capitalizeWords(genreField.getText().trim());
                                double rating = Double.parseDouble(ratingField.getText().trim());

                                boolean success = DbAssist.updateSongById(songId, title, artist, language, genre, rating);
                                showAlert(success ? "Success" : "Failed", success ? "✅ Song updated." : "❌ Failed to update.", stage);
                            } catch (Exception ex) {
                                showAlert("Error", "❌ Invalid input: " + ex.getMessage(), stage);
                            }
                        }
                        return null;
                    });

                    dialog.showAndWait();
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        deleteCol.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("Delete");
            {
                btn.setOnAction(e -> {
                    int index = getIndex();
                    Map<String, String> songData = getTableView().getItems().get(index);
                    String title = songData.get("Title");
                    int songId = Integer.parseInt(songData.get("ID"));

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.initOwner(stage);
                    confirm.setTitle("Confirm Deletion");
                    confirm.setHeaderText("Delete \"" + title + "\"?");
                    confirm.setContentText("This action cannot be undone.");

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            boolean success = DbAssist.deleteSongById(songId);
                            if (success) {
                                getTableView().getItems().remove(index);
                                showAlert("Deleted", "✅ \"" + title + "\" deleted.", stage);
                            } else {
                                showAlert("Failed", "❌ Could not delete.", stage);
                            }
                        }
                    });
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(titleCol, artistCol, langCol, genreCol, ratingCol, editCol, deleteCol);

        searchBtn.setOnAction(e -> {
            table.getItems().clear();
            List<Integer> ids = DbFinder.findSongIdsByTitle(titleField.getText().trim());
            ids.stream()
                .map(id -> {
                    Map<String, String> data = DbFinder.searchSong(id);
                    if (data != null) data.put("ID", String.valueOf(id));
                    return data;
                })
                .filter(data -> data != null)
                .forEach(data -> table.getItems().add(data));
        });

        backBtn.setOnAction(e -> buildAdminMainView(stage));

        layout.getChildren().addAll(new Label("Search Song Title:"), titleField, searchBtn, table, backBtn);
    }

    private String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) return input;
        String[] words = input.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            sb.append(Character.toUpperCase(word.charAt(0)))
              .append(word.substring(1).toLowerCase())
              .append(" ");
        }
        return sb.toString().trim();
    }

    private void showAlert(String title, String message, Stage owner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}