/***************************************
 * 對應課程: Chapter 5
 * CourseWork: 模擬顧客點餐系統(只有四格儀錶板數據)
 ***************************************/

package _20250728;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RestaurantSimulation2 extends JFrame {
    private static final long serialVersionUID = 1L;

    // 系統狀態
    private volatile boolean isRunning = false;
    private final BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();
    private final AtomicInteger customerCounter = new AtomicInteger(1);
    private final AtomicInteger tableCounter = new AtomicInteger(1);

    // GUI 組件
    private JTextArea customerArea;
    private JTextArea queueArea;
    private JTextArea processingArea;
    private JTextArea completedArea;
    private JLabel statusLabel;
    private JSlider speedSlider;
    private JSlider workerSlider; // 新增員工數量滑桿
    private JButton startButton, stopButton;

    // 工作線程
    private Thread customerGeneratorThread;
    private Thread[] workerThreads;

    // 餐點類型和製作時間
    private enum FoodType {
        BURGER("漢堡", 2000),
        FRIES("薯條", 1000),
        DRINK("飲料", 500);

        private final String name;
        private final int cookingTime;

        FoodType(String name, int cookingTime) {
            this.name = name;
            this.cookingTime = cookingTime;
        }

        public String getName() { return name; }
        public int getCookingTime() { return cookingTime; }
    }

    // 訂單類別
    private static class Order {
        private final int orderId;
        private final FoodType foodType;
        private final int tableNumber;
        private final String orderTime;

        public Order(int orderId, FoodType foodType, int tableNumber) {
            this.orderId = orderId;
            this.foodType = foodType;
            this.tableNumber = tableNumber;
            this.orderTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }

        public int getOrderId() { return orderId; }
        public FoodType getFoodType() { return foodType; }
        public int getTableNumber() { return tableNumber; }
        public String getOrderTime() { return orderTime; }

        @Override
        public String toString() {
            return String.format("訂單#%d: %s (桌號:%d) [%s]",
                    orderId, foodType.getName(), tableNumber, orderTime);
        }
    }

    public RestaurantSimulation2() {
        initializeGUI();
        setupEventHandlers();
    }

    private void initializeGUI() {
        setTitle("餐廳點餐流程模擬系統");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 控制面板
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.NORTH);

        // 顯示面板
        JPanel displayPanel = createDisplayPanel();
        mainPanel.add(displayPanel, BorderLayout.CENTER);

        // 狀態面板
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("系統控制"));

        startButton = new JButton("開始模擬");
        stopButton = new JButton("結束模擬");
        stopButton.setEnabled(false);

        // 速度控制滑桿
        JLabel speedLabel = new JLabel("顧客進場速度:");
        speedSlider = new JSlider(1, 10, 5);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setMajorTickSpacing(3);
        speedSlider.setMinorTickSpacing(1);

        // 員工數量滑桿
        JLabel workerLabel = new JLabel("員工數量:");
        workerSlider = new JSlider(1, 10, 3);
        workerSlider.setPaintTicks(true);
        workerSlider.setPaintLabels(true);
        workerSlider.setMajorTickSpacing(3);
        workerSlider.setMinorTickSpacing(1);

        panel.add(startButton);
        panel.add(stopButton);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(speedLabel);
        panel.add(speedSlider);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(workerLabel);
        panel.add(workerSlider);

        return panel;
    }

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顧客到店區域
        customerArea = createTextArea("顧客到店記錄");
        JScrollPane customerScroll = new JScrollPane(customerArea);
        customerScroll.setBorder(BorderFactory.createTitledBorder("顧客到店"));
        customerScroll.setPreferredSize(new Dimension(240, 250));

        // 訂單佇列區域
        queueArea = createTextArea("等待處理的訂單");
        JScrollPane queueScroll = new JScrollPane(queueArea);
        queueScroll.setBorder(BorderFactory.createTitledBorder("訂單佇列"));
        queueScroll.setPreferredSize(new Dimension(240, 250));

        // 處理中區域
        processingArea = createTextArea("正在製作的訂單");
        JScrollPane processingScroll = new JScrollPane(processingArea);
        processingScroll.setBorder(BorderFactory.createTitledBorder("製作中"));
        processingScroll.setPreferredSize(new Dimension(240, 250));

        // 完成區域
        completedArea = createTextArea("完成的訂單");
        JScrollPane completedScroll = new JScrollPane(completedArea);
        completedScroll.setBorder(BorderFactory.createTitledBorder("完成送達"));
        completedScroll.setPreferredSize(new Dimension(240, 250));

        panel.add(customerScroll);
        panel.add(queueScroll);
        panel.add(processingScroll);
        panel.add(completedScroll);

        return panel;
    }

    private JTextArea createTextArea(String placeholder) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("微軟正黑體", Font.PLAIN, 12));
        area.setBackground(new Color(248, 248, 248));
        return area;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEtchedBorder());

        statusLabel = new JLabel("系統就緒");
        statusLabel.setFont(new Font("微軟正黑體", Font.BOLD, 12));
        panel.add(statusLabel);

        return panel;
    }

    private void setupEventHandlers() {
        startButton.addActionListener(e -> startSimulation());
        stopButton.addActionListener(e -> stopSimulation());
    }

    private void startSimulation() {
        isRunning = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        speedSlider.setEnabled(false);
        workerSlider.setEnabled(false); // 禁用員工滑桿
        statusLabel.setText("模擬進行中...");

        // 清空顯示區域
        SwingUtilities.invokeLater(() -> {
            customerArea.setText("");
            queueArea.setText("");
            processingArea.setText("");
            completedArea.setText("");
        });

        // 重置計數器
        customerCounter.set(1);
        tableCounter.set(1);
        orderQueue.clear();

        // 啟動顧客產生線程
        startCustomerGenerator();

        // 啟動工作人員線程
        startWorkers();

        updateGUI();
    }

    private void stopSimulation() {
        isRunning = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        speedSlider.setEnabled(true);
        workerSlider.setEnabled(true); // 啟用員工滑桿
        statusLabel.setText("模擬已停止");

        // 中斷線程
        if (customerGeneratorThread != null) {
            customerGeneratorThread.interrupt();
        }

        if (workerThreads != null) {
            for (Thread worker : workerThreads) {
                if (worker != null) {
                    worker.interrupt();
                }
            }
        }
    }

    private void startCustomerGenerator() {
        customerGeneratorThread = new Thread(() -> {
            Random random = new Random();

            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    // 根據滑桿調整速度 (1-10 對應 2000-500ms)
                    int speed = speedSlider.getValue();
                    int delay = 2500 - (speed * 200);

                    Thread.sleep(delay);

                    if (!isRunning) break;

                    // 生成顧客和訂單
                    generateCustomerAndOrder(random);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        customerGeneratorThread.start();
    }

    private void generateCustomerAndOrder(Random random) {
        int customerId = customerCounter.getAndIncrement();
        int tableNum = tableCounter.getAndIncrement();
        if (tableNum > 20) tableCounter.set(1); // 重置桌號

        FoodType foodType = FoodType.values()[random.nextInt(FoodType.values().length)];
        Order order = new Order(customerId, foodType, tableNum);

        // 添加到佇列
        orderQueue.offer(order);

        // 更新GUI
        SwingUtilities.invokeLater(() -> {
            customerArea.append(String.format("顧客#%d 到店 (桌號:%d)\n", customerId, tableNum));
            customerArea.setCaretPosition(customerArea.getDocument().getLength());
            updateQueueDisplay();
        });
    }

    private void startWorkers() {
        int workerCount = workerSlider.getValue();
        workerThreads = new Thread[workerCount];
        for (int i = 0; i < workerThreads.length; i++) {
            final int workerId = i + 1;
            workerThreads[i] = new Thread(() -> {
                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    try {
                        Order order = orderQueue.take();
                        processOrder(order, workerId);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            workerThreads[i].start();
        }
    }

    private void processOrder(Order order, int workerId) throws InterruptedException {
        // 顯示開始製作
        SwingUtilities.invokeLater(() -> {
            processingArea.append(String.format("工作人員#%d 製作: %s\n", workerId, order.toString()));
            processingArea.setCaretPosition(processingArea.getDocument().getLength());
            updateQueueDisplay();
        });

        // 模擬製作時間
        Thread.sleep(order.getFoodType().getCookingTime());

        if (!isRunning) return;

        // 完成製作
        SwingUtilities.invokeLater(() -> {
            // 從處理中移除
            String processingText = processingArea.getText();
            String lineToRemove = String.format("工作人員#%d 製作: %s\n", workerId, order.toString());
            processingText = processingText.replace(lineToRemove, "");
            processingArea.setText(processingText);

            // 添加到完成區域
            String completionTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            completedArea.append(String.format("%s 完成送達 [%s]\n",
                    order.toString(), completionTime));
            completedArea.setCaretPosition(completedArea.getDocument().getLength());
        });
    }

    private void updateQueueDisplay() {
        StringBuilder queueText = new StringBuilder();
        queueText.append(String.format("佇列中訂單數量: %d\n", orderQueue.size()));
        queueText.append("─────────────────\n");

        int count = 0;
        for (Order order : orderQueue) {
            if (count++ < 10) { // 只顯示前10個
                queueText.append(order.toString()).append("\n");
            } else {
                queueText.append("... 還有更多訂單\n");
                break;
            }
        }

        queueArea.setText(queueText.toString());
    }

    private void updateGUI() {
        Timer timer = new Timer(1000, e -> {
            if (!isRunning) {
                ((Timer) e.getSource()).stop();
                return;
            }

            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(String.format("模擬進行中... | 佇列訂單: %d | 總顧客: %d",
                        orderQueue.size(), customerCounter.get() - 1));
            });
        });
        timer.start();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new RestaurantSimulation2().setVisible(true);
        });
    }
}