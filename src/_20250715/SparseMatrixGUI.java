/****************************************************
 * 對應課程: Chapter 2
 * CourseWork2: 矩陣生成, 矩陣轉置, 矩陣加法, 效能評估程式
 ****************************************************/

package _20250715;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Random;

public class SparseMatrixGUI extends JFrame {
    private JTextField sizeField;
    private JTextField densityField;
    private JButton generateButton;
    private JButton showSparseButton;
    private JButton generateSecondButton;
    private JButton showSparseButton2;
    private JButton addButton;
    private JButton subButton;
    private JButton transposeButton; // 新增轉置按鈕
    private JButton transposeOriginButton; // 新增原始矩陣轉置按鈕
    private JButton fastTransposeAvgButton; // 新增快速轉置平均按鈕
    private JButton originTransposeAvgButton; // 新增原始轉置平均按鈕
    private JTable matrixTable;
    private JTable matrixTable2;
    private JTextArea sparseTextArea;
    private JTextArea sparseTextArea2;
    private JTextArea resultTextArea; // 新增結果顯示區
    private int[][] lastMatrix;
    private int[][] lastMatrix2;
    private int[][] transposedMatrix; // 儲存轉置結果
    private JLabel messageLabel; // 新增訊息標籤
    private JLabel timeLabel; // 顯示執行時間

    public SparseMatrixGUI() {
        setTitle("Sparse Matrix Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 1000);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Message label for errors or info
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
        messageLabel.setForeground(Color.RED);
        messageLabel.setPreferredSize(new Dimension(600, 30));
        add(messageLabel, BorderLayout.NORTH);

        // Top input & operation panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // 將左右間距縮小為10

        // Matrix size input
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        JLabel sizeLabel = new JLabel("矩陣大小(n*n):");
        sizeLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        topPanel.add(sizeLabel, gbc);
        sizeField = new JTextField(5);
        sizeField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        sizeField.setPreferredSize(new Dimension(200, 28));
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(sizeField, gbc);

        // Density input
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        JLabel densityLabel = new JLabel("密集度百分比(0~1):");
        densityLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        topPanel.add(densityLabel, gbc);
        densityField = new JTextField("0.1", 4);
        densityField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        densityField.setPreferredSize(new Dimension(200, 28));
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(densityField, gbc);

        // Buttons on next row
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        generateButton = new JButton("產生稀疏矩陣 No.1");
        generateButton.setPreferredSize(new Dimension(200, 28));
        generateButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        generateButton.setForeground(new Color(255, 140, 0));
        topPanel.add(generateButton, gbc);

        gbc.gridx = 2; gbc.gridwidth = 2;
        generateSecondButton = new JButton("產生稀疏矩陣 No.2");
        generateSecondButton.setPreferredSize(new Dimension(200, 28));
        generateSecondButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        generateSecondButton.setForeground(new Color(255, 140, 0));
        topPanel.add(generateSecondButton, gbc);

        // Show sparse matrix buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        showSparseButton = new JButton("較省空間顯示稀疏矩陣 No.1");
        showSparseButton.setPreferredSize(new Dimension(200, 28));
        showSparseButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        showSparseButton.setForeground(new Color(34, 139, 34));
        topPanel.add(showSparseButton, gbc);

        gbc.gridx = 2; gbc.gridwidth = 2;
        showSparseButton2 = new JButton("較省空間顯示稀疏矩陣 No.2");
        showSparseButton2.setPreferredSize(new Dimension(200, 28));
        showSparseButton2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        showSparseButton2.setForeground(new Color(34, 139, 34));
        topPanel.add(showSparseButton2, gbc);

        // 在 topPanel 下方新增加減按鈕
        addButton = new JButton("矩陣相加 ＋");
        addButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        addButton.setForeground(Color.BLUE);
        subButton = new JButton("矩陣相減 －");
        subButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        subButton.setForeground(Color.BLUE);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        topPanel.add(addButton, gbc);
        gbc.gridx = 2; gbc.gridwidth = 2;
        topPanel.add(subButton, gbc);

        // Transpose button
        transposeButton = new JButton("矩陣 No.2 快速矩陣轉置（Fast Transpose）");
        transposeButton.setPreferredSize(new Dimension(350, 28));
        transposeButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15)); // 改回支援中文的字體
        transposeButton.setForeground(Color.BLUE); // 字體顏色設為藍色
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        topPanel.add(transposeButton, gbc);

        // 新增原始矩陣轉置按鈕
        transposeOriginButton = new JButton("矩陣 No.2 原始矩陣轉置（Naive Transpose）");
        transposeOriginButton.setPreferredSize(new Dimension(350, 28));
        transposeOriginButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15)); // 改回支援中文的字體
        transposeOriginButton.setForeground(Color.BLUE); // 字體顏色設為藍色
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 4;
        topPanel.add(transposeOriginButton, gbc);

        // 新增平均執行時間按鈕
        fastTransposeAvgButton = new JButton("快速矩陣轉置執行時間10次平均");
        fastTransposeAvgButton.setPreferredSize(new Dimension(300, 28));
        fastTransposeAvgButton.setForeground(new Color(220, 20, 60)); // 字體顏色設為紅色(220,20,60)
        fastTransposeAvgButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        topPanel.add(fastTransposeAvgButton, gbc);

        originTransposeAvgButton = new JButton("原始矩陣轉置執行時間10次平均");
        originTransposeAvgButton.setPreferredSize(new Dimension(300, 28));
        originTransposeAvgButton.setForeground(new Color(220, 20, 60)); // 字體顏色設為紅色(220,20,60)
        originTransposeAvgButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        gbc.gridx = 2; gbc.gridy = 6; gbc.gridwidth = 2;
        topPanel.add(originTransposeAvgButton, gbc);

        // 增加空白區域讓按鈕與下方區域明顯隔開
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 4;
        gbc.insets = new Insets(30, 0, 10, 0); // 上方多留空間
        topPanel.add(Box.createVerticalStrut(20), gbc);
        gbc.insets = new Insets(10, 10, 10, 10); // 恢復原本insets

        add(topPanel, BorderLayout.PAGE_START);

        // Center: Matrix Table area with labels
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 2, 2));
        // Matrix title label font/color
        Font matrixLabelFont = new Font("Microsoft JhengHei", Font.BOLD, 18);
        Color matrixLabelColor = new Color(255, 140, 0);

        // Matrix 1
        JPanel mat1Panel = new JPanel(new BorderLayout());
        JLabel mat1Label = new JLabel("產生稀疏矩陣 No.1", SwingConstants.CENTER);
        mat1Label.setFont(matrixLabelFont);
        mat1Label.setForeground(matrixLabelColor);
        mat1Panel.add(mat1Label, BorderLayout.NORTH);
        matrixTable = new JTable();
        matrixTable.setEnabled(false);
        matrixTable.setForeground(new Color(255, 140, 0)); // 內容與標題同色（橘色）
        matrixTable.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        JScrollPane mat1Scroll = new JScrollPane(matrixTable);
        mat1Panel.add(mat1Scroll, BorderLayout.CENTER);
        centerPanel.add(mat1Panel);

        // Matrix 2
        JPanel mat2Panel = new JPanel(new BorderLayout());
        JLabel mat2Label = new JLabel("產生稀疏矩陣 No.2", SwingConstants.CENTER);
        mat2Label.setFont(matrixLabelFont);
        mat2Label.setForeground(matrixLabelColor);
        mat2Panel.add(mat2Label, BorderLayout.NORTH);
        matrixTable2 = new JTable();
        matrixTable2.setEnabled(false);
        matrixTable2.setForeground(new Color(255, 140, 0)); // 內容與標題同色（橘色）
        matrixTable2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        JScrollPane mat2Scroll = new JScrollPane(matrixTable2);
        mat2Panel.add(mat2Scroll, BorderLayout.CENTER);
        centerPanel.add(mat2Panel);

        // Sparse 1 Label
        JPanel sparseLabelPanel1 = new JPanel(new BorderLayout());
        JLabel sparse1Label = new JLabel("較省空間顯示稀疏矩陣 No.1", SwingConstants.CENTER);
        sparse1Label.setForeground(new Color(34, 139, 34));
        sparse1Label.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        sparseLabelPanel1.add(sparse1Label, BorderLayout.NORTH);

        sparseTextArea = new JTextArea();
        sparseTextArea.setEditable(false);
        sparseTextArea.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        sparseTextArea.setForeground(new Color(34, 139, 34)); // 內容與標題同色
        JScrollPane sparseScroll1 = new JScrollPane(sparseTextArea);
        sparseLabelPanel1.add(sparseScroll1, BorderLayout.CENTER);
        centerPanel.add(sparseLabelPanel1);

        // Sparse 2 Label
        JPanel sparseLabelPanel2 = new JPanel(new BorderLayout());
        JLabel sparse2Label = new JLabel("較省空間顯示稀疏矩陣 No.2", SwingConstants.CENTER);
        sparse2Label.setForeground(new Color(34, 139, 34));
        sparse2Label.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        sparseLabelPanel2.add(sparse2Label, BorderLayout.NORTH);

        sparseTextArea2 = new JTextArea();
        sparseTextArea2.setEditable(false);
        sparseTextArea2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        sparseTextArea2.setForeground(new Color(34, 139, 34)); // 內容與標題同色
        JScrollPane sparseScroll2 = new JScrollPane(sparseTextArea2);
        sparseLabelPanel2.add(sparseScroll2, BorderLayout.CENTER);
        centerPanel.add(sparseLabelPanel2);

        add(centerPanel, BorderLayout.CENTER);

        // 結果顯示區
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        resultTextArea.setForeground(Color.BLUE); // 設定內容為藍色
        JScrollPane resultScroll = new JScrollPane(resultTextArea);
        JPanel resultPanel = new JPanel(new BorderLayout());
        JLabel resultLabel = new JLabel("運算結果", SwingConstants.CENTER);
        resultLabel.setFont(new Font("500", Font.BOLD, 18));
        resultLabel.setForeground(Color.BLUE); // 設定標題為藍色
        resultPanel.add(resultLabel, BorderLayout.NORTH);
        resultPanel.add(resultScroll, BorderLayout.CENTER);
        // 讓結果區與其他區塊一樣大
        resultPanel.setPreferredSize(new Dimension(0, getHeight() / 4));
        // 新增時間顯示區
        timeLabel = new JLabel(" ", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        timeLabel.setForeground(new Color(220, 20, 60)); // 醒目的紅色
        resultPanel.add(timeLabel, BorderLayout.SOUTH);
        add(resultPanel, BorderLayout.SOUTH);


        // Action listeners
        generateButton.addActionListener(e -> {
            generateMatrix();
        });
        showSparseButton.addActionListener(e -> {
            showSparseMatrix();
        });
        generateSecondButton.addActionListener(e -> {
            generateSecondMatrix();
        });
        showSparseButton2.addActionListener(e -> {
            showSparseMatrix2();
        });
        addButton.addActionListener(e -> addMatrices());
        subButton.addActionListener(e -> subMatrices());
        transposeButton.addActionListener(e -> fastTransposeMatrix2());
        transposeOriginButton.addActionListener(e -> originTransposeMatrix2());
        fastTransposeAvgButton.addActionListener(e -> fastTransposeMatrix2Avg10());
        originTransposeAvgButton.addActionListener(e -> originTransposeMatrix2Avg10());
    }

    private void generateMatrix() {
        int n;
        double density;
        try {
            n = Integer.parseInt(sizeField.getText());
            if (n <= 0 || n > 2000) { // 修改上限為2000
                JOptionPane.showMessageDialog(this, "請輸入 1~2000 之間的整數", "錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }
            density = Double.parseDouble(densityField.getText());
            if (density < 0 || density > 1) {
                JOptionPane.showMessageDialog(this, "請輸入 0~1 之間的密集度", "錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "請輸入有效的整數與密集度", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }
        lastMatrix = createSparseMatrix(n, density);
        showMatrix(lastMatrix);
        sparseTextArea.setText("");
    }

    private int[][] createSparseMatrix(int n, double density) {
        int[][] matrix = new int[n][n];
        Random rand = new Random();
        int total = n * n;
        int nonZeroCount = (int) Math.round(total * density);
        for (int i = 0; i < nonZeroCount; i++) {
            int row = rand.nextInt(n);
            int col = rand.nextInt(n);
            while (matrix[row][col] != 0) {
                row = rand.nextInt(n);
                col = rand.nextInt(n);
            }
            matrix[row][col] = rand.nextInt(9) + 1; // 1~9 的隨機數
        }
        return matrix;
    }

    private void showMatrix(int[][] matrix) {
        int n = matrix.length;
        String[] columns = new String[n];
        for (int i = 0; i < n; i++) {
            columns[i] = String.valueOf(i);
        }
        String[][] data = new String[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                data[i][j] = String.valueOf(matrix[i][j]);
            }
        }
        DefaultTableModel model = new DefaultTableModel(data, columns);
        matrixTable.setModel(model);
    }

    private void generateSecondMatrix() {
        int n;
        double density;
        try {
            n = Integer.parseInt(sizeField.getText());
            if (n <= 0 || n > 2000) { // 修改上限為2000
                JOptionPane.showMessageDialog(this, "請輸入 1~2000 之間的整數", "錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }
            density = Double.parseDouble(densityField.getText());
            if (density < 0 || density > 1) {
                JOptionPane.showMessageDialog(this, "請輸入 0~1 之間的密集度", "錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "請輸入有效的整數與密集度", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }
        lastMatrix2 = createSparseMatrix(n, density);
        showMatrix2(lastMatrix2);
    }

    private void showMatrix2(int[][] matrix) {
        int n = matrix.length;
        String[] columns = new String[n];
        for (int i = 0; i < n; i++) {
            columns[i] = String.valueOf(i);
        }
        String[][] data = new String[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                data[i][j] = String.valueOf(matrix[i][j]);
            }
        }
        DefaultTableModel model = new DefaultTableModel(data, columns);
        matrixTable2.setModel(model);
    }

    private void showSparseMatrix() {
        if (lastMatrix == null) {
            sparseTextArea.setText("請先產生第一個矩陣");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Sparse Matrix Representation\n");
        sb.append("(row, col, value)\n");
        for (int i = 0; i < lastMatrix.length; i++) {
            for (int j = 0; j < lastMatrix[i].length; j++) {
                if (lastMatrix[i][j] != 0) {
                    sb.append("(").append(i).append(" ,   ").append(j).append(" ,   ").append(lastMatrix[i][j]).append(")\n");
                }
            }
        }
        sparseTextArea.setText(sb.toString());
    }

    private void showSparseMatrix2() {
        if (lastMatrix2 == null) {
            sparseTextArea2.setText("請先產生第二個矩陣");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Sparse Matrix Representation\n");
        sb.append("(row, col, value)\n");
        for (int i = 0; i < lastMatrix2.length; i++) {
            for (int j = 0; j < lastMatrix2[i].length; j++) {
                if (lastMatrix2[i][j] != 0) {
                    sb.append("(").append(i).append(" ,   ").append(j).append(" ,   ").append(lastMatrix2[i][j]).append(")\n");
                }
            }
        }
        sparseTextArea2.setText(sb.toString());
    }

    private void addMatrices() {
        if (lastMatrix == null || lastMatrix2 == null) {
            JOptionPane.showMessageDialog(this, "請先產生兩個矩陣", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (lastMatrix.length != lastMatrix2.length || lastMatrix[0].length != lastMatrix2[0].length) {
            messageLabel.setText("兩個矩陣大小不同，無法做運算！");
            JOptionPane.showMessageDialog(this, "兩個矩陣大小不同，無法做運算！", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            messageLabel.setText("");
        }
        int n = lastMatrix.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = lastMatrix[i][j] + lastMatrix2[i][j];
            }
        }
        showResultMatrix(result, "相加結果 ➕");
    }

    private void subMatrices() {
        if (lastMatrix == null || lastMatrix2 == null) {
            JOptionPane.showMessageDialog(this, "請先產生兩個矩陣", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (lastMatrix.length != lastMatrix2.length || lastMatrix[0].length != lastMatrix2[0].length) {
            messageLabel.setText("兩個矩陣大小不同，無法做運算！");
            JOptionPane.showMessageDialog(this, "兩個矩陣大小不同，無法做運算！", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            messageLabel.setText("");
        }
        int n = lastMatrix.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = lastMatrix[i][j] - lastMatrix2[i][j];
            }
        }
        showResultMatrix(result, "相減結果 ➖");
    }

    // 快速轉置方法
    private void fastTransposeMatrix2() {
        if (lastMatrix2 == null) {
            JOptionPane.showMessageDialog(this, "請先產生第二個矩陣", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }
        long start = System.nanoTime();
        int n = lastMatrix2.length;
        int t = 0;
        // 計算非零元素個數
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (lastMatrix2[i][j] != 0) t++;
        // 建立三元組陣列
        int[][] terms = new int[t][3];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (lastMatrix2[i][j] != 0) {
                    terms[idx][0] = i;
                    terms[idx][1] = j;
                    terms[idx][2] = lastMatrix2[i][j];
                    idx++;
                }
            }
        }
        // 快速轉置
        int[] colCount = new int[n];
        for (int i = 0; i < t; i++) {
            colCount[terms[i][1]]++;
        }
        int[] startPos = new int[n];
        startPos[0] = 0;
        for (int i = 1; i < n; i++) {
            startPos[i] = startPos[i - 1] + colCount[i - 1];
        }
        int[][] transTerms = new int[t][3];
        for (int i = 0; i < t; i++) {
            int col = terms[i][1];
            int pos = startPos[col];
            transTerms[pos][0] = terms[i][1]; // row <-> col
            transTerms[pos][1] = terms[i][0];
            transTerms[pos][2] = terms[i][2];
            startPos[col]++;
        }
        long end = System.nanoTime(); // 將計時移到這裡，僅計算轉置
        // 轉回矩陣
        transposedMatrix = new int[n][n];
        for (int i = 0; i < t; i++) {
            int row = transTerms[i][0];
            int col = transTerms[i][1];
            int val = transTerms[i][2];
            transposedMatrix[row][col] = val;
        }
        showTransposedMatrix();
        timeLabel.setText("快速矩陣轉置執行時間: " + String.format("%,d", (end - start)) + " ns");
    }
    // 顯示轉置結果
    private void showTransposedMatrix() {
        if (transposedMatrix == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("矩陣 No.2 快速矩陣轉置(Fast Transpose)結果 ⟲\n");
        for (int i = 0; i < transposedMatrix.length; i++) {
            for (int j = 0; j < transposedMatrix[i].length; j++) {
                sb.append(transposedMatrix[i][j]).append("\t");
            }
            sb.append("\n");
        }
        resultTextArea.setText(sb.toString());
        resultTextArea.setCaretPosition(0);
        resultTextArea.revalidate();
        resultTextArea.repaint();
        resultTextArea.getParent().revalidate();
        resultTextArea.getParent().repaint();
    }

    // 原始二維陣列轉置方法
    private void originTransposeMatrix2() {
        if (lastMatrix2 == null) {
            JOptionPane.showMessageDialog(this, "請先產生第二個矩陣", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }
        long start = System.nanoTime();
        int n = lastMatrix2.length;
        int[][] trans = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                trans[j][i] = lastMatrix2[i][j];
            }
        }
        long end = System.nanoTime();
        showResultMatrix(trans, "矩陣 No.2 原始矩陣轉置(Naive Transpose)結果 ⟲");
        timeLabel.setText("原始矩陣轉置執行時間: " + String.format("%,d", (end - start)) + " ns");
    }

    // ===== 新增：快速矩陣轉置10次平均 =====
    private void fastTransposeMatrix2Avg10() {
        if (lastMatrix2 == null) {
            JOptionPane.showMessageDialog(this, "請先產生第二個矩陣", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int n = lastMatrix2.length;
        int t = 0;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (lastMatrix2[i][j] != 0) t++;
        int[][] terms = new int[t][3];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (lastMatrix2[i][j] != 0) {
                    terms[idx][0] = i;
                    terms[idx][1] = j;
                    terms[idx][2] = lastMatrix2[i][j];
                    idx++;
                }
            }
        }
        long total = 0;
        for (int run = 0; run < 10; run++) {
            // 重新計算 startPos
            int[] colCount = new int[n];
            for (int i = 0; i < t; i++) {
                colCount[terms[i][1]]++;
            }
            int[] startPos = new int[n];
            startPos[0] = 0;
            for (int i = 1; i < n; i++) {
                startPos[i] = startPos[i - 1] + colCount[i - 1];
            }
            int[][] transTerms = new int[t][3];
            long start = System.nanoTime();
            for (int i = 0; i < t; i++) {
                int col = terms[i][1];
                int pos = startPos[col];
                transTerms[pos][0] = terms[i][1];
                transTerms[pos][1] = terms[i][0];
                transTerms[pos][2] = terms[i][2];
                startPos[col]++;
            }
            long end = System.nanoTime();
            total += (end - start);
        }
        long avg = total / 10;
        timeLabel.setText("快速矩陣轉置10次平均執行時間: " + String.format("%,d", avg) + " ns");
    }

    // ===== 新增：原始矩陣轉置10次平均 =====
    private void originTransposeMatrix2Avg10() {
        if (lastMatrix2 == null) {
            JOptionPane.showMessageDialog(this, "請先產生第二個矩陣", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int n = lastMatrix2.length;
        long total = 0;
        for (int run = 0; run < 10; run++) {
            int[][] trans = new int[n][n];
            long start = System.nanoTime();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    trans[j][i] = lastMatrix2[i][j];
                }
            }
            long end = System.nanoTime();
            total += (end - start);
        }
        long avg = total / 10;
        timeLabel.setText("原始矩陣轉置10次平均執行時間: " + String.format("%,d", avg) + " ns");
    }

    private void showResultMatrix(int[][] matrix, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                sb.append(matrix[i][j]).append("\t");
            }
            sb.append("\n");
        }
        resultTextArea.setText(sb.toString());
        resultTextArea.setCaretPosition(0); // 自動捲到最上方
        resultTextArea.revalidate(); // 重新布局
        resultTextArea.repaint();    // 重新繪製
        resultTextArea.getParent().revalidate(); // 重新布局父元件
        resultTextArea.getParent().repaint();    // 重新繪製父元件
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SparseMatrixGUI().setVisible(true);
        });
    }
}
