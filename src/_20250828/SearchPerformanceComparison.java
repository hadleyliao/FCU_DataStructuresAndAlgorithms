package _20250828;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;

public class SearchPerformanceComparison {
    private static final int QUERY_COUNT = 1000;
    private static int actualDataSize = 0;
    private static String[] data;
    private static String[] sortedData;
    private static final String[] queries = new String[QUERY_COUNT];
    private static final Random random = new Random();
    private static final Set<String> hashSet = new HashSet<>(2_000_000); // 指定初始容量，避免rehash

    public static void main(String[] args) {
        // 從CSV讀取資料
        try {
            loadDataFromCSV("src/_20250828/item_transactions.csv");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "讀取item_transactions.csv失敗: " + e.getMessage());
            return;
        }
        // 建立雜湊表（只做一次，不計入搜尋效能）
        hashSet.clear();
        for (String v : data) hashSet.add(v);
        // warm up（只針對hashSet）
        for (int i = 0; i < 1000; i++) {
            String key = data[random.nextInt(actualDataSize)];
            hashSet.contains(key);
        }
        // 測試次數提升到1000
        int testCount = 1000;
        String[] existKeys = new String[testCount];
        for (int i = 0; i < testCount; i++) existKeys[i] = data[random.nextInt(actualDataSize)];
        String[] notExistKeys = new String[testCount];
        for (int i = 0; i < testCount; i++) {
            String key;
            do {
                key = "-" + random.nextInt(actualDataSize * 10);
            } while (Arrays.binarySearch(data, key) >= 0);
            notExistKeys[i] = key;
        }
        // 先測hashSet，讓其JIT優化最充分
        long hashExistTime = testHashSearchForKeys(existKeys);
        long hashNotExistTime = testHashSearchForKeys(notExistKeys);
        long linearExistTime = testSearchForKeys(SearchPerformanceComparison::linearSearch, data, existKeys);
        long binaryExistTime = testSearchForKeys(SearchPerformanceComparison::binarySearch, sortedData, existKeys);
        long interpolationExistTime = testSearchForKeys(SearchPerformanceComparison::interpolationSearch, sortedData, existKeys);
        long linearNotExistTime = testSearchForKeys(SearchPerformanceComparison::linearSearch, data, notExistKeys);
        long binaryNotExistTime = testSearchForKeys(SearchPerformanceComparison::binarySearch, sortedData, notExistKeys);
        long interpolationNotExistTime = testSearchForKeys(SearchPerformanceComparison::interpolationSearch, sortedData, notExistKeys);
        System.out.println("\n[" + testCount + "個存在KEY的搜尋平均時間(ns)]");
        System.out.printf("雜湊搜尋\t\t%d\n", hashExistTime / testCount);
        System.out.printf("線性搜尋\t\t%d\n", linearExistTime / testCount);
        System.out.printf("二分搜尋\t\t%d\n", binaryExistTime / testCount);
        System.out.printf("插補搜尋\t%d\n", interpolationExistTime / testCount);
        System.out.println("\n[" + testCount + "個不存在KEY的搜尋平均時間(ns)]");
        System.out.printf("雜湊搜尋\t\t%d\n", hashNotExistTime / testCount);
        System.out.printf("線性搜尋\t\t%d\n", linearNotExistTime / testCount);
        System.out.printf("二分搜尋\t\t%d\n", binaryNotExistTime / testCount);
        System.out.printf("插補搜尋\t%d\n", interpolationNotExistTime / testCount);
        // 收集所有效能數據，依序：線性搜尋, 二分搜尋, 插補搜尋, 雜湊搜尋
        String[] methods = {"線性搜尋", "二分搜尋", "插補搜尋", "雜湊搜尋"};
        long[] existTimes = {linearExistTime, binaryExistTime, interpolationExistTime, hashExistTime};
        long[] notExistTimes = {linearNotExistTime, binaryNotExistTime, interpolationNotExistTime, hashNotExistTime};
        // 顯示GUI
        SwingUtilities.invokeLater(() -> showResultGUI(methods, existTimes, notExistTimes, testCount));
    }

    private static void showResultGUI(String[] methods, long[] existTimes, long[] notExistTimes, int testCount) {
        JFrame frame = new JFrame("搜尋法效能比較");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLocationRelativeTo(null);
        String[] columns = {"搜尋法", "存在KEY平均(ns)", "不存在KEY平均(ns)", "存在KEY倍數", "不存在KEY倍數"};
        Object[][] tableData = new Object[methods.length][5];
        for (int i = 0; i < methods.length; i++) {
            tableData[i][0] = methods[i];
            tableData[i][1] = String.format("%d", Math.round((double)existTimes[i] / testCount));
            tableData[i][2] = String.format("%d", Math.round((double)notExistTimes[i] / testCount));
            tableData[i][3] = String.format("%.6f", (double)existTimes[i] / (existTimes[0] == 0 ? 1 : existTimes[0]));
            tableData[i][4] = String.format("%.6f", (double)notExistTimes[i] / (notExistTimes[0] == 0 ? 1 : notExistTimes[0]));
        }
        JTable table = new JTable(tableData, columns);
        table.setFont(new Font("SansSerif", Font.PLAIN, 16));
        table.setRowHeight(28);
        JScrollPane scrollPane = new JScrollPane(table);
        JLabel label = new JLabel("倍數 = 與線性搜尋比較 (越小越快)，測試次數: " + testCount, JLabel.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        frame.add(label, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static void generateQueries() {
        for (int i = 0; i < QUERY_COUNT; i++) {
            // 一半查詢存在值，一半查詢不存在值
            if (i % 2 == 0) {
                queries[i] = data[random.nextInt(actualDataSize)];
            } else {
                queries[i] = "-" + random.nextInt(actualDataSize * 10);
            }
        }
    }

    private static long testSearch(SearchFunction searchFunc, String[] arr, boolean sorted) {
        long start = System.nanoTime();
        for (String q : queries) {
            searchFunc.search(arr, q);
        }
        return System.nanoTime() - start;
    }

    private static long testSearch() {
        long start = System.nanoTime();
        for (String q : queries) {
            hashSet.contains(q);
        }
        return System.nanoTime() - start;
    }

    // 新增：針對指定key陣列測試搜尋效能
    private static long testSearchForKeys(SearchFunction searchFunc, String[] arr, String[] keys) {
        long start = System.nanoTime();
        for (String q : keys) {
            searchFunc.search(arr, q);
        }
        return System.nanoTime() - start;
    }
    // 新增：針對指定key陣列測試雜湊搜尋效能
    private static long testHashSearchForKeys(String[] keys) {
        long start = System.nanoTime();
        for (String q : keys) {
            hashSet.contains(q);
        }
        return System.nanoTime() - start;
    }

    // 線性搜尋
    private static int linearSearch(String[] arr, String target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(target)) return i;
        }
        return -1;
    }

    // 二分搜尋
    private static int binarySearch(String[] arr, String target) {
        int left = 0, right = arr.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (arr[mid].equals(target)) return mid;
            if (arr[mid].compareTo(target) < 0) left = mid + 1;
            else right = mid - 1;
        }
        return -1;
    }

    // 插補搜尋
    private static int interpolationSearch(String[] arr, String target) {
        int low = 0, high = arr.length - 1;
        while (low <= high && target.compareTo(arr[low]) >= 0 && target.compareTo(arr[high]) <= 0) {
            if (low == high) {
                if (arr[low].equals(target)) return low;
                return -1;
            }
            int pos = low + (int)(((long)(high - low) * (target.compareTo(arr[low]))) / (arr[high].compareTo(arr[low])));
            if (pos < low || pos > high) break;
            if (arr[pos].equals(target)) return pos;
            if (arr[pos].compareTo(target) < 0) low = pos + 1;
            else high = pos - 1;
        }
        return -1;
    }

    // 讀取CSV檔案，將價格欄位存入data陣列
    private static void loadDataFromCSV(String filePath) throws IOException {
        java.util.List<String> dataList = new java.util.ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;
                // 取交易代碼（第1欄）
                dataList.add(parts[0]);
            }
        }
        actualDataSize = dataList.size();
        if (actualDataSize < 10_000) {
            throw new IOException("資料筆數不足: " + actualDataSize);
        }
        data = new String[actualDataSize];
        sortedData = new String[actualDataSize];
        for (int i = 0; i < actualDataSize; i++) data[i] = dataList.get(i);
        System.arraycopy(data, 0, sortedData, 0, actualDataSize);
        Arrays.sort(sortedData);
    }

    @FunctionalInterface
    interface SearchFunction {
        int search(String[] arr, String target);
    }
}
