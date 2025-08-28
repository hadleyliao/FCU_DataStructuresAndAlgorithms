# FCU Data Structures And Algorithms

本專案為逢甲大學資料結構與演算法課程相關的 Java 練習與作業彙整。

## 課程內容

- 第1章 資料結構導論（Introduction）
- 第2章 陣列與矩陣（Arrays and Matrices）
- 第3章 鏈結串列（Linked Lists）
- 第4章 堆疊（Stacks）
- 第5章 佇列（Queues）
- 第6章 樹與二元樹（Trees and Binary Trees）
- 第7章 圖形結構（Graphs）
- 第8章 資料排序（Sorting）
- 第9章 資料搜尋（Searching）

## 專案結構

- `src/`：所有 Java 原始碼、HTML、CSS、JS 檔案
    - `_20250701/`
        - `GCDLCMCalculator.java`：最大公因數/最小公倍數計算器
        - `ImageViewer.java`：網頁介面簡易圖片瀏覽器
        - `index.html`、`index2.html`、`pomodoro.html`：網頁介面番茄鐘工具
        - `script.js`、`style.css`：網頁腳本與樣式
    - `_20250708/`
        - `MatrixSearchDemo.java`：矩陣搜尋演算法比較
    - `_20250714/`
        - `FactorialComparison.java`：階乘運算效率比較（for迴圈）
        - `FactorialComparison2.java`：階乘運算效率比較（遞迴）
    - `_20250715/`
        - `SparseMatrixGUI.java`：稀疏矩陣操作與顯示
        - `Timetable.java`：課表管理工具
    - `_20250723/`
        - `MusicPlayer.java`：簡易音樂播放器（命令列）
        - `MusicPlayerGUI.java`：簡易音樂播放器（圖形介面）
    - `_20250724/`
        - `InfixConverterGUI.java`：中序轉後序運算式工具
        - `SimplePaint.java`：簡易小畫家1
        - `DrawingApp.java`：簡易小畫家2
    - `_20250728/`
        - `RestaurantSimulation.java` ~ `RestaurantSimulation7.java`：餐廳點餐、排隊、出餐等流程模擬（多版本）
    - `_20250729/`
        - `ArrayBinaryTreeGUI.java`：陣列實作二元樹操作
        - `ProducerGUI.java`：生產者消費者問題模擬
    - `_20250805/`
        - `GraphDemoGUI.java`：圖形結構操作與展示（圖形介面）
        - `GraphGenerator.java`：圖形結構產生器
        - `GraphGUI.java`：圖形結構互動介面
    - `_20250811/`
        - `ArticulationPointComparison.java`：關節點（割點）演算法效能比較
        - `articulation_point_result.csv`：關節點演算法效能比較結果（CSV）
        - `關節點演算法效能比較.pptx`：關節點演算法效能比較ppt
    - `_20250812/`
        - `GraphGeneratorGUI.java`：圖形結構產生器（圖形介面）最短路徑。Dijkstra 演算法、Floyd-Warshall 演算法
    - `_20250818/`
        - `StockDataGenerator.java`：隨機股票資料產生器
        - `StockRankingGUI.java`：股票漲跌幅排行圖形介面
        - `stock_data.csv`：範例股票資料（CSV）
    - `_20250828/`
        - `coursework1/`
            - `ItemTransactionDataGenerator.java`：隨機產生大量物品交易資料（含交易代碼、日期、客戶代碼、物品名稱、價格）
            - `item_transactions.csv`：由程式產生的範例交易資料（CSV 格式）
            - `SearchPerformanceComparison.java`：線性搜尋、二分搜尋、插補搜尋、雜湊搜尋等方法效能比較（含 GUI 與統計分析）
        - `coursework2/`
            - `TradeRecord.java`：單筆交易資料類別
            - `TradeDataManager.java`：股票逐筆交易資料的載入、管理與查詢
            - `TradeDataGUI.java`：股票逐筆交易資料的圖形化查詢介面
            - `2317-Minute-Trade.csv`、`2330-Minute-Trade.csv`、`2382-Minute-Trade.csv`：三檔股票逐筆交易資料（CSV 格式）
            - `prompt.md`：作業說明文件
          
- 主要功能包含：
    - 演算法比較（如矩陣搜尋、階乘運算）
    - 資料結構操作（如二元樹、稀疏矩陣）
    - GUI 應用（如音樂播放器、餐廳模擬、繪圖工具）
    - 其他課堂練習

## 執行方式

1. 使用 IntelliJ IDEA 或其他支援 Java 的 IDE 開啟本專案。
2. 進入 `src/` 目錄，選擇欲執行的 Java 檔案，執行主方法（`main`）。
3. 部分 HTML/JS/CSS 檔案可直接以瀏覽器開啟。
