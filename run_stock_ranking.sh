#!/bin/bash
# 自動化編譯與執行股票資料產生器與GUI排行榜

# 編譯資料產生器
javac src/_20250818/StockDataGenerator.java
# 執行資料產生器
java -cp src _20250818.StockDataGenerator

# 編譯GUI排行榜
javac src/_20250818/StockRankingGUI.java
# 執行GUI排行榜
java -cp src _20250818.StockRankingGUI

