/***********************
 * 對應課程: Chapter 1
 * CourseWork1: 效能比較
 ***********************/

package _20250708;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;


public class MatrixSearchDemo extends JFrame {
    private JTextField inputField, searchField;
    private JButton generateButton, linearBtn, binaryBtn, hashBtn, allSearchBtn, testAvgBtn;
    private JTable originalTable, sortedTable;
    private JTextArea resultArea;
    private JScrollPane originalScrollPane, sortedScrollPane;


    private int[][] matrixData;
    private int[][] sortedMatrixData;
    private Set<Integer> hashSet;


    public MatrixSearchDemo() {
        setTitle("矩陣搜尋演算法比較");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());


        // 🔼 上方控制區
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("輸入矩陣大小 n："));
        inputField = new JTextField(5);
        topPanel.add(inputField);
        generateButton = new JButton("產生矩陣");
        topPanel.add(generateButton);
        topPanel.add(new JLabel("搜尋數字："));
        searchField = new JTextField(5);
        topPanel.add(searchField);
        add(topPanel, BorderLayout.NORTH);


        // 📋 中間表格區（原始矩陣 + 排序矩陣）
        JPanel tablePanel = new JPanel(new GridLayout(2, 1));
        originalTable = new JTable();
        sortedTable = new JTable();
        originalScrollPane = new JScrollPane(originalTable);
        sortedScrollPane = new JScrollPane(sortedTable);
        tablePanel.add(originalScrollPane);
        tablePanel.add(sortedScrollPane);
        add(tablePanel, BorderLayout.CENTER);


        // ⬇ 底部：按鈕區 + 結果區（改放下方）
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());


        JPanel buttonPanel = new JPanel();
        linearBtn = new JButton("循序搜尋");
        binaryBtn = new JButton("二元搜尋");
        hashBtn = new JButton("雜湊搜尋");
        allSearchBtn = new JButton("全部搜尋");
        testAvgBtn = new JButton("隨機搜尋數字20次測試");
        buttonPanel.add(linearBtn);
        buttonPanel.add(binaryBtn);
        buttonPanel.add(hashBtn);
        buttonPanel.add(allSearchBtn);
        buttonPanel.add(testAvgBtn);
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);


        resultArea = new JTextArea(20, 80);
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JScrollPane resultScroll = new JScrollPane(resultArea);
        bottomPanel.add(resultScroll, BorderLayout.CENTER);


        add(bottomPanel, BorderLayout.SOUTH);


        // 🔁 綁定事件
        generateButton.addActionListener(e -> generateMatrix());
        linearBtn.addActionListener(e -> performSearch("linear"));
        binaryBtn.addActionListener(e -> performSearch("binary"));
        hashBtn.addActionListener(e -> performSearch("hash"));
        allSearchBtn.addActionListener(e -> {
            performSearch("linear");
            performSearch("binary");
            performSearch("hash");
        });
        testAvgBtn.addActionListener(e -> testAverageSearchTime());
    }


    // 其他方法（generateMatrix、showMatrix、performSearch、linearSearch、binarySearch、testAverageSearchTime）
    // 請參照上一版完整程式碼，邏輯完全相同


    private void generateMatrix() {
        int n;
        try {
            n = Integer.parseInt(inputField.getText().trim());
            if (n <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "請輸入正整數", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }


        List<Integer> nums = new ArrayList<>();
        for (int i = 1; i <= n * n; i++) nums.add(i);
        Collections.shuffle(nums);


        matrixData = new int[n][n];
        sortedMatrixData = new int[n][n];
        hashSet = new HashSet<>(nums);


        int index = 0;
        for (int i = 0; i < n; i++) {
            int[] row = new int[n];
            for (int j = 0; j < n; j++) {
                int val = nums.get(index++);
                matrixData[i][j] = val;
                row[j] = val;
            }
            Arrays.sort(row);
            sortedMatrixData[i] = row;
        }


        showMatrix(originalTable, matrixData);
        showMatrix(sortedTable, sortedMatrixData);
        resultArea.setText("✅ 矩陣產生完成，請輸入數字開始搜尋\n");
    }


    private void showMatrix(JTable table, int[][] data) {
        int n = data.length;
        String[][] strData = new String[n][n];
        String[] cols = new String[n];
        Arrays.fill(cols, "");


        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                strData[i][j] = String.valueOf(data[i][j]);


        DefaultTableModel model = new DefaultTableModel(strData, cols) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table.setModel(model);
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        table.setRowHeight(30);


        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < n; i++)
            table.getColumnModel().getColumn(i).setCellRenderer(center);
    }


    private void performSearch(String method) {
        if (matrixData == null) {
            JOptionPane.showMessageDialog(this, "請先產生矩陣", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }


        int target;
        try {
            target = Integer.parseInt(searchField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "請輸入整數", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }


        boolean found = false;
        long start = System.nanoTime();


        switch (method) {
            case "linear":
                found = linearSearch(target);
                break;
            case "binary":
                found = binarySearch(target);
                break;
            case "hash":
                found = hashSet.contains(target);
                break;
        }


        long end = System.nanoTime();
        double ms = (end - start) / 1_000_000.0;
        String result = String.format("🔍 %s搜尋：%s，耗時 %.3f 毫秒\n",
                methodName(method), found ? "找到" : "找不到", ms);
        resultArea.append(result);
    }


    private String methodName(String m) {
        return switch (m) {
            case "linear" -> "線性";
            case "binary" -> "二分";
            case "hash" -> "HashSet";
            default -> m;
        };
    }


    private boolean linearSearch(int target) {
        for (int[] row : matrixData)
            for (int val : row)
                if (val == target) return true;
        return false;
    }


    private boolean binarySearch(int target) {
        for (int[] row : sortedMatrixData)
            if (Arrays.binarySearch(row, target) >= 0) return true;
        return false;
    }


    private void testAverageSearchTime() {
        if (matrixData == null) {
            JOptionPane.showMessageDialog(this, "請先產生矩陣", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }


        int trials = 20;
        Random rand = new Random();
        List<Integer> values = new ArrayList<>();
        for (int[] row : matrixData)
            for (int val : row)
                values.add(val);


        double totalLinear = 0, totalBinary = 0, totalHash = 0;


        for (int i = 0; i < trials; i++) {
            int randomValue = values.get(rand.nextInt(values.size()));


            long start = System.nanoTime();
            linearSearch(randomValue);
            long end = System.nanoTime();
            totalLinear += (end - start) / 1_000_000.0;


            start = System.nanoTime();
            binarySearch(randomValue);
            end = System.nanoTime();
            totalBinary += (end - start) / 1_000_000.0;


            start = System.nanoTime();
            hashSet.contains(randomValue);
            end = System.nanoTime();
            totalHash += (end - start) / 1_000_000.0;
        }


        String result = String.format(
                """
                📊 平均搜尋時間（共隨機 %d 次）：
                🔹 循序搜尋：%.4f 毫秒
                🔹 二元搜尋：%.4f 毫秒
                🔹 雜湊搜尋：%.4f 毫秒
   
                """,
                trials,
                totalLinear / trials,
                totalBinary / trials,
                totalHash / trials
        );


        resultArea.append(result);
    }


    /**
     * 按下「隨機搜尋數字20次測試」按鈕時，會自動隨機選取矩陣中的值，
     * 進行20次搜尋，每次搜尋的數字都是隨機選取（可能重複，但大多不同），
     * 並分別記錄循序搜尋、二元搜尋、雜湊搜尋的平均搜尋時間，
     * 結果會顯示在下方結果區。
     *
     * 三種搜尋法（循序搜尋、二元搜尋、雜湊搜尋）都會針對這個隨機數字各自執行一次，
     * 並記錄時間，最後計算平均值。
     */
    public void benchmarkSearches() {
        int trials = 20;
        long totalLinear = 0;
        long totalBinary = 0;
        long totalHash = 0;
        int n = matrixData.length;
        java.util.Random rand = new java.util.Random();
        for (int t = 0; t < trials; t++) {
            int target = rand.nextInt(n * n) + 1; // 產生 1~n*n 的隨機數


            // 線性搜尋
            long start = System.nanoTime();
            linearSearch(target);
            long end = System.nanoTime();
            totalLinear += (end - start);


            // 二元搜尋
            start = System.nanoTime();
            binarySearch(target);
            end = System.nanoTime();
            totalBinary += (end - start);


            // 雜湊搜尋
            start = System.nanoTime();
            hashSet.contains(target);
            end = System.nanoTime();
            totalHash += (end - start);
        }
        String result = String.format("\n搜尋基準測試 (20 次隨機搜尋):\n線性搜尋平均: %.2f ns\n二元搜尋平均: %.2f ns\n雜湊搜尋平均: %.2f ns\n",
                totalLinear / (double) trials,
                totalBinary / (double) trials,
                totalHash / (double) trials
        );
        resultArea.append(result);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MatrixSearchDemo().setVisible(true));
    }
}
