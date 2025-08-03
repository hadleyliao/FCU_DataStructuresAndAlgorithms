/*****************************
 * 對應課程: Chapter 5
 * CourseWork2: 模擬顧客點餐系統
 *****************************/

package _20250728;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class RestaurantSimulation extends JFrame {
    private final BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();
    private final List<Order> processingOrders = new CopyOnWriteArrayList<>();
    private final List<Order> completedOrders = new CopyOnWriteArrayList<>();

    private volatile boolean isRunning = true;
    private int customerSpeed = 1000; // 預設1秒
    private int workerCount = 2; // 預設2個工作人員

    // GUI 元件
    private JTextArea logArea;
    private JTextArea queueArea;
    private JTextArea processingArea;
    private JTextArea completedArea;
    private JSlider speedSlider;
    private JSlider workerSlider;
    private AnimationPanel animationPanel;

    // 餐點類型與製作時間
    private static final Map<String, Integer> FOOD_TIMES = Map.of(
            "漢堡", 2000,
            "薯條", 1000,
            "飲料", 500,
            "沙拉", 800,
            "披薩", 3000,
            "炸雞", 1500
    );

    public RestaurantSimulation() {
        setTitle("餐廳點餐系統模擬");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initComponents();

        // 啟動顧客產生執行緒
        new Thread(this::generateCustomers).start();

        // 啟動工作人員執行緒
        startWorkers();
    }

    private void initComponents() {
        // 控制面板
        JPanel controlPanel = new JPanel(new GridLayout(1, 4));

        speedSlider = new JSlider(500, 2000, 1000);
        speedSlider.setMajorTickSpacing(500);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(e -> customerSpeed = speedSlider.getValue());

        workerSlider = new JSlider(1, 5, 2);
        workerSlider.setMajorTickSpacing(1);
        workerSlider.setPaintTicks(true);
        workerSlider.setPaintLabels(true);
        workerSlider.addChangeListener(e -> {
            workerCount = workerSlider.getValue();
            startWorkers(); // 重新調整工作人員數量
        });

        JButton stopButton = new JButton("結束模擬");
        stopButton.addActionListener(e -> {
            isRunning = false;
            stopButton.setEnabled(false);
        });

        controlPanel.add(new JLabel("顧客到達速度:"));
        controlPanel.add(speedSlider);
        controlPanel.add(new JLabel("工作人員數量:"));
        controlPanel.add(workerSlider);
        controlPanel.add(stopButton);

        add(controlPanel, BorderLayout.NORTH);

        // 主顯示面板
        JPanel mainPanel = new JPanel(new GridLayout(1, 3));

        // 訂單佇列面板
        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBorder(BorderFactory.createTitledBorder("等待處理的訂單"));
        queueArea = new JTextArea();
        queueArea.setEditable(false);
        queuePanel.add(new JScrollPane(queueArea), BorderLayout.CENTER);

        // 處理中面板
        JPanel processingPanel = new JPanel(new BorderLayout());
        processingPanel.setBorder(BorderFactory.createTitledBorder("處理中的訂單"));
        processingArea = new JTextArea();
        processingArea.setEditable(false);
        processingPanel.add(new JScrollPane(processingArea), BorderLayout.CENTER);

        // 已完成面板
        JPanel completedPanel = new JPanel(new BorderLayout());
        completedPanel.setBorder(BorderFactory.createTitledBorder("已完成的訂單"));
        completedArea = new JTextArea();
        completedArea.setEditable(false);
        completedPanel.add(new JScrollPane(completedArea), BorderLayout.CENTER);

        mainPanel.add(queuePanel);
        mainPanel.add(processingPanel);
        mainPanel.add(completedPanel);

        add(mainPanel, BorderLayout.CENTER);

        // 動畫面板
        animationPanel = new AnimationPanel();
        add(animationPanel, BorderLayout.SOUTH);

        // 日誌面板
        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);

        // 更新GUI的定時器
        new javax.swing.Timer(500, e -> updateGUI()).start();
    }

    private void startWorkers() {
        // 先停止所有現有工作人員
        for (Thread worker : Thread.getAllStackTraces().keySet()) {
            if (worker.getName().startsWith("Worker-")) {
                worker.interrupt();
            }
        }

        // 啟動新的工作人員
        for (int i = 0; i < workerCount; i++) {
            Thread worker = new Thread(this::processOrders, "Worker-" + i);
            worker.start();
        }
    }

    private void generateCustomers() {
        Random random = new Random();
        int customerId = 1;

        while (isRunning) {
            try {
                // 隨機等待時間 (0.5~2秒)
                Thread.sleep(customerSpeed);

                if (!isRunning) break;

                // 隨機選擇餐點
                List<String> foods = new ArrayList<>(FOOD_TIMES.keySet());
                Collections.shuffle(foods);
                int itemCount = random.nextInt(3) + 1; // 1~3個餐點
                List<String> selectedFoods = foods.subList(0, itemCount);

                // 創建訂單
                Order order = new Order(
                        customerId++,
                        "桌號-" + (random.nextInt(10) + 1),
                        selectedFoods,
                        System.currentTimeMillis()
                );

                orderQueue.put(order);
                log("新顧客到達! " + order);
                animationPanel.addOrderToQueue(order);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processOrders() {
        while (isRunning) {
            try {
                Order order = orderQueue.take();
                processingOrders.add(order);
                animationPanel.moveOrderToProcessing(order);
                log("開始處理訂單: " + order);

                // 計算總製作時間 (取最長的單一餐點時間)
                int maxTime = order.getFoods().stream()
                        .mapToInt(FOOD_TIMES::get)
                        .max()
                        .orElse(0);

                // 模擬製作時間
                Thread.sleep(maxTime);

                // 訂單完成
                processingOrders.remove(order);
                order.setCompletionTime(System.currentTimeMillis());
                completedOrders.add(order);
                animationPanel.moveOrderToCompleted(order);
                log("訂單完成: " + order);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void updateGUI() {
        // 更新訂單佇列顯示
        queueArea.setText("");
        orderQueue.forEach(order -> queueArea.append(order + "\n"));

        // 更新處理中訂單顯示
        processingArea.setText("");
        processingOrders.forEach(order -> processingArea.append(order + "\n"));

        // 更新已完成訂單顯示
        completedArea.setText("");
        completedOrders.forEach(order -> completedArea.append(order + "\n"));

        // 重繪動畫
        animationPanel.repaint();
    }

    private void log(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String timestamp = sdf.format(new Date());
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RestaurantSimulation simulation = new RestaurantSimulation();
            simulation.setVisible(true);
        });
    }

    // 訂單類
    private static class Order {
        private final int customerId;
        private final String tableNumber;
        private final List<String> foods;
        private final long orderTime;
        private long completionTime;

        public Order(int customerId, String tableNumber, List<String> foods, long orderTime) {
            this.customerId = customerId;
            this.tableNumber = tableNumber;
            this.foods = foods;
            this.orderTime = orderTime;
        }

        public List<String> getFoods() {
            return foods;
        }

        public void setCompletionTime(long completionTime) {
            this.completionTime = completionTime;
        }

        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            return String.format("顧客#%d (%s) - 餐點: %s - 下單時間: %s",
                    customerId,
                    tableNumber,
                    String.join(", ", foods),
                    sdf.format(new Date(orderTime)));
        }
    }

    // 動畫面板
    private class AnimationPanel extends JPanel {
        private final List<Order> queueOrders = new ArrayList<>();
        private final List<Order> processingOrdersAnim = new ArrayList<>();
        private final List<Order> completedOrdersAnim = new ArrayList<>();

        public AnimationPanel() {
            setPreferredSize(new Dimension(900, 150));
            setBackground(Color.WHITE);
        }

        public void addOrderToQueue(Order order) {
            queueOrders.add(order);
            repaint();
        }

        public void moveOrderToProcessing(Order order) {
            queueOrders.remove(order);
            processingOrdersAnim.add(order);
            repaint();
        }

        public void moveOrderToCompleted(Order order) {
            processingOrdersAnim.remove(order);
            completedOrdersAnim.add(order);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // 繪製三個區域
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(50, 50, 250, 80);  // 等待區
            g.fillRect(350, 50, 250, 80); // 處理區
            g.fillRect(650, 50, 250, 80); // 完成區

            g.setColor(Color.BLACK);
            g.drawString("等待區", 150, 40);
            g.drawString("處理區", 450, 40);
            g.drawString("完成區", 750, 40);

            // 繪製等待區訂單
            for (int i = 0; i < queueOrders.size(); i++) {
                if (i < 5) { // 最多顯示5個
                    drawOrder(g, queueOrders.get(i), 60 + i * 50, 60);
                }
            }

            // 繪製處理區訂單
            for (int i = 0; i < processingOrdersAnim.size(); i++) {
                if (i < 5) { // 最多顯示5個
                    drawOrder(g, processingOrdersAnim.get(i), 360 + i * 50, 60);
                }
            }

            // 繪製完成區訂單
            for (int i = 0; i < completedOrdersAnim.size(); i++) {
                if (i < 5) { // 最多顯示5個
                    drawOrder(g, completedOrdersAnim.get(i), 660 + i * 50, 60);
                }
            }
        }

        private void drawOrder(Graphics g, Order order, int x, int y) {
            g.setColor(new Color(200, 230, 255));
            g.fillRect(x, y, 40, 40);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, 40, 40);
            g.drawString("#" + order.customerId, x + 10, y + 25);
        }
    }
}