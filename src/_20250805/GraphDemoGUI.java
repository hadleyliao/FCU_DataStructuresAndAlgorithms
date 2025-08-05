/**********************************
 * 對應課程: Chapter 7
 * CourseWork1: 圖論基本應用1(ChatGPT)
 **********************************/

package _20250805;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

class Graph {
    private final java.util.Map<String, java.util.List<Edge>> adjList = new java.util.HashMap<>();

    static class Edge {
        String to;
        int weight;
        Edge(String to, int weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    public void addNode(String node) {
        adjList.putIfAbsent(node, new java.util.ArrayList<Edge>());
    }

    public void addEdge(String from, String to, int weight) {
        addNode(from);
        addNode(to);
        adjList.get(from).add(new Edge(to, weight));
        adjList.get(to).add(new Edge(from, weight)); // 無向圖
    }

    public java.util.List<String> getNodes() {
        return new java.util.ArrayList<String>(adjList.keySet());
    }

    public java.util.List<Edge> getEdges(String node) {
        return adjList.getOrDefault(node, new java.util.ArrayList<Edge>());
    }

    // Dijkstra 最短路徑
    public java.util.List<String> shortestPath(String start, String end) {
        java.util.Map<String, Integer> dist = new java.util.HashMap<>();
        java.util.Map<String, String> prev = new java.util.HashMap<>();
        java.util.PriorityQueue<NodeDist> pq = new java.util.PriorityQueue<>(Comparator.comparingInt(nd -> nd.dist));
        for (String node : adjList.keySet()) {
            dist.put(node, Integer.MAX_VALUE);
        }
        dist.put(start, 0);
        pq.add(new NodeDist(start, 0));
        while (!pq.isEmpty()) {
            NodeDist nd = pq.poll();
            String u = nd.node;
            int d = nd.dist;
            // 跳過非最短距離的節點
            if (d > dist.get(u)) continue;
            for (Edge e : getEdges(u)) {
                int alt = dist.get(u) + e.weight;
                if (alt < dist.get(e.to)) {
                    dist.put(e.to, alt);
                    prev.put(e.to, u);
                    pq.add(new NodeDist(e.to, alt));
                }
            }
        }
        java.util.List<String> path = new java.util.ArrayList<>();
        String curr = end;
        while (curr != null && prev.containsKey(curr)) {
            path.add(0, curr);
            curr = prev.get(curr);
        }
        if (start.equals(end)) path.add(start);
        else if (!path.isEmpty()) path.add(0, start);
        return path;
    }
    static class NodeDist {
        String node;
        int dist;
        NodeDist(String node, int dist) {
            this.node = node;
            this.dist = dist;
        }
    }

    // Graph 類別新增 DFS 與 BFS 方法
    public java.util.List<String> dfs(String start) {
        java.util.List<String> result = new java.util.ArrayList<>();
        java.util.Set<String> visited = new java.util.HashSet<>();
        dfsHelper(start, visited, result);
        return result;
    }
    private void dfsHelper(String node, java.util.Set<String> visited, java.util.List<String> result) {
        if (visited.contains(node)) return;
        visited.add(node);
        result.add(node);
        for (Edge e : getEdges(node)) {
            dfsHelper(e.to, visited, result);
        }
    }
    public java.util.List<String> bfs(String start) {
        java.util.List<String> result = new java.util.ArrayList<>();
        java.util.Set<String> visited = new java.util.HashSet<>();
        java.util.Queue<String> queue = new java.util.LinkedList<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            String node = queue.poll();
            result.add(node);
            for (Edge e : getEdges(node)) {
                if (!visited.contains(e.to)) {
                    visited.add(e.to);
                    queue.add(e.to);
                }
            }
        }
        return result;
    }
}

public class GraphDemoGUI extends JFrame {
    private Graph graph = new Graph();
    private JPanel graphPanel;
    private JComboBox<String> startBox, endBox;
    private JTextArea resultArea;
    private List<String> highlightPath = new ArrayList<>(); // 新增屬性儲存高亮路徑

    public GraphDemoGUI() {
        setTitle("Graph 應用展示");
        setSize(700, 600); // 調整視窗大小
        setMinimumSize(new Dimension(600, 500)); // 限制最小視窗
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 節點與邊初始化
        graph.addEdge("A", "B", 4);
        graph.addEdge("A", "C", 2);
        graph.addEdge("B", "C", 1);
        graph.addEdge("B", "D", 5);
        graph.addEdge("C", "D", 8);
        graph.addEdge("C", "E", 10);
        graph.addEdge("D", "E", 2);
        graph.addEdge("D", "Z", 6);
        graph.addEdge("E", "Z", 3);

        // 圖形放大並置中
        graphPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGraph(g);
            }
        };
        graphPanel.setPreferredSize(new Dimension(600, 350));
        graphPanel.setBackground(Color.WHITE);
        add(graphPanel, BorderLayout.CENTER);

        // 下方輸入及按鈕合理佈局
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 0;
        controlPanel.add(new JLabel("起點:"), gbc);
        gbc.gridx++;
        startBox = new JComboBox<>(graph.getNodes().toArray(new String[0]));
        controlPanel.add(startBox, gbc);
        gbc.gridx++;
        controlPanel.add(new JLabel("終點:"), gbc);
        gbc.gridx++;
        endBox = new JComboBox<>(graph.getNodes().toArray(new String[0]));
        controlPanel.add(endBox, gbc);
        gbc.gridx++;
        JButton findBtn = new JButton("查詢最短路徑");
        controlPanel.add(findBtn, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        controlPanel.add(new JLabel("節點數:"), gbc);
        gbc.gridx++;
        JTextField nodeField = new JTextField("5", 3);
        controlPanel.add(nodeField, gbc);
        gbc.gridx++;
        controlPanel.add(new JLabel("邊數:"), gbc);
        gbc.gridx++;
        JTextField edgeField = new JTextField("7", 3);
        controlPanel.add(edgeField, gbc);
        gbc.gridx++;
        JButton autoGenBtn = new JButton("自動產生圖");
        controlPanel.add(autoGenBtn, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        controlPanel.add(new JLabel("DFS/BFS起點:"), gbc);
        gbc.gridx++;
        JTextField searchStartField = new JTextField("A", 3);
        controlPanel.add(searchStartField, gbc);
        gbc.gridx++;
        JButton dfsBtn = new JButton("DFS遍歷");
        controlPanel.add(dfsBtn, gbc);
        gbc.gridx++;
        JButton bfsBtn = new JButton("BFS遍歷");
        controlPanel.add(bfsBtn, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 5;
        resultArea = new JTextArea(3, 50);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        controlPanel.add(scrollPane, gbc);

        add(controlPanel, BorderLayout.SOUTH);

        findBtn.addActionListener(e -> {
            String start = (String) startBox.getSelectedItem();
            String end = (String) endBox.getSelectedItem();
            java.util.List<String> path = graph.shortestPath(start, end);
            if (path.size() < 2) {
                resultArea.setText("無路徑");
            } else {
                resultArea.setText("最短路徑: " + String.join(" -> ", path));
            }
            graphPanel.repaint();
        });

        autoGenBtn.addActionListener(e -> {
            int nodeCount, edgeCount;
            try {
                nodeCount = Integer.parseInt(nodeField.getText());
                edgeCount = Integer.parseInt(edgeField.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "請輸入正確的數字");
                return;
            }
            if (nodeCount < 2 || edgeCount < 1 || edgeCount > nodeCount * (nodeCount-1) / 2) {
                JOptionPane.showMessageDialog(this, "節點或邊數不合理");
                return;
            }
            // 清空原圖
            graph = new Graph();
            // 產生節點
            List<String> nodes = new ArrayList<>();
            for (int i = 0; i < nodeCount; i++) {
                String name = String.valueOf((char)('A' + i));
                graph.addNode(name);
                nodes.add(name);
            }
            // 隨機產生邊
            Set<String> used = new HashSet<>();
            Random rand = new Random();
            int added = 0;
            while (added < edgeCount) {
                int a = rand.nextInt(nodeCount);
                int b = rand.nextInt(nodeCount);
                if (a == b) continue;
                String key = nodes.get(a) + "," + nodes.get(b);
                String key2 = nodes.get(b) + "," + nodes.get(a);
                if (used.contains(key) || used.contains(key2)) continue;
                int w = rand.nextInt(10) + 1;
                graph.addEdge(nodes.get(a), nodes.get(b), w);
                used.add(key);
                added++;
            }
            // 更新下拉選單
            startBox.setModel(new DefaultComboBoxModel<>(graph.getNodes().toArray(new String[0])));
            endBox.setModel(new DefaultComboBoxModel<>(graph.getNodes().toArray(new String[0])));
            resultArea.setText("");
            graphPanel.repaint();
        });
        dfsBtn.addActionListener(e -> {
            String start = searchStartField.getText().trim();
            if (!graph.getNodes().contains(start)) {
                resultArea.setText("起點不存在");
                graphPanel.repaint();
                return;
            }
            java.util.List<String> result = graph.dfs(start);
            resultArea.setText("DFS遍歷: " + String.join(" -> ", result));
            // 在圖上高亮 DFS 路徑
            highlightPath = result;
            graphPanel.repaint();
        });
        bfsBtn.addActionListener(e -> {
            String start = searchStartField.getText().trim();
            if (!graph.getNodes().contains(start)) {
                resultArea.setText("起點不存在");
                graphPanel.repaint();
                return;
            }
            java.util.List<String> result = graph.bfs(start);
            resultArea.setText("BFS遍歷: " + String.join(" -> ", result));
            // 在圖上高亮 BFS 路徑
            highlightPath = result;
            graphPanel.repaint();
        });
    }

    private void drawGraph(Graphics g) {
        Map<String, Point> nodePos = new HashMap<>();
        String[] nodes = graph.getNodes().toArray(new String[0]);
        int cx = 350, cy = 220, r = 150;
        for (int i = 0; i < nodes.length; i++) {
            double angle = 2 * Math.PI * i / nodes.length;
            int x = cx + (int)(r * Math.cos(angle));
            int y = cy + (int)(r * Math.sin(angle));
            nodePos.put(nodes[i], new Point(x, y));
        }
        // 畫邊
        for (String from : nodes) {
            for (Graph.Edge e : graph.getEdges(from)) {
                if (from.compareTo(e.to) < 0) { // 避免重複畫
                    Point p1 = nodePos.get(from);
                    Point p2 = nodePos.get(e.to);
                    g.setColor(Color.GRAY);
                    g.drawLine(p1.x, p1.y, p2.x, p2.y);
                    g.setColor(Color.BLACK);
                    int mx = (p1.x + p2.x) / 2;
                    int my = (p1.y + p2.y) / 2;
                    g.drawString(String.valueOf(e.weight), mx, my);
                }
            }
        }
        // 畫節點
        for (String node : nodes) {
            Point p = nodePos.get(node);
            g.setColor(Color.CYAN);
            g.fillOval(p.x - 20, p.y - 20, 40, 40);
            g.setColor(Color.BLACK);
            g.drawOval(p.x - 20, p.y - 20, 40, 40);
            g.drawString(node, p.x - 7, p.y + 5);
        }
        // 畫最短路徑
        String start = (String) startBox.getSelectedItem();
        String end = (String) endBox.getSelectedItem();
        java.util.List<String> path = graph.shortestPath(start, end);
        for (int i = 1; i < path.size(); i++) {
            Point p1 = nodePos.get(path.get(i-1));
            Point p2 = nodePos.get(path.get(i));
            g.setColor(Color.RED);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
        // 畫 DFS/BFS 高亮路徑
        if (!highlightPath.isEmpty()) {
            g.setColor(Color.GREEN);
            for (int i = 1; i < highlightPath.size(); i++) {
                Point p1 = nodePos.get(highlightPath.get(i-1));
                Point p2 = nodePos.get(highlightPath.get(i));
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GraphDemoGUI().setVisible(true);
        });
    }
}
