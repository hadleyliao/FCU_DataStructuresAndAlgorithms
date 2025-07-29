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

    public ProducerGUI() {
        setTitle("生產者範例");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Buffer 大小: "));
        bufferSizeField = new JTextField("5", 5);
        topPanel.add(bufferSizeField);
        startButton = new JButton("開始生產");
        topPanel.add(startButton);
        add(topPanel, BorderLayout.NORTH);

        bufferPanel = new JPanel();
        bufferPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        bufferPanel.setBorder(BorderFactory.createTitledBorder("Buffer 內容 (數據顯示)"));
        add(bufferPanel, BorderLayout.CENTER);

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.EAST);

        animationPanel = new FlowAnimationPanel();
        animationPanel.setPreferredSize(new Dimension(700, 200)); // 放大動畫區域
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

        public FlowAnimationPanel() {
            setBackground(Color.WHITE);
        }

        public void animateProduce(String item) {
            animatingItem = item;
            animX = 40; // 生產者區塊右側
            animY = 60;
            targetX = 220; // buffer 區塊左側
            isProducing = true;
            startAnimation();
        }
        public void animateConsume(String item) {
            animatingItem = item;
            animX = 220; // buffer 區塊右側
            animY = 60;
            targetX = 400; // 消費者區塊左側
            isProducing = false;
            startAnimation();
        }
        private void startAnimation() {
            if (timer != null && timer.isRunning()) timer.stop();
            timer = new Timer(10, e -> {
                if (isProducing) {
                    if (animX < targetX) {
                        animX += 5;
                        repaint();
                    } else {
                        ((Timer)e.getSource()).stop();
                        animatingItem = null;
                        repaint();
                    }
                } else {
                    if (animX < targetX) {
                        animX += 5;
                        repaint();
                    } else {
                        ((Timer)e.getSource()).stop();
                        animatingItem = null;
                        repaint();
                    }
                }
            });
            timer.start();
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // 畫三個區塊
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(10, 40, 60, 40); // 生產者
            g.fillRect(190, 40, 60, 40); // buffer
            g.fillRect(370, 40, 60, 40); // 消費者
            g.setColor(Color.BLACK);
            g.drawRect(10, 40, 60, 40);
            g.drawRect(190, 40, 60, 40);
            g.drawRect(370, 40, 60, 40);
            g.drawString("生產者", 20, 35);
            g.drawString("Buffer", 200, 35);
            g.drawString("消費者", 380, 35);
            // 畫箭頭
            g.drawLine(70, 60, 190, 60);
            g.drawLine(250, 60, 370, 60);
            g.drawLine(185, 60, 190, 60); // buffer左
            g.drawLine(250, 60, 255, 60); // buffer右
            // 畫動畫物品
            if (animatingItem != null) {
                g.setColor(isProducing ? Color.YELLOW : Color.PINK);
                g.fillOval(animX, animY, 30, 30);
                g.setColor(Color.BLACK);
                g.drawOval(animX, animY, 30, 30);
                g.drawString("物品", animX + 5, animY + 20);
            }
        }
    }
}
