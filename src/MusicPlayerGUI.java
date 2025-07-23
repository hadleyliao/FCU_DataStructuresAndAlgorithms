import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MusicPlayerGUI extends Application {
    private MusicPlayer player = new MusicPlayer();
    private ListView<String> playlistView = new ListView<>();
    private Label currentSongLabel = new Label("目前播放: 無");

    @Override
    public void start(Stage primaryStage) {
        TextField songInput = new TextField();
        songInput.setPromptText("輸入歌曲名稱");
        Button addBtn = new Button("新增歌曲");
        Button removeBtn = new Button("刪除歌曲");
        Button prevBtn = new Button("上一首");
        Button nextBtn = new Button("下一首");

        addBtn.setOnAction(e -> {
            String title = songInput.getText().trim();
            if (player.addSong(title)) {
                updatePlaylist();
                songInput.clear();
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
            player.playPrevious();
            updatePlaylist();
        });

        nextBtn.setOnAction(e -> {
            player.playNext();
            updatePlaylist();
        });

        VBox controls = new VBox(5, songInput, addBtn, removeBtn, prevBtn, nextBtn);
        VBox right = new VBox(10, currentSongLabel, playlistView);
        HBox root = new HBox(15, controls, right);
        root.setStyle("-fx-padding: 20;");

        updatePlaylist();

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("簡易音樂播放器");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updatePlaylist() {
        playlistView.getItems().setAll(player.getPlaylist());
        String song = player.getCurrentSong();
        currentSongLabel.setText("目前播放: " + (song == null ? "無" : song));
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

