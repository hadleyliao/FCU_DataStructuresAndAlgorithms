/************************************
 * 對應課程: Chapter 5
 * CourseWork: 模擬顧客點餐系統
 ************************************/

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

public class RestaurantSimulation5 extends JFrame {
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
    private final String CUSTOMER_EMOJI = "🧑";
    private final String WORKER_EMOJI = "🧑‍🍳";
    private final Map<String, Integer> COOKING_TIME = Map.of(
            "漢堡", 2000, "薯條", 1000, "飲料", 500, "炸雞", 3000,
            "沙拉", 1500, "披薩", 2500, "熱狗", 1800, "咖啡", 800
    );

    public RestaurantSimulation5() {
        initializeGUI();
        setupEventHandlers();
        initializeWorkers();
    }

    private void initializeWorkers() {
        workers.add(new Worker(1, 230, 420, "🧑‍🍳"));
        workers.add(new Worker(2, 350, 420, "🧑‍🍳"));
        workers.add(new Worker(3, 470, 420, "🧑‍🍳"));
    }

    private void initializeGUI() {
        setTitle("餐廳點餐系統模擬");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel dashboardPanel = createDashboardPanel();
        add(dashboardPanel, BorderLayout.NORTH);

        animationPanel = createAnimationPanel();
        add(animationPanel, BorderLayout.CENTER);

        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);

        setSize(1400, 900);
        setLocationRelativeTo(null);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 16, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

        panel.add(createInfoPanel("產生訂單", Color.decode("#f8fafc")));
        panel.add(createInfoPanel("佇列訂單", Color.decode("#fff3cd")));
        panel.add(createInfoPanel("製作餐點", Color.decode("#ffe0b2")));
        panel.add(createInfoPanel("餐點完成", Color.decode("#e0f7fa")));

        return panel;
    }

    private JPanel createInfoPanel(String title, Color bgColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,2,0,Color.decode("#eeeeee")),
                BorderFactory.createTitledBorder(title)
        ));
        panel.setBackground(bgColor);

        JLabel label = new JLabel("0", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 32));
        switch (title) {
            case "產生訂單": orderGeneratedLabel = label; break;
            case "佇列訂單": queuedOrdersLabel = label; break;
            case "製作餐點": processingOrdersLabel = label; break;
            case "餐點完成": completedOrdersLabel = label; break;
        }
        panel.add(label, BorderLayout.CENTER);

        JTextArea area = new JTextArea(3, 15);
        area.setEditable(false);
        area.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        switch (title) {
            case "佇列訂單": queueArea = area; panel.add(scroll, BorderLayout.SOUTH); break;
            case "製作餐點": processingArea = area; panel.add(scroll, BorderLayout.SOUTH); break;
            case "餐點完成": completedArea = area; panel.add(scroll, BorderLayout.SOUTH); break;
        }
        return panel;
    }

    private JPanel createAnimationPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawAnimation((Graphics2D) g);
            }
        };
        panel.setBackground(new Color(245, 248, 255));
        panel.setBorder(new TitledBorder("動畫模擬區域"));
        return panel;
    }

    // ===== 美化動畫區域 =====
    private void drawAnimation(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = animationPanel.getWidth();
        int height = animationPanel.getHeight();

        drawRestaurantScene(g2d, width, height);
        drawWorkers(g2d);
        drawCustomers(g2d);
        drawQueueArea(g2d, width, height);
        drawCompletedArea(g2d, width, height);
    }

    private void drawRestaurantScene(Graphics2D g2d, int width, int height) {
        // 地板漸層
        GradientPaint floorPaint = new GradientPaint(0, height-180, new Color(235,235,235), 0, height, new Color(210,210,210));
        g2d.setPaint(floorPaint);
        g2d.fillRect(0, height-180, width, 180);

        // 櫃台區域圓角卡片
        g2d.setColor(new Color(139,69,19));
        g2d.fillRoundRect(170, 370, 400, 80, 30, 30);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(170, 370, 400, 80, 30, 30);

        g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, 32));
        g2d.drawString("🏪 廚房", 340, 420);

        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        g2d.setColor(new Color(60,60,180));
        g2d.drawString("👥 顧客等待區", 70, 320);

        g2d.setColor(new Color(190,140,60));
        g2d.drawString("📋 訂單佇列", 630, 320);

        g2d.setColor(new Color(60,180,90));
        g2d.drawString("✅ 完成區", 980, 320);
    }

    private void drawWorkers(Graphics2D g2d) {
        synchronized (workers) {
            for (Worker worker : workers) {
                // 工作人員emoji
                g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, 56));
                g2d.drawString(WORKER_EMOJI, worker.x, worker.y);

                // 卡片陰影背景
                g2d.setColor(new Color(255,255,255,180));
                g2d.fillRoundRect(worker.x-20, worker.y+12, 90, 35, 18, 18);

                // 工作狀態
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString("工作人員" + worker.id, worker.x, worker.y+30);

                if (worker.currentOrder != null) {
                    g2d.setColor(Color.RED);
                    g2d.drawString("製作中", worker.x, worker.y+48);
                    // 顯示餐點emoji
                    String emoji = getFoodEmoji(worker.currentOrder.foodType);
                    g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, 36));
                    g2d.drawString(emoji, worker.x+55, worker.y+38);

                    drawProgressBar(g2d, worker);
                } else {
                    g2d.setColor(new Color(60,180,90));
                    g2d.drawString("待命中", worker.x, worker.y+48);
                }
            }
        }
    }

    private void drawProgressBar(Graphics2D g2d, Worker worker) {
        if (worker.currentOrder == null) return;

        long elapsed = System.currentTimeMillis() - worker.startTime;
        int totalTime = COOKING_TIME.get(worker.currentOrder.foodType);
        float progress = Math.min(1.0f, (float) elapsed / totalTime);

        int barWidth = 70;
        int barHeight = 12;
        int barX = worker.x+2;
        int barY = worker.y+55;

        // 背景圓角
        g2d.setColor(new Color(230,230,230));
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 10, 10);

        // 進度漸層
        GradientPaint gp = new GradientPaint(barX, barY, new Color(60,180,90), barX+barWidth, barY, new Color(250,210,60));
        g2d.setPaint(gp);
        g2d.fillRoundRect(barX, barY, (int)(barWidth * progress), barHeight, 10, 10);

        // 邊框
        g2d.setColor(new Color(190,190,190));
        g2d.drawRoundRect(barX, barY, barWidth, barHeight, 10, 10);
    }

    private void drawCustomers(Graphics2D g2d) {
        synchronized (customers) {
            for (Customer customer : customers) {
                // 顧客emoji
                g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, 56));
                g2d.drawString(CUSTOMER_EMOJI, customer.x, customer.y);

                // 對話泡泡-餐點emoji
                if (customer.order != null) {
                    String emoji = getFoodEmoji(customer.order.foodType);
                    g2d.setColor(Color.WHITE);
                    g2d.fillRoundRect(customer.x+35, customer.y-38, 55, 44, 18, 18);
                    g2d.setColor(new Color(180,180,180));
                    g2d.drawRoundRect(customer.x+35, customer.y-38, 55, 44, 18, 18);
                    g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, 36));
                    g2d.drawString(emoji, customer.x+42, customer.y-8);
                }

                // 顧客ID
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2d.setColor(new Color(60,60,180));
                g2d.drawString("C" + customer.id, customer.x, customer.y+25);

                // 狀態顏色
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2d.setColor(getCustomerStatusColor(customer.status));
                g2d.drawString(customer.status, customer.x-5, customer.y+40);
            }
        }
    }

    private Color getCustomerStatusColor(String status) {
        switch (status) {
            case "點餐中": return new Color(250,210,60);
            case "等待中": return new Color(220,70,70);
            case "完成": return new Color(60,180,90);
            default: return Color.GRAY;
        }
    }

    private void drawQueueArea(Graphics2D g2d, int width, int height) {
        // 佇列區域卡片陰影
        g2d.setColor(new Color(255, 255, 200, 120));
        g2d.fillRoundRect(600, 340, 300, 150, 30, 30);
        g2d.setColor(new Color(200,200,70));
        g2d.drawRoundRect(600, 340, 300, 150, 30, 30);

        int startX = 620;
        int startY = 360;
        int col = 0, row = 0;

        synchronized (orderQueue) {
            for (Order order : orderQueue) {
                int x = startX + (col * 74);
                int y = startY + (row * 48);

                // 訂單圓角卡片
                g2d.setColor(new Color(255,250,170,240));
                g2d.fillRoundRect(x, y, 65, 38, 16, 16);
                g2d.setColor(new Color(200,180,80));
                g2d.drawRoundRect(x, y, 65, 38, 16, 16);

                String emoji = getFoodEmoji(order.foodType);
                g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
                g2d.drawString(emoji, x+5, y+30);

                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString("T"+ order.tableNumber, x+32, y+18);
                g2d.drawString(order.foodType.substring(0,2), x+32, y+32);

                col++;
                if (col >= 4) {
                    col = 0; row++;
                    if (row >= 3) break;
                }
            }
        }
    }

    private void drawCompletedArea(Graphics2D g2d, int width, int height) {
        g2d.setColor(new Color(200,255,200,120));
        g2d.fillRoundRect(960, 340, 200, 150, 30, 30);
        g2d.setColor(new Color(60,180,90));
        g2d.drawRoundRect(960, 340, 200, 150, 30, 30);

        int startX = 980;
        int startY = 360;
        int count = 0;

        synchronized (completedOrders) {
            int start = Math.max(0, completedOrders.size() - 6);
            for (int i = start; i < completedOrders.size() && count < 6; i++) {
                Order order = completedOrders.get(i);
                int x = startX + (count % 3) * 62;
                int y = startY + (count / 3) * 48;

                g2d.setColor(new Color(230,255,230));
                g2d.fillRoundRect(x, y, 55, 38, 14, 14);
                g2d.setColor(new Color(60,180,90));
                g2d.drawRoundRect(x, y, 55, 38, 14, 14);

                String emoji = getFoodEmoji(order.foodType);
                g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
                g2d.drawString(emoji, x+5, y+28);

                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString("T"+order.tableNumber, x+30, y+15);
                g2d.drawString("✓", x+30, y+33);

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
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

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

        customers.clear(); orderQueue.clear(); processingOrders.clear(); completedOrders.clear();
        customerCounter.set(0); tableCounter.set(1);

        animationTimer.start();

        new Thread(this::customerGeneratorThread).start();

        for (Worker worker : workers) {
            new Thread(() -> workerThread(worker)).start();
        }
    }

    private void stopSimulation() {
        isRunning = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        animationTimer.stop();

        for (Worker worker : workers) {
            worker.currentOrder = null; worker.startTime = 0;
        }
    }

    private void customerGeneratorThread() {
        Random random = new Random();
        while (isRunning) {
            try {
                int interval = speedSlider.getValue() + random.nextInt(1000);
                Thread.sleep(interval);

                if (isRunning) generateCustomer();
            } catch (InterruptedException e) { break; }
        }
    }

    private void generateCustomer() {
        int customerId = customerCounter.incrementAndGet();
        int tableNumber = tableCounter.getAndIncrement();
        if (tableNumber > 20) tableCounter.set(1);

        String foodType = FOOD_TYPES[new Random().nextInt(FOOD_TYPES.length)];
        Order order = new Order(customerId, tableNumber, foodType, System.currentTimeMillis());

        Customer customer = new Customer(customerId, 70, 270, order);
        customers.add(customer);

        javax.swing.Timer moveTimer = new javax.swing.Timer(45, null);
        moveTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (customer.x < 200) {
                    customer.x += 5;
                    customer.status = "點餐中";
                } else {
                    orderQueue.offer(order);
                    customer.status = "等待中";
                    customer.x = 640; customer.y = 310;
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

                int cookingTime = COOKING_TIME.get(order.foodType);
                Thread.sleep(cookingTime);

                if (isRunning) {
                    processingOrders.remove(order);
                    order.completedTime = System.currentTimeMillis();
                    completedOrders.add(order);

                    synchronized (customers) {
                        customers.stream()
                                .filter(c -> c.id == order.customerId)
                                .findFirst()
                                .ifPresent(c -> {
                                    c.status = "完成";
                                    javax.swing.Timer leaveTimer = new javax.swing.Timer(90, null);
                                    leaveTimer.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            c.x += 6;
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
            } catch (InterruptedException e) { break; }
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
        // 更新佇列
        StringBuilder queueText = new StringBuilder();
        synchronized (orderQueue) {
            for (Order order : orderQueue) {
                String emoji = getFoodEmoji(order.foodType);
                queueText.append(String.format("桌%d: %s %s\n", order.tableNumber, emoji, order.foodType));
            }
        }
        queueArea.setText(queueText.toString());

        // 製作區
        StringBuilder processText = new StringBuilder();
        synchronized (processingOrders) {
            for (Order order : processingOrders) {
                String emoji = getFoodEmoji(order.foodType);
                processText.append(String.format("桌%d: %s %s (製作中)\n", order.tableNumber, emoji, order.foodType));
            }
        }
        processingArea.setText(processText.toString());

        // 完成區
        StringBuilder completeText = new StringBuilder();
        synchronized (completedOrders) {
            int start = Math.max(0, completedOrders.size() - 10);
            for (int i = start; i < completedOrders.size(); i++) {
                Order order = completedOrders.get(i);
                String emoji = getFoodEmoji(order.foodType);
                long duration = order.completedTime - order.orderTime;
                completeText.append(String.format("桌%d: %s %s (%.1fs)\n",
                        order.tableNumber, emoji, order.foodType, duration / 1000.0));
            }
        }
        completedArea.setText(completeText.toString());
    }

    // ===== 內部類別 =====
    private static class Order {
        int customerId, tableNumber;
        String foodType;
        long orderTime, completedTime;

        Order(int customerId, int tableNumber, String foodType, long orderTime) {
            this.customerId = customerId;
            this.tableNumber = tableNumber;
            this.foodType = foodType;
            this.orderTime = orderTime;
        }
    }

    private static class Customer {
        int id, x, y;
        String status;
        Order order;

        Customer(int id, int x, int y, Order order) {
            this.id = id; this.x = x; this.y = y;
            this.status = "進場"; this.order = order;
        }
    }

    private static class Worker {
        int id, x, y;
        String emoji;
        Order currentOrder;
        long startTime;

        Worker(int id, int x, int y, String emoji) {
            this.id = id; this.x = x; this.y = y; this.emoji = emoji;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 建議加入FlatLaf美化（若有FlatLaf jar）:
                // com.formdev.flatlaf.FlatLightLaf.install();
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) { e.printStackTrace(); }
            new RestaurantSimulation5().setVisible(true);
        });
    }
}