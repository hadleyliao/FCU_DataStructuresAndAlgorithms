/****************************************************************************************************
 * 對應課程: Chapter 8
 * CourseWork1: 股票成交量/價排行榜
 * • 股市每天有成千上萬的成交紀錄，投資人想要知道成交金額或成交量最大的股票。
 * • 使用者可以在GUI上輸入股票代號以及指定特定日期或某個日期區間, 找出成交金額/成交量 最高的前 K(使用者設定) 檔股票，
     並且透過 GUI 或 Web 介面 即時顯示排行榜。
 * • 可以自動產生400個交易日期, 1800檔股票的資料檔以供測試功能.
 * • 資料欄位有股票代碼, 交易日期, 成交量, 成交金額.
 * • Hint: Sorting
 ****************************************************************************************************/

package _20250818;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;

public class StockDataGenerator {
    public static void main(String[] args) throws IOException {
        int numStocks = 1800; // 股票數量
        int numDays = 400;    // 天數
        String[] stockIds = new String[numStocks]; // 儲存所有股票代碼
        // 產生股票代碼 1000~2799
        for (int i = 0; i < numStocks; i++) {
            stockIds[i] = String.format("%04d", 1000 + i); // 股票代碼格式化為4位數字
        }
        LocalDate startDate = LocalDate.of(2023, 1, 1); // 起始日期
        Random rand = new Random(); // 隨機數產生器
        // 建立檔案寫入器，寫入CSV檔案
        try (FileWriter fw = new FileWriter("src/_20250818/stock_data.csv")) {
            fw.write("stock_id,date,volume,amount\n"); // 寫入表頭
            // 依天數與股票代碼產生資料
            for (int d = 0; d < numDays; d++) {
                LocalDate date = startDate.plusDays(d); // 計算當天日期
                for (String stockId : stockIds) {
                    int volume = rand.nextInt(1000000) + 1000; // 隨機產生成交量(1,000~1,001,000)
                    int amount = volume * (rand.nextInt(200) + 10); // 隨機產生成交金額(單價10~209)
                    fw.write(stockId + "," + date + "," + volume + "," + amount + "\n"); // 寫入一筆資料
                }
            }
        }
        System.out.println("資料產生完成: src/_20250818/stock_data.csv"); // 完成訊息
    }
}
