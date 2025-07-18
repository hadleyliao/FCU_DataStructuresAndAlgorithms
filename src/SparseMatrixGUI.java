import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private JTable matrixTable;
    private JTable matrixTable2;
    private JTextArea sparseTextArea;
    private JTextArea sparseTextArea2;
    private JTextArea resultTextArea; // 新增結果顯示區
    private int[][] lastMatrix;
    private int[][] lastMatrix2;

    public SparseMatrixGUI() {
        setTitle("Sparse Matrix Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 1000);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top input & operation panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 35, 10, 35);

        // Matrix size input
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        topPanel.add(new JLabel("矩陣大小(n*n):"), gbc);
        sizeField = new JTextField(5);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(sizeField, gbc);

        // Density input
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        topPanel.add(new JLabel("密集度百分比(0~1):"), gbc);
        densityField = new JTextField("0.1", 4);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(densityField, gbc);

        // Buttons on next row
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        generateButton = new JButton("產生稀疏矩陣 No.1");
        generateButton.setPreferredSize(new Dimension(200, 28));
        topPanel.add(generateButton, gbc);

        gbc.gridx = 2; gbc.gridwidth = 2;
        generateSecondButton = new JButton("產生稀疏矩陣 No.2");
        generateSecondButton.setPreferredSize(new Dimension(200, 28));
        topPanel.add(generateSecondButton, gbc);

        // Show sparse matrix buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        showSparseButton = new JButton("較省空間顯示稀疏矩陣 No.1");
        showSparseButton.setPreferredSize(new Dimension(200, 28));
        topPanel.add(showSparseButton, gbc);

        gbc.gridx = 2; gbc.gridwidth = 2;
        showSparseButton2 = new JButton("較省空間顯示稀疏矩陣 No.2");
        showSparseButton2.setPreferredSize(new Dimension(200, 28));
        topPanel.add(showSparseButton2, gbc);

        // 在 topPanel 下方新增加減按鈕
        addButton = new JButton("矩陣相加 ➕");
        subButton = new JButton("矩陣相減 ➖");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        topPanel.add(addButton, gbc);
        gbc.gridx = 2; gbc.gridwidth = 2;
        topPanel.add(subButton, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Center: Matrix Table area with labels
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 2, 2));
        // Matrix title label font/color
        Font matrixLabelFont = new Font("SansSerif", Font.BOLD, 18);
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
        JScrollPane mat2Scroll = new JScrollPane(matrixTable2);
        mat2Panel.add(mat2Scroll, BorderLayout.CENTER);
        centerPanel.add(mat2Panel);

        // Sparse 1 Label
        JPanel sparseLabelPanel1 = new JPanel(new BorderLayout());
        JLabel sparse1Label = new JLabel("較省空間顯示稀疏矩陣 No.1", SwingConstants.CENTER);
        sparse1Label.setForeground(new Color(34, 139, 34));
        sparse1Label.setFont(new Font("SansSerif", Font.BOLD, 18));
        sparseLabelPanel1.add(sparse1Label, BorderLayout.NORTH);

        sparseTextArea = new JTextArea();
        sparseTextArea.setEditable(false);
        sparseTextArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        sparseTextArea.setForeground(new Color(34, 139, 34)); // 內容與標題同色
        JScrollPane sparseScroll1 = new JScrollPane(sparseTextArea);
        sparseLabelPanel1.add(sparseScroll1, BorderLayout.CENTER);
        centerPanel.add(sparseLabelPanel1);

        // Sparse 2 Label
        JPanel sparseLabelPanel2 = new JPanel(new BorderLayout());
        JLabel sparse2Label = new JLabel("較省空間顯示稀疏矩陣 No.2", SwingConstants.CENTER);
        sparse2Label.setForeground(new Color(34, 139, 34));
        sparse2Label.setFont(new Font("SansSerif", Font.BOLD, 18));
        sparseLabelPanel2.add(sparse2Label, BorderLayout.NORTH);

        sparseTextArea2 = new JTextArea();
        sparseTextArea2.setEditable(false);
        sparseTextArea2.setFont(new Font("Monospaced", Font.PLAIN, 15));
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
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        resultLabel.setForeground(Color.BLUE); // 設定標題為藍色
        resultPanel.add(resultLabel, BorderLayout.NORTH);
        resultPanel.add(resultScroll, BorderLayout.CENTER);
        // 讓結果區與其他區塊一樣大
        resultPanel.setPreferredSize(new Dimension(0, getHeight() / 4));
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
    }

    private void generateMatrix() {
        int n;
        double density;
        try {
            n = Integer.parseInt(sizeField.getText());
            if (n <= 0 || n > 100) {
                JOptionPane.showMessageDialog(this, "請輸入 1~100 之間的整數", "錯誤", JOptionPane.ERROR_MESSAGE);
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
            if (n <= 0 || n > 100) {
                JOptionPane.showMessageDialog(this, "請輸入 1~100 之間的整數", "錯誤", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "兩個矩陣大小不一致，無法相加", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
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
            JOptionPane.showMessageDialog(this, "兩個矩陣大小不一致，無法相減", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
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