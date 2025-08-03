/*************************
 * 課程名稱: 資料結構與演算法
 * 對應課程: Chapter 6
 * CourseWork1: 基礎樹操作
**************************/

package _20250729;

import javax.swing.*;
import java.awt.*;

public class ArrayBinaryTreeGUI extends JPanel {
    String[] arr = {"A", "B", "C", "D", "E", "F", "G"};
    JTextField inputField;
    JButton updateButton;
    JButton deleteButton;
    JButton randomButton;
    JLabel infoLabel;
    JCheckBox inorderBox;
    JCheckBox preorderBox;
    JCheckBox postorderBox;
    JButton traverseButton;
    JTextArea resultArea;

    public ArrayBinaryTreeGUI() {
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        inputField = new JTextField(15); // 將欄位長度由30改為15
        updateButton = new JButton("更新陣列");
        deleteButton = new JButton("刪除節點");
        randomButton = new JButton("隨機產生N個數字");
        infoLabel = new JLabel("請輸入以逗號分隔的節點（如：A,B,C,D,E,F,G）");
        topPanel.add(infoLabel);
        topPanel.add(inputField);
        topPanel.add(updateButton);
        topPanel.add(deleteButton);
        topPanel.add(randomButton);
        add(topPanel, BorderLayout.NORTH);
        updateButton.addActionListener(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                arr = text.split(",");
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = arr[i].trim();
                    if (arr[i].isEmpty()) arr[i] = null;
                }
                repaint();
            }
        });
        deleteButton.addActionListener(e -> {
            String key = inputField.getText().trim();
            if (key.isEmpty()) return;
            // 找到所有等於 key 的節點
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] != null && arr[i].equals(key)) {
                    // 判斷是否為樹葉節點
                    int left = 2 * i + 1;
                    int right = 2 * i + 2;
                    boolean isLeaf = (left >= arr.length || arr[left] == null) && (right >= arr.length || arr[right] == null);
                    if (isLeaf) {
                        arr[i] = null;
                    } else {
                        // 只將該節點及其子樹設為 null，不重整陣列
                        deleteSubtree(i);
                    }
                }
            }
            repaint();
        });
        randomButton.addActionListener(e -> {
            String text = inputField.getText().trim();
            int n;
            try {
                n = Integer.parseInt(text);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "請輸入有效的整數N");
                return;
            }
            if (n <= 0) {
                JOptionPane.showMessageDialog(this, "N必須大於0");
                return;
            }
            // 產生不重複的隨機數字
            java.util.Set<Integer> set = new java.util.LinkedHashSet<>();
            java.util.Random rand = new java.util.Random();
            int max = Math.max(n * 2, 100);
            while (set.size() < n) {
                set.add(rand.nextInt(max));
            }
            // 將現有陣列擴充或覆蓋
            arr = set.stream().map(String::valueOf).toArray(String[]::new);
            repaint();
        });
        // 拜訪選項與結果顯示
        inorderBox = new JCheckBox("Inorder");
        preorderBox = new JCheckBox("Preorder");
        postorderBox = new JCheckBox("Postorder");
        traverseButton = new JButton("拜訪");
        resultArea = new JTextArea(5, 60);
        resultArea.setEditable(false);
        JPanel traversePanel = new JPanel();
        traversePanel.add(new JLabel("選擇拜訪方式："));
        traversePanel.add(inorderBox);
        traversePanel.add(preorderBox);
        traversePanel.add(postorderBox);
        traversePanel.add(traverseButton);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(traversePanel);
        bottomPanel.add(new JScrollPane(resultArea));
        add(bottomPanel, BorderLayout.SOUTH);
        traverseButton.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            if (inorderBox.isSelected()) {
                sb.append("Inorder: ");
                inorder(0, sb);
                sb.append("\n");
            }
            if (preorderBox.isSelected()) {
                sb.append("Preorder: ");
                preorder(0, sb);
                sb.append("\n");
            }
            if (postorderBox.isSelected()) {
                sb.append("Postorder: ");
                postorder(0, sb);
                sb.append("\n");
            }
            if (!inorderBox.isSelected() && !preorderBox.isSelected() && !postorderBox.isSelected()) {
                sb.append("請至少選擇一種拜訪方式！\n");
            }
            resultArea.setText(sb.toString());
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawTree(g, 0, getWidth() / 2 - 15, 80, 120);
    }

    void drawTree(Graphics g, int index, int x, int y, int xOffset) {
        if (index >= arr.length || arr[index] == null) return;
        g.drawOval(x, y, 30, 30);
        g.drawString(arr[index], x + 12, y + 20);
        int left = 2 * index + 1;
        if (left < arr.length && arr[left] != null) {
            g.drawLine(x + 15, y + 30, x - xOffset + 15, y + 80);
            drawTree(g, left, x - xOffset, y + 80, xOffset / 2);
        }
        int right = 2 * index + 2;
        if (right < arr.length && arr[right] != null) {
            g.drawLine(x + 15, y + 30, x + xOffset + 15, y + 80);
            drawTree(g, right, x + xOffset, y + 80, xOffset / 2);
        }
    }

    // 遞迴刪除子樹
    void deleteSubtree(int index) {
        if (index >= arr.length || arr[index] == null) return;
        arr[index] = null;
        deleteSubtree(2 * index + 1);
        deleteSubtree(2 * index + 2);
    }

    // inorder 拜訪
    void inorder(int index, StringBuilder sb) {
        if (index >= arr.length || arr[index] == null) return;
        inorder(2 * index + 1, sb);
        sb.append(arr[index]).append(" ");
        inorder(2 * index + 2, sb);
    }

    // preorder 拜訪
    void preorder(int index, StringBuilder sb) {
        if (index >= arr.length || arr[index] == null) return;
        sb.append(arr[index]).append(" ");
        preorder(2 * index + 1, sb);
        preorder(2 * index + 2, sb);
    }

    // postorder 拜訪
    void postorder(int index, StringBuilder sb) {
        if (index >= arr.length || arr[index] == null) return;
        postorder(2 * index + 1, sb);
        postorder(2 * index + 2, sb);
        sb.append(arr[index]).append(" ");
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Array Binary Tree Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.add(new ArrayBinaryTreeGUI());
        frame.setVisible(true);
    }
}
