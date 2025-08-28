package _20250828.coursework2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class TradeDataGUI extends JFrame {
    private final TradeDataManager manager = new TradeDataManager();
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField dateField, startDateField, endDateField, timeField, rangeDaysField;
    private final JCheckBox cbDate, cbTime, cbOpen, cbClose, cbHigh, cbLow, cbVolume;
    private final List<String> selectedFields = new ArrayList<>();
    private final JFileChooser fileChooser = new JFileChooser();

    public TradeDataGUI() {
        setTitle("股票交易資料查詢系統");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 上方：檔案選擇與載入
        JPanel topPanel = new JPanel();
        JButton loadBtn = new JButton("載入CSV檔案");
        topPanel.add(loadBtn);
        add(topPanel, BorderLayout.NORTH);

        // ===== 新增說明文字 =====
        JLabel minuteQueryLabel = new JLabel("查詢特定某一分鐘的交易資料");
        minuteQueryLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        minuteQueryLabel.setForeground(Color.BLUE);

        // 中間：查詢條件
        JPanel queryPanel = new JPanel(new GridLayout(3, 1));
        JPanel datePanel = new JPanel();
        datePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(minuteQueryLabel);
        datePanel.add(new JLabel("日期(yyyy/MM/dd):"));
        dateField = new JTextField(10);
        datePanel.add(dateField);
        datePanel.add(new JLabel("時間(HH:mm):"));
        timeField = new JTextField(5);
        datePanel.add(timeField);
        queryPanel.add(datePanel);

        JPanel rangePanel = new JPanel();
        rangePanel.add(new JLabel("區間查詢 起始日期:"));
        startDateField = new JTextField(10);
        rangePanel.add(startDateField);
        rangePanel.add(new JLabel("結束日期:"));
        endDateField = new JTextField(10);
        rangePanel.add(endDateField);
        rangePanel.add(new JLabel("或天數:"));
        rangeDaysField = new JTextField(3);
        rangePanel.add(rangeDaysField);
        queryPanel.add(rangePanel);

        JPanel fieldPanel = new JPanel();
        cbDate = new JCheckBox("日期", true);
        cbTime = new JCheckBox("時間", true);
        cbOpen = new JCheckBox("開盤價", true);
        cbClose = new JCheckBox("收盤價", true);
        cbHigh = new JCheckBox("最高價", true);
        cbLow = new JCheckBox("最低價", true);
        cbVolume = new JCheckBox("成交量", true);
        fieldPanel.add(cbDate); fieldPanel.add(cbTime); fieldPanel.add(cbOpen);
        fieldPanel.add(cbClose); fieldPanel.add(cbHigh); fieldPanel.add(cbLow); fieldPanel.add(cbVolume);
        queryPanel.add(fieldPanel);
        add(queryPanel, BorderLayout.WEST);

        // 下方：查詢與匯出按鈕
        JPanel bottomPanel = new JPanel();
        JButton queryBtn = new JButton("查詢");
        JButton exportBtn = new JButton("匯出CSV");
        bottomPanel.add(queryBtn);
        bottomPanel.add(exportBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // 右側：結果表格
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 事件
        loadBtn.addActionListener(e -> loadCSV());
        queryBtn.addActionListener(e -> doQuery());
        exportBtn.addActionListener(e -> exportCSV());
    }

    private void loadCSV() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                manager.loadFromCSV(file.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "載入成功!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "載入失敗: " + ex.getMessage());
            }
        }
    }

    private void doQuery() {
        selectedFields.clear();
        if (cbDate.isSelected()) selectedFields.add("date");
        if (cbTime.isSelected()) selectedFields.add("time");
        if (cbOpen.isSelected()) selectedFields.add("open");
        if (cbClose.isSelected()) selectedFields.add("close");
        if (cbHigh.isSelected()) selectedFields.add("high");
        if (cbLow.isSelected()) selectedFields.add("low");
        if (cbVolume.isSelected()) selectedFields.add("volume");

        List<TradeRecord> records = new ArrayList<>();
        String date = dateField.getText().trim();
        String time = timeField.getText().trim();
        String startDate = startDateField.getText().trim();
        String endDate = endDateField.getText().trim();
        String rangeDays = rangeDaysField.getText().trim();

        if (!date.isEmpty() && !time.isEmpty()) {
            // 日期+時間查詢
            TradeRecord r = manager.queryByDateTime(date, time);
            if (r != null) records.add(r);
        } else if (!date.isEmpty()) {
            // 單日查詢
            records = manager.queryByDate(date);
        } else if (!startDate.isEmpty() && (!endDate.isEmpty() || !rangeDays.isEmpty())) {
            // 區間查詢
            String realEndDate = endDate;
            if (!rangeDays.isEmpty()) {
                realEndDate = addDays(startDate, Integer.parseInt(rangeDays)-1);
            }
            records = manager.queryByDateRange(startDate, realEndDate);
        }

        List<Map<String, Object>> data = manager.queryWithFields(records, selectedFields);
        showTable(data, selectedFields);
        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(this, "查無資料，請確認查詢條件或資料內容！");
        }
    }

    private void showTable(List<Map<String, Object>> data, List<String> fields) {
        tableModel.setColumnIdentifiers(fields.toArray());
        tableModel.setRowCount(0);
        for (Map<String, Object> row : data) {
            Object[] vals = fields.stream().map(row::get).toArray();
            tableModel.addRow(vals);
        }
    }

    private void exportCSV() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "無查詢結果可匯出");
            return;
        }
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                List<Map<String, Object>> data = new ArrayList<>();
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        row.put(tableModel.getColumnName(j), tableModel.getValueAt(i, j));
                    }
                    data.add(row);
                }
                manager.exportToCSV(data, selectedFields, file.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "匯出成功!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "匯出失敗: " + ex.getMessage());
            }
        }
    }

    // 工具：日期加天數（yyyy/MM/dd）
    private String addDays(String date, int days) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy/MM/dd");
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(date));
            cal.add(Calendar.DATE, days);
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return date;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TradeDataGUI().setVisible(true));
    }
}
