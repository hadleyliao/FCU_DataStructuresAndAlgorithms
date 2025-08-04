/***************************************
 * 對應課程: Chapter 5
 * CourseWork: 模擬顧客點餐系統(儀錶板數據)
 ***************************************/

package _20250728;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RestaurantSimulation4 extends JFrame {
    // 系統組件
    private final BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();
    private final List<Order> processingOrders = Collections.synchronizedList(new ArrayList<>());
    private final List<Order> completedOrders = Collections.synchronizedList(new ArrayList<>());

    // GUI 組件
    private JLabel orderGeneratedLabel, queuedOrdersLabel, processingOrdersLabel, completedOrdersLabel;
    private JTextArea queueArea, processingArea, completedArea;
    private JPanel animationPanel;
    private JSlider speedSlider;
    private JButton startButton, stopButton;

    // 系統狀態
    private volatile boolean isRunning = false;
    private final AtomicInteger customerCounter = new AtomicInteger(0);
    private final AtomicInteger tableCounter = new AtomicInteger(1);
    private javax.swing.Timer animationTimer;
    private final List<Customer> customers = Collections.synchronizedList(new ArrayList<>());

    // 餐點類型和製作時間
    private final String[] FOOD_TYPES = {"漢堡", "薯條", "飲料", "炸雞", "沙拉"};
    private final Map<String, Integer> COOKING_TIME = Map.of(
            "漢堡", 2000,
            "薯條", 1000,
            "飲料", 500,
            "炸雞", 3000,
            "沙拉", 1500
    );

    public RestaurantSimulation4() {
        initializeGUI();
        setupEventHandlers();
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

        setSize(1200, 800);
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
        panel.setBackground(Color.WHITE);
        panel.setBorder(new TitledBorder("動畫模擬區域"));
        return panel;
    }

    private void drawAnimation(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = animationPanel.getWidth();
        int height = animationPanel.getHeight();

        // 繪製流程箭頭
        drawProcessFlow(g2d, width, height);

        // 繪製顧客
        synchronized (customers) {
            for (Customer customer : customers) {
                drawCustomer(g2d, customer);
            }
        }

        // 繪製佇列視覺化
        drawQueueVisualization(g2d, width, height);
    }

    private void drawProcessFlow(Graphics2D g2d, int width, int height) {
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(Color.BLUE);

        int y = height / 2;
        int[] xPoints = {width/6, width/3, width/2, 2*width/3, 5*width/6};

        // 繪製箭頭
        for (int i = 0; i < xPoints.length - 1; i++) {
            g2d.drawLine(xPoints[i], y, xPoints[i+1], y);
            // 箭頭頭部
            g2d.drawLine(xPoints[i+1] - 10, y - 5, xPoints[i+1], y);
            g2d.drawLine(xPoints[i+1] - 10, y + 5, xPoints[i+1], y);
        }

        // 標籤
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("標楷體", Font.BOLD, 12));
        String[] labels = {"顧客進入", "產生訂單", "加入佇列", "製作餐點", "完成送達"};
        for (int i = 0; i < labels.length; i++) {
            g2d.drawString(labels[i], xPoints[i] - 30, y - 20);
        }
    }

    private void drawCustomer(Graphics2D g2d, Customer customer) {
        g2d.setColor(customer.color);
        g2d.fillOval(customer.x - 15, customer.y - 15, 30, 30);
        g2d.setColor(Color.BLACK);
        g2d.drawString("C" + customer.id, customer.x - 10, customer.y + 5);
    }

    private void drawQueueVisualization(Graphics2D g2d, int width, int height) {
        // 佇列視覺化區域
        int queueY = height - 100;
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(50, queueY, width - 100, 80);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(50, queueY, width - 100, 80);
        g2d.drawString("訂單佇列", 60, queueY + 15);

        // 繪製佇列中的訂單
        int x = 70;
        synchronized (orderQueue) {
            for (Order order : orderQueue) {
                g2d.setColor(Color.YELLOW);
                g2d.fillRect(x, queueY + 20, 40, 40);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, queueY + 20, 40, 40);
                g2d.drawString("T" + order.tableNumber, x + 5, queueY + 35);
                g2d.drawString(order.foodType.substring(0, 1), x + 5, queueY + 50);
                x += 50;
                if (x > width - 150) break;
            }
        }
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
        animationTimer = new javax.swing.Timer(50, e -> {
            updateCustomerPositions();
            updateGUI();
            animationPanel.repaint();
        });
    }

    private void startSimulation() {
        isRunning = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        // 啟動動畫
        animationTimer.start();

        // 啟動顧客生成執行緒
        new Thread(this::customerGeneratorThread).start();

        // 啟動工作人員執行緒
        for (int i = 0; i < 3; i++) {
            new Thread(() -> workerThread("工作人員" + Thread.currentThread().getId())).start();
        }
    }

    private void stopSimulation() {
        isRunning = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        animationTimer.stop();
    }

    private void customerGeneratorThread() {
        Random random = new Random();
        while (isRunning) {
            try {
                // 根據滑桿調整顧客到達間隔
                int interval = speedSlider.getValue();
                Thread.sleep(interval + random.nextInt(1000));

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
        Customer customer = new Customer(customerId, 50, animationPanel.getHeight() / 2);
        customers.add(customer);

        // 創建訂單
        String foodType = FOOD_TYPES[new Random().nextInt(FOOD_TYPES.length)];
        int tableNumber = tableCounter.getAndIncrement();
        if (tableNumber > 20) tableCounter.set(1);

        Order order = new Order(customerId, tableNumber, foodType, System.currentTimeMillis());
        orderQueue.offer(order);
    }

    private void workerThread(String workerName) {
        while (isRunning) {
            try {
                Order order = orderQueue.take();
                processingOrders.add(order);

                // 模擬製作時間
                int cookingTime = COOKING_TIME.get(order.foodType);
                Thread.sleep(cookingTime);

                processingOrders.remove(order);
                order.completedTime = System.currentTimeMillis();
                completedOrders.add(order);

                // 移除對應的顧客動畫
                customers.removeIf(c -> c.id == order.customerId);

            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void updateCustomerPositions() {
        synchronized (customers) {
            for (Customer customer : customers) {
                if (customer.x < animationPanel.getWidth() / 3) {
                    customer.x += 2;
                }
            }
        }
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
                queueText.append(String.format("桌%d:%s\n", order.tableNumber, order.foodType));
            }
        }
        queueArea.setText(queueText.toString());

        // 更新製作區域
        StringBuilder processText = new StringBuilder();
        synchronized (processingOrders) {
            for (Order order : processingOrders) {
                processText.append(String.format("桌%d:%s(製作中)\n", order.tableNumber, order.foodType));
            }
        }
        processingArea.setText(processText.toString());

        // 更新完成區域（顯示最近10筆）
        StringBuilder completeText = new StringBuilder();
        synchronized (completedOrders) {
            int start = Math.max(0, completedOrders.size() - 10);
            for (int i = start; i < completedOrders.size(); i++) {
                Order order = completedOrders.get(i);
                long duration = order.completedTime - order.orderTime;
                completeText.append(String.format("桌%d:%s(%.1fs)\n",
                        order.tableNumber, order.foodType, duration / 1000.0));
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
        Color color;

        Customer(int id, int x, int y) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.color = new Color(
                    (int)(Math.random() * 255),
                    (int)(Math.random() * 255),
                    (int)(Math.random() * 255)
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            new RestaurantSimulation4().setVisible(true);
        });
    }
}