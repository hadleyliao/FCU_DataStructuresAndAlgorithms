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
    // 音樂播放器邏輯物件
    private MusicPlayer player = new MusicPlayer();
    // 顯示播放清單的 ListView
    private ListView<String> playlistView = new ListView<>();
    // 顯示目前播放歌曲的 Label
    private Label currentSongLabel = new Label("目前播放: 無");
    // JavaFX 媒體播放器
    private MediaPlayer mediaPlayer;
    // 播放進度條
    private Slider progressSlider = new Slider();

    @Override
    public void start(Stage primaryStage) {
        // 建立各種按鈕
        Button addBtn = new Button("➕ 新增歌曲");
        Button removeBtn = new Button("➖ 刪除歌曲");
        Button prevBtn = new Button("⏮ 上一首");
        Button nextBtn = new Button("⏭ 下一首");
        Button playBtn = new Button("▶ 播放");
        Button pauseBtn = new Button("⏸ 暫停");
        Button stopBtn = new Button("■ 停止");

        // 設定進度條初始狀態
        progressSlider.setMin(0);
        progressSlider.setMax(1);
        progressSlider.setValue(0);
        progressSlider.setDisable(true);
        progressSlider.setPrefWidth(200);

        // 新增歌曲：選擇 mp3 檔案加入清單
        addBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("新增音樂檔案");
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

        // 刪除歌曲：刪除目前選取的歌曲
        removeBtn.setOnAction(e -> {
            int idx = playlistView.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < player.getPlaylist().size()) {
                String title = player.getPlaylist().get(idx).getName();
                if (player.removeSong(title)) {
                    updatePlaylist();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("刪除歌曲");
                alert.setHeaderText(null);
                alert.setContentText("請先在清單中選擇要刪除的歌曲。");
                alert.showAndWait();
            }
        });

        // 上一首
        prevBtn.setOnAction(e -> {
            if (player.playPrevious()) {
                playCurrentSong();
                updatePlaylist();
            }
        });

        // 下一首
        nextBtn.setOnAction(e -> {
            if (player.playNext()) {
                playCurrentSong();
                updatePlaylist();
            }
        });

        // 播放
        playBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.play();
            } else {
                playCurrentSong();
            }
        });
        // 暫停
        pauseBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        });
        // 停止
        stopBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        });
        // 音量控制條
        Label volumeLabel = new Label("音量");
        Slider volumeSlider = new Slider(0, 1, 0.5); // 初始音量 0.5
        volumeSlider.setPrefWidth(200);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        // 音量調整事件
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue());
            }
        });
        // 左側控制區域
        Label progressLabel = new Label("播放進度條");
        VBox controls = new VBox(5, addBtn, removeBtn, prevBtn, nextBtn, playBtn, pauseBtn, stopBtn, progressLabel, progressSlider, volumeLabel, volumeSlider);
        // 右側顯示區域
        Label playlistLabel = new Label("播放清單");
        VBox right = new VBox(5, currentSongLabel, playlistLabel, playlistView);
        // 主畫面
        HBox root = new HBox(15, controls, right);
        root.setStyle("-fx-padding: 20;");

        updatePlaylist();

        Scene scene = new Scene(root, 500, 350);
        primaryStage.setTitle("簡易音樂播放器🎵");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // 播放目前選取的歌曲
    private void playCurrentSong() {
        File songFile = player.getCurrentSongFile();
        if (songFile != null && songFile.exists()) {
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                Media media = new Media(songFile.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                // 準備好後自動播放，並設定進度條
                mediaPlayer.setOnReady(() -> {
                    progressSlider.setDisable(false);
                    progressSlider.setMax(media.getDuration().toSeconds());
                    progressSlider.setValue(0);
                    mediaPlayer.setVolume(0.5); // 播放時設初始音量
                    mediaPlayer.play();
                });
                // 監聽播放進度
                mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    progressSlider.setValue(newTime.toSeconds());
                });
                // 播放結束時進度條歸零
                mediaPlayer.setOnEndOfMedia(() -> {
                    progressSlider.setValue(progressSlider.getMax());
                });
                // 停止時進度條歸零
                mediaPlayer.setOnStopped(() -> {
                    progressSlider.setValue(0);
                });
                // 播放錯誤提示
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

    // 更新播放清單與目前播放資訊
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

    // 主程式進入點
    public static void main(String[] args) {
        launch(args);
    }
}
