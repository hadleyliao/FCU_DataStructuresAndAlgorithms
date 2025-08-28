package _20250828.coursework2;

import java.io.*;
import java.util.*;

public class TradeDataManager {
    private final Map<String, List<TradeRecord>> dateMap = new HashMap<>();

    // 讀取 CSV 檔案
    public void loadFromCSV(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line = br.readLine(); // 讀取 header
        if (line == null) return;
        String[] headers = line.split(",");
        Map<String, Integer> colIdx = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            colIdx.put(headers[i].trim().toLowerCase(), i);
        }
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length < headers.length) continue;
            String rawDate = parts[colIdx.getOrDefault("date", 0)].trim().replaceAll("\\s+", "");
            String date = formatDate(rawDate); // 格式化日期
            String time = parts[colIdx.getOrDefault("time", 1)].trim();
            double open = Double.parseDouble(parts[colIdx.getOrDefault("open", 2)].trim());
            double close = Double.parseDouble(parts[colIdx.getOrDefault("close", 3)].trim());
            double high = Double.parseDouble(parts[colIdx.getOrDefault("high", 4)].trim());
            double low = Double.parseDouble(parts[colIdx.getOrDefault("low", 5)].trim());
            long volume = Long.parseLong(parts[colIdx.getOrDefault("volume", 6)].trim());
            TradeRecord record = new TradeRecord(date, time, open, close, high, low, volume); // 用格式化後的 date
            dateMap.computeIfAbsent(date, k -> new ArrayList<>()).add(record); // 用格式化後的 date 當 key
        }
        br.close();
    }

    // 查詢某日期所有交易
    public List<TradeRecord> queryByDate(String date) {
        String queryDate = formatDate(date);
        return dateMap.getOrDefault(queryDate, Collections.emptyList());
    }

    // 查詢日期區間
    private String formatDate(String date) {
        // 將 yyyy/M/d 轉成 yyyy/MM/dd
        String[] parts = date.split("/");
        if (parts.length != 3) return date;
        String yyyy = parts[0];
        String MM = parts[1].length() == 1 ? "0" + parts[1] : parts[1];
        String dd = parts[2].length() == 1 ? "0" + parts[2] : parts[2];
        return yyyy + "/" + MM + "/" + dd;
    }

    public List<TradeRecord> queryByDateRange(String startDate, String endDate) {
        String start = formatDate(startDate);
        String end = formatDate(endDate);
        System.out.println("[DEBUG] 查詢區間: " + start + " ~ " + end);
        System.out.println("[DEBUG] 所有日期key: " + dateMap.keySet());
        List<TradeRecord> result = new ArrayList<>();
        for (String date : dateMap.keySet()) {
            if (date.compareTo(start) >= 0 && date.compareTo(end) <= 0) {
                result.addAll(dateMap.get(date));
            }
        }
        result.sort(Comparator.comparing(TradeRecord::getDate).thenComparing(TradeRecord::getTime));
        return result;
    }

    // 查詢日期+時間
    public TradeRecord queryByDateTime(String date, String time) {
        String queryDate = formatDate(date);
        List<TradeRecord> records = dateMap.get(queryDate);
        if (records == null) return null;
        String queryTime = time.replace(":", "").replace(" ", "").trim();
        for (TradeRecord r : records) {
            String recordTime = r.getTime().replace(":", "").replace(" ", "").trim();
            // 只比對前4~6碼，支援 HHmm、HHmmss、HH mm、HH mm ss
            if (recordTime.startsWith(queryTime)) return r;
        }
        return null;
    }

    // 指定欄位查詢（回傳 Map）
    public List<Map<String, Object>> queryWithFields(List<TradeRecord> records, List<String> fields) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (TradeRecord r : records) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (String f : fields) {
                switch (f) {
                    case "date": map.put("date", r.getDate()); break;
                    case "time": map.put("time", r.getTime()); break;
                    case "open": map.put("open", r.getOpen()); break;
                    case "close": map.put("close", r.getClose()); break;
                    case "high": map.put("high", r.getHigh()); break;
                    case "low": map.put("low", r.getLow()); break;
                    case "volume": map.put("volume", r.getVolume()); break;
                }
            }
            result.add(map);
        }
        return result;
    }

    // 匯出查詢結果到 CSV
    public void exportToCSV(List<Map<String, Object>> data, List<String> fields, String filePath) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
        // 寫欄位
        bw.write(String.join(",", fields));
        bw.newLine();
        // 寫資料
        for (Map<String, Object> row : data) {
            List<String> line = new ArrayList<>();
            for (String f : fields) {
                line.add(row.get(f).toString());
            }
            bw.write(String.join(",", line));
            bw.newLine();
        }
        bw.close();
    }
}