package gui;

import database.DbFinder;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlayerControls {
    private static MediaPlayer currentPlayer;
    private static int currentIndex;
    private static List<Integer> songIds;

    // 🔊 Store current volume globally
    private static double currentVolume = 0.7;

    public static void playPlaylist(List<Integer> ids, int startIndex, boolean shuffle, Stage parentStage) {
        if (ids == null || ids.isEmpty()) return;

        songIds = new ArrayList<>(ids);
        if (shuffle) Collections.shuffle(songIds);

        currentIndex = startIndex >= 0 && startIndex < songIds.size() ? startIndex : 0;

        playCurrent(parentStage);
    }

    private static void playCurrent(Stage parentStage) {
        if (songIds.isEmpty()) return;

        int songId = songIds.get(currentIndex);
        Map<String, String> songData = DbFinder.searchSong(songId);

        if (songData == null) {
            next(parentStage);
            return;
        }

        File audioFile = new File(songData.get("file_name"));
        if (!audioFile.exists()) {
            next(parentStage);
            return;
        }

        Media media = new Media(audioFile.toURI().toString());
        if (currentPlayer != null) currentPlayer.stop();
        currentPlayer = new MediaPlayer(media);

        Platform.runLater(() -> {
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initOwner(parentStage);
            popup.setTitle("🎵 Playlist Player");

            Label title = new Label("🎧 " + songData.get("Title") + " — " + songData.get("Artist"));
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Button playPauseBtn = new Button("⏸ Pause");
            Button nextBtn = new Button("⏭ Next");
            Button prevBtn = new Button("⏮ Prev");

            Slider seekBar = new Slider();
            Slider volumeSlider = new Slider(0, 1, currentVolume); // 🔊 Use currentVolume

            currentPlayer.setVolume(currentVolume); // 🔊 Apply retained volume

            currentPlayer.setOnReady(() -> seekBar.setMax(currentPlayer.getTotalDuration().toSeconds()));

            currentPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!seekBar.isValueChanging()) {
                    seekBar.setValue(newTime.toSeconds());
                }
            });

            seekBar.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                if (!isChanging) {
                    currentPlayer.seek(Duration.seconds(seekBar.getValue()));
                }
            });

            seekBar.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                double percent = e.getX() / seekBar.getWidth();
                Duration seekTo = currentPlayer.getTotalDuration().multiply(percent);
                currentPlayer.seek(seekTo);
            });

            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                currentVolume = newVal.doubleValue(); // 🔄 Save new volume
                currentPlayer.setVolume(currentVolume);
            });

            playPauseBtn.setOnAction(e -> {
                if (currentPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    currentPlayer.pause();
                    playPauseBtn.setText("▶️ Play");
                } else {
                    currentPlayer.play();
                    playPauseBtn.setText("⏸ Pause");
                }
            });

            nextBtn.setOnAction(e -> {
                popup.close();
                next(parentStage);
            });

            prevBtn.setOnAction(e -> {
                popup.close();
                previous(parentStage);
            });

            popup.setOnCloseRequest(e -> {
                if (currentPlayer != null) currentPlayer.stop();
            });

            HBox controls = new HBox(10, prevBtn, playPauseBtn, nextBtn);
            controls.setAlignment(Pos.CENTER);

            VBox layout = new VBox(12, title,
                    new Label("Seek:"), seekBar,
                    new Label("Volume:"), volumeSlider,
                    controls);
            layout.setStyle("-fx-padding: 20;");
            layout.setAlignment(Pos.CENTER);

            popup.setScene(new Scene(layout, 460, 300));
            popup.show();

            currentPlayer.play();
        });
    }

    private static void next(Stage stage) {
        currentIndex = (currentIndex + 1) % songIds.size();
        playCurrent(stage);
    }

    private static void previous(Stage stage) {
        currentIndex = (currentIndex - 1 + songIds.size()) % songIds.size();
        playCurrent(stage);
    }
}