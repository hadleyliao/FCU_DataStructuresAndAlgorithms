package _20250818;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class StockRankingGUI extends JFrame {
    // 定義GUI元件
    private JTextField tfStockId, tfStartDate, tfEndDate, tfTopK;
    private JComboBox<String> cbType;
    private JButton btnSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<StockRecord> records; // 儲存所有股票資料

    public StockRankingGUI() {
        setTitle("股票排行榜");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 建立輸入面板
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("股票代號(可空):"));
        tfStockId = new JTextField(6);
        inputPanel.add(tfStockId);
        inputPanel.add(new JLabel("起始日期(yyyy-mm-dd):"));
        tfStartDate = new JTextField(10);
        inputPanel.add(tfStartDate);
        inputPanel.add(new JLabel("結束日期(yyyy-mm-dd):"));
        tfEndDate = new JTextField(10);
        inputPanel.add(tfEndDate);
        inputPanel.add(new JLabel("前K名:"));
        tfTopK = new JTextField(3);
        inputPanel.add(tfTopK);
        cbType = new JComboBox<>(new String[]{"成交量", "成交金額"}); // 排序類型選擇
        inputPanel.add(cbType);
        btnSearch = new JButton("查詢");
        inputPanel.add(btnSearch);
        add(inputPanel, BorderLayout.NORTH);

        // 建立表格顯示查詢結果
        tableModel = new DefaultTableModel(new String[]{"股票代號", "日期", "成交量", "成交金額"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 查詢按鈕事件處理
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchAndShow(); // 執行查詢與顯示
            }
        });

        loadData(); // 載入CSV資料
    }

    // 載入CSV檔案資料到records
    private void loadData() {
        records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/_20250818/stock_data.csv"))) {
            String line = br.readLine(); // 跳過表頭
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String stockId = parts[0];
                LocalDate date = LocalDate.parse(parts[1]);
                int volume = Integer.parseInt(parts[2]);
                int amount = Integer.parseInt(parts[3]);
                records.add(new StockRecord(stockId, date, volume, amount)); // 加入一筆資料
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "資料讀取失敗: " + ex.getMessage());
        }
    }

    // 查詢並顯示結果
    private void searchAndShow() {
        String stockId = tfStockId.getText().trim(); // 取得股票代號
        String start = tfStartDate.getText().trim(); // 取得起始日期
        String end = tfEndDate.getText().trim();     // 取得結束日期
        String kStr = tfTopK.getText().trim();       // 取得K值
        int k;
        try {
            k = Integer.parseInt(kStr); // 轉換K值
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "K值請輸入正整數");
            return;
        }
        LocalDate startDate, endDate;
        try {
            startDate = LocalDate.parse(start); // 轉換起始日期
            endDate = LocalDate.parse(end);     // 轉換結束日期
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "日期格式錯誤");
            return;
        }
        String type = (String) cbType.getSelectedItem(); // 取得排序類型
        List<StockRecord> filtered = new ArrayList<>(); // 篩選後的資料
        for (StockRecord r : records) {
            if (!stockId.isEmpty() && !r.stockId.equals(stockId)) continue; // 股票代號不符則跳過
            if (r.date.isBefore(startDate) || r.date.isAfter(endDate)) continue; // 日期不符則跳過
            filtered.add(r); // 加入符合條件的資料
        }
        // 根據選擇的類型排序
        // ====== 排序邏輯（使用 Java 內建 TimSort，依 Comparator 由大到小排序） ======
        Comparator<StockRecord> cmp = type.equals("成交量") ?
                Comparator.comparingInt((StockRecord r) -> r.volume).reversed() :
                Comparator.comparingInt((StockRecord r) -> r.amount).reversed();
        filtered.sort(cmp); // 排序
        // ====== 排序邏輯結束 ======
        tableModel.setRowCount(0); // 清空表格
        for (int i = 0; i < Math.min(k, filtered.size()); i++) {
            StockRecord r = filtered.get(i);
            tableModel.addRow(new Object[]{r.stockId, r.date.toString(), Integer.valueOf(r.volume), Integer.valueOf(r.amount)}); // 加入表格
        }
    }

    // 股票資料結構
    static class StockRecord {
        String stockId;
        LocalDate date;
        int volume;
        int amount;
        StockRecord(String stockId, LocalDate date, int volume, int amount) {
            this.stockId = stockId;
            this.date = date;
            this.volume = volume;
            this.amount = amount;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StockRankingGUI().setVisible(true)); // 啟動GUI
    }
}
