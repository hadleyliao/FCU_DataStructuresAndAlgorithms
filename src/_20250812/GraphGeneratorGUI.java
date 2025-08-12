/*****************************************************************************
 * 對應課程: Chapter 7
 * CourseWork1: 最短路徑(Claude)
 * •設計一個圖型自動產生程式, 使用者可以指定點與邊的數量程式自動依二點在GUI上的矩離指定權重.
 * •程式可將圖型以及圖型的相鄰矩陣顯示在GUI上.
 * •使用者可以指定某個點, 程式可以找出該點到其他點的最短路徑, 並顯示出來.
 * •程式可以自動找出任意二點之間的矩離.
 ****************************************************************************/

package _20250812;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class GraphGeneratorGUI extends JFrame {
    private int numNodes = 5;
    private int numEdges = 7;
    private java.util.List<Node> nodes;
    private java.util.List<Edge> edges;
    private double[][] adjacencyMatrix;
    private int selectedStartNode = 0;
    private ShortestPathResult shortestPaths;
    private double[][] distanceMatrix;

    private JPanel graphPanel;
    private JTextArea matrixArea;
    private JTextArea pathArea;
    private JSpinner nodeSpinner;
    private JSpinner edgeSpinner;
    private JComboBox<String> startNodeCombo;

    // 節點類
    static class Node {
        int id;
        int x, y;
        String label;

        Node(int id, int x, int y, String label) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.label = label;
        }
    }

    // 邊類
    static class Edge {
        int from, to;
        double weight;

        Edge(int from, int to, double weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    // 最短路徑結果類
    static class ShortestPathResult {
        double[] distances;
        int[] previous;

        ShortestPathResult(double[] distances, int[] previous) {
            this.distances = distances;
            this.previous = previous;
        }
    }

    public GraphGeneratorGUI() {
        setTitle("圖形自動產生與最短路徑分析系統");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initializeComponents();
        generateGraph();

        setSize(1200, 800);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeComponents() {
        // 控制面板
        JPanel controlPanel = new JPanel(new FlowLayout());

        controlPanel.add(new JLabel("節點數量:"));
        nodeSpinner = new JSpinner(new SpinnerNumberModel(5, 2, 1000, 1));
        nodeSpinner.setEditor(new JSpinner.NumberEditor(nodeSpinner, "#"));
        nodeSpinner.setValue(Integer.valueOf(5));
        ((JSpinner.DefaultEditor) nodeSpinner.getEditor()).getTextField().setEditable(true);
        nodeSpinner.addChangeListener(e -> {
            try {
                numNodes = Integer.parseInt(nodeSpinner.getValue().toString());
            } catch (Exception ex) {
                numNodes = 2;
            }
            updateEdgeSpinnerRange();
        });
        controlPanel.add(nodeSpinner);

        controlPanel.add(new JLabel("邊的數量:"));
        edgeSpinner = new JSpinner(new SpinnerNumberModel(7, 1, 100000, 1));
        edgeSpinner.setEditor(new JSpinner.NumberEditor(edgeSpinner, "#"));
        edgeSpinner.setValue(Integer.valueOf(7));
        ((JSpinner.DefaultEditor) edgeSpinner.getEditor()).getTextField().setEditable(true);
        edgeSpinner.addChangeListener(e -> {
            try {
                numEdges = Integer.parseInt(edgeSpinner.getValue().toString());
            } catch (Exception ex) {
                numEdges = 1;
            }
        });
        controlPanel.add(edgeSpinner);

        controlPanel.add(new JLabel("起始節點:"));
        startNodeCombo = new JComboBox<>();
        startNodeCombo.addActionListener(e -> {
            if (startNodeCombo.getSelectedIndex() >= 0) {
                selectedStartNode = startNodeCombo.getSelectedIndex();
            }
        });
        controlPanel.add(startNodeCombo);

        JButton generateBtn = new JButton("重新生成圖形");
        generateBtn.addActionListener(e -> generateGraph());
        controlPanel.add(generateBtn);

        JButton pathBtn = new JButton("計算最短路徑");
        pathBtn.addActionListener(e -> calculateShortestPaths());
        controlPanel.add(pathBtn);

        JButton allDistBtn = new JButton("計算所有距離");
        allDistBtn.addActionListener(e -> calculateAllDistances());
        controlPanel.add(allDistBtn);

        add(controlPanel, BorderLayout.NORTH);

        // 主要內容區域
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 圖形顯示面板
        graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGraph(g);
            }
        };
        graphPanel.setPreferredSize(new Dimension(600, 400));
        graphPanel.setBackground(Color.WHITE);
        graphPanel.setBorder(BorderFactory.createTitledBorder("圖形顯示"));

        mainPanel.add(graphPanel, BorderLayout.CENTER);

        // 右側信息面板
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        // 鄰接矩陣顯示
        matrixArea = new JTextArea();
        matrixArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        matrixArea.setEditable(false);
        JScrollPane matrixScroll = new JScrollPane(matrixArea);
        matrixScroll.setBorder(BorderFactory.createTitledBorder("鄰接矩陣"));
        matrixScroll.setPreferredSize(new Dimension(400, 200));

        // 最短路徑結果顯示
        pathArea = new JTextArea();
        pathArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        pathArea.setEditable(false);
        JScrollPane pathScroll = new JScrollPane(pathArea);
        pathScroll.setBorder(BorderFactory.createTitledBorder("最短路徑結果"));
        pathScroll.setPreferredSize(new Dimension(400, 200));

        infoPanel.add(matrixScroll);
        infoPanel.add(pathScroll);

        mainPanel.add(infoPanel, BorderLayout.EAST);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void updateEdgeSpinnerRange() {
        int maxEdges = (numNodes * (numNodes - 1)) / 2;
        int minEdges = Math.max(numNodes - 1, 1);
        int current = numEdges;
        if (current < minEdges) current = minEdges;
        if (current > maxEdges) current = maxEdges;
        edgeSpinner.setModel(new SpinnerNumberModel(current, minEdges, maxEdges, 1));
        numEdges = current;
    }

    private double calculateDistance(Node node1, Node node2) {
        double dx = node1.x - node2.x;
        double dy = node1.y - node2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void generateGraph() {
        if (numNodes < 2) {
            JOptionPane.showMessageDialog(this, "節點數量必須大於等於2！", "輸入錯誤", JOptionPane.ERROR_MESSAGE);
            numNodes = 2;
            nodeSpinner.setValue(Integer.valueOf(2));
        }
        int maxEdges = (numNodes * (numNodes - 1)) / 2;
        if (numEdges < numNodes - 1 || numEdges > maxEdges) {
            JOptionPane.showMessageDialog(this, "邊的數量必須介於 " + (numNodes - 1) + " 和 " + maxEdges + " 之間！", "輸入錯誤", JOptionPane.ERROR_MESSAGE);
            numEdges = Math.max(numNodes - 1, 1);
            edgeSpinner.setValue(Integer.valueOf(numEdges));
        }

        Random random = new Random();
        int width = 550;
        int height = 350;
        int margin = 50;

        // 生成節點
        nodes = new ArrayList<>();
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double radius = Math.min(width, height) / 2.0 - margin;
        for (int i = 0; i < numNodes; i++) {
            double angle = 2 * Math.PI * i / numNodes;
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));
            String label = String.valueOf((char)('A' + i));
            nodes.add(new Node(i, x, y, label));
        }

        // 生成邊（確保連通性）
        edges = new ArrayList<>();
        Set<String> usedConnections = new HashSet<>();

        // 最小生成樹確保連通性
        Set<Integer> connected = new HashSet<>();
        Set<Integer> remaining = new HashSet<>();
        connected.add(Integer.valueOf(0));
        for (int i = 1; i < numNodes; i++) {
            remaining.add(Integer.valueOf(i));
        }

        while (!remaining.isEmpty()) {
            double minDistance = Double.MAX_VALUE;
            Edge bestConnection = null;

            for (int connectedNode : connected) {
                for (int remainingNode : remaining) {
                    double distance = calculateDistance(nodes.get(connectedNode), nodes.get(remainingNode));
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestConnection = new Edge(connectedNode, remainingNode, distance);
                    }
                }
            }

            if (bestConnection != null) {
                edges.add(bestConnection);
                String key = Math.min(bestConnection.from, bestConnection.to) + "-" +
                        Math.max(bestConnection.from, bestConnection.to);
                usedConnections.add(key);
                connected.add(Integer.valueOf(bestConnection.to));
                remaining.remove(Integer.valueOf(bestConnection.to));
            }
        }

        // 添加剩餘隨機邊
        while (edges.size() < numEdges && edges.size() < (numNodes * (numNodes - 1)) / 2) {
            int from = random.nextInt(numNodes);
            int to = random.nextInt(numNodes);

            if (from != to) {
                String key = Math.min(from, to) + "-" + Math.max(from, to);
                if (!usedConnections.contains(key)) {
                    double weight = calculateDistance(nodes.get(from), nodes.get(to));
                    edges.add(new Edge(from, to, weight));
                    usedConnections.add(key);
                }
            }
        }

        // 建立鄰接矩陣
        adjacencyMatrix = new double[numNodes][numNodes];
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                adjacencyMatrix[i][j] = (i == j) ? 0 : Double.POSITIVE_INFINITY;
            }
        }

        for (Edge edge : edges) {
            adjacencyMatrix[edge.from][edge.to] = edge.weight;
            adjacencyMatrix[edge.to][edge.from] = edge.weight; // 無向圖
        }

        updateUI();
    }

    private void updateUI() {
        // 更新起始節點下拉選單
        startNodeCombo.removeAllItems();
        for (Node node : nodes) {
            startNodeCombo.addItem(node.label);
        }
        if (selectedStartNode >= nodes.size()) {
            selectedStartNode = 0;
        }
        startNodeCombo.setSelectedIndex(selectedStartNode);

        // 更新鄰接矩陣顯示
        updateMatrixDisplay();

        // 清除路徑結果
        pathArea.setText("");
        shortestPaths = null;

        // 重繪圖形
        graphPanel.repaint();
    }

    private void updateMatrixDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("鄰接矩陣 (權重):\n\n");

        // 表頭
        sb.append("     ");
        for (Node node : nodes) {
            sb.append(String.format("%8s", node.label));
        }
        sb.append("\n");

        // 矩陣內容
        for (int i = 0; i < numNodes; i++) {
            sb.append(String.format("%3s  ", nodes.get(i).label));
            for (int j = 0; j < numNodes; j++) {
                if (adjacencyMatrix[i][j] == Double.POSITIVE_INFINITY) {
                    sb.append("      ∞ ");
                } else {
                    sb.append(String.format("%8.0f", Double.valueOf(adjacencyMatrix[i][j])));
                }
            }
            sb.append("\n");
        }

        matrixArea.setText(sb.toString());
    }

    // Dijkstra 演算法：計算單一來源最短路徑
    // 此方法會回傳從 start 節點到所有其他節點的最短距離與前驅節點陣列
    private ShortestPathResult dijkstra(int start) {
        double[] distances = new double[numNodes];
        int[] previous = new int[numNodes];
        boolean[] visited = new boolean[numNodes];

        Arrays.fill(distances, Double.POSITIVE_INFINITY);
        Arrays.fill(previous, -1);
        distances[start] = 0;

        for (int i = 0; i < numNodes; i++) {
            double minDistance = Double.POSITIVE_INFINITY;
            int minIndex = -1;

            // 找出尚未拜訪且距離最小的節點
            for (int j = 0; j < numNodes; j++) {
                if (!visited[j] && distances[j] < minDistance) {
                    minDistance = distances[j];
                    minIndex = j;
                }
            }

            if (minIndex == -1) break;
            visited[minIndex] = true;

            // 更新相鄰節點的距離
            for (int j = 0; j < numNodes; j++) {
                if (!visited[j] && adjacencyMatrix[minIndex][j] != Double.POSITIVE_INFINITY) {
                    double newDistance = distances[minIndex] + adjacencyMatrix[minIndex][j];
                    if (newDistance < distances[j]) {
                        distances[j] = newDistance;
                        previous[j] = minIndex;
                    }
                }
            }
        }

        return new ShortestPathResult(distances, previous);
    }

    private void calculateShortestPaths() {
        if (adjacencyMatrix == null) return;

        shortestPaths = dijkstra(selectedStartNode);

        StringBuilder sb = new StringBuilder();
        sb.append("從節點 ").append(nodes.get(selectedStartNode).label).append(" 的最短路徑:\n\n");

        for (int i = 0; i < numNodes; i++) {
            if (i == selectedStartNode) continue;

            sb.append("到 ").append(nodes.get(i).label).append(": ");
            if (shortestPaths.distances[i] == Double.POSITIVE_INFINITY) {
                sb.append("不可達\n");
            } else {
                sb.append(String.format("%.0f", Double.valueOf(shortestPaths.distances[i])));

                // 重構路徑
                java.util.List<Integer> path = reconstructPath(shortestPaths.previous, selectedStartNode, i);
                if (!path.isEmpty()) {
                    sb.append("\n   路徑: ");
                    for (int j = 0; j < path.size(); j++) {
                        sb.append(nodes.get(path.get(j)).label);
                        if (j < path.size() - 1) sb.append(" → ");
                    }
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        pathArea.setText(sb.toString());
        graphPanel.repaint(); // 重繪以顯示最短路徑
    }

    private java.util.List<Integer> reconstructPath(int[] previous, int start, int end) {
        java.util.List<Integer> path = new ArrayList<>();
        int current = end;

        while (current != -1) {
            path.add(0, Integer.valueOf(current));
            current = previous[current];
        }

        return path.get(0) == start ? path : new ArrayList<>();
    }

    // Floyd-Warshall 演算法：計算所有節點對之間的最短距離
    // 此方法會更新 distanceMatrix，內容為任兩點間的最短距離
    private void calculateAllDistances() {
        if (adjacencyMatrix == null) return;

        // Floyd-Warshall算法
        distanceMatrix = new double[numNodes][numNodes];
        for (int i = 0; i < numNodes; i++) {
            System.arraycopy(adjacencyMatrix[i], 0, distanceMatrix[i], 0, numNodes);
        }

        for (int k = 0; k < numNodes; k++) {
            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numNodes; j++) {
                    if (distanceMatrix[i][k] + distanceMatrix[k][j] < distanceMatrix[i][j]) {
                        distanceMatrix[i][j] = distanceMatrix[i][k] + distanceMatrix[k][j];
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("所有節點間最短距離矩陣:\n\n");

        // 表頭
        sb.append("     ");
        for (Node node : nodes) {
            sb.append(String.format("%8s", node.label));
        }
        sb.append("\n");

        // 矩陣內容
        for (int i = 0; i < numNodes; i++) {
            sb.append(String.format("%3s  ", nodes.get(i).label));
            for (int j = 0; j < numNodes; j++) {
                if (distanceMatrix[i][j] == Double.POSITIVE_INFINITY) {
                    sb.append("      ∞ ");
                } else {
                    sb.append(String.format("%8.0f", Double.valueOf(distanceMatrix[i][j])));
                }
            }
            sb.append("\n");
        }

        pathArea.setText(sb.toString());
    }

    private void drawGraph(Graphics g) {
        if (nodes == null || edges == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 繪製邊
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(2));
        for (Edge edge : edges) {
            Node fromNode = nodes.get(edge.from);
            Node toNode = nodes.get(edge.to);

            g2d.drawLine(fromNode.x, fromNode.y, toNode.x, toNode.y);

            // 顯示權重
            int midX = (fromNode.x + toNode.x) / 2;
            int midY = (fromNode.y + toNode.y) / 2;
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.valueOf((int)edge.weight), midX, midY - 5);
            g2d.setColor(Color.LIGHT_GRAY);
        }

        // 繪製最短路徑高亮
        if (shortestPaths != null) {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3));

            for (int i = 0; i < numNodes; i++) {
                if (i != selectedStartNode && shortestPaths.previous[i] != -1) {
                    java.util.List<Integer> path = reconstructPath(shortestPaths.previous, selectedStartNode, i);
                    for (int j = 0; j < path.size() - 1; j++) {
                        Node fromNode = nodes.get(path.get(j));
                        Node toNode = nodes.get(path.get(j + 1));
                        g2d.drawLine(fromNode.x, fromNode.y, toNode.x, toNode.y);
                    }
                }
            }
        }

        // 繪製節點
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);

            if (i == selectedStartNode) {
                g2d.setColor(Color.RED);
            } else {
                g2d.setColor(Color.BLUE);
            }

            g2d.fillOval(node.x - 20, node.y - 20, 40, 40);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(node.x - 20, node.y - 20, 40, 40);

            // 節點標籤
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(node.label);
            int textHeight = fm.getAscent();
            g2d.drawString(node.label, node.x - textWidth/2, node.y + textHeight/2);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new GraphGeneratorGUI();
        });
    }
}