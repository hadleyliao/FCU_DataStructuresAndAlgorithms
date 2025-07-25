import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Deque;
import java.util.LinkedList;

public class SimplePaint extends JFrame {
    private final int CANVAS_WIDTH = 700;
    private final int CANVAS_HEIGHT = 500;
    private Color currentColor = Color.BLACK;
    private int brushSize = 5;
    private BufferedImage canvasImage;
    private Point lastPoint = null;

    // GUI組件
    private JPanel colorPreview;
    private JLabel brushSizeLabel;

    // Undo/Redo 歷史紀錄
    private final int HISTORY_SIZE = 10;
    private Deque<BufferedImage> undoStack = new LinkedList<>();
    private Deque<BufferedImage> redoStack = new LinkedList<>();

    public SimplePaint() {
        setTitle("簡易畫圖程式 - Simple Paint");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        // 先建立工具列與畫布
        initializeCanvas();
        createToolPanel();
        createCanvasPanel();
        pack(); // 讓視窗自動適應內容大小
        setResizable(false); // 不允許調整視窗大小，避免誤操作
        setLocationRelativeTo(null);
        // 初始保存空白畫布
        saveToUndo();
    }

    private void initializeCanvas() {
        canvasImage = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvasImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        g2.dispose();
    }

    private void createCanvasPanel() {
        JPanel canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(canvasImage, 0, 0, null);
            }
        };

        canvasPanel.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        canvasPanel.setBackground(Color.WHITE);
        canvasPanel.setBorder(BorderFactory.createLoweredBevelBorder());

        // 滑鼠繪圖事件
        canvasPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                saveToUndo();
                lastPoint = e.getPoint();
            }

            public void mouseReleased(MouseEvent e) {
                lastPoint = null;
            }
        });

        canvasPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    Graphics2D g2 = canvasImage.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(currentColor);
                    g2.setStroke(new BasicStroke(brushSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(lastPoint.x, lastPoint.y, e.getX(), e.getY());
                    g2.dispose();
                    lastPoint = e.getPoint();
                    canvasPanel.repaint();
                }
            }
        });

        add(canvasPanel, BorderLayout.CENTER);
    }

    private void createToolPanel() {
        JPanel mainToolPanel = new JPanel(new BorderLayout());
        mainToolPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        // 上方工具列
        JPanel topToolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // 顏色選擇區域
        JPanel colorSection = createColorSection();
        topToolPanel.add(colorSection);

        // 分隔線
        topToolPanel.add(new JSeparator(SwingConstants.VERTICAL));

        // 筆刷大小區域
        JPanel brushSection = createBrushSection();
        topToolPanel.add(brushSection);

        // 分隔線
        topToolPanel.add(new JSeparator(SwingConstants.VERTICAL));

        // 操作按鈕區域
        JPanel actionSection = createActionSection();
        topToolPanel.add(actionSection);

        mainToolPanel.add(topToolPanel, BorderLayout.NORTH);
        add(mainToolPanel, BorderLayout.NORTH);
    }

    private JPanel createColorSection() {
        JPanel colorSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        Border sectionBorder = BorderFactory.createTitledBorder("顏色");
        colorSection.setBorder(sectionBorder);

        // 當前顏色預覽
        colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(40, 40));
        colorPreview.setBackground(currentColor);
        colorPreview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createLoweredBevelBorder()
        ));
        colorSection.add(colorPreview);

        // 顏色選擇按鈕
        JButton colorBtn = new JButton("選擇顏色");
        colorBtn.setPreferredSize(new Dimension(90, 35));
        colorBtn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "選擇顏色", currentColor);
            if (chosen != null) {
                currentColor = chosen;
                colorPreview.setBackground(currentColor);
                colorPreview.repaint();
            }
        });
        colorSection.add(colorBtn);

        // 移除預選色
        // 不再加入 quickColors 區塊

        return colorSection;
    }

    private JPanel createBrushSection() {
        JPanel brushSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        Border sectionBorder = BorderFactory.createTitledBorder("筆刷");
        brushSection.setBorder(sectionBorder);

        brushSizeLabel = new JLabel("大小: " + brushSize);
        brushSizeLabel.setPreferredSize(new Dimension(60, 20));
        brushSection.add(brushSizeLabel);

        JSlider sizeSlider = new JSlider(1, 50, brushSize);
        sizeSlider.setPreferredSize(new Dimension(120, 40));
        sizeSlider.setMajorTickSpacing(10);
        sizeSlider.setMinorTickSpacing(5);
        sizeSlider.setPaintTicks(true);
        sizeSlider.setPaintLabels(true);
        sizeSlider.addChangeListener(e -> {
            brushSize = sizeSlider.getValue();
            brushSizeLabel.setText("大小: " + brushSize);
        });
        brushSection.add(sizeSlider);

        return brushSection;
    }

    private JPanel createActionSection() {
        JPanel actionSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        Border sectionBorder = BorderFactory.createTitledBorder("操作");
        actionSection.setBorder(sectionBorder);

        // Undo按鈕
        JButton undoBtn = createStyledButton("↶ 復原", "復原上一步操作");
        undoBtn.addActionListener(e -> performUndo());
        actionSection.add(undoBtn);

        // Redo按鈕
        JButton redoBtn = createStyledButton("↷ 重做", "重做操作");
        redoBtn.addActionListener(e -> performRedo());
        actionSection.add(redoBtn);

        // 清除按鈕
        JButton clearBtn = createStyledButton("🗑 清除", "清除整個畫布");
        clearBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "確定要清除整個畫布嗎？",
                    "確認清除",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                clearCanvas();
            }
        });
        actionSection.add(clearBtn);

        return actionSection;
    }

    private JButton createStyledButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(80, 35));
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        return button;
    }

    private void performUndo() {
        if (!undoStack.isEmpty()) {
            redoStack.addLast(deepCopy(canvasImage));
            if (redoStack.size() > HISTORY_SIZE) {
                redoStack.removeFirst();
            }
            canvasImage = undoStack.removeLast();
            repaint();
        }
    }

    private void performRedo() {
        if (!redoStack.isEmpty()) {
            undoStack.addLast(deepCopy(canvasImage));
            if (undoStack.size() > HISTORY_SIZE) {
                undoStack.removeFirst();
            }
            canvasImage = redoStack.removeLast();
            repaint();
        }
    }

    private void clearCanvas() {
        saveToUndo();
        Graphics2D g2d = canvasImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        g2d.dispose();
        repaint();
    }

    private void saveToUndo() {
        if (undoStack.size() == HISTORY_SIZE) {
            undoStack.removeFirst();
        }
        undoStack.addLast(deepCopy(canvasImage));
        redoStack.clear();
    }

    private BufferedImage deepCopy(BufferedImage src) {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Graphics2D g = copy.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return copy;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SimplePaint().setVisible(true);
        });
    }
}