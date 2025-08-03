package _20250701;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

public class ImageViewer extends JFrame {
    private JLabel imageLabel;
    private JButton prevButton, nextButton, chooseDirButton;
    private File[] imageFiles;
    private int currentIndex = -1;
    private File currentDir;

    public ImageViewer() {
        setTitle("Java 看圖軟體");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 圖片顯示區
        imageLabel = new JLabel("請選擇圖片目錄", SwingConstants.CENTER);
        imageLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        add(imageLabel, BorderLayout.CENTER);

        // 按鈕區
        JPanel buttonPanel = new JPanel();
        prevButton = new JButton("上一張");
        nextButton = new JButton("下一張");
        chooseDirButton = new JButton("選擇圖片目錄");
        buttonPanel.add(prevButton);
        buttonPanel.add(chooseDirButton);
        buttonPanel.add(nextButton);
        add(buttonPanel, BorderLayout.SOUTH);

        prevButton.addActionListener(e -> showImage(currentIndex - 1));
        nextButton.addActionListener(e -> showImage(currentIndex + 1));
        chooseDirButton.addActionListener(e -> chooseDirectory());

        updateButtonState();
    }

    private void chooseDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentDir = chooser.getSelectedFile();
            imageFiles = currentDir.listFiles((dir, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".bmp");
            });
            if (imageFiles != null && imageFiles.length > 0) {
                Arrays.sort(imageFiles);
                currentIndex = 0;
                showImage(currentIndex);
            } else {
                imageLabel.setText("此目錄沒有圖片");
                currentIndex = -1;
                updateButtonState();
            }
        }
    }

    private void showImage(int index) {
        if (imageFiles == null || imageFiles.length == 0 || index < 0 || index >= imageFiles.length) {
            return;
        }
        currentIndex = index;
        ImageIcon icon = new ImageIcon(imageFiles[currentIndex].getAbsolutePath());
        // 圖片縮小為原始 1/2
        Image img = icon.getImage();
        int w = img.getWidth(null) / 2;
        int h = img.getHeight(null) / 2;
        if (w > 0 && h > 0) {
            img = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
        }
        imageLabel.setIcon(icon);
        imageLabel.setText(null);
        updateButtonState();
    }

    private void updateButtonState() {
        boolean hasImages = imageFiles != null && imageFiles.length > 0 && currentIndex >= 0;
        prevButton.setEnabled(hasImages && currentIndex > 0);
        nextButton.setEnabled(hasImages && currentIndex < imageFiles.length - 1);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImageViewer().setVisible(true));
    }
}

