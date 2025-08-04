/************************************
 * 對應課程: Chapter 5
 * CourseWork: 模擬顧客點餐系統(gemini)
 ************************************/

package _20250728;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RestaurantSimulation6 extends JFrame {
    // 系統組件
    private BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();
    private List<Order> processingOrders = Collections.synchronizedList(new ArrayList<>());
    private List<Order> completedOrders = Collections.synchronizedList(new ArrayList<>());

    // GUI 組件
    private JLabel orderGeneratedLabel, queuedOrdersLabel, processingOrdersLabel, completedOrdersLabel;
    private JLabel queueLabel, processingLabel, completedLabel; // 使用JLabel替代JTextArea
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

    // 字型
    private Font emojiFont;
    private Font mainFont = new Font("標楷體", Font.PLAIN, 12);
    private Font titleFont = new Font("標楷體", Font.BOLD, 16);

    // 餐點類型和對應emoji
    private final String[] FOOD_TYPES = {"漢堡", "薯條", "飲料", "炸雞", "沙拉", "披薩", "熱狗", "咖啡"};
    private final String[] FOOD_EMOJIS = {"🍔", "🍟", "🥤", "🍗", "🥗", "🍕", "🌭", "☕"};
    private final Map<String, Integer> COOKING_TIME = Map.of(
            "漢堡", 2000, "薯條", 1000, "飲料", 500, "炸雞", 3000,
            "沙拉", 1500, "披薩", 2500, "熱狗", 1800, "咖啡", 800
    );

    public RestaurantSimulation6() {
        initializeEmojiFont();
        initializeGUI();
        setupEventHandlers();
        initializeWorkers();
    }

    private void initializeEmojiFont() {
        // 嘗試尋找系統中的Emoji字型，以提供更好的顯示效果
        Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        boolean foundEmojiFont = false;
        for (Font font : allFonts) {
            if (font.getFontName().contains("Emoji") || font.getFontName().contains("emoji")) {
                emojiFont = font;
                foundEmojiFont = true;
                break;
            }
        }
        // 如果找不到，使用通用字型
        if (!foundEmojiFont) {
            emojiFont = new Font("SansSerif", Font.PLAIN, 20);
        }
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
        setLayout(new BorderLayout(10, 10));

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
        panel.setBackground(new Color(240, 240, 240));

        // 產生訂單
        panel.add(createInfoPanel("產生訂單", Color.LIGHT_GRAY, orderGeneratedLabel = new JLabel("0")));

        // 佇列訂單
        JPanel queuePanel = createInfoPanel("佇列訂單", new Color(255, 255, 204), queuedOrdersLabel = new JLabel("0"));
        queueLabel = new JLabel("<html></html>");
        queueLabel.setFont(mainFont);
        JScrollPane queueScroll = new JScrollPane(queueLabel);
        queueScroll.setPreferredSize(new Dimension(150, 80));
        queuePanel.add(queueScroll, BorderLayout.SOUTH);
        panel.add(queuePanel);

        // 製作餐點
        JPanel processPanel = createInfoPanel("製作餐點", new Color(255, 230, 204), processingOrdersLabel = new JLabel("0"));
        processingLabel = new JLabel("<html></html>");
        processingLabel.setFont(mainFont);
        JScrollPane processScroll = new JScrollPane(processingLabel);
        processScroll.setPreferredSize(new Dimension(150, 80));
        processPanel.add(processScroll, BorderLayout.SOUTH);
        panel.add(processPanel);

        // 餐點完成
        JPanel completePanel = createInfoPanel("餐點完成", new Color(204, 255, 204), completedOrdersLabel = new JLabel("0"));
        completedLabel = new JLabel("<html></html>");
        completedLabel.setFont(mainFont);
        JScrollPane completeScroll = new JScrollPane(completedLabel);
        completeScroll.setPreferredSize(new Dimension(150, 80));
        completePanel.add(completeScroll, BorderLayout.SOUTH);
        panel.add(completePanel);

        return panel;
    }

    private JPanel createInfoPanel(String title, Color bgColor, JLabel label) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(titleFont);
        panel.setBorder(border);
        panel.setBackground(bgColor);

        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("標楷體", Font.BOLD, 28));
        panel.add(label, BorderLayout.CENTER);

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
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(new TitledBorder("動畫模擬區域"));
        return panel;
    }

    private void drawAnimation(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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

        // 繪製完成區
        drawCompletedArea(g2d, width, height);
    }

    private void drawRestaurantScene(Graphics2D g2d, int width, int height) {
        // 櫃台區域
        g2d.setColor(new Color(139, 69, 19)); // 棕色
        g2d.fillRoundRect(150, 350, 400, 80, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(150, 350, 400, 80, 15, 15);
        g2d.setFont(titleFont);
        g2d.drawString("🏪 廚房工作區", 280, 375);

        // 等待區標示
        g2d.setColor(new Color(0, 102, 204));
        g2d.setFont(titleFont);
        g2d.drawString("👥 顧客等待區", 50, 300);

        // 佇列標示
        g2d.drawString("📋 訂單佇列", 600, 300);

        // 完成區標示
        g2d.drawString("✅ 完成區", 950, 300);
    }

    private void drawWorkers(Graphics2D g2d) {
        synchronized (workers) {
            for (Worker worker : workers) {
                g2d.setFont(emojiFont.deriveFont(Font.PLAIN, 30f));
                g2d.drawString(worker.emoji, worker.x, worker.y);

                g2d.setFont(mainFont);
                g2d.setColor(Color.BLACK);
                g2d.drawString("工作人員" + worker.id, worker.x - 10, worker.y + 40);

                if (worker.currentOrder != null) {
                    g2d.setColor(Color.RED);
                    g2d.drawString("製作中:", worker.x - 15, worker.y + 55);
                    String emoji = getFoodEmoji(worker.currentOrder.foodType);
                    g2d.setFont(emojiFont.deriveFont(Font.PLAIN, 20f));
                    g2d.drawString(emoji, worker.x + 40, worker.y + 55);
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

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(barX, barY, barWidth, barHeight);
        g2d.setColor(new Color(50, 205, 50)); // 淺綠色
        g2d.fillRect(barX, barY, (int) (barWidth * progress), barHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(barX, barY, barWidth, barHeight);
    }

    private void drawCustomers(Graphics2D g2d) {
        synchronized (customers) {
            for (Customer customer : customers) {
                g2d.setFont(emojiFont.deriveFont(Font.PLAIN, 25f));
                g2d.drawString("🙋‍♂️", customer.x, customer.y);

                if (customer.order != null) {
                    String emoji = getFoodEmoji(customer.order.foodType);
                    g2d.setFont(emojiFont.deriveFont(Font.PLAIN, 18f));
                    g2d.setColor(new Color(255, 255, 255, 200));
                    g2d.fillOval(customer.x + 25, customer.y - 30, 35, 25);
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.drawOval(customer.x + 25, customer.y - 30, 35, 25);
                    g2d.drawString(emoji, customer.x + 30, customer.y - 10);
                }

                g2d.setFont(mainFont);
                g2d.setColor(Color.BLUE);
                g2d.drawString("C" + customer.id, customer.x, customer.y + 25);

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
        g2d.setColor(new Color(255, 255, 200, 100));
        g2d.fillRect(580, 320, 300, 150);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(580, 320, 300, 150);

        int startX = 590;
        int startY = 340;
        int col = 0, row = 0;

        synchronized (orderQueue) {
            for (Order order : orderQueue) {
                int x = startX + (col * 70);
                int y = startY + (row * 40);

                g2d.setColor(new Color(255, 255, 204));
                g2d.fillRect(x, y, 60, 30);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, 60, 30);

                g2d.setFont(emojiFont.deriveFont(Font.PLAIN, 16f));
                g2d.drawString(getFoodEmoji(order.foodType), x + 5, y + 20);

                g2d.setFont(new Font("標楷體", Font.PLAIN, 10));
                g2d.drawString("桌" + order.tableNumber, x + 25, y + 15);

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
        g2d.setColor(new Color(200, 255, 200, 100));
        g2d.fillRect(920, 320, 200, 150);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(920, 320, 200, 150);

        int startX = 930;
        int startY = 340;
        int count = 0;

        synchronized (completedOrders) {
            int start = Math.max(0, completedOrders.size() - 6);
            for (int i = start; i < completedOrders.size() && count < 6; i++) {
                Order order = completedOrders.get(i);
                int x = startX + (count % 3) * 60;
                int y = startY + (count / 3) * 40;

                g2d.setColor(new Color(204, 255, 204));
                g2d.fillRect(x, y, 50, 30);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, 50, 30);

                g2d.setFont(emojiFont.deriveFont(Font.PLAIN, 14f));
                g2d.drawString(getFoodEmoji(order.foodType), x + 5, y + 20);

                g2d.setFont(new Font("標楷體", Font.PLAIN, 8));
                g2d.drawString("桌" + order.tableNumber, x + 25, y + 12);
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
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(new Color(220, 220, 220));

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

        // 動畫定時器，更新頻率提高以使動畫更平滑
        animationTimer = new javax.swing.Timer(30, e -> {
            updateCustomerPositions();
            updateGUI();
            animationPanel.repaint();
        });
    }

    private void startSimulation() {
        isRunning = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        customers.clear();
        orderQueue.clear();
        processingOrders.clear();
        completedOrders.clear();
        customerCounter.set(0);
        tableCounter.set(1);

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
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void generateCustomer() {
        int customerId = customerCounter.incrementAndGet();
        int tableNumber = tableCounter.getAndIncrement();
        if (tableNumber > 20) tableCounter.set(1);

        String foodType = FOOD_TYPES[new Random().nextInt(FOOD_TYPES.length)];
        Order order = new Order(customerId, tableNumber, foodType, System.currentTimeMillis());

        Customer customer = new Customer(customerId, 50, 250, order);
        customers.add(customer);
    }

    // 顧客移動方法獨立出來，在 timer 中不斷更新
    private void updateCustomerPositions() {
        synchronized(customers) {
            for(Customer customer : customers) {
                if (customer.status.equals("進場") && customer.x < 150) {
                    customer.x += 2;
                    if (customer.x >= 150) {
                        customer.status = "點餐中";
                        orderQueue.offer(customer.order);
                    }
                } else if (customer.status.equals("點餐中") && customer.x < 600) {
                    customer.x += 3;
                    if (customer.x >= 600) {
                        customer.status = "等待中";
                        customer.y = 280;
                    }
                } else if (customer.status.equals("完成")) {
                    customer.x += 5;
                }
            }
        }
        // 移除離開的顧客
        customers.removeIf(c -> c.x > animationPanel.getWidth());
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
                                .ifPresent(c -> c.status = "完成");
                    }
                    worker.currentOrder = null;
                    worker.startTime = 0;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void updateGUI() {
        SwingUtilities.invokeLater(() -> {
            orderGeneratedLabel.setText(String.valueOf(customerCounter.get()));
            queuedOrdersLabel.setText(String.valueOf(orderQueue.size()));
            processingOrdersLabel.setText(String.valueOf(processingOrders.size()));
            completedOrdersLabel.setText(String.valueOf(completedOrders.size()));

            updateLabelsWithHtml();
        });
    }

    private void updateLabelsWithHtml() {
        // 更新佇列區域
        StringBuilder queueHtml = new StringBuilder("<html>");
        synchronized (orderQueue) {
            for (Order order : orderQueue) {
                String emoji = getFoodEmoji(order.foodType);
                queueHtml.append(String.format("桌%d:%s&nbsp;%s<br>", order.tableNumber, emoji, order.foodType));
            }
        }
        queueHtml.append("</html>");
        queueLabel.setText(queueHtml.toString());

        // 更新製作區域
        StringBuilder processHtml = new StringBuilder("<html>");
        synchronized (processingOrders) {
            for (Order order : processingOrders) {
                String emoji = getFoodEmoji(order.foodType);
                processHtml.append(String.format("桌%d:%s&nbsp;%s(製作中)<br>", order.tableNumber, emoji, order.foodType));
            }
        }
        processHtml.append("</html>");
        processingLabel.setText(processHtml.toString());

        // 更新完成區域
        StringBuilder completeHtml = new StringBuilder("<html>");
        synchronized (completedOrders) {
            int start = Math.max(0, completedOrders.size() - 10);
            for (int i = start; i < completedOrders.size(); i++) {
                Order order = completedOrders.get(i);
                String emoji = getFoodEmoji(order.foodType);
                long duration = order.completedTime - order.orderTime;
                completeHtml.append(String.format("桌%d:%s&nbsp;%s(%.1fs)<br>",
                        order.tableNumber, emoji, order.foodType, duration / 1000.0));
            }
        }
        completeHtml.append("</html>");
        completedLabel.setText(completeHtml.toString());
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