package _20250828;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * 台灣股票交易資料處理系統
 * 完整版本包含所有功能
 */
public class StockDataProcessor {

    // GUI 元件
    private JFrame mainFrame;
    private JPanel mainPanel;
    private JTextArea dataDisplayArea;
    private JScrollPane scrollPane;
    private JLabel statusLabel;

    // 查詢功能GUI元件
    private JTextField singleDateField;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextField dayCountField;
    private JTextField dateTimeField;
    private JButton singleDateButton;
    private JButton dateRangeButton;
    private JButton dateTimeButton;
    private JButton exportButton;

    // 欄位選擇元件
    private JCheckBox[] fieldCheckBoxes;
    private String[] fieldNames = {"日期", "時間", "股票代號", "開盤價", "最高價", "最低價", "收盤價", "成交量"};

    // 資料儲存
    private List<StockRecord> stockData;
    private HashMap<String, List<StockRecord>> dailyDataMap; // 功能二: Hash Table管理每日資料
    private HashMap<String, StockRecord> dateTimeMap; // 功能四: 日期+時間快速查詢
    private String currentFilePath;
    private SimpleDateFormat dateFormat;
    private List<StockRecord> currentQueryResult; // 當前查詢結果

    /**
     * 股票交易紀錄類別
     */
    public static class StockRecord {
        private String date;        // 日期
        private String time;        // 時間
        private String stockCode;   // 股票代號
        private double openPrice;   // 開盤價
        private double highPrice;   // 最高價
        private double lowPrice;    // 最低價
        private double closePrice;  // 收盤價
        private long volume;        // 成交量

        // 建構函式
        public StockRecord(String date, String time, String stockCode,
                           double openPrice, double highPrice, double lowPrice,
                           double closePrice, long volume) {
            this.date = date;
            this.time = time;
            this.stockCode = stockCode;
            this.openPrice = openPrice;
            this.highPrice = highPrice;
            this.lowPrice = lowPrice;
            this.closePrice = closePrice;
            this.volume = volume;
        }

        // Getter 方法
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getStockCode() { return stockCode; }
        public double getOpenPrice() { return openPrice; }
        public double getHighPrice() { return highPrice; }
        public double getLowPrice() { return lowPrice; }
        public double getClosePrice() { return closePrice; }
        public long getVolume() { return volume; }

        public String getDateTime() { return date + " " + time; }

        @Override
        public String toString() {
            return String.format("%s %s [%s] 開:%.2f 高:%.2f 低:%.2f 收:%.2f 量:%d",
                    date, time, stockCode, openPrice, highPrice, lowPrice, closePrice, volume);
        }

        // 根據選擇的欄位輸出
        public String toStringWithSelectedFields(boolean[] fieldSelection) {
            StringBuilder sb = new StringBuilder();
            if (fieldSelection[0]) sb.append(date).append(",");
            if (fieldSelection[1]) sb.append(time).append(",");
            if (fieldSelection[2]) sb.append(stockCode).append(",");
            if (fieldSelection[3]) sb.append(String.format("%.2f", openPrice)).append(",");
            if (fieldSelection[4]) sb.append(String.format("%.2f", highPrice)).append(",");
            if (fieldSelection[5]) sb.append(String.format("%.2f", lowPrice)).append(",");
            if (fieldSelection[6]) sb.append(String.format("%.2f", closePrice)).append(",");
            if (fieldSelection[7]) sb.append(volume).append(",");

            // 移除最後一個逗號
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }
            return sb.toString();
        }
    }

    /**
     * 建構函式
     */
    public StockDataProcessor() {
        stockData = new ArrayList<>();
        dailyDataMap = new HashMap<>();
        dateTimeMap = new HashMap<>();
        dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        currentQueryResult = new ArrayList<>();
        initializeGUI();
    }

    /**
     * 初始化 GUI 介面
     */
    private void initializeGUI() {
        // 主視窗設定
        mainFrame = new JFrame("台灣股票交易資料處理系統");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1200, 800);
        mainFrame.setLocationRelativeTo(null);

        // 主面板
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 頂部面板 - 檔案選擇區域
        JPanel topPanel = createFileSelectionPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 左側面板 - 查詢功能
        JPanel leftPanel = createQueryPanel();
        mainPanel.add(leftPanel, BorderLayout.WEST);

        // 中央面板 - 資料顯示區域
        JPanel centerPanel = createDataDisplayPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 底部面板 - 狀態列
        JPanel bottomPanel = createStatusPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainFrame.add(mainPanel);
    }

    /**
     * 建立檔案選擇面板
     */
    private JPanel createFileSelectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("檔案選擇"));

        fileComboBox = new JComboBox<>();
        fileComboBox.setPreferredSize(new Dimension(220, 28));
        fileComboBox.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        fileComboBox.addItem("請選擇預設檔案");
        for (String fname : PRESET_FILES) {
            File f = new File("src/_20250828/" + fname);
            if (f.exists()) fileComboBox.addItem(fname);
        }
        fileComboBox.addActionListener(e -> onPresetFileSelected());

        panel.add(fileComboBox);
        return panel;
    }

    // 新增：下拉選單選擇檔案時自動載入
    private void onPresetFileSelected() {
        int idx = fileComboBox.getSelectedIndex();
        if (idx <= 0) return; // 0為提示文字
        String fname = (String) fileComboBox.getSelectedItem();
        File f = new File("src/_20250828/" + fname);
        if (f.exists()) {
            currentFilePath = f.getAbsolutePath();
            loadCSVFile(f);
        }
    }

    /**
     * 建立查詢面板
     */
    private JPanel createQueryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 600));
        panel.setBorder(BorderFactory.createTitledBorder("查詢功能"));

        // 功能二: 單日查詢
        JPanel singleDatePanel = new JPanel(new GridBagLayout());
        singleDatePanel.setBorder(BorderFactory.createTitledBorder("功能二: 單日查詢"));
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        singleDatePanel.add(new JLabel("日期 (yyyy/MM/dd):"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        singleDateField = new JTextField("2024/05/20", 14);
        singleDatePanel.add(singleDateField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        singleDateButton = new JButton("查詢單日資料");
        singleDateButton.addActionListener(e -> querySingleDate());
        singleDatePanel.add(singleDateButton, gbc);

        // 功能三: 日期區間查詢
        JPanel dateRangePanel = new JPanel(new GridBagLayout());
        dateRangePanel.setBorder(BorderFactory.createTitledBorder("功能三: 日期區間查詢"));

        gbc.gridx = 0; gbc.gridy = 0;
        dateRangePanel.add(new JLabel("起始日期:"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        startDateField = new JTextField("2024/05/20", 14);
        dateRangePanel.add(startDateField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        dateRangePanel.add(new JLabel("結束日期:"), gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        endDateField = new JTextField("2024/05/22", 14);
        dateRangePanel.add(endDateField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        dateRangePanel.add(new JLabel("或天數:"), gbc);
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL;
        dayCountField = new JTextField("3", 5);
        dateRangePanel.add(dayCountField, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        dateRangeButton = new JButton("查詢區間資料");
        dateRangeButton.addActionListener(e -> queryDateRange());
        dateRangePanel.add(dateRangeButton, gbc);

        // 功能四: 日期時間查詢
        JPanel dateTimePanel = new JPanel(new GridBagLayout());
        dateTimePanel.setBorder(BorderFactory.createTitledBorder("功能四: 精確時間查詢"));

        gbc.gridx = 0; gbc.gridy = 0;
        dateTimePanel.add(new JLabel("日期+時間:"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        dateTimeField = new JTextField("2024/05/20 09:30", 17);
        dateTimePanel.add(dateTimeField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        dateTimeButton = new JButton("查詢該分鐘資料");
        dateTimeButton.addActionListener(e -> queryDateTime());
        dateTimePanel.add(dateTimeButton, gbc);

        // 功能五: 欄位選擇
        JPanel fieldPanel = createFieldSelectionPanel();

        // 功能六: 匯出功能
        JPanel exportPanel = new JPanel();
        exportPanel.setBorder(BorderFactory.createTitledBorder("功能六: 匯出功能"));
        exportButton = new JButton("匯出查詢結果為 CSV");
        exportButton.addActionListener(e -> exportToCSV());
        exportPanel.add(exportButton);

        panel.add(singleDatePanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(dateRangePanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(dateTimePanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(fieldPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(exportPanel);

        return panel;
    }

    /**
     * 建立欄位選擇面板
     */
    private JPanel createFieldSelectionPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.setBorder(BorderFactory.createTitledBorder("功能五: 欄位選擇"));

        fieldCheckBoxes = new JCheckBox[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            fieldCheckBoxes[i] = new JCheckBox(fieldNames[i], true);
            panel.add(fieldCheckBoxes[i]);
        }

        return panel;
    }

    /**
     * 建立資料顯示面板
     */
    private JPanel createDataDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("資料顯示"));

        dataDisplayArea = new JTextArea();
        dataDisplayArea.setEditable(false);
        dataDisplayArea.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        dataDisplayArea.setText("請選擇 CSV 檔案以載入股票資料...");

        scrollPane = new JScrollPane(dataDisplayArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 建立狀態面板
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        statusLabel = new JLabel("就緒");
        statusLabel.setForeground(Color.BLUE);

        panel.add(new JLabel("狀態: "));
        panel.add(statusLabel);

        return panel;
    }

    /**
     * 檔案選擇監聽器
     */
    private class FileSelectionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectCSVFile();
        }
    }

    /**
     * 選擇 CSV 檔案
     */
    private void selectCSVFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "CSV 檔案 (*.csv)", "csv");
        fileChooser.setFileFilter(filter);
        fileChooser.setCurrentDirectory(new File("."));
        int result = fileChooser.showOpenDialog(mainFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            currentFilePath = selectedFile.getAbsolutePath();
            loadCSVFile(selectedFile);
            fileComboBox.setSelectedIndex(0); // 手動選檔時，下拉選單回到預設
        }
    }

    /**
     * 讀取 CSV 檔案並建立 Hash Table
     */
    private void loadCSVFile(File file) {
        try {
            statusLabel.setText("讀取檔案中...");
            statusLabel.setForeground(Color.ORANGE);

            // 清空舊資料
            stockData.clear();
            dailyDataMap.clear();
            dateTimeMap.clear();

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            int lineCount = 0;
            boolean isFirstLine = true;
            StringBuilder displayText = new StringBuilder();

            // TODO: 將以下路徑改為您的 CSV 檔案路徑
            // 範例 CSV 格式（請根據實際檔案格式調整）:
            // Date,Time,StockCode,Open,High,Low,Close,Volume
            // 2024/05/20,09:01,2330,580.0,582.0,579.0,581.0,1000

            while ((line = reader.readLine()) != null) {
                lineCount++;

                if (isFirstLine) {
                    displayText.append("檔案標題: ").append(line).append("\n");
                    displayText.append("==========================================\n");
                    isFirstLine = false;
                    continue;
                }

                try {
                    StockRecord record = parseCSVLine(line);
                    if (record != null) {
                        stockData.add(record);

                        // 功能二: 建立日期 Hash Table
                        String dateKey = record.getDate();
                        dailyDataMap.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(record);

                        // 功能四: 建立日期+時間 Hash Table
                        String dateTimeKey = record.getDateTime();
                        dateTimeMap.put(dateTimeKey, record);

                        if (lineCount <= 50) {
                            displayText.append(record.toString()).append("\n");
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("解析第 " + lineCount + " 行時發生錯誤: " + ex.getMessage());
                }
            }

            reader.close();

            // 對每日資料按時間排序
            for (List<StockRecord> dailyRecords : dailyDataMap.values()) {
                dailyRecords.sort((a, b) -> a.getTime().compareTo(b.getTime()));
            }

            // 更新顯示
            if (stockData.size() > 50) {
                displayText.append("\n... 還有 ").append(stockData.size() - 50).append(" 筆資料 ...\n");
            }
            displayText.append("\n總共載入 ").append(stockData.size()).append(" 筆交易紀錄");
            displayText.append("\n建立 ").append(dailyDataMap.size()).append(" 個日期索引");

            dataDisplayArea.setText(displayText.toString());
            dataDisplayArea.setCaretPosition(0);

            statusLabel.setText("載入完成 - 共 " + stockData.size() + " 筆資料，" + dailyDataMap.size() + " 個交易日");
            statusLabel.setForeground(Color.GREEN);

        } catch (IOException ex) {
            handleFileError("檔案讀取錯誤", ex);
        } catch (Exception ex) {
            handleFileError("未預期的錯誤", ex);
        }
    }

    /**
     * 解析 CSV 行資料
     */
    private StockRecord parseCSVLine(String line) {
        try {
            String[] parts = line.split(",");

            if (parts.length >= 8) {
                String date = parts[0].trim();
                String time = parts[1].trim();
                String stockCode = parts[2].trim();
                double openPrice = Double.parseDouble(parts[3].trim());
                double highPrice = Double.parseDouble(parts[4].trim());
                double lowPrice = Double.parseDouble(parts[5].trim());
                double closePrice = Double.parseDouble(parts[6].trim());
                long volume = Long.parseLong(parts[7].trim());

                return new StockRecord(date, time, stockCode, openPrice,
                        highPrice, lowPrice, closePrice, volume);
            }
        } catch (NumberFormatException ex) {
            System.err.println("數字格式錯誤: " + line);
        }

        return null;
    }

    /**
     * 功能二: 查詢單日資料
     */
    private void querySingleDate() {
        String inputDate = singleDateField.getText().trim();

        if (dailyDataMap.containsKey(inputDate)) {
            List<StockRecord> dayData = dailyDataMap.get(inputDate);
            currentQueryResult = new ArrayList<>(dayData);
            displayQueryResult(dayData, "單日查詢結果 (" + inputDate + ")");
            statusLabel.setText("查詢完成 - 找到 " + dayData.size() + " 筆 " + inputDate + " 的交易資料");
            statusLabel.setForeground(Color.GREEN);
        } else {
            dataDisplayArea.setText("查詢結果: 找不到 " + inputDate + " 的交易資料");
            currentQueryResult.clear();
            statusLabel.setText("查詢完成 - 無資料");
            statusLabel.setForeground(Color.ORANGE);
        }
    }

    /**
     * 功能三: 查詢日期區間資料
     */
    private void queryDateRange() {
        try {
            String startDateStr = startDateField.getText().trim();
            String endDateStr = endDateField.getText().trim();
            String dayCountStr = dayCountField.getText().trim();

            Date startDate = dateFormat.parse(startDateStr);
            Date endDate;

            // 判斷使用結束日期還是天數
            if (!endDateStr.isEmpty()) {
                endDate = dateFormat.parse(endDateStr);
            } else if (!dayCountStr.isEmpty()) {
                int dayCount = Integer.parseInt(dayCountStr);
                Calendar cal = Calendar.getInstance();
                cal.setTime(startDate);
                cal.add(Calendar.DAY_OF_MONTH, dayCount - 1);
                endDate = cal.getTime();
            } else {
                JOptionPane.showMessageDialog(mainFrame, "請輸入結束日期或天數", "輸入錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<StockRecord> rangeData = new ArrayList<>();
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);

            while (!cal.getTime().after(endDate)) {
                String dateKey = dateFormat.format(cal.getTime());
                if (dailyDataMap.containsKey(dateKey)) {
                    rangeData.addAll(dailyDataMap.get(dateKey));
                }
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            currentQueryResult = rangeData;
            String rangeStr = startDateStr + " ~ " + dateFormat.format(endDate);
            displayQueryResult(rangeData, "日期區間查詢結果 (" + rangeStr + ")");
            statusLabel.setText("查詢完成 - 找到 " + rangeData.size() + " 筆區間資料");
            statusLabel.setForeground(Color.GREEN);

        } catch (ParseException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(mainFrame, "日期格式錯誤，請使用 yyyy/MM/dd 格式",
                    "輸入錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 功能四: 查詢特定日期時間
     */
    private void queryDateTime() {
        String inputDateTime = dateTimeField.getText().trim();

        if (dateTimeMap.containsKey(inputDateTime)) {
            StockRecord record = dateTimeMap.get(inputDateTime);
            currentQueryResult = Arrays.asList(record);

            StringBuilder result = new StringBuilder();
            result.append("精確時間查詢結果 (").append(inputDateTime).append(")\n");
            result.append("==========================================\n");
            result.append("股票代號: ").append(record.getStockCode()).append("\n");
            result.append("開盤價: ").append(String.format("%.2f", record.getOpenPrice())).append("\n");
            result.append("最高價: ").append(String.format("%.2f", record.getHighPrice())).append("\n");
            result.append("最低價: ").append(String.format("%.2f", record.getLowPrice())).append("\n");
            result.append("收盤價: ").append(String.format("%.2f", record.getClosePrice())).append("\n");
            result.append("成交量: ").append(record.getVolume()).append("\n");

            dataDisplayArea.setText(result.toString());
            dataDisplayArea.setCaretPosition(0);

            statusLabel.setText("查詢完成 - 找到 " + inputDateTime + " 的交易資料");
            statusLabel.setForeground(Color.GREEN);
        } else {
            dataDisplayArea.setText("查詢結果: 找不到 " + inputDateTime + " 的交易資料");
            currentQueryResult.clear();
            statusLabel.setText("查詢完成 - 無資料");
            statusLabel.setForeground(Color.ORANGE);
        }
    }

    /**
     * 顯示查詢結果
     */
    private void displayQueryResult(List<StockRecord> data, String title) {
        StringBuilder result = new StringBuilder();
        result.append(title).append("\n");
        result.append("==========================================\n");

        // 取得欄位選擇
        boolean[] fieldSelection = getFieldSelection();

        // 顯示標題行
        result.append(getHeaderString(fieldSelection)).append("\n");
        result.append("------------------------------------------\n");

        // 顯示資料（限制顯示筆數以避免介面卡頓）
        int displayLimit = Math.min(data.size(), 1000);
        for (int i = 0; i < displayLimit; i++) {
            StockRecord record = data.get(i);
            if (isAllFieldsSelected(fieldSelection)) {
                result.append(record.toString()).append("\n");
            } else {
                result.append(record.toStringWithSelectedFields(fieldSelection)).append("\n");
            }
        }

        if (data.size() > displayLimit) {
            result.append("\n... 還有 ").append(data.size() - displayLimit).append(" 筆資料 ...\n");
        }

        result.append("\n總共 ").append(data.size()).append(" 筆查詢結果");

        dataDisplayArea.setText(result.toString());
        dataDisplayArea.setCaretPosition(0);
    }

    /**
     * 功能六: 匯出查詢結果為 CSV
     */
    private void exportToCSV() {
        if (currentQueryResult.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "沒有可匯出的查詢結果", "匯出錯誤", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("儲存查詢結果");
        fileChooser.setSelectedFile(new File("query_result.csv"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV 檔案", "csv");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showSaveDialog(mainFrame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                boolean[] fieldSelection = getFieldSelection();

                // 寫入標題行
                writer.println(getHeaderString(fieldSelection));

                // 寫入資料
                for (StockRecord record : currentQueryResult) {
                    writer.println(record.toStringWithSelectedFields(fieldSelection));
                }

                statusLabel.setText("匯出完成 - 儲存至 " + file.getName());
                statusLabel.setForeground(Color.GREEN);
                JOptionPane.showMessageDialog(mainFrame, "查詢結果已成功匯出至:\n" + file.getAbsolutePath(),
                        "匯出完成", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                handleFileError("匯出錯誤", ex);
            }
        }
    }

    /**
     * 取得欄位選擇狀態
     */
    private boolean[] getFieldSelection() {
        boolean[] selection = new boolean[fieldCheckBoxes.length];
        for (int i = 0; i < fieldCheckBoxes.length; i++) {
            selection[i] = fieldCheckBoxes[i].isSelected();
        }
        return selection;
    }

    /**
     * 檢查是否選擇了所有欄位
     */
    private boolean isAllFieldsSelected(boolean[] fieldSelection) {
        for (boolean selected : fieldSelection) {
            if (!selected) return false;
        }
        return true;
    }

    /**
     * 取得標題字串
     */
    private String getHeaderString(boolean[] fieldSelection) {
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < fieldNames.length; i++) {
            if (fieldSelection[i]) {
                header.append(fieldNames[i]).append(",");
            }
        }
        if (header.length() > 0) {
            header.setLength(header.length() - 1); // 移除最後一個逗號
        }
        return header.toString();
    }

    /**
     * 處理檔案錯誤
     */
    private void handleFileError(String errorType, Exception ex) {
        String errorMessage = errorType + ": " + ex.getMessage();

        dataDisplayArea.setText("錯誤: " + errorMessage);
        statusLabel.setText(errorMessage);
        statusLabel.setForeground(Color.RED);

        // 顯示錯誤對話框
        JOptionPane.showMessageDialog(mainFrame,
                errorMessage,
                "檔案處理錯誤",
                JOptionPane.ERROR_MESSAGE);

        ex.printStackTrace();
    }

    /**
     * 顯示 GUI
     */
    public void show() {
        mainFrame.setVisible(true);
    }

    /**
     * 取得載入的股票資料
     */
    public List<StockRecord> getStockData() {
        return new ArrayList<>(stockData);
    }

    /**
     * 取得每日資料對應表
     */
    public HashMap<String, List<StockRecord>> getDailyDataMap() {
        return new HashMap<>(dailyDataMap);
    }

    /**
     * 主程式進入點
     */
    public static void main(String[] args) {
        // 設定系統外觀
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // 啟動 GUI
        SwingUtilities.invokeLater(() -> {
            StockDataProcessor processor = new StockDataProcessor();
            // 啟動時自動載入第一個存在的預設檔案
            for (String fname : PRESET_FILES) {
                File f = new File("src/_20250828/" + fname);
                if (f.exists()) {
                    processor.loadCSVFile(f);
                    processor.fileComboBox.setSelectedItem(fname);
                    break;
                }
            }
            processor.show();
        });
    }

    private JComboBox<String> fileComboBox; // 新增：下拉選單選擇檔案
    private static final String[] PRESET_FILES = {
        "2317-Minute-Trade.csv",
        "2330-Minute-Trade.csv",
        "2382-Minute-Trade.csv"
    };
}
