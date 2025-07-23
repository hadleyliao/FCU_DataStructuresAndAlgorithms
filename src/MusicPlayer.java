import java.util.ArrayList;
import java.util.List;

public class MusicPlayer {
    private List<String> playlist;
    private int currentIndex;

    public MusicPlayer() {
        playlist = new ArrayList<>();
        currentIndex = -1; // No song is playing initially
    }

    // 在播放清單尾端新增一首歌
    public boolean addSong(String title) {
        if (title == null || title.isEmpty()) return false;
        playlist.add(title);
        if (currentIndex == -1) {
            currentIndex = 0; // Start from the first song added
        }
        return true;
    }

    // 從清單中刪除第一個出現 title 的指定歌曲
    public boolean removeSong(String title) {
        int index = playlist.indexOf(title);
        if (index != -1) {
            playlist.remove(index);
            // 調整 currentIndex
            if (playlist.isEmpty()) {
                currentIndex = -1;
            } else if (index < currentIndex) {
                currentIndex -= 1;
            } else if (index == currentIndex) {
                if (currentIndex >= playlist.size()) {
                    currentIndex = playlist.size() - 1;
                }
            }
            return true;
        }
        return false;
    }

    // 播放下一首歌
    public boolean playNext() {
        if (playlist.isEmpty()) return false;
        if (currentIndex < playlist.size() - 1) {
            currentIndex++;
            return true;
        }
        return false;
    }

    // 播放上一首歌
    public boolean playPrevious() {
        if (playlist.isEmpty()) return false;
        if (currentIndex > 0) {
            currentIndex--;
            return true;
        }
        return false;
    }

    // 列出目前清單中的所有歌曲
    public List<String> getPlaylist() {
        return new ArrayList<>(playlist);
    }

    // 取得目前播放的歌曲
    public String getCurrentSong() {
        if (playlist.isEmpty() || currentIndex == -1) {
            return null;
        } else {
            return playlist.get(currentIndex);
        }
    }

    // 取得目前播放歌曲的索引
    public int getCurrentIndex() {
        return currentIndex;
    }

    // Main 方法示範使用
    public static void main(String[] args) {
        MusicPlayer player = new MusicPlayer();
        player.addSong("Shape of You");
        player.addSong("Believer");
        player.addSong("Counting Stars");
        System.out.println(player.getPlaylist());

        System.out.println("Current song: " + player.getCurrentSong());
        player.playNext();
        System.out.println("Current song: " + player.getCurrentSong());
        player.playNext();
        player.playNext();
        player.playPrevious();
        player.removeSong("Believer");
        System.out.println(player.getPlaylist());
        System.out.println("Current song: " + player.getCurrentSong());
    }
}