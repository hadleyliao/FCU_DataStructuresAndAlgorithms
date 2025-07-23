import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class MusicPlayerGUI extends Application {
    private MusicPlayer player = new MusicPlayer();
    private ListView<String> playlistView = new ListView<>();
    private Label currentSongLabel = new Label("目前播放: 無");
    private MediaPlayer mediaPlayer;
    private Slider progressSlider = new Slider();

    @Override
    public void start(Stage primaryStage) {
        TextField songInput = new TextField();
        songInput.setPromptText("輸入歌曲名稱");
        Button addBtn = new Button("➕ 新增歌曲");
        Button removeBtn = new Button("➖ 刪除歌曲");
        Button prevBtn = new Button("⏮ 上一首");
        Button nextBtn = new Button("⏭ 下一首");
        Button fileBtn = new Button("選擇檔案");
        Button playBtn = new Button("▶ 播放");
        Button pauseBtn = new Button("⏸ 暫停");
        Button stopBtn = new Button("■ 停止");

        progressSlider.setMin(0);
        progressSlider.setMax(1);
        progressSlider.setValue(0);
        progressSlider.setDisable(true);
        progressSlider.setPrefWidth(200);

        addBtn.setOnAction(e -> {
            // 只新增歌名（不推薦，建議用選檔案）
            String title = songInput.getText().trim();
            if (!title.isEmpty()) {
                File fakeFile = new File(title); // 只是示意
                if (player.addSong(fakeFile)) {
                    updatePlaylist();
                    songInput.clear();
                }
            }
        });

        fileBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("選擇音樂檔案");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("MP3 Files", "*.mp3")
            );
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                if (player.addSong(file)) {
                    updatePlaylist();
                }
            }
        });

        removeBtn.setOnAction(e -> {
            String title = songInput.getText().trim();
            if (player.removeSong(title)) {
                updatePlaylist();
                songInput.clear();
            }
        });

        prevBtn.setOnAction(e -> {
            if (player.playPrevious()) {
                playCurrentSong();
                updatePlaylist();
            }
        });

        nextBtn.setOnAction(e -> {
            if (player.playNext()) {
                playCurrentSong();
                updatePlaylist();
            }
        });

        playBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.play();
            } else {
                playCurrentSong();
            }
        });
        pauseBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        });
        stopBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        });
        VBox controls = new VBox(5, fileBtn, songInput, addBtn, removeBtn, prevBtn, nextBtn, playBtn, pauseBtn, stopBtn, progressSlider);
        VBox right = new VBox(10, currentSongLabel, playlistView);
        HBox root = new HBox(15, controls, right);
        root.setStyle("-fx-padding: 20;");

        updatePlaylist();

        Scene scene = new Scene(root, 500, 350);
        primaryStage.setTitle("簡易音樂播放器");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void playCurrentSong() {
        File songFile = player.getCurrentSongFile();
        if (songFile != null && songFile.exists()) {
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                Media media = new Media(songFile.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setOnReady(() -> {
                    progressSlider.setDisable(false);
                    progressSlider.setMax(media.getDuration().toSeconds());
                    progressSlider.setValue(0);
                    mediaPlayer.play();
                });
                mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    progressSlider.setValue(newTime.toSeconds());
                });
                mediaPlayer.setOnEndOfMedia(() -> {
                    progressSlider.setValue(progressSlider.getMax());
                });
                mediaPlayer.setOnStopped(() -> {
                    progressSlider.setValue(0);
                });
                mediaPlayer.setOnError(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("播放錯誤");
                    alert.setHeaderText("無法播放音樂檔案");
                    StringBuilder msg = new StringBuilder();
                    if (media.getError() != null) {
                        msg.append(media.getError().getMessage()).append("\n");
                    }
                    if (mediaPlayer.getError() != null) {
                        msg.append(mediaPlayer.getError().getMessage()).append("\n");
                    }
                    alert.setContentText(msg.length() > 0 ? msg.toString() : "未知錯誤");
                    alert.showAndWait();
                });
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("播放錯誤");
                alert.setHeaderText("發生例外");
                alert.setContentText(ex.toString());
                alert.showAndWait();
            }
        } else {
            progressSlider.setDisable(true);
            progressSlider.setValue(0);
        }
    }

    private void updatePlaylist() {
        playlistView.getItems().clear();
        for (File f : player.getPlaylist()) {
            playlistView.getItems().add(f.getName());
        }
        File song = player.getCurrentSongFile();
        currentSongLabel.setText("目前播放: " + (song == null ? "無" : song.getName()));
        int idx = player.getCurrentIndex();
        if (idx >= 0 && idx < playlistView.getItems().size()) {
            playlistView.getSelectionModel().select(idx);
        } else {
            playlistView.getSelectionModel().clearSelection();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
