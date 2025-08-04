package _20250728;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RestaurantSimulation3 extends JFrame {
    // 系統組件
    private BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();
    private List<Order> processingOrders = Collections.synchronizedList(new ArrayList<>());
    private List<Order> completedOrders = Collections.synchronizedList(new ArrayList<>());

    // GUI 組件
    private JLabel orderGeneratedLabel, queuedOrdersLabel, processingOrdersLabel, completedOrdersLabel;
    private JTextArea queueArea, processingArea, completedArea;
    private JPanel animationPanel;
    private JSlider speedSlider;
    private JButton startButton, stopButton;

    // 系統狀態
    private volatile boolean isRunning = false;
    private AtomicInteger customerCounter = new AtomicInteger(0);
    private AtomicInteger tableCounter = new AtomicInteger(1);
    private javax.swing.Timer animationTimer;
    private List<Customer> customers = Collections.synchronizedList(new ArrayList<>());
    private List<Worker> workers = Collections.synchronizedList(new ArrayList<>());

    // 餐點類型和對應emoji
    private final String[] FOOD_TYPES = {"漢堡", "薯條", "飲料", "炸雞", "沙拉", "披薩", "熱狗", "咖啡"};
    private final String[] FOOD_EMOJIS = {"🍔", "🍟", "🥤", "🍗", "🥗", "🍕", "🌭", "☕"};
    private final Map<String, Integer> COOKING_TIME = Map.of(
            "漢堡", 2000, "薯條", 1000, "飲料", 500, "炸雞", 3000,
            "沙拉", 1500, "披薩", 2500, "熱狗", 1800, "咖啡", 800
    );

    public RestaurantSimulation3() {
        initializeGUI();
        setupEventHandlers();
        initializeWorkers();
    }

    private void initializeWorkers() {
        // 初始化3個工作人員
        workers.add(new Worker(1, 200, 400, "👨‍🍳"));
        workers.add(new Worker(2, 300, 400, "👩‍🍳"));
        workers.add(new Worker(3, 400, 400, "🧑‍🍳"));
    }

    private void initializeGUI() {
        setTitle("餐廳點餐系統模擬");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 上方儀表板
        JPanel dashboardPanel = createDashboardPanel();
        add(dashboardPanel, BorderLayout.NORTH);

        // 中間動畫區域
        animationPanel = createAnimationPanel();
        add(animationPanel, BorderLayout.CENTER);

        // 下方控制面板
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);

        setSize(1400, 900);
        setLocationRelativeTo(null);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 產生訂單
        JPanel orderGenPanel = createInfoPanel("產生訂單", Color.LIGHT_GRAY);
        orderGeneratedLabel = new JLabel("0", SwingConstants.CENTER);
        orderGeneratedLabel.setFont(new Font("標楷體", Font.BOLD, 24));
        orderGenPanel.add(orderGeneratedLabel, BorderLayout.CENTER);

        // 佇列訂單
        JPanel queuePanel = createInfoPanel("佇列訂單", Color.YELLOW);
        queuedOrdersLabel = new JLabel("0", SwingConstants.CENTER);
        queuedOrdersLabel.setFont(new Font("標楷體", Font.BOLD, 24));
        queuePanel.add(queuedOrdersLabel, BorderLayout.CENTER);
        queueArea = new JTextArea(3, 15);
        queueArea.setEditable(false);
        queueArea.setFont(new Font("標楷體", Font.PLAIN, 10));
        JScrollPane queueScroll = new JScrollPane(queueArea);
        queuePanel.add(queueScroll, BorderLayout.SOUTH);

        // 製作餐點
        JPanel processPanel = createInfoPanel("製作餐點", Color.ORANGE);
        processingOrdersLabel = new JLabel("0", SwingConstants.CENTER);
        processingOrdersLabel.setFont(new Font("標楷體", Font.BOLD, 24));
        processPanel.add(processingOrdersLabel, BorderLayout.CENTER);
        processingArea = new JTextArea(3, 15);
        processingArea.setEditable(false);
        processingArea.setFont(new Font("標楷體", Font.PLAIN, 10));
        JScrollPane processScroll = new JScrollPane(processingArea);
        processPanel.add(processScroll, BorderLayout.SOUTH);

        // 餐點完成
        JPanel completePanel = createInfoPanel("餐點完成", Color.GREEN);
        completedOrdersLabel = new JLabel("0", SwingConstants.CENTER);
        completedOrdersLabel.setFont(new Font("標楷體", Font.BOLD, 24));
        completePanel.add(completedOrdersLabel, BorderLayout.CENTER);
        completedArea = new JTextArea(3, 15);
        completedArea.setEditable(false);
        completedArea.setFont(new Font("標楷體", Font.PLAIN, 10));
        JScrollPane completeScroll = new JScrollPane(completedArea);
        completePanel.add(completeScroll, BorderLayout.SOUTH);

        panel.add(orderGenPanel);
        panel.add(queuePanel);
        panel.add(processPanel);
        panel.add(completePanel);

        return panel;
    }

    private JPanel createInfoPanel(String title, Color bgColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder(title));
        panel.setBackground(bgColor);
        return panel;
    }

    private JPanel createAnimationPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawAnimation(g);
            }
        };
        panel.setBackground(new Color(240, 248, 255)); // 淡藍色背景
        panel.setBorder(new TitledBorder("動畫模擬區域"));
        return panel;
    }

    private void drawAnimation(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        int width = animationPanel.getWidth();
        int height = animationPanel.getHeight();

        // 繪製餐廳場景
        drawRestaurantScene(g2d, width, height);

        // 繪製工作人員
        drawWorkers(g2d);

        // 繪製顧客
        drawCustomers(g2d);

        // 繪製佇列區域
        drawQueueArea(g2d, width, height);

        // 繪製完成區標示
        drawCompletedArea(g2d, width, height);
    }

    private void drawRestaurantScene(Graphics2D g2d, int width, int height) {
        // 繪製地板線條
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{10}, 0));
        for (int i = 50; i < width; i += 100) {
            g2d.drawLine(i, height - 200, i + 50, height - 200);
        }

        // 繪製櫃台
        g2d.setColor(new Color(139, 69, 19)); // 棕色
        g2d.fillRoundRect(150, 350, 400, 80, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(150, 350, 400, 80, 15, 15);
        g2d.setFont(new Font("標楷體", Font.BOLD, 16));
        g2d.drawString("🏪 廚房工作區", 300, 375);

        // 繪製等待區標示
        g2d.setColor(Color.BLUE);
        g2d.drawString("👥 顧客等待區", 50, 300);

        // 繪製佇列標示
        g2d.drawString("📋 訂單佇列", 600, 300);

        // 繪製完成區標示
        g2d.drawString("✅ 完成區", 950, 300);
    }

    private void drawWorkers(Graphics2D g2d) {
        synchronized (workers) {
            for (Worker worker : workers) {
                // 工作人員emoji
                g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
                g2d.drawString(worker.emoji, worker.x, worker.y);

                // 工作狀態
                g2d.setFont(new Font("標楷體", Font.PLAIN, 12));
                g2d.setColor(Color.BLACK);
                g2d.drawString("工作人員" + worker.id, worker.x - 10, worker.y + 40);

                if (worker.currentOrder != null) {
                    g2d.setColor(Color.RED);
                    g2d.drawString("製作中:", worker.x - 15, worker.y + 55);
                    // 顯示正在製作的餐點emoji
                    String emoji = getFoodEmoji(worker.currentOrder.foodType);
                    g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
                    g2d.drawString(emoji, worker.x + 40, worker.y + 55);

                    // 進度條
                    drawProgressBar(g2d, worker);
                } else {
                    g2d.setColor(Color.GREEN);
                    g2d.drawString("待命中", worker.x - 5, worker.y + 55);
                }
            }
        }
    }

    private void drawProgressBar(Graphics2D g2d, Worker worker) {
        if (worker.currentOrder == null) return;

        long elapsed = System.currentTimeMillis() - worker.startTime;
        int totalTime = COOKING_TIME.get(worker.currentOrder.foodType);
        float progress = Math.min(1.0f, (float) elapsed / totalTime);

        int barWidth = 60;
        int barHeight = 8;
        int barX = worker.x - 10;
        int barY = worker.y + 65;

        // 背景
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(barX, barY, barWidth, barHeight);

        // 進度
        g2d.setColor(Color.GREEN);
        g2d.fillRect(barX, barY, (int)(barWidth * progress), barHeight);

        // 邊框
        g2d.setColor(Color.BLACK);
        g2d.drawRect(barX, barY, barWidth, barHeight);
    }

    private void drawCustomers(Graphics2D g2d) {
        synchronized (customers) {
            for (Customer customer : customers) {
                // 顧客emoji
                g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 25));
                g2d.drawString("🙋‍♂️", customer.x, customer.y);

                // 顧客想要的餐點
                if (customer.order != null) {
                    String emoji = getFoodEmoji(customer.order.foodType);
                    g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                    // 對話泡泡
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval(customer.x + 25, customer.y - 30, 35, 25);
                    g2d.setColor(Color.BLACK);
                    g2d.drawOval(customer.x + 25, customer.y - 30, 35, 25);
                    g2d.drawString(emoji, customer.x + 30, customer.y - 10);
                }

                // 顧客ID
                g2d.setFont(new Font("標楷體", Font.PLAIN, 12));
                g2d.setColor(Color.BLUE);
                g2d.drawString("C" + customer.id, customer.x, customer.y + 25);

                // 狀態顯示
                g2d.setColor(getCustomerStatusColor(customer.status));
                g2d.drawString(customer.status, customer.x - 10, customer.y + 40);
            }
        }
    }

    private Color getCustomerStatusColor(String status) {
        switch (status) {
            case "點餐中": return Color.ORANGE;
            case "等待中": return Color.RED;
            case "完成": return Color.GREEN;
            default: return Color.BLACK;
        }
    }

    private void drawQueueArea(Graphics2D g2d, int width, int height) {
        // 佇列區域背景
        g2d.setColor(new Color(255, 255, 200, 100));
        g2d.fillRect(580, 320, 300, 150);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(580, 320, 300, 150);

        // 繪製佇列中的訂單
        int startX = 590;
        int startY = 340;
        int col = 0, row = 0;

        synchronized (orderQueue) {
            for (Order order : orderQueue) {
                int x = startX + (col * 70);
                int y = startY + (row * 40);

                // 訂單卡片
                g2d.setColor(Color.YELLOW);
                g2d.fillRect(x, y, 60, 30);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, 60, 30);

                // 餐點emoji
                String emoji = getFoodEmoji(order.foodType);
                g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                g2d.drawString(emoji, x + 5, y + 20);

                // 桌號
                g2d.setFont(new Font("標楷體", Font.PLAIN, 10));
                g2d.drawString("T" + order.tableNumber, x + 25, y + 15);
                g2d.drawString(order.foodType.substring(0, 2), x + 25, y + 25);

                col++;
                if (col >= 4) {
                    col = 0;
                    row++;
                    if (row >= 3) break;
                }
            }
        }
    }

    private void drawCompletedArea(Graphics2D g2d, int width, int height) {
        // 完成區域背景
        g2d.setColor(new Color(200, 255, 200, 100));
        g2d.fillRect(920, 320, 200, 150);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(920, 320, 200, 150);

        // 顯示最近完成的訂單
        int startX = 930;
        int startY = 340;
        int count = 0;

        synchronized (completedOrders) {
            int start = Math.max(0, completedOrders.size() - 6);
            for (int i = start; i < completedOrders.size() && count < 6; i++) {
                Order order = completedOrders.get(i);
                int x = startX + (count % 3) * 60;
                int y = startY + (count / 3) * 40;

                // 完成的餐點
                g2d.setColor(Color.GREEN);
                g2d.fillRect(x, y, 50, 30);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, 50, 30);

                String emoji = getFoodEmoji(order.foodType);
                g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                g2d.drawString(emoji, x + 5, y + 20);

                g2d.setFont(new Font("標楷體", Font.PLAIN, 8));
                g2d.drawString("T" + order.tableNumber, x + 25, y + 12);
                g2d.drawString("✓", x + 25, y + 25);

                count++;
            }
        }
    }

    private String getFoodEmoji(String foodType) {
        for (int i = 0; i < FOOD_TYPES.length; i++) {
            if (FOOD_TYPES[i].equals(foodType)) {
                return FOOD_EMOJIS[i];
            }
        }
        return "🍽️";
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        panel.add(new JLabel("顧客到達速度:"));
        speedSlider = new JSlider(500, 3000, 1500);
        speedSlider.setMajorTickSpacing(500);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        panel.add(speedSlider);

        startButton = new JButton("開始模擬");
        stopButton = new JButton("結束模擬");
        stopButton.setEnabled(false);

        panel.add(startButton);
        panel.add(stopButton);

        return panel;
    }

    private void setupEventHandlers() {
        startButton.addActionListener(e -> startSimulation());
        stopButton.addActionListener(e -> stopSimulation());

        // 動畫定時器
        animationTimer = new javax.swing.Timer(100, e -> {
            updateCustomerPositions();
            updateGUI();
            animationPanel.repaint();
        });
    }

    private void startSimulation() {
        isRunning = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        // 清空之前的數據
        customers.clear();
        orderQueue.clear();
        processingOrders.clear();
        completedOrders.clear();
        customerCounter.set(0);
        tableCounter.set(1);

        // 啟動動畫
        animationTimer.start();

        // 啟動顧客生成執行緒
        new Thread(this::customerGeneratorThread).start();

        // 啟動工作人員執行緒
        for (Worker worker : workers) {
            new Thread(() -> workerThread(worker)).start();
        }
    }

    private void stopSimulation() {
        isRunning = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        animationTimer.stop();

        // 重置工作人員狀態
        for (Worker worker : workers) {
            worker.currentOrder = null;
            worker.startTime = 0;
        }
    }

    private void customerGeneratorThread() {
        Random random = new Random();
        while (isRunning) {
            try {
                int interval = speedSlider.getValue() + random.nextInt(1000);
                Thread.sleep(interval);

                if (isRunning) {
                    generateCustomer();
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void generateCustomer() {
        int customerId = customerCounter.incrementAndGet();
        int tableNumber = tableCounter.getAndIncrement();
        if (tableNumber > 20) tableCounter.set(1);

        // 隨機選擇餐點
        String foodType = FOOD_TYPES[new Random().nextInt(FOOD_TYPES.length)];
        Order order = new Order(customerId, tableNumber, foodType, System.currentTimeMillis());

        // 創建顧客動畫
        Customer customer = new Customer(customerId, 50, 250, order);
        customers.add(customer);

        // 顧客移動到櫃台後才加入訂單佇列
        javax.swing.Timer moveTimer = new javax.swing.Timer(50, null);
        moveTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (customer.x < 120) {
                    customer.x += 3;
                    customer.status = "點餐中";
                } else {
                    // 到達櫃台，加入訂單佇列
                    orderQueue.offer(order);
                    customer.status = "等待中";
                    customer.x = 600; // 移動到等待區
                    customer.y = 280;
                    moveTimer.stop();
                }
            }
        });
        moveTimer.start();
    }

    private void workerThread(Worker worker) {
        while (isRunning) {
            try {
                Order order = orderQueue.take();
                worker.currentOrder = order;
                worker.startTime = System.currentTimeMillis();
                processingOrders.add(order);

                // 模擬製作時間
                int cookingTime = COOKING_TIME.get(order.foodType);
                Thread.sleep(cookingTime);

                if (isRunning) {
                    processingOrders.remove(order);
                    order.completedTime = System.currentTimeMillis();
                    completedOrders.add(order);

                    // 顧客完成狀態
                    synchronized (customers) {
                        customers.stream()
                                .filter(c -> c.id == order.customerId)
                                .findFirst()
                                .ifPresent(c -> {
                                    c.status = "完成";
                                    // 顧客離開動畫
                                    javax.swing.Timer leaveTimer = new javax.swing.Timer(100, null);
                                    leaveTimer.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            c.x += 5;
                                            if (c.x > animationPanel.getWidth()) {
                                                customers.remove(c);
                                                leaveTimer.stop();
                                            }
                                        }
                                    });
                                    leaveTimer.start();
                                });
                    }

                    worker.currentOrder = null;
                    worker.startTime = 0;
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void updateCustomerPositions() {
        // 顧客位置更新已在個別timer中處理
    }

    private void updateGUI() {
        SwingUtilities.invokeLater(() -> {
            orderGeneratedLabel.setText(String.valueOf(customerCounter.get()));
            queuedOrdersLabel.setText(String.valueOf(orderQueue.size()));
            processingOrdersLabel.setText(String.valueOf(processingOrders.size()));
            completedOrdersLabel.setText(String.valueOf(completedOrders.size()));

            updateTextAreas();
        });
    }

    private void updateTextAreas() {
        // 更新佇列區域
        StringBuilder queueText = new StringBuilder();
        synchronized (orderQueue) {
            for (Order order : orderQueue) {
                String emoji = getFoodEmoji(order.foodType);
                queueText.append(String.format("桌%d:%s%s\n", order.tableNumber, emoji, order.foodType));
            }
        }
        queueArea.setText(queueText.toString());

        // 更新製作區域
        StringBuilder processText = new StringBuilder();
        synchronized (processingOrders) {
            for (Order order : processingOrders) {
                String emoji = getFoodEmoji(order.foodType);
                processText.append(String.format("桌%d:%s%s(製作中)\n", order.tableNumber, emoji, order.foodType));
            }
        }
        processingArea.setText(processText.toString());

        // 更新完成區域
        StringBuilder completeText = new StringBuilder();
        synchronized (completedOrders) {
            int start = Math.max(0, completedOrders.size() - 10);
            for (int i = start; i < completedOrders.size(); i++) {
                Order order = completedOrders.get(i);
                String emoji = getFoodEmoji(order.foodType);
                long duration = order.completedTime - order.orderTime;
                completeText.append(String.format("桌%d:%s%s(%.1fs)\n",
                        order.tableNumber, emoji, order.foodType, duration / 1000.0));
            }
        }
        completedArea.setText(completeText.toString());
    }

    // 內部類別
    private static class Order {
        int customerId;
        int tableNumber;
        String foodType;
        long orderTime;
        long completedTime;

        Order(int customerId, int tableNumber, String foodType, long orderTime) {
            this.customerId = customerId;
            this.tableNumber = tableNumber;
            this.foodType = foodType;
            this.orderTime = orderTime;
        }
    }

    private static class Customer {
        int id;
        int x, y;
        String status;
        Order order;

        Customer(int id, int x, int y, Order order) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.status = "進場";
            this.order = order;
        }
    }

    private static class Worker {
        int id;
        int x, y;
        String emoji;
        Order currentOrder;
        long startTime;

        Worker(int id, int x, int y, String emoji) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.emoji = emoji;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            new RestaurantSimulation6().setVisible(true);
        });
    }
}