/********************************
 * 對應課程: Chapter 6
 * CourseWork2: Heap Application
 ********************************/
package _20250729;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

public class ProducerGUI extends JFrame {
    private JTextField bufferSizeField;
    private JButton startButton;
    private JTextArea logArea;
    private JPanel bufferPanel;
    private ArrayBlockingQueue<String> buffer;
    private ProducerThread producerThread;
    private ConsumerThread consumerThread;
    private FlowAnimationPanel animationPanel;
    private JSlider speedSlider;
    private JLabel speedLabel;
    private int animationDelay = 60;

    // 5-speed levels (極簡文字)
    private static final int[] SPEED_LEVELS = {180, 100, 60, 30, 10};
    private static final String[] SPEED_NAMES = {"很慢", "慢", "中", "快", "很快"};

    public ProducerGUI() {
        setTitle("Producer-Consumer Visualizer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650); // 直接設定視窗大小
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(Color.WHITE);
        setUIFont(new javax.swing.plaf.FontUIResource("SansSerif", Font.PLAIN, 15));
        initializeComponents();
        setupEventListeners();
    }

    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(key, f);
        }
    }

    private void initializeComponents() {
        // Top controls
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 8));
        controlsPanel.setBackground(Color.WHITE);
        controlsPanel.add(new JLabel("緩衝區大小") {{ setFont(new Font("SansSerif", Font.BOLD, 18)); }});
        bufferSizeField = new JTextField("5", 4);
        bufferSizeField.setFont(new Font("SansSerif", Font.BOLD, 14));
        controlsPanel.add(bufferSizeField);
        startButton = new JButton("開始");
        startButton.setFocusPainted(false);
        startButton.setFont(new Font("SansSerif", Font.BOLD, 22));
        startButton.setBackground(new Color(33, 150, 243)); // 藍色底
        startButton.setForeground(Color.WHITE);
        startButton.setPreferredSize(new Dimension(140, 54));
        startButton.setBorder(BorderFactory.createEmptyBorder());
        controlsPanel.add(startButton);
        JButton clearButton = new JButton("重設");
        clearButton.setFocusPainted(false);
        clearButton.setFont(new Font("SansSerif", Font.BOLD, 22));
        clearButton.setBackground(new Color(76, 175, 80)); // 綠色底
        clearButton.setForeground(Color.WHITE);
        clearButton.setPreferredSize(new Dimension(140, 54));
        clearButton.setBorder(BorderFactory.createEmptyBorder());
        controlsPanel.add(clearButton);

        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 8));
        speedPanel.setBackground(Color.WHITE);
        speedSlider = new JSlider(0, 4, 2);
        speedSlider.setFocusable(false);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setBackground(Color.WHITE);
        java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
        String[] speedNamesTW = {"很慢", "慢", "中", "快", "很快"};
        for (int i = 0; i < speedNamesTW.length; i++) {
            labelTable.put(i, new JLabel(speedNamesTW[i]));
        }
        speedSlider.setLabelTable(labelTable);
        speedPanel.add(new JLabel("速度") {{ setFont(new Font("SansSerif", Font.BOLD, 18)); }});
        speedPanel.add(speedSlider);
        speedLabel = new JLabel("中 (" + SPEED_LEVELS[2] + "毫秒)");
        speedLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        speedPanel.add(speedLabel);

        JPanel topRowPanel = new JPanel(new GridLayout(1, 2));
        topRowPanel.setBackground(Color.WHITE);
        topRowPanel.add(controlsPanel);
        topRowPanel.add(speedPanel);
        topPanel.add(topRowPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Center panels
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);

        // 標題 row
        JPanel labelPanel = new JPanel(new GridLayout(1, 2, 32, 0)); // 增加標題間距
        labelPanel.setBackground(Color.WHITE);
        JLabel bufferTitleLabel = new JLabel("緩衝區數據", SwingConstants.CENTER);
        bufferTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        JLabel logTitleLabel = new JLabel("日誌紀錄", SwingConstants.CENTER);
        logTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        labelPanel.add(bufferTitleLabel);
        labelPanel.add(logTitleLabel);
        centerPanel.add(Box.createVerticalStrut(16)); // 增加上下間距
        centerPanel.add(labelPanel);
        centerPanel.add(Box.createVerticalStrut(8));

        // 內容 row
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 32, 0)); // 增加內容間距
        contentPanel.setBackground(Color.WHITE);
        bufferPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 16)); // 增加格子間距
        bufferPanel.setBackground(Color.WHITE);
        JScrollPane bufferScroll = new JScrollPane(bufferPanel);
        bufferScroll.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16)); // 緩衝區左右留白
        contentPanel.add(bufferScroll);
        logArea = new JTextArea();
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBackground(Color.WHITE);
        logArea.setForeground(Color.DARK_GRAY);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setText("Set buffer size and Start...\n");
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16)); // 日誌左右留白
        contentPanel.add(logScroll);
        centerPanel.add(contentPanel);
        add(centerPanel, BorderLayout.CENTER);

        // Animation panel - 增加高度以容纳边框
        animationPanel = new FlowAnimationPanel();
        animationPanel.setPreferredSize(new Dimension(850, 220));
        animationPanel.setBackground(Color.WHITE);
        add(animationPanel, BorderLayout.SOUTH);

        clearButton.addActionListener(e -> {
            if (producerThread != null && producerThread.isAlive()) producerThread.interrupt();
            if (consumerThread != null && consumerThread.isAlive()) consumerThread.interrupt();
            buffer = null;
            bufferPanel.removeAll();
            bufferPanel.revalidate();
            bufferPanel.repaint();
            logArea.setText("Reset\n");
            bufferSizeField.setEnabled(true);
            startButton.setEnabled(true);
            startButton.setText("開始");
            if (animationPanel != null) animationPanel.updateBufferSnapshot(); // 保證重設時動畫緩衝區也清空
        });
    }

    private void setupEventListeners() {
        startButton.addActionListener(e -> {
            if (startButton.getText().equals("開始")) startSimulation();
            else stopSimulation();
        });
        speedSlider.addChangeListener(e -> {
            int level = speedSlider.getValue();
            animationDelay = SPEED_LEVELS[level];
            speedLabel.setText(SPEED_NAMES[level] + " (" + animationDelay + "ms)");
            if (animationPanel != null) animationPanel.updateAnimationDelay(animationDelay);
            if (producerThread != null) producerThread.updateSpeed(level);
            if (consumerThread != null) consumerThread.updateSpeed(level);
        });
        bufferSizeField.addActionListener(e -> startButton.doClick());
    }

    private void startSimulation() {
        int n;
        try {
            n = Integer.parseInt(bufferSizeField.getText());
            if (n <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a positive integer for Buffer size!", "Error", JOptionPane.ERROR_MESSAGE);
            bufferSizeField.setBackground(new Color(255, 220, 220));
            bufferSizeField.setForeground(Color.RED);
            return;
        }
        bufferSizeField.setBackground(Color.WHITE);
        bufferSizeField.setForeground(Color.BLACK);
        buffer = new ArrayBlockingQueue<>(n);
        logArea.setText("Simulation started! Buffer size: " + n + "\n");

        if (producerThread != null && producerThread.isAlive()) producerThread.interrupt();
        if (consumerThread != null && consumerThread.isAlive()) consumerThread.interrupt();

        int speedLevel = speedSlider.getValue();
        producerThread = new ProducerThread(speedLevel);
        consumerThread = new ConsumerThread(speedLevel);
        producerThread.start();
        consumerThread.start();

        startButton.setText("停止");
        startButton.setBackground(new Color(244, 67, 54));
        bufferSizeField.setEnabled(false);
        speedSlider.setEnabled(true);
    }

    private void stopSimulation() {
        if (producerThread != null && producerThread.isAlive()) producerThread.interrupt();
        if (consumerThread != null && consumerThread.isAlive()) consumerThread.interrupt();
        startButton.setText("開始");
        startButton.setBackground(new Color(72, 201, 176));
        bufferSizeField.setEnabled(true);
        logArea.append("Simulation stopped\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // 动畫面板 (明确区块边框版本)
    class FlowAnimationPanel extends JPanel {
        private String animatingItem = null;
        private int animX = 0;
        private int animY = 50;
        private int targetX = 0;
        private boolean isProducing = true;
        private Timer timer;
        private String animatingLabel = null;
        private float alpha = 1.0f;
        private int scale = 20;
        private java.util.List<String> bufferSnapshot = new java.util.ArrayList<>();
        private int animStep = 3;
        private double alphaStep = 0.05;
        private int scaleStep = 2;

        public FlowAnimationPanel() {
            setBackground(Color.WHITE);
        }

        public void animateProduce(String item) {
            animatingItem = item;
            animatingLabel = item.substring(item.length() - 3);
            animX = 100;
            animY = 70;
            targetX = 320;
            isProducing = true;
            alpha = 0.3f;
            scale = 11;
            startAnimation();
        }

        public void animateConsume(String item) {
            animatingItem = item;
            animatingLabel = item.substring(item.length() - 3);
            animX = 380;
            animY = 70;
            targetX = 630;
            isProducing = false;
            alpha = 0.3f;
            scale = 11;
            startAnimation();
        }

        public void updateAnimationDelay(int delay) {
            animationDelay = delay;
            if (delay <= 15) { animStep = 8; alphaStep = 0.15; scaleStep = 4; }
            else if (delay <= 40) { animStep = 5; alphaStep = 0.1; scaleStep = 3; }
            else if (delay <= 80) { animStep = 3; alphaStep = 0.05; scaleStep = 2; }
            else { animStep = 1; alphaStep = 0.02; scaleStep = 1; }
            if (timer != null && timer.isRunning())
                timer.setDelay(Math.max(10, animationDelay / 2));
        }

        public void updateBufferSnapshot() {
            bufferSnapshot.clear();
            if (buffer != null) bufferSnapshot.addAll(buffer);
            repaint();
        }

        private void startAnimation() {
            if (timer != null && timer.isRunning()) timer.stop();
            timer = new Timer(Math.max(10, animationDelay / 2), e -> {
                if (animX < targetX) {
                    animX += animStep;
                    if (alpha < 1.0f) alpha += alphaStep;
                    if (scale < 20) scale += scaleStep;
                    repaint();
                } else {
                    ((Timer) e.getSource()).stop();
                    animatingItem = null;
                    repaint();
                }
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 依據視窗寬度自動調整區塊位置
            int panelWidth = getWidth();
            int blockWidth = 260;
            int blockHeight = 140;
            int blockY = 30;
            int gap = (panelWidth - blockWidth * 3) / 4;
            int producerX = gap;
            int bufferX = gap * 2 + blockWidth;
            int consumerX = gap * 3 + blockWidth * 2;

            // 繪製區塊邊框和背景
            // Producer 區塊
            g2.setColor(new Color(48, 196, 113, 30)); // 淡綠色背景
            g2.fillRoundRect(producerX, blockY, blockWidth, blockHeight, 12, 12);
            g2.setColor(new Color(48, 196, 113));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(producerX, blockY, blockWidth, blockHeight, 12, 12);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString("生產者 (Producer)", producerX + 20, blockY + 35);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.drawString("產生資料並放入緩衝區", producerX + 20, blockY + 60);

            // Buffer 區塊
            g2.setColor(new Color(0, 122, 255, 30)); // 淡藍色背景
            g2.fillRoundRect(bufferX, blockY, blockWidth, blockHeight, 12, 12);
            g2.setColor(new Color(0, 122, 255));
            g2.drawRoundRect(bufferX, blockY, blockWidth, blockHeight, 12, 12);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString("緩衝區 (Buffer)", bufferX + 20, blockY + 35);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.drawString("暫存資料，容量有限", bufferX + 20, blockY + 60);

            // Consumer 區塊
            g2.setColor(new Color(244, 67, 54, 30)); // 淡紅色背景
            g2.fillRoundRect(consumerX, blockY, blockWidth, blockHeight, 12, 12);
            g2.setColor(new Color(244, 67, 54));
            g2.drawRoundRect(consumerX, blockY, blockWidth, blockHeight, 12, 12);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString("消費者 (Consumer)", consumerX + 20, blockY + 35);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.drawString("取出資料並消費", consumerX + 20, blockY + 60);

            // 繪製功能描述
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.setColor(new Color(80, 80, 80));

            // 繪製箭頭 (在區塊之間)
            g2.setStroke(new BasicStroke(3));
            g2.setColor(new Color(100, 100, 100));

            // Producer -> Buffer 箭頭
            int arrow1X = producerX + blockWidth + 5;
            int arrow1EndX = bufferX - 5;
            int arrowY = blockY + blockHeight / 2;
            g2.drawLine(arrow1X, arrowY, arrow1EndX, arrowY);
            g2.fillPolygon(new int[]{arrow1EndX, arrow1EndX - 8, arrow1EndX - 8},
                    new int[]{arrowY, arrowY - 5, arrowY + 5}, 3);

            // Buffer -> Consumer 箭頭
            int arrow2X = bufferX + blockWidth + 5;
            int arrow2EndX = consumerX - 5;
            g2.drawLine(arrow2X, arrowY, arrow2EndX, arrowY);
            g2.fillPolygon(new int[]{arrow2EndX, arrow2EndX - 8, arrow2EndX - 8},
                    new int[]{arrowY, arrowY - 5, arrowY + 5}, 3);

            // 繪製動畫項目 (圓形)
            if (animatingItem != null) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(alpha, 1.0f)));
                g2.setColor(isProducing ? new Color(48,196,113,180) : new Color(244,67,54,180));
                g2.fillOval(animX, animY, scale + 22, scale + 22);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Consolas", Font.BOLD, 12));
                g2.drawString(animatingLabel, animX + 8, animY + 22);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }

            // 繪製 buffer 內容 (在 Buffer 區塊內)
            int bx = bufferX + 20, by = blockY + 85, bw = 45, bh = 28;
            int maxItemsPerRow = 5;
            int gapBuffer = 8; // 避免與上方 gap 變數重名
            for (int i = 0; i < bufferSnapshot.size(); i++) {
                String item = bufferSnapshot.get(i);
                String label = item.length() > 3 ? item.substring(item.length()-3) : item;
                int row = i / maxItemsPerRow;
                int col = i % maxItemsPerRow;
                int x = bx + col * (bw + gapBuffer);
                int y = by + row * (bh + gapBuffer);

                // 直接繪製，不再判斷 x + bw 是否超出區塊
                g2.setColor(new Color(240, 248, 255));
                g2.fillRoundRect(x, y, bw, bh, 4, 4);
                g2.setColor(new Color(0, 122, 255));
                g2.drawRoundRect(x, y, bw, bh, 4, 4);
                g2.setFont(new Font("Consolas", Font.BOLD, 9));
                g2.drawString(label, x + 8, y + 14);
            }
        }
    }

    private void updateBufferPanel(String highlightItem, boolean isProduced) {
        SwingUtilities.invokeLater(() -> {
            bufferPanel.removeAll();
            bufferPanel.setLayout(new GridLayout(0, 3, 6, 8));
            for (String item : buffer) {
                JLabel label = new JLabel(item, SwingConstants.CENTER);
                label.setOpaque(true);
                label.setFont(new Font("Consolas", Font.PLAIN, 12));
                label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                if (item.equals(highlightItem)) {
                    label.setBackground(isProduced ? new Color(220,255,220) : new Color(255,220,220));
                    label.setForeground(isProduced ? new Color(48,196,113) : new Color(244,67,54));
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(Color.DARK_GRAY);
                }
                bufferPanel.add(label);
            }
            bufferPanel.revalidate();
            bufferPanel.repaint();
            if (animationPanel != null) animationPanel.updateBufferSnapshot(); // 保證每次更新都同步動畫緩衝區
        });
    }

    private void animateProduce(String item) { animationPanel.animateProduce(item); }
    private void animateConsume(String item) { animationPanel.animateConsume(item); }

    private class ProducerThread extends Thread {
        private final Random rand = new Random();
        private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        private volatile int speedLevel;
        private volatile long produceDelay = 420;

        public ProducerThread(int speedLevel) {
            this.speedLevel = speedLevel;
            updateSpeed(speedLevel);
        }
        public void updateSpeed(int level) {
            this.speedLevel = level;
            this.produceDelay = 90 + (7 - level) * 90;
        }
        @Override public void run() {
            while (!isInterrupted()) {
                String item = sdf.format(new Date()) + " #" + String.format("%03d", rand.nextInt(900) + 100);
                try {
                    buffer.put(item);
                    SwingUtilities.invokeLater(() -> {
                        logArea.append("Produced: " + item + "\n");
                        logArea.setCaretPosition(logArea.getDocument().getLength());
                    });
                    updateBufferPanel(item, true);
                    animateProduce(item);
                    Thread.sleep(produceDelay);
                } catch (InterruptedException e) { break; }
            }
            SwingUtilities.invokeLater(() -> {
                startButton.setText("開始");
                startButton.setBackground(new Color(72, 201, 176));
                bufferSizeField.setEnabled(true);
            });
        }
    }

    private class ConsumerThread extends Thread {
        private volatile int speedLevel;
        private volatile long consumeDelay = 520;

        public ConsumerThread(int speedLevel) {
            this.speedLevel = speedLevel;
            updateSpeed(speedLevel);
        }
        public void updateSpeed(int level) {
            this.speedLevel = level;
            this.consumeDelay = 120 + (7 - level) * 120;
        }
        @Override public void run() {
            while (!isInterrupted()) {
                try {
                    String minItem = null;
                    int minNum = Integer.MAX_VALUE;
                    synchronized (buffer) {
                        for (String item : buffer) {
                            int idx = item.lastIndexOf("#");
                            if (idx != -1) {
                                try {
                                    int num = Integer.parseInt(item.substring(idx + 1));
                                    if (num < minNum) { minNum = num; minItem = item; }
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                        if (minItem != null) buffer.remove(minItem);
                    }
                    if (minItem != null) {
                        final String consumed = minItem;
                        SwingUtilities.invokeLater(() -> {
                            logArea.append("Consumed: " + consumed + "\n");
                            logArea.setCaretPosition(logArea.getDocument().getLength());
                        });
                        updateBufferPanel(consumed, false);
                        animateConsume(consumed);
                    }
                    Thread.sleep(consumeDelay);
                } catch (InterruptedException e) { break; }
            }
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { e.printStackTrace(); }
        SwingUtilities.invokeLater(() -> new ProducerGUI().setVisible(true));
    }
}