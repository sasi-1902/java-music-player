package gui;

import backend.SongManager;
import database.DbFinder;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserDashboard {

    public void start(Stage stage) {
        double width = stage.getWidth();
        double height = stage.getHeight();
        double x = stage.getX();
        double y = stage.getY();

        Label welcomeLabel = new Label("🎵 Welcome to MAD Music Player!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button quickPlayBtn = new Button("🎲 Quick Play");
        Button exploreBtn = new Button("🌐 Explore");
        Button searchBtn = new Button("🔍 Search");
        Button backBtn = new Button("🔙 Back to Login");

        quickPlayBtn.setOnAction(e -> {
            System.out.println("🎲 Quick Play clicked");
            List<Integer> allSongIds = DbFinder.getAllSongs();
            System.out.println("Found song IDs: " + allSongIds);
        
            if (allSongIds == null || allSongIds.isEmpty()) {
                showMessage("Oops", "No songs available to play!");
                return;
            }
        
            int randomIndex = (int) (Math.random() * allSongIds.size());
            System.out.println("Random index: " + randomIndex);
            PlayerControls.playPlaylist(allSongIds, randomIndex, true, stage);
        });

        exploreBtn.setOnAction(e -> showExploreScene(stage));
        searchBtn.setOnAction(e -> showSearchScene(stage));
        backBtn.setOnAction(e -> new LoginScreen("user").start(stage));

        VBox layout = new VBox(20, welcomeLabel, quickPlayBtn, exploreBtn, searchBtn, backBtn);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, width, height);
        stage.setScene(scene);
        stage.setTitle("User Dashboard - MAD Music Player");
        stage.setX(x);
        stage.setY(y);
        stage.show();
    }

    private void showExploreScene(Stage stage) {
        double width = stage.getWidth();
        double height = stage.getHeight();
        double x = stage.getX();
        double y = stage.getY();

        Label heading = new Label("🌍 Explore by:");
        heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button langBtn = new Button("Languages");
        Button artistBtn = new Button("Artists");
        Button genreBtn = new Button("Genres");
        Button backBtn = new Button("🔙 Back");

        langBtn.setOnAction(e -> showSubcategories(stage, "Language", DbFinder.getAllLanguages()));
        artistBtn.setOnAction(e -> showSubcategories(stage, "Artist", DbFinder.getAllArtists()));
        genreBtn.setOnAction(e -> showSubcategories(stage, "Genre", DbFinder.getAllGenres()));

        backBtn.setOnAction(e -> start(stage));

        VBox exploreLayout = new VBox(15, heading, langBtn, artistBtn, genreBtn, backBtn);
        exploreLayout.setAlignment(Pos.CENTER);
        exploreLayout.setPrefWidth(600);

        stage.setScene(new Scene(exploreLayout, width, height));
        stage.setX(x);
        stage.setY(y);
    }

    private void showSubcategories(Stage stage, String categoryType, List<String> subcategories) {
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.getChildren().add(new Label("🔍 Select " + categoryType + ":"));
    
        subcategories.stream()
                .sorted(String::compareToIgnoreCase)
                .forEach(sub -> {
                    Button btn = new Button(sub);
                    btn.setMaxWidth(Double.MAX_VALUE);
                    btn.setOnAction(e -> showSongsByCategory(stage, categoryType, sub));
                    content.getChildren().add(btn);
                });
    
        Button backBtn = new Button("🔙 Back");
        backBtn.setOnAction(e -> showExploreScene(stage));
        content.getChildren().add(backBtn);
    
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(500);
    
        stage.setScene(new Scene(scrollPane, 600, 500));
    }

    private void showSongsByCategory(Stage stage, String categoryType, String value) {
        List<Integer> originalIds = SongManager.getSongsByCategory(categoryType, value);
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        TableView<Map<String, String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, String>, String> titleCol = new TableColumn<>("Title");
        TableColumn<Map<String, String>, String> artistCol = new TableColumn<>("Artist");
        TableColumn<Map<String, String>, Void> playCol = new TableColumn<>("▶ Start from here");

        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("Title")));
        artistCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("Artist")));

        List<Map<String, String>> songs = new ArrayList<>();
        List<Integer> sortedIds = new ArrayList<>();

        for (int id : originalIds) {
            Map<String, String> song = DbFinder.searchSong(id);
            if (song != null) {
                song.put("ID", String.valueOf(id));
                songs.add(song);
            }
        }

        songs.sort((a, b) -> a.get("Title").compareToIgnoreCase(b.get("Title")));
        songs.forEach(song -> {
            table.getItems().add(song);
            sortedIds.add(Integer.parseInt(song.get("ID")));
        });

        playCol.setCellFactory(tc -> new TableCell<>() {
            private final Button playBtn = new Button("▶");

            {
                playBtn.setOnAction(e -> {
                    int index = getIndex();
                    PlayerControls.playPlaylist(sortedIds, index, false, stage);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : playBtn);
            }
    });

    table.getColumns().addAll(titleCol, artistCol, playCol);

    Button backBtn = new Button("🔙 Back");
    backBtn.setOnAction(e -> showSubcategories(stage, categoryType, switch (categoryType) {
        case "Language" -> DbFinder.getAllLanguages();
        case "Artist" -> DbFinder.getAllArtists();
        case "Genre" -> DbFinder.getAllGenres();
        default -> List.of();
    }));

    layout.getChildren().addAll(new Label("🎼 Songs in " + value + ":"), table, backBtn);

    ScrollPane scrollPane = new ScrollPane(layout);
    scrollPane.setFitToWidth(true);
    scrollPane.setPrefViewportHeight(600);

    stage.setScene(new Scene(scrollPane, 800, 600));
}

    private void showSearchScene(Stage stage) {
        double width = stage.getWidth();
        double height = stage.getHeight();
        double x = stage.getX();
        double y = stage.getY();

        TextField searchField = new TextField();
        searchField.setPromptText("Type song title...");

        TableView<Map<String, String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, String>, String> titleCol = new TableColumn<>("Title");
        TableColumn<Map<String, String>, String> artistCol = new TableColumn<>("Artist");
        TableColumn<Map<String, String>, Void> playCol = new TableColumn<>("▶");

        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("Title")));
        artistCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("Artist")));

        playCol.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("Play");

            {
                btn.setOnAction(e -> {
                    Map<String, String> songData = getTableView().getItems().get(getIndex());
                    try {
                        int songId = Integer.parseInt(songData.get("ID"));
                        backend.AudioPlayer.playSong(songId, stage);
                    } catch (Exception ex) {
                        showMessage("Error", "❌ Unable to play this song.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(titleCol, artistCol, playCol);

        searchField.textProperty().addListener((obs, oldText, newText) -> {
            table.getItems().clear();
            if (newText.isBlank()) return;
            List<Map<String, String>> results = DbFinder.searchSongsByTitleLike(newText);
            table.getItems().addAll(results);
        });

        Button backBtn = new Button("🔙 Back");
        backBtn.setOnAction(e -> start(stage));

        VBox searchLayout = new VBox(15, new Label("🔎 Search Song Titles:"), searchField, table, backBtn);
        searchLayout.setAlignment(Pos.CENTER);
        searchLayout.setPrefWidth(600);

        stage.setScene(new Scene(searchLayout, width, height));
    }

    private void showMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}