/***********************************
 * 對應課程: Chapter 7
 * CourseWork2: 圖形生成器(Claude)
 ***********************************/

package _20250805;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class GraphGenerator extends JFrame {
    private JTextField vertexField;
    private JTextField edgeField;
    private JButton generateButton;
    private JButton mstButton; // 新增 MST 按鈕
    private GraphPanel graphPanel;
    private JTextArea vertexInfoArea;
    private JTextArea edgeInfoArea;

    public GraphGenerator() {
        setTitle("圖形生成器 - Graph Generator with Cost");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 創建控制面板
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("設定參數"));

        controlPanel.add(new JLabel("頂點數量:"));
        vertexField = new JTextField("5", 5);
        controlPanel.add(vertexField);

        controlPanel.add(new JLabel("邊數量:"));
        edgeField = new JTextField("7", 5);
        controlPanel.add(edgeField);

        generateButton = new JButton("開始畫圖");
        generateButton.addActionListener(new GenerateButtonListener());
        controlPanel.add(generateButton);

        mstButton = new JButton("計算最小生成樹");
        mstButton.addActionListener(new MSTButtonListener());
        controlPanel.add(mstButton);

        // 創建主要內容面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 創建繪圖面板
        graphPanel = new GraphPanel();
        graphPanel.setPreferredSize(new Dimension(600, 500));

        // 創建資訊顯示面板
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setPreferredSize(new Dimension(300, 500));

        // 頂點資訊區域
        JPanel vertexPanel = new JPanel(new BorderLayout());
        vertexPanel.setBorder(BorderFactory.createTitledBorder("頂點資訊"));
        vertexInfoArea = new JTextArea();
        vertexInfoArea.setEditable(false);
        vertexInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane vertexScrollPane = new JScrollPane(vertexInfoArea);
        vertexScrollPane.setPreferredSize(new Dimension(290, 230));
        vertexPanel.add(vertexScrollPane, BorderLayout.CENTER);

        // 邊資訊區域
        JPanel edgePanel = new JPanel(new BorderLayout());
        edgePanel.setBorder(BorderFactory.createTitledBorder("邊資訊 (包含成本)"));
        edgeInfoArea = new JTextArea();
        edgeInfoArea.setEditable(false);
        edgeInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane edgeScrollPane = new JScrollPane(edgeInfoArea);
        edgeScrollPane.setPreferredSize(new Dimension(290, 230));
        edgePanel.add(edgeScrollPane, BorderLayout.CENTER);

        infoPanel.add(vertexPanel);
        infoPanel.add(edgePanel);

        mainPanel.add(graphPanel, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.EAST);

        add(controlPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        setSize(950, 600);
        setLocationRelativeTo(null);

        // 初始化資訊顯示
        updateInfoDisplay();
    }

    private class GenerateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int vertexCount = Integer.parseInt(vertexField.getText());
                int edgeCount = Integer.parseInt(edgeField.getText());

                if (vertexCount < 1) {
                    JOptionPane.showMessageDialog(GraphGenerator.this,
                            "頂點數量必須至少為1", "錯誤", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int maxEdges = vertexCount * (vertexCount - 1) / 2; // 完全圖的最大邊數
                if (edgeCount < 0 || edgeCount > maxEdges) {
                    JOptionPane.showMessageDialog(GraphGenerator.this,
                            "邊數量必須在0到" + maxEdges + "之間", "錯誤", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                graphPanel.generateGraph(vertexCount, edgeCount);
                updateInfoDisplay();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(GraphGenerator.this,
                        "請輸入有效的數字", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class MSTButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            mstEdges = calculateMST(graphPanel.getVertices(), graphPanel.getEdges());
            updateInfoDisplay();
            graphPanel.repaint();
        }
    }

    private List<Edge> mstEdges = new ArrayList<>(); // MST 邊

    private void updateInfoDisplay() {
        // 更新頂點資訊
        StringBuilder vertexInfo = new StringBuilder();
        List<Vertex> vertices = graphPanel.getVertices();

        if (vertices.isEmpty()) {
            vertexInfo.append("尚未生成圖形\n");
            vertexInfo.append("請設定參數後點擊「開始畫圖」");
        } else {
            vertexInfo.append("頂點總數: ").append(vertices.size()).append("\n\n");
            vertexInfo.append("頂點列表:\n");
            vertexInfo.append("ID  座標(X, Y)\n");
            vertexInfo.append("================\n");
            for (Vertex vertex : vertices) {
                vertexInfo.append(String.format("V%-2d (%3d, %3d)\n",
                        vertex.id, vertex.x, vertex.y));
            }
        }
        vertexInfoArea.setText(vertexInfo.toString());

        // 更新邊資訊
        StringBuilder edgeInfo = new StringBuilder();
        List<Edge> edges = graphPanel.getEdges();

        if (edges.isEmpty() && !vertices.isEmpty()) {
            edgeInfo.append("邊總數: 0\n\n");
            edgeInfo.append("無邊連接");
        } else if (!edges.isEmpty()) {
            edgeInfo.append("邊總數: ").append(edges.size()).append("\n\n");
            edgeInfo.append("邊列表 (雙向):\n");
            edgeInfo.append("邊      成本\n");
            edgeInfo.append("================\n");
            for (Edge edge : edges) {
                edgeInfo.append(String.format("V%d-V%d   %2d\n",
                        edge.from.id, edge.to.id, edge.cost));
            }

            // 計算總成本
            int totalCost = edges.stream().mapToInt(edge -> edge.cost).sum();
            edgeInfo.append("\n總成本: ").append(totalCost);
        } else {
            edgeInfo.append("尚未生成圖形");
        }

        // 更新 MST 邊資訊
        if (!mstEdges.isEmpty()) {
            edgeInfo.append("\n最小生成樹 MST 邊數: ").append(mstEdges.size()).append("\n");
            int mstCost = mstEdges.stream().mapToInt(e -> e.cost).sum();
            edgeInfo.append("MST 總成本: ").append(mstCost).append("\n");
            edgeInfo.append("MST 邊列表:\n");
            for (Edge edge : mstEdges) {
                edgeInfo.append(String.format("V%d-V%d   %2d\n", edge.from.id, edge.to.id, edge.cost));
            }
        }

        edgeInfoArea.setText(edgeInfo.toString());
        // 如果有 MST 結果，設置橘色文字
        if (!mstEdges.isEmpty()) {
            edgeInfoArea.setForeground(new Color(255, 140, 0)); // 橘色
        } else {
            edgeInfoArea.setForeground(Color.BLACK);
        }
    }

    // Kruskal 演算法計算 MST
    private List<Edge> calculateMST(List<Vertex> vertices, List<Edge> edges) {
        List<Edge> result = new ArrayList<>();
        if (vertices.isEmpty() || edges.isEmpty()) return result;
        int n = vertices.size();
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        edges.sort(Comparator.comparingInt(e -> e.cost));
        int count = 0;
        for (Edge edge : edges) {
            int a = find(parent, edge.from.id);
            int b = find(parent, edge.to.id);
            if (a != b) {
                result.add(edge);
                parent[a] = b;
                count++;
                if (count == n - 1) break;
            }
        }
        return result;
    }
    private int find(int[] parent, int x) {
        if (parent[x] != x) parent[x] = find(parent, parent[x]);
        return parent[x];
    }

    private class GraphPanel extends JPanel {
        private List<Vertex> vertices;
        private List<Edge> edges;
        private Random random;

        public GraphPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createTitledBorder("圖形顯示區"));
            vertices = new ArrayList<>();
            edges = new ArrayList<>();
            random = new Random();
        }

        public List<Vertex> getVertices() {
            return new ArrayList<>(vertices);
        }

        public List<Edge> getEdges() {
            return new ArrayList<>(edges);
        }

        public void generateGraph(int vertexCount, int edgeCount) {
            vertices.clear();
            edges.clear();

            // 生成頂點位置（圓形排列）
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = Math.min(centerX, centerY) - 100;

            for (int i = 0; i < vertexCount; i++) {
                double angle = 2 * Math.PI * i / vertexCount;
                int x = centerX + (int)(radius * Math.cos(angle));
                int y = centerY + (int)(radius * Math.sin(angle));
                vertices.add(new Vertex(i, x, y));
            }

            // 生成無向邊（雙向但只建立一次）
            Set<String> edgeSet = new HashSet<>();
            int edgesAdded = 0;

            while (edgesAdded < edgeCount && edgeSet.size() < vertexCount * (vertexCount - 1) / 2) {
                int from = random.nextInt(vertexCount);
                int to = random.nextInt(vertexCount);

                if (from != to) {
                    // 確保較小的ID在前面，避免重複邊
                    int minId = Math.min(from, to);
                    int maxId = Math.max(from, to);
                    String edgeKey = minId + "-" + maxId;

                    if (!edgeSet.contains(edgeKey)) {
                        edgeSet.add(edgeKey);
                        int cost = random.nextInt(99) + 1; // 1-99之間的隨機成本
                        edges.add(new Edge(vertices.get(minId), vertices.get(maxId), cost));
                        edgesAdded++;
                    }
                }
            }

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (vertices.isEmpty()) {
                // 顯示提示文字
                g2d.setColor(Color.GRAY);
                g2d.setFont(new Font("Arial", Font.BOLD, 18));
                String message = "請設定頂點和邊的數量";
                String message2 = "然後點擊「開始畫圖」";
                FontMetrics fm = g2d.getFontMetrics();
                int x1 = (getWidth() - fm.stringWidth(message)) / 2;
                int x2 = (getWidth() - fm.stringWidth(message2)) / 2;
                int y = getHeight() / 2 - 15;
                g2d.drawString(message, x1, y);
                g2d.drawString(message2, x2, y + 30);
                return;
            }

            // 先畫 MST 邊（用橘色高亮）
            g2d.setColor(Color.ORANGE);
            g2d.setStroke(new BasicStroke(4));
            for (Edge edge : mstEdges) {
                g2d.drawLine(edge.from.x, edge.from.y, edge.to.x, edge.to.y);
                // 在邊中點顯示橘色成本文字
                int midX = (edge.from.x + edge.to.x) / 2;
                int midY = (edge.from.y + edge.to.y) / 2;
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.setColor(Color.ORANGE);
                String costText = String.valueOf(edge.cost);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = midX - fm.stringWidth(costText) / 2;
                int textY = midY - 10;
                g2d.drawString(costText, textX, textY);
                g2d.setColor(Color.ORANGE);
            }

            // 繪製邊和成本標籤
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(2));
            for (Edge edge : edges) {
                // 繪製邊
                g2d.drawLine(edge.from.x, edge.from.y, edge.to.x, edge.to.y);

                // 計算邊的中點位置
                int midX = (edge.from.x + edge.to.x) / 2;
                int midY = (edge.from.y + edge.to.y) / 2;

                // 繪製成本標籤背景
                g2d.setColor(Color.YELLOW);
                g2d.fillRoundRect(midX - 12, midY - 8, 24, 16, 6, 6);

                // 繪製成本標籤邊框
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(midX - 12, midY - 8, 24, 16, 6, 6);

                // 繪製成本數字
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                String costText = String.valueOf(edge.cost);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = midX - fm.stringWidth(costText) / 2;
                int textY = midY + fm.getAscent() / 2;
                g2d.drawString(costText, textX, textY);

                // 重設顏色和筆觸以繪製下一條邊
                g2d.setColor(Color.BLUE);
                g2d.setStroke(new BasicStroke(2));
            }

            // 繪製頂點
            for (Vertex vertex : vertices) {
                // 繪製頂點圓圈
                g2d.setColor(Color.RED);
                g2d.fillOval(vertex.x - 15, vertex.y - 15, 30, 30);

                // 繪製頂點邊框
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(vertex.x - 15, vertex.y - 15, 30, 30);

                // 繪製頂點標籤
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                String label = "V" + vertex.id;
                FontMetrics fm = g2d.getFontMetrics();
                int labelX = vertex.x - fm.stringWidth(label) / 2;
                int labelY = vertex.y + fm.getAscent() / 2;
                g2d.drawString(label, labelX, labelY);
            }

            // 顯示統計信息（放在右上角避免重疊）
            // 已取消頂點/邊/總成本文字顯示
        }
    }

    private class Vertex {
        int id;
        int x, y;

        public Vertex(int id, int x, int y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }

    private class Edge {
        Vertex from, to;
        int cost;

        public Edge(Vertex from, Vertex to, int cost) {
            this.from = from;
            this.to = to;
            this.cost = cost;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            new GraphGenerator().setVisible(true);
        });
    }
}