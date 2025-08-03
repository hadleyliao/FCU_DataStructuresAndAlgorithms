/********************************
 * 課程名稱: 資料結構與演算法
 * 對應課程: Chapter 6
 * CourseWork2: Heap Application
 ********************************/

package _20250729;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private int animationDelay = 10; // 動畫延遲(毫秒)

    public ProducerGUI() {
        setTitle("生產者範例");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 600);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Buffer 大小: "));
        bufferSizeField = new JTextField("5", 5);
        topPanel.add(bufferSizeField);
        startButton = new JButton("開始生產");
        topPanel.add(startButton);
        // 新增速度調整拉桿，放在 topPanel 左側
        speedSlider = new JSlider(10, 200, 30); // 10~200ms, 預設30ms
        speedSlider.setMajorTickSpacing(30);
        speedSlider.setMinorTickSpacing(10);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setBorder(BorderFactory.createTitledBorder("動畫速度 (數值越小越快)"));
        speedSlider.addChangeListener(e -> {
            animationDelay = speedSlider.getValue();
            if (animationPanel != null) {
                animationPanel.updateAnimationDelay(animationDelay);
            }
        });
        topPanel.add(speedSlider, 0); // 插入最左邊
        add(topPanel, BorderLayout.NORTH);

        // 將 bufferPanel 和 logArea 放入同一個 JPanel，並用 GridLayout 讓大小一致
        JPanel upperPanel = new JPanel(new GridLayout(1, 2));
        bufferPanel = new JPanel();
        bufferPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        bufferPanel.setBorder(BorderFactory.createTitledBorder("Buffer 內容 (數據顯示)"));
        upperPanel.add(bufferPanel);

        logArea = new JTextArea();
        logArea.setEditable(false);
        upperPanel.add(new JScrollPane(logArea));

        add(upperPanel, BorderLayout.CENTER);

        animationPanel = new FlowAnimationPanel();
        animationPanel.setPreferredSize(new Dimension(700, 300)); // 動畫區塊
        add(animationPanel, BorderLayout.SOUTH);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int n;
                try {
                    n = Integer.parseInt(bufferSizeField.getText());
                    if (n <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ProducerGUI.this, "請輸入正整數作為 buffer 大小");
                    return;
                }
                buffer = new ArrayBlockingQueue<>(n);
                logArea.setText("");
                if (producerThread != null && producerThread.isAlive()) {
                    producerThread.interrupt();
                }
                if (consumerThread != null && consumerThread.isAlive()) {
                    consumerThread.interrupt();
                }
                producerThread = new ProducerThread();
                consumerThread = new ConsumerThread();
                producerThread.start();
                consumerThread.start();
                startButton.setEnabled(false);
                bufferSizeField.setEnabled(false);
            }
        });
    }

    private void updateBufferPanel(String highlightItem, boolean isProduced) {
        SwingUtilities.invokeLater(() -> {
            bufferPanel.removeAll();
            for (String item : buffer) {
                JLabel label = new JLabel(item);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                if (item.equals(highlightItem)) {
                    if (isProduced) {
                        label.setBackground(Color.YELLOW);
                    } else {
                        label.setBackground(Color.PINK);
                    }
                } else {
                    label.setBackground(Color.WHITE);
                }
                bufferPanel.add(label);
            }
            bufferPanel.revalidate();
            bufferPanel.repaint();
        });
    }

    private void animateProduce(String item) {
        animationPanel.animateProduce(item);
    }
    private void animateConsume(String item) {
        animationPanel.animateConsume(item);
    }

    private class ProducerThread extends Thread {
        private final Random rand = new Random();
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        @Override
        public void run() {
            while (!isInterrupted()) {
                String item = sdf.format(new Date()) + " 編號:" + (rand.nextInt(900) + 100);
                try {
                    buffer.put(item); // 滿時會阻塞
                    SwingUtilities.invokeLater(() -> logArea.append("生產: " + item + "\n"));
                    updateBufferPanel(item, true);
                    animateProduce(item);
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    break;
                }
            }
            SwingUtilities.invokeLater(() -> {
                startButton.setEnabled(true);
                bufferSizeField.setEnabled(true);
            });
        }
    }

    private class ConsumerThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    String minItem = null;
                    int minNum = Integer.MAX_VALUE;
                    // 找出最小編號物品
                    synchronized (buffer) {
                        for (String item : buffer) {
                            int idx = item.lastIndexOf(":");
                            if (idx != -1) {
                                int num = Integer.parseInt(item.substring(idx + 1));
                                if (num < minNum) {
                                    minNum = num;
                                    minItem = item;
                                }
                            }
                        }
                        if (minItem != null) {
                            buffer.remove(minItem);
                        }
                    }
                    if (minItem != null) {
                        String finalMinItem = minItem;
                        SwingUtilities.invokeLater(() -> logArea.append("消費: " + finalMinItem + "\n"));
                        updateBufferPanel(finalMinItem, false);
                        animateConsume(finalMinItem);
                    }
                    Thread.sleep(300); // 消費速度可調整
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProducerGUI().setVisible(true));
    }

    // 動畫面板類別
    class FlowAnimationPanel extends JPanel {
        private String animatingItem = null;
        private int animX = 0;
        private int animY = 60;
        private int targetX = 0;
        private boolean isProducing = true;
        private Timer timer;
        private String animatingLabel = null;
        private float alpha = 1.0f; // 透明度
        private int scale = 30; // 物品大小

        public FlowAnimationPanel() {
            setBackground(Color.WHITE);
        }

        public void animateProduce(String item) {
            animatingItem = item;
            animatingLabel = item.substring(item.length() - 3); // 顯示編號末3碼
            animX = 40;
            animY = 60;
            targetX = 220;
            isProducing = true;
            alpha = 0.2f;
            scale = 10;
            startAnimation();
        }
        public void animateConsume(String item) {
            animatingItem = item;
            animatingLabel = item.substring(item.length() - 3);
            animX = 220;
            animY = 60;
            targetX = 400;
            isProducing = false;
            alpha = 0.2f;
            scale = 10;
            startAnimation();
        }
        public void updateAnimationDelay(int delay) {
            if (timer != null && timer.isRunning()) {
                timer.setDelay(delay);
            }
        }
        private void startAnimation() {
            if (timer != null && timer.isRunning()) timer.stop();
            timer = new Timer(animationDelay, e -> {
                if (animX < targetX) {
                    animX += 5;
                    if (alpha < 1.0f) alpha += 0.08f;
                    if (scale < 30) scale += 2;
                    repaint();
                } else {
                    ((Timer)e.getSource()).stop();
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
            // 畫三個區塊
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillRect(10, 40, 60, 40); // 生產者
            g2.fillRect(190, 40, 60, 40); // buffer
            g2.fillRect(370, 40, 60, 40); // 消費者
            g2.setColor(Color.BLACK);
            g2.drawRect(10, 40, 60, 40);
            g2.drawRect(190, 40, 60, 40);
            g2.drawRect(370, 40, 60, 40);
            g2.drawString("生產者", 20, 35);
            g2.drawString("Buffer", 200, 35);
            g2.drawString("消費者", 380, 35);
            // 畫箭頭
            g2.setStroke(new BasicStroke(2));
            g2.setColor(new Color(80, 160, 255));
            g2.drawLine(70, 60, 190, 60);
            g2.setColor(new Color(255, 120, 120));
            g2.drawLine(250, 60, 370, 60);
            g2.setColor(Color.GRAY);
            g2.drawLine(185, 60, 190, 60);
            g2.drawLine(250, 60, 255, 60);
            // 畫動畫物品
            if (animatingItem != null) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(alpha, 1.0f)));
                GradientPaint gp = new GradientPaint(animX, animY, isProducing ? Color.YELLOW : Color.PINK, animX+scale, animY+scale, Color.WHITE);
                g2.setPaint(gp);
                g2.fillOval(animX, animY, scale, scale);
                g2.setColor(Color.DARK_GRAY);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(animX, animY, scale, scale);
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                g2.setColor(Color.BLACK);
                if (animatingLabel != null)
                    g2.drawString(animatingLabel, animX + scale/4, animY + scale/2 + 4);
            }
        }
    }
}
