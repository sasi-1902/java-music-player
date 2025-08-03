package backend;

import database.DbFinder;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Map;

public class AudioPlayer {

    private static MediaPlayer currentPlayer;

    public static void playSong(int songId, Stage parentStage) {
        System.out.println("🔁 playSong called with ID: " + songId);
        Map<String, String> songData = DbFinder.searchSong(songId);
        if (songData == null) {
            System.err.println("❌ Song not found with ID: " + songId);
            return;
        }

        String relativePath = songData.get("file_name");
        File audioFile = new File(relativePath);
        String absolutePath = audioFile.getAbsolutePath();

        System.out.println("🔊 Attempting to play: " + absolutePath);

        if (!audioFile.exists()) {
            System.err.println("❌ Audio file does not exist at path: " + absolutePath);
            return;
        }

        if (currentPlayer != null) {
            currentPlayer.stop();
        }

        try {
            Media media = new Media(audioFile.toURI().toString());
            currentPlayer = new MediaPlayer(media);
        } catch (Exception e) {
            System.err.println("❌ Failed to load media: " + e.getMessage());
            return;
        }

        Platform.runLater(() -> {
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initOwner(parentStage); // ✅ Show on same desktop/window
            popup.setTitle("🎵 Now Playing: " + songData.get("Title"));

            Label title = new Label("🎧 " + songData.get("Title") + " — " + songData.get("Artist"));
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Button playPauseBtn = new Button("⏸ Pause");
            Slider seekBar = new Slider();
            Slider volumeSlider = new Slider(0, 1, 0.7);

            currentPlayer.setVolume(0.7);

            currentPlayer.setOnReady(() -> seekBar.setMax(currentPlayer.getTotalDuration().toSeconds()));

            // ✅ Pause/Resume
            playPauseBtn.setOnAction(e -> {
                if (currentPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    currentPlayer.pause();
                    playPauseBtn.setText("▶️ Play");
                } else {
                    currentPlayer.play();
                    playPauseBtn.setText("⏸ Pause");
                }
            });

            // ✅ Sync seek bar
            currentPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!seekBar.isValueChanging()) {
                    seekBar.setValue(newTime.toSeconds());
                }
            });

            // ✅ Dragging support
            seekBar.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                if (!isChanging) {
                    currentPlayer.seek(Duration.seconds(seekBar.getValue()));
                }
            });

            // ✅ Clickable support
            seekBar.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                double percent = e.getX() / seekBar.getWidth();
                Duration seekTo = currentPlayer.getTotalDuration().multiply(percent);
                currentPlayer.seek(seekTo);
            });

            // ✅ Volume control
            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                currentPlayer.setVolume(newVal.doubleValue());
            });

            // ✅ Stop song when window closes
            popup.setOnCloseRequest(e -> {
                if (currentPlayer != null) currentPlayer.stop();
            });

            VBox layout = new VBox(12, title, playPauseBtn,
                    new Label("Seek:"), seekBar,
                    new Label("Volume:"), volumeSlider);
            layout.setStyle("-fx-padding: 20;");
            layout.setAlignment(Pos.CENTER);

            popup.setScene(new Scene(layout, 420, 300));
            popup.show();

            currentPlayer.play();
        });
    }
}