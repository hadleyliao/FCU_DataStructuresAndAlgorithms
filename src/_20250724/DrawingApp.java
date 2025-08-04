/*************************
 * 對應課程: Chapter 4
 * CourseWork1: 小畫家2
 **************************/

package _20250724;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.border.AbstractBorder;
import java.awt.image.BufferedImage;

// 自定義圓角邊框
class RoundedBorder extends AbstractBorder {
    private int radius;
    private Color color;

    public RoundedBorder(int radius, Color color) {
        this.radius = radius;
        this.color = color;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(2, 2, 2, 2);
    }
}

// 自定義圓角按鈕
class RoundedButton extends JButton {
    private int radius;
    private Color baseColor;
    private Color hoverColor;
    private boolean isHovered = false;

    public RoundedButton(String text, Color baseColor, int radius) {
        super(text);
        this.baseColor = baseColor;
        this.radius = radius;
        this.hoverColor = baseColor.brighter();

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bgColor = isHovered ? hoverColor : baseColor;
        if (getModel().isPressed()) {
            bgColor = baseColor.darker();
        }

        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        super.paintComponent(g);
    }

    public void setBaseColor(Color color) {
        this.baseColor = color;
        this.hoverColor = color.brighter();
    }

    public void setHoverColor(Color color) {
        this.hoverColor = color;
    }
}

// 主視窗類別
public class DrawingApp extends JFrame {
    private DrawPanel drawPanel;
    private JLabel statusLabel;
    private Color[] quickColors = {
            Color.BLACK, new Color(220, 20, 60), new Color(30, 144, 255),
            new Color(34, 139, 34), new Color(255, 140, 0), new Color(148, 0, 211),
            new Color(255, 20, 147), new Color(0, 191, 255), new Color(50, 205, 50)
    };

    public DrawingApp() {
        setTitle("專業繪圖工具 - Professional Drawing Tool");
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 設定現代化的 Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initializeComponents();
        setupLayout();
        setupMenuBar();
    }

    private void initializeComponents() {
        drawPanel = new DrawPanel();
        drawPanel.setBorder(new RoundedBorder(10, new Color(200, 200, 200)));
        drawPanel.setBackground(Color.WHITE);

        statusLabel = new JLabel("就緒 - Ready");
        statusLabel.setFont(new Font("微軟正黑體", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel.setBackground(new Color(240, 240, 240));
        statusLabel.setOpaque(true);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // 主繪圖區域
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.add(drawPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 工具面板
        JPanel toolPanel = createToolPanel();
        add(toolPanel, BorderLayout.WEST);

        // 頂部控制面板
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // 狀態列
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // 檔案操作按鈕
        RoundedButton newBtn = new RoundedButton("新建", new Color(52, 152, 219), 8);
        RoundedButton saveBtn = new RoundedButton("儲存", new Color(46, 204, 113), 8);
        RoundedButton openBtn = new RoundedButton("開啟", new Color(155, 89, 182), 8);

        newBtn.setPreferredSize(new Dimension(80, 35));
        saveBtn.setPreferredSize(new Dimension(80, 35));
        openBtn.setPreferredSize(new Dimension(80, 35));

        newBtn.setFont(new Font("微軟正黑體", Font.BOLD, 12));
        saveBtn.setFont(new Font("微軟正黑體", Font.BOLD, 12));
        openBtn.setFont(new Font("微軟正黑體", Font.BOLD, 12));

        newBtn.setForeground(Color.WHITE);
        saveBtn.setForeground(Color.WHITE);
        openBtn.setForeground(Color.WHITE);

        newBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "確定要新建畫布嗎？未儲存的內容將遺失！",
                    "新建畫布",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                drawPanel.clearCanvas();
                statusLabel.setText("新建畫布 - New Canvas Created");
            }
        });

        saveBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("儲存畫布");
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                try {
                    java.io.File fileToSave = fileChooser.getSelectedFile();
                    if (!fileToSave.getName().toLowerCase().endsWith(".png")) {
                        fileToSave = new java.io.File(fileToSave.getAbsolutePath() + ".png");
                    }
                    BufferedImage img = drawPanel.exportImage();
                    javax.imageio.ImageIO.write(img, "png", fileToSave);
                    JOptionPane.showMessageDialog(this, "儲存成功！", "訊息", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "儲存失敗: " + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        openBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("開啟畫布");
            int userSelection = fileChooser.showOpenDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                try {
                    java.io.File fileToOpen = fileChooser.getSelectedFile();
                    BufferedImage loadedImg = javax.imageio.ImageIO.read(fileToOpen);
                    if (loadedImg != null) {
                        drawPanel.importImage(loadedImg);
                        statusLabel.setText("已開啟檔案");
                    } else {
                        JOptionPane.showMessageDialog(this, "檔案格式錯誤或無法開啟！", "錯誤", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "開啟失敗: " + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(newBtn);
        panel.add(saveBtn);
        panel.add(openBtn);

        // 分隔線
        panel.add(createSeparator());

        // Undo/Redo 按鈕
        RoundedButton undoBtn = new RoundedButton("復原", new Color(231, 76, 60), 8);
        RoundedButton redoBtn = new RoundedButton("重做", new Color(230, 126, 34), 8);

        undoBtn.setPreferredSize(new Dimension(70, 35));
        redoBtn.setPreferredSize(new Dimension(70, 35));
        undoBtn.setFont(new Font("微軟正黑體", Font.BOLD, 12));
        redoBtn.setFont(new Font("微軟正黑體", Font.BOLD, 12));
        undoBtn.setForeground(Color.WHITE);
        redoBtn.setForeground(Color.WHITE);

        undoBtn.addActionListener(e -> {
            drawPanel.undo();
            statusLabel.setText("已復原 - Undo Applied");
        });
        redoBtn.addActionListener(e -> {
            drawPanel.redo();
            statusLabel.setText("已重做 - Redo Applied");
        });

        panel.add(undoBtn);
        panel.add(redoBtn);

        return panel;
    }

    private JPanel createToolPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(200, 0));

        // 顏色選擇區域
        panel.add(createColorSection());
        panel.add(Box.createVerticalStrut(20));

        // 筆刷設定區域
        panel.add(createBrushSection());
        panel.add(Box.createVerticalStrut(20));

        // 工具選擇區域
        panel.add(createToolSection());

        return panel;
    }

    private JPanel createColorSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(248, 249, 250));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("顏色選擇");
        title.setFont(new Font("微軟正黑體", Font.BOLD, 14));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(title);
        section.add(Box.createVerticalStrut(10));

        // 自定義顏色按鈕
        RoundedButton colorBtn = new RoundedButton("自定義顏色", new Color(100, 100, 100), 8);
        colorBtn.setPreferredSize(new Dimension(160, 35));
        colorBtn.setMaximumSize(new Dimension(160, 35));
        colorBtn.setFont(new Font("微軟正黑體", Font.PLAIN, 12));
        colorBtn.setForeground(Color.WHITE);
        colorBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        colorBtn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "選擇畫筆顏色", drawPanel.getCurrentColor());
            if (chosen != null) {
                drawPanel.setColor(chosen);
                updateColorButton(colorBtn, chosen);
                statusLabel.setText("顏色已更改 - Color Changed");
            }
        });

        section.add(colorBtn);
        section.add(Box.createVerticalStrut(10));

        // 快速顏色選擇
        JLabel quickLabel = new JLabel("快速選色:");
        quickLabel.setFont(new Font("微軟正黑體", Font.PLAIN, 12));
        quickLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(quickLabel);
        section.add(Box.createVerticalStrut(5));

        JPanel colorGrid = new JPanel(new GridLayout(3, 3, 5, 5));
        colorGrid.setBackground(new Color(248, 249, 250));
        colorGrid.setMaximumSize(new Dimension(160, 80));
        colorGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (Color color : quickColors) {
            JButton quickColorBtn = new JButton();
            quickColorBtn.setBackground(color);
            quickColorBtn.setPreferredSize(new Dimension(25, 25));
            quickColorBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            quickColorBtn.addActionListener(e -> {
                drawPanel.setColor(color);
                updateColorButton(colorBtn, color);
                statusLabel.setText("快速選色 - Quick Color Selected");
            });
            colorGrid.add(quickColorBtn);
        }

        section.add(colorGrid);
        return section;
    }

    private JPanel createBrushSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(248, 249, 250));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("筆刷設定");
        title.setFont(new Font("微軟正黑體", Font.BOLD, 14));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(title);
        section.add(Box.createVerticalStrut(10));

        JLabel thicknessLabel = new JLabel("筆刷粗細: 3px");
        thicknessLabel.setFont(new Font("微軟正黑體", Font.PLAIN, 12));
        thicknessLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(thicknessLabel);
        section.add(Box.createVerticalStrut(5));

        JSlider thicknessSlider = new JSlider(1, 50, 3);
        thicknessSlider.setBackground(new Color(248, 249, 250));
        thicknessSlider.setMaximumSize(new Dimension(160, 50));
        thicknessSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        thicknessSlider.addChangeListener(e -> {
            int value = thicknessSlider.getValue();
            drawPanel.setThickness(value);
            thicknessLabel.setText("筆刷粗細: " + value + "px");
            statusLabel.setText("筆刷粗細: " + value + "px - Brush Size Changed");
        });

        section.add(thicknessSlider);
        return section;
    }

    private JPanel createToolSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(248, 249, 250));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("工具選擇");
        title.setFont(new Font("微軟正黑體", Font.BOLD, 14));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(title);
        section.add(Box.createVerticalStrut(10));

        ButtonGroup toolGroup = new ButtonGroup();

        JRadioButton penTool = new JRadioButton("畫筆工具", true);
        JRadioButton eraserTool = new JRadioButton("橡皮擦");

        penTool.setBackground(new Color(248, 249, 250));
        eraserTool.setBackground(new Color(248, 249, 250));
        penTool.setFont(new Font("微軟正黑體", Font.PLAIN, 12));
        eraserTool.setFont(new Font("微軟正黑體", Font.PLAIN, 12));
        penTool.setAlignmentX(Component.LEFT_ALIGNMENT);
        eraserTool.setAlignmentX(Component.LEFT_ALIGNMENT);

        penTool.addActionListener(e -> {
            drawPanel.setTool("pen");
            statusLabel.setText("畫筆工具已選擇 - Pen Tool Selected");
        });
        eraserTool.addActionListener(e -> {
            drawPanel.setTool("eraser");
            statusLabel.setText("橡皮擦已選擇 - Eraser Tool Selected");
        });

        toolGroup.add(penTool);
        toolGroup.add(eraserTool);

        section.add(penTool);
        section.add(eraserTool);

        return section;
    }

    private void updateColorButton(RoundedButton button, Color color) {
        button.setBaseColor(color);
        int brightness = (int)(0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
        button.setForeground(brightness < 128 ? Color.WHITE : Color.BLACK);
        button.repaint();
    }

    private Component createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(1, 30));
        separator.setForeground(new Color(220, 220, 220));
        return separator;
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(248, 249, 250));

        JMenu fileMenu = new JMenu("檔案");
        JMenu editMenu = new JMenu("編輯");
        JMenu helpMenu = new JMenu("說明");

        fileMenu.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
        editMenu.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
        helpMenu.setFont(new Font("微軟正黑體", Font.PLAIN, 14));

        // 檔案選單項目
        JMenuItem newItem = new JMenuItem("新建 (Ctrl+N)");
        JMenuItem openItem = new JMenuItem("開啟 (Ctrl+O)");
        JMenuItem saveItem = new JMenuItem("儲存 (Ctrl+S)");
        JMenuItem exitItem = new JMenuItem("結束");

        newItem.addActionListener(e -> drawPanel.clearCanvas());
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // 編輯選單項目
        JMenuItem undoItem = new JMenuItem("復原 (Ctrl+Z)");
        JMenuItem redoItem = new JMenuItem("重做 (Ctrl+Y)");
        JMenuItem clearItem = new JMenuItem("清除畫布");

        undoItem.addActionListener(e -> drawPanel.undo());
        redoItem.addActionListener(e -> drawPanel.redo());
        clearItem.addActionListener(e -> drawPanel.clearCanvas());

        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(clearItem);

        // 說明選單項目
        JMenuItem aboutItem = new JMenuItem("關於");
        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "專業繪圖工具 v2.0\n\n" +
                            "功能特色:\n" +
                            "• 多種顏色選擇\n" +
                            "• 可調整筆刷粗細\n" +
                            "• 復原/重做功能\n" +
                            "• 現代化界面設計\n\n" +
                            "製作者: AI Assistant",
                    "關於程式",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            DrawingApp app = new DrawingApp();
            app.setVisible(true);
        });
    }

    // 優化後的畫布面板
    class DrawPanel extends JPanel {
        private ArrayList<Stroke> strokes = new ArrayList<>();
        private ArrayList<ArrayList<Stroke>> history = new ArrayList<>();
        private int historyPointer = -1;
        private Color currentColor = Color.BLACK;
        private int currentThickness = 3;
        private String currentTool = "pen";
        private int lastX, lastY;
        private boolean drawing = false;
        private ArrayList<Point> currentStroke = new ArrayList<>();

        public DrawPanel() {
            setBackground(Color.WHITE);
            saveHistory();

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    lastX = e.getX();
                    lastY = e.getY();
                    drawing = true;
                    currentStroke = new ArrayList<>();
                    currentStroke.add(new Point(lastX, lastY));
                }

                public void mouseReleased(MouseEvent e) {
                    if (drawing && !currentStroke.isEmpty()) {
                        Color strokeColor = currentTool.equals("eraser") ? getBackground() : currentColor;
                        strokes.add(new Stroke(new ArrayList<>(currentStroke), strokeColor,
                                currentTool.equals("eraser") ? currentThickness * 2 : currentThickness));
                        saveHistory();
                    }
                    drawing = false;
                    currentStroke.clear();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (drawing) {
                        int x = e.getX();
                        int y = e.getY();
                        currentStroke.add(new Point(x, y));
                        lastX = x;
                        lastY = y;
                        repaint();
                    }
                }
            });
        }

        private void saveHistory() {
            while (history.size() > historyPointer + 1) {
                history.remove(history.size() - 1);
            }

            ArrayList<Stroke> snapshot = new ArrayList<>();
            for (Stroke stroke : strokes) {
                snapshot.add(new Stroke(new ArrayList<>(stroke.points), stroke.color, stroke.thickness));
            }

            history.add(snapshot);
            historyPointer = history.size() - 1;

            if (history.size() > 20) {  // 增加歷史記錄數量
                history.remove(0);
                historyPointer--;
            }
        }

        public void undo() {
            if (historyPointer > 0) {
                historyPointer--;
                strokes.clear();
                for (Stroke stroke : history.get(historyPointer)) {
                    strokes.add(new Stroke(new ArrayList<>(stroke.points), stroke.color, stroke.thickness));
                }
                repaint();
            }
        }

        public void redo() {
            if (historyPointer < history.size() - 1) {
                historyPointer++;
                strokes.clear();
                for (Stroke stroke : history.get(historyPointer)) {
                    strokes.add(new Stroke(new ArrayList<>(stroke.points), stroke.color, stroke.thickness));
                }
                repaint();
            }
        }

        public void clearCanvas() {
            strokes.clear();
            saveHistory();
            repaint();
        }

        public void setColor(Color c) {
            currentColor = c;
        }

        public void setThickness(int t) {
            currentThickness = t;
        }

        public void setTool(String tool) {
            currentTool = tool;
        }

        public Color getCurrentColor() {
            return currentColor;
        }

        public BufferedImage exportImage() {
            BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            paint(g2);
            g2.dispose();
            return img;
        }

        public void importImage(BufferedImage img) {
            Graphics g = getGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            saveHistory();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            // 繪製所有筆畫
            for (Stroke stroke : strokes) {
                g2.setColor(stroke.color);
                g2.setStroke(new BasicStroke(stroke.thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                for (int i = 1; i < stroke.points.size(); i++) {
                    Point p1 = stroke.points.get(i - 1);
                    Point p2 = stroke.points.get(i);
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }

            // 繪製當前正在畫的筆畫
            if (drawing && !currentStroke.isEmpty()) {
                Color strokeColor = currentTool.equals("eraser") ? getBackground() : currentColor;
                g2.setColor(strokeColor);
                g2.setStroke(new BasicStroke(currentTool.equals("eraser") ? currentThickness * 2 : currentThickness,
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                for (int i = 1; i < currentStroke.size(); i++) {
                    Point p1 = currentStroke.get(i - 1);
                    Point p2 = currentStroke.get(i);
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }

        // 筆畫資料結構
        class Stroke {
            ArrayList<Point> points;
            Color color;
            int thickness;

            Stroke(ArrayList<Point> points, Color color, int thickness) {
                this.points = points;
                this.color = color;
                this.thickness = thickness;
            }
        }
    }
}

