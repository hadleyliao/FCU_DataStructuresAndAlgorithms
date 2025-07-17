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
    private JTable matrixTable;
    private JTable matrixTable2;
    private JTextArea sparseTextArea;
    private JTextArea sparseTextArea2;
    private int[][] lastMatrix;
    private int[][] lastMatrix2;

    public SparseMatrixGUI() {
        setTitle("Sparse Matrix Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top input & operation panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 10, 3, 10);

        // Matrix size input
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        topPanel.add(new JLabel("Enter matrix size (n):"), gbc);
        sizeField = new JTextField(5);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(sizeField, gbc);

        // Density input
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        topPanel.add(new JLabel("Enter density(0~1):"), gbc);
        densityField = new JTextField("0.1", 5);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(densityField, gbc);

        // Buttons on next row
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        generateButton = new JButton("Generate Sparse Matrix No.1");
        generateButton.setPreferredSize(new Dimension(200, 28));
        topPanel.add(generateButton, gbc);

        gbc.gridx = 2; gbc.gridwidth = 2;
        generateSecondButton = new JButton("Generate Sparse Matrix No.2");
        generateSecondButton.setPreferredSize(new Dimension(200, 28));
        topPanel.add(generateSecondButton, gbc);

        // Show sparse matrix buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        showSparseButton = new JButton("顯示第1個稀疏矩陣");
        showSparseButton.setPreferredSize(new Dimension(200, 28));
        topPanel.add(showSparseButton, gbc);

        gbc.gridx = 2; gbc.gridwidth = 2;
        showSparseButton2 = new JButton("顯示第2個稀疏矩陣");
        showSparseButton2.setPreferredSize(new Dimension(200, 28));
        topPanel.add(showSparseButton2, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Center: Matrix Table area with labels
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 2, 2));
        // Matrix title label font/color
        Font matrixLabelFont = new Font("SansSerif", Font.BOLD, 20);
        Color matrixLabelColor = new Color(255, 140, 0);

        // Matrix 1
        JPanel mat1Panel = new JPanel(new BorderLayout());
        JLabel mat1Label = new JLabel("Generate Sparse Matrix No.1", SwingConstants.CENTER);
        mat1Label.setFont(matrixLabelFont);
        mat1Label.setForeground(matrixLabelColor);
        mat1Panel.add(mat1Label, BorderLayout.NORTH);
        matrixTable = new JTable();
        matrixTable.setEnabled(false);
        JScrollPane mat1Scroll = new JScrollPane(matrixTable);
        mat1Panel.add(mat1Scroll, BorderLayout.CENTER);
        centerPanel.add(mat1Panel);

        // Matrix 2
        JPanel mat2Panel = new JPanel(new BorderLayout());
        JLabel mat2Label = new JLabel("Generate Sparse Matrix No.2", SwingConstants.CENTER);
        mat2Label.setFont(matrixLabelFont);
        mat2Label.setForeground(matrixLabelColor);
        mat2Panel.add(mat2Label, BorderLayout.NORTH);
        matrixTable2 = new JTable();
        matrixTable2.setEnabled(false);
        JScrollPane mat2Scroll = new JScrollPane(matrixTable2);
        mat2Panel.add(mat2Scroll, BorderLayout.CENTER);
        centerPanel.add(mat2Panel);

        // Sparse 1 Label
        JPanel sparseLabelPanel1 = new JPanel(new BorderLayout());
        JLabel sparse1Label = new JLabel("顯示第1個稀疏矩陣", SwingConstants.CENTER);
        sparse1Label.setForeground(new Color(34, 139, 34));
        sparse1Label.setFont(new Font("SansSerif", Font.BOLD, 16));
        sparseLabelPanel1.add(sparse1Label, BorderLayout.NORTH);

        sparseTextArea = new JTextArea();
        sparseTextArea.setEditable(false);
        sparseTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane sparseScroll1 = new JScrollPane(sparseTextArea);
        sparseLabelPanel1.add(sparseScroll1, BorderLayout.CENTER);
        centerPanel.add(sparseLabelPanel1);

        // Sparse 2 Label
        JPanel sparseLabelPanel2 = new JPanel(new BorderLayout());
        JLabel sparse2Label = new JLabel("顯示第2個稀疏矩陣", SwingConstants.CENTER);
        sparse2Label.setForeground(new Color(34, 139, 34));
        sparse2Label.setFont(new Font("SansSerif", Font.BOLD, 16));
        sparseLabelPanel2.add(sparse2Label, BorderLayout.NORTH);

        sparseTextArea2 = new JTextArea();
        sparseTextArea2.setEditable(false);
        sparseTextArea2.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane sparseScroll2 = new JScrollPane(sparseTextArea2);
        sparseLabelPanel2.add(sparseScroll2, BorderLayout.CENTER);
        centerPanel.add(sparseLabelPanel2);

        add(centerPanel, BorderLayout.CENTER);

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SparseMatrixGUI().setVisible(true);
        });
    }
}