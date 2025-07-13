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
    private JTable matrixTable;
    private JScrollPane scrollPane;

    public SparseMatrixGUI() {
        setTitle("Sparse Matrix Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Matrix size n: "));
        sizeField = new JTextField(5);
        topPanel.add(sizeField);
        topPanel.add(new JLabel("Density (0~1): "));
        densityField = new JTextField("0.1", 5);
        topPanel.add(densityField);
        generateButton = new JButton("Generate Sparse Matrix");
        topPanel.add(generateButton);
        add(topPanel, BorderLayout.NORTH);

        matrixTable = new JTable();
        scrollPane = new JScrollPane(matrixTable);
        add(scrollPane, BorderLayout.CENTER);

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateMatrix();
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
        int[][] matrix = createSparseMatrix(n, density);
        showMatrix(matrix);
    }

    private int[][] createSparseMatrix(int n, double density) {
        int[][] matrix = new int[n][n];
        Random rand = new Random();
        int total = n * n;
        int nonZeroCount = (int) Math.round(total * density);
        for (int i = 0; i < nonZeroCount; i++) {
            int row = rand.nextInt(n);
            int col = rand.nextInt(n);
            // 避免重複填同一格
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SparseMatrixGUI().setVisible(true);
        });
    }
}
