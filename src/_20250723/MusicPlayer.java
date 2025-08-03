/***********************************
 * 對應課程: Chapter 3
 * CourseWork1: 具清單功能的音樂撥放器
 ***********************************/

package _20250723;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer {
    private List<File> playlist;
    private int currentIndex;

    public MusicPlayer() {
        playlist = new ArrayList<>();
        currentIndex = -1; // No song is playing initially
    }

    // 在播放清單尾端新增一首歌
    public boolean addSong(File file) {
        if (file == null || !file.exists()) return false;
        playlist.add(file);
        if (currentIndex == -1) {
            currentIndex = 0; // Start from the first song added
        }
        return true;
    }

    // 從清單中刪除第一個出現 title 的指定歌曲
    public boolean removeSong(String title) {
        for (int i = 0; i < playlist.size(); i++) {
            if (playlist.get(i).getName().equals(title)) {
                playlist.remove(i);
                // 調整 currentIndex
                if (playlist.isEmpty()) {
                    currentIndex = -1;
                } else if (i < currentIndex) {
                    currentIndex -= 1;
                } else if (i == currentIndex) {
                    if (currentIndex >= playlist.size()) {
                        currentIndex = playlist.size() - 1;
                    }
                }
                return true;
            }
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
    public List<File> getPlaylist() {
        return playlist;
    }

    // 取得目前播放的歌曲
    public File getCurrentSongFile() {
        if (currentIndex >= 0 && currentIndex < playlist.size()) {
            return playlist.get(currentIndex);
        }
        return null;
    }

    // 取得目前播放歌曲的索引
    public int getCurrentIndex() {
        return currentIndex;
    }

    // Main 方法示範使用
    public static void main(String[] args) {
        MusicPlayer player = new MusicPlayer();
        player.addSong(new File("Shape of You.mp3"));
        player.addSong(new File("Believer.mp3"));
        player.addSong(new File("Counting Stars.mp3"));
        System.out.println(player.getPlaylist());

        System.out.println("Current song: " + player.getCurrentSongFile());
        player.playNext();
        System.out.println("Current song: " + player.getCurrentSongFile());
        player.playNext();
        player.playNext();
        player.playPrevious();
        player.removeSong("Believer.mp3");
        System.out.println(player.getPlaylist());
        System.out.println("Current song: " + player.getCurrentSongFile());
    }
}