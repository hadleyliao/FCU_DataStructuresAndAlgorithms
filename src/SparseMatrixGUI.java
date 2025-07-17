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
        setSize(900, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 設定全域字型
        Font uiFont = new Font("Microsoft JhengHei", Font.PLAIN, 18);
        UIManager.put("Label.font", uiFont);
        UIManager.put("Button.font", uiFont);
        UIManager.put("TextField.font", uiFont);
        UIManager.put("Table.font", uiFont);
        UIManager.put("TableHeader.font", uiFont);
        UIManager.put("TextArea.font", uiFont);
        UIManager.put("Panel.font", uiFont);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        // Input Row
        JPanel inputPanelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanelLeft.add(new JLabel("Enter matrix size (n):"));
        sizeField = new JTextField(5);
        inputPanelLeft.add(sizeField);

        JPanel inputPanelRight = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanelRight.add(new JLabel("Enter density(0~1):"));
        densityField = new JTextField("0.1", 5);
        inputPanelRight.add(densityField);

        // 使用 GridBagLayout 讓按鈕自動換行且隨視窗調整
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridx = 0; gbc.gridy = 0;
        generateButton = new JButton("Generate Sparse Matrix No.1");
        buttonPanel.add(generateButton, gbc);
        gbc.gridx++;
        generateSecondButton = new JButton("Generate Sparse Matrix No.2");
        buttonPanel.add(generateSecondButton, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        showSparseButton = new JButton("顯示第1個稀疏矩陣");
        buttonPanel.add(showSparseButton, gbc);
        gbc.gridx++;
        showSparseButton2 = new JButton("顯示第2個稀疏矩陣");
        buttonPanel.add(showSparseButton2, gbc);

        // topPanel 只放輸入欄位，按鈕獨立一行
        topPanel.add(inputPanelLeft);
        topPanel.add(inputPanelRight);
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.add(topPanel, BorderLayout.NORTH);
        topWrapper.add(buttonPanel, BorderLayout.CENTER);
        add(topWrapper, BorderLayout.NORTH);

        // Center Panel (4 grid: 2x2)
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 5, 5));

        // Top-left: Matrix 1
        JPanel matrix1Panel = new JPanel(new BorderLayout());
        JLabel matrix1Label = new JLabel("<html><span style='color:#FF8C00;font-size:18px;'>Generate Sparse Matrix No.1</span></html>");
        matrix1Label.setHorizontalAlignment(JLabel.CENTER);
        matrix1Panel.add(matrix1Label, BorderLayout.NORTH);
        matrixTable = new JTable();
        JScrollPane matrixScroll1 = new JScrollPane(matrixTable);
        matrix1Panel.add(matrixScroll1, BorderLayout.CENTER);
        centerPanel.add(matrix1Panel);

        // Top-right: Matrix 2
        JPanel matrix2Panel = new JPanel(new BorderLayout());
        JLabel matrix2Label = new JLabel("<html><span style='color:#FF8C00;font-size:18px;'>Generate Sparse Matrix No.2</span></html>");
        matrix2Label.setHorizontalAlignment(JLabel.CENTER);
        matrix2Panel.add(matrix2Label, BorderLayout.NORTH);
        matrixTable2 = new JTable();
        JScrollPane matrixScroll2 = new JScrollPane(matrixTable2);
        matrix2Panel.add(matrixScroll2, BorderLayout.CENTER);
        centerPanel.add(matrix2Panel);

        // Bottom-left: Sparse 1
        JPanel sparse1Panel = new JPanel(new BorderLayout());
        JLabel sparse1Label = new JLabel("<html><span style='color:#228B22;font-size:16px;'>顯示第1個稀疏矩陣</span></html>");
        sparse1Label.setHorizontalAlignment(JLabel.CENTER);
        sparse1Panel.add(sparse1Label, BorderLayout.NORTH);
        sparseTextArea = new JTextArea();
        sparseTextArea.setEditable(false);
        sparseTextArea.setForeground(new Color(34, 139, 34));
        JScrollPane sparseScroll1 = new JScrollPane(sparseTextArea);
        sparse1Panel.add(sparseScroll1, BorderLayout.CENTER);
        centerPanel.add(sparse1Panel);

        // Bottom-right: Sparse 2
        JPanel sparse2Panel = new JPanel(new BorderLayout());
        JLabel sparse2Label = new JLabel("<html><span style='color:#228B22;font-size:16px;'>顯示第2個稀疏矩陣</span></html>");
        sparse2Label.setHorizontalAlignment(JLabel.CENTER);
        sparse2Panel.add(sparse2Label, BorderLayout.NORTH);
        sparseTextArea2 = new JTextArea();
        sparseTextArea2.setEditable(false);
        sparseTextArea2.setForeground(new Color(34, 139, 34));
        JScrollPane sparseScroll2 = new JScrollPane(sparseTextArea2);
        sparse2Panel.add(sparseScroll2, BorderLayout.CENTER);
        centerPanel.add(sparse2Panel);

        add(centerPanel, BorderLayout.CENTER);

        // Action listeners
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateMatrix();
            }
        });
        showSparseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSparseMatrix();
            }
        });
        generateSecondButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateSecondMatrix();
            }
        });
        showSparseButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSparseMatrix2();
            }
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
                    sb.append("(").append(i).append(" ,      ").append(j).append(" ,      ").append(lastMatrix[i][j]).append(")\n");
                }
            }
        }
        sparseTextArea.setForeground(Color.BLACK);
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
                    sb.append("(").append(i).append(" ,      ").append(j).append(" ,      ").append(lastMatrix2[i][j]).append(")\n");
                }
            }
        }
        sparseTextArea2.setForeground(Color.BLACK);
        sparseTextArea2.setText(sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SparseMatrixGUI().setVisible(true);
        });
    }
}