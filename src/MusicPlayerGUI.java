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

    @Override
    public void start(Stage primaryStage) {
        TextField songInput = new TextField();
        songInput.setPromptText("輸入歌曲名稱");
        Button addBtn = new Button("新增歌曲");
        Button removeBtn = new Button("刪除歌曲");
        Button prevBtn = new Button("上一首");
        Button nextBtn = new Button("下一首");
        Button fileBtn = new Button("選擇檔案");

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

        VBox controls = new VBox(5, songInput, addBtn, fileBtn, removeBtn, prevBtn, nextBtn);
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
                    mediaPlayer.play();
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
