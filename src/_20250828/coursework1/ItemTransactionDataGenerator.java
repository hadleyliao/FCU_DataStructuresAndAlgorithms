package _20250828.coursework1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class ItemTransactionDataGenerator {
    private static final String[] ITEM_NAMES = {
        "ItemA", "ItemB", "ItemC", "ItemD", "ItemE",
        "ItemF", "ItemG", "ItemH", "ItemI", "ItemJ"
    };
    private static final int CUSTOMER_COUNT = 1000000; // 100萬客戶
    private static final double MIN_PRICE = 10.0;
    private static final double MAX_PRICE = 1000.0;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        long recordCount = 1_000_000L;
        String fileName = "item_transactions.csv";
        String filePath = "src/_20250828/" + fileName;
        System.out.println("開始產生" + recordCount + "筆資料，請耐心等候...");
        generateData(filePath, recordCount);
        System.out.println("資料產生完成，檔案位置: " + filePath);
    }

    private static void generateData(String filePath, long recordCount) {
        Random random = new Random();
        LocalDate startDate = LocalDate.now().minusYears(1);
        long daysRange = 365;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (long i = 0; i < recordCount; i++) {
                // 交易代碼
                String txCode = String.format("TX%08d", i + 1);
                // 交易日期
                LocalDate date = startDate.plusDays(random.nextInt((int) daysRange));
                String dateStr = date.format(DATE_FORMAT);
                // 客戶代碼
                int custId = random.nextInt(CUSTOMER_COUNT) + 1;
                String custCode = String.format("CUST%06d", custId);
                // 物品名稱
                String itemName = ITEM_NAMES[random.nextInt(ITEM_NAMES.length)];
                // 價格
                double price = MIN_PRICE + (MAX_PRICE - MIN_PRICE) * random.nextDouble();
                String priceStr = String.format("%.2f", price);
                // 寫入一行
                writer.write(txCode + "," + dateStr + "," + custCode + "," + itemName + "," + priceStr);
                writer.newLine();
                if ((i+1) % 1_000_000 == 0) {
                    System.out.println("已產生 " + (i+1) + " 筆...");
                }
            }
        } catch (IOException e) {
            System.err.println("寫入檔案時發生錯誤: " + e.getMessage());
        }
    }
}
