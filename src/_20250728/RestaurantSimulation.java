/*****************************
 * 對應課程: Chapter 5
 * CourseWork: 模擬顧客點餐系統
 *****************************/

package _20250728;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

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
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        initComponents();

        // 啟動顧客產生執行緒
        new Thread(this::generateCustomers).start();

        // 啟動工作人員執行緒
        startWorkers();
    }

    private void initComponents() {
        // 控制面板
        JPanel controlPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
            animationPanel.setWorkerCount(workerCount);
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

        // 主顯示面板 (使用JSplitPane實現可調整大小)
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.7);

        // 左側面板 (文字資訊)
        JPanel leftPanel = new JPanel(new BorderLayout());

        // 訂單資訊面板 (使用TabbedPane)
        JTabbedPane orderTabbedPane = new JTabbedPane();

        // 訂單佇列面板
        queueArea = new JTextArea();
        queueArea.setEditable(false);
        orderTabbedPane.addTab("等待處理的訂單", new JScrollPane(queueArea));

        // 處理中面板
        processingArea = new JTextArea();
        processingArea.setEditable(false);
        orderTabbedPane.addTab("處理中的訂單", new JScrollPane(processingArea));

        // 已完成面板
        completedArea = new JTextArea();
        completedArea.setEditable(false);
        orderTabbedPane.addTab("已完成的訂單", new JScrollPane(completedArea));

        leftPanel.add(orderTabbedPane, BorderLayout.CENTER);

        // 日誌面板
        logArea = new JTextArea();
        logArea.setEditable(false);
        leftPanel.add(new JScrollPane(logArea), BorderLayout.SOUTH);

        mainSplitPane.setLeftComponent(leftPanel);

        // 右側面板 (動畫)
        animationPanel = new AnimationPanel();
        mainSplitPane.setRightComponent(animationPanel);

        add(mainSplitPane, BorderLayout.CENTER);

        // 更新GUI的定時器
        new javax.swing.Timer(100, e -> updateGUI()).start();
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
                animationPanel.addCustomer(order.customerId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processOrders() {
        int workerId = Integer.parseInt(Thread.currentThread().getName().split("-")[1]);

        while (isRunning) {
            try {
                Order order = orderQueue.take();
                processingOrders.add(order);
                animationPanel.assignOrderToWorker(workerId, order.customerId);
                log("工作人員 " + workerId + " 開始處理訂單: " + order);

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
                animationPanel.completeOrder(workerId, order.customerId);
                log("工作人員 " + workerId + " 完成訂單: " + order);

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
            RestaurantSimulation7 simulation = new RestaurantSimulation7();
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
        private final List<Integer> waitingCustomers = new ArrayList<>();
        private final Map<Integer, Integer> workerAssignments = new HashMap<>();
        private final List<Integer> completedCustomers = new ArrayList<>();
        private int workerCount = 2;

        // 動畫位置
        private int customerX = 50;
        private int[] workerX;
        private int tableY = 150;
        private int workerY = 250;
        private int completedX = 800;

        // 圖像資源
        private ImageIcon customerIcon;
        private ImageIcon workerIcon;
        private ImageIcon foodIcon;

        public AnimationPanel() {
            setPreferredSize(new Dimension(400, 400));
            setBackground(new Color(240, 240, 240));
            setBorder(BorderFactory.createTitledBorder("餐廳現場動畫"));

            // 載入圖像資源 (使用簡單繪圖代替實際圖片)
            customerIcon = new ImageIcon(createCustomerImage());
            workerIcon = new ImageIcon(createWorkerImage());
            foodIcon = new ImageIcon(createFoodImage());

            workerX = new int[5]; // 最多5個工作人員
            for (int i = 0; i < workerX.length; i++) {
                workerX[i] = 200 + i * 100;
            }
        }

        public void setWorkerCount(int count) {
            this.workerCount = count;
            repaint();
        }

        public void addCustomer(int customerId) {
            waitingCustomers.add(customerId);
            repaint();
        }

        public void assignOrderToWorker(int workerId, int customerId) {
            waitingCustomers.remove(Integer.valueOf(customerId));
            workerAssignments.put(workerId, customerId);
            repaint();
        }

        public void completeOrder(int workerId, int customerId) {
            workerAssignments.remove(workerId);
            completedCustomers.add(customerId);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // 繪製餐廳背景
            drawRestaurantBackground(g);

            // 繪製等待區顧客
            for (int i = 0; i < waitingCustomers.size(); i++) {
                if (i < 5) { // 最多顯示5個
                    int x = customerX + i * 60;
                    customerIcon.paintIcon(this, g, x, tableY - 30);
                    g.setColor(Color.BLACK);
                    g.drawString("#" + waitingCustomers.get(i), x + 15, tableY - 35);
                }
            }

            // 繪製工作人員和他們正在處理的訂單
            for (int i = 0; i < workerCount; i++) {
                workerIcon.paintIcon(this, g, workerX[i] - 25, workerY - 25);
                g.setColor(Color.BLACK);
                g.drawString("員工" + (i+1), workerX[i] - 10, workerY - 30);

                if (workerAssignments.containsKey(i)) {
                    int customerId = workerAssignments.get(i);
                    foodIcon.paintIcon(this, g, workerX[i] - 15, workerY - 50);
                    g.setColor(Color.BLACK);
                    g.drawString("#" + customerId, workerX[i] - 5, workerY - 55);
                }
            }

            // 繪製已完成訂單
            for (int i = 0; i < completedCustomers.size(); i++) {
                if (i < 5) { // 最多顯示5個
                    int x = completedX;
                    int y = tableY + (i % 3) * 40;
                    foodIcon.paintIcon(this, g, x, y);
                    g.setColor(Color.BLACK);
                    g.drawString("#" + completedCustomers.get(i), x + 10, y + 15);
                }
            }
        }

        private void drawRestaurantBackground(Graphics g) {
            // 繪製桌子
            g.setColor(new Color(139, 69, 19)); // 棕色桌子
            g.fillRect(customerX - 20, tableY, 400, 20);

            // 繪製廚房區域
            g.setColor(new Color(200, 200, 200));
            g.fillRect(150, workerY - 50, 500, 80);
            g.setColor(Color.BLACK);
            g.drawString("廚房", 350, workerY - 60);

            // 繪製完成區
            g.setColor(new Color(200, 255, 200));
            g.fillRect(completedX - 20, tableY, 100, 120);
            g.setColor(Color.BLACK);
            g.drawString("完成區", completedX - 10, tableY - 10);
        }

        private Image createCustomerImage() {
            BufferedImage img = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setColor(new Color(100, 150, 255));
            g2d.fillOval(5, 5, 20, 20); // 頭
            g2d.setColor(Color.BLACK);
            g2d.drawOval(5, 5, 20, 20);
            g2d.dispose();
            return img;
        }

        private Image createWorkerImage() {
            BufferedImage img = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setColor(new Color(255, 150, 100));
            g2d.fillOval(5, 5, 20, 20); // 頭
            g2d.setColor(Color.BLACK);
            g2d.drawOval(5, 5, 20, 20);
            g2d.dispose();
            return img;
        }

        private Image createFoodImage() {
            BufferedImage img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setColor(new Color(255, 200, 100));
            g2d.fillRect(5, 5, 15, 15); // 食物
            g2d.setColor(Color.BLACK);
            g2d.drawRect(5, 5, 15, 15);
            g2d.dispose();
            return img;
        }
    }
}