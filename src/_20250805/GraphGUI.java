/***********************************
 * 對應課程: Chapter 7
 * CourseWork1: 圖論基本應用2(Claude)
 ***********************************/

package _20250805;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

// 節點類別
class Node {
    int x, y;
    String label;
    boolean visited = false;
    Color color = Color.CYAN;

    public Node(int x, int y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }

    public boolean contains(int px, int py) {
        return Math.sqrt((px - x) * (px - x) + (py - y) * (py - y)) <= 25;
    }
}

// 邊類別
class Edge {
    Node from, to;
    int weight;
    Color color = Color.BLACK;

    public Edge(Node from, Node to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }
}

// 主要的圖GUI類別
public class GraphGUI extends JFrame {
    private java.util.List<Node> nodes;
    private java.util.List<Edge> edges;
    private GraphPanel graphPanel;
    private Node selectedNode = null;
    private Node draggedNode = null;
    private boolean addNodeMode = false;
    private boolean addEdgeMode = false;
    private JComboBox<String> startNodeCombo;
    private Node edgeStartNode = null;

    public GraphGUI() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        setTitle("圖結構GUI應用程式");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 創建繪圖面板
        graphPanel = new GraphPanel();
        add(graphPanel, BorderLayout.CENTER);

        // 創建控制面板
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.WEST);

        // 創建狀態欄
        JLabel statusBar = new JLabel(" ✔ 準備就緒 - 點擊按鈕開始操作");
        add(statusBar, BorderLayout.SOUTH);

        setSize(900, 700);
        setLocationRelativeTo(null);

        // 添加一些初始節點和邊作為示例
        initializeExampleGraph();
    }

    private void initializeExampleGraph() {
        Node a = new Node(150, 150, "A");
        Node b = new Node(300, 100, "B");
        Node c = new Node(450, 150, "C");
        Node d = new Node(200, 300, "D");
        Node e = new Node(350, 300, "E");

        nodes.add(a);
        nodes.add(b);
        nodes.add(c);
        nodes.add(d);
        nodes.add(e);

        edges.add(new Edge(a, b, 5));
        edges.add(new Edge(b, c, 3));
        edges.add(new Edge(a, d, 7));
        edges.add(new Edge(d, e, 2));
        edges.add(new Edge(c, e, 4));
        edges.add(new Edge(b, e, 6));
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("控制面板"));
        panel.setPreferredSize(new Dimension(200, 0));

        // 節點操作
        JPanel nodePanel = new JPanel(new GridLayout(3, 1, 5, 5));
        nodePanel.setBorder(BorderFactory.createTitledBorder("節點操作"));

        JButton addNodeBtn = new JButton("添加節點");
        addNodeBtn.addActionListener(e -> {
            addNodeMode = true;
            addEdgeMode = false;
            edgeStartNode = null;
            resetNodeColors();
        });

        JButton deleteNodeBtn = new JButton("刪除節點");
        deleteNodeBtn.addActionListener(e -> deleteSelectedNode());

        JButton clearBtn = new JButton("清除所有");
        clearBtn.addActionListener(e -> {
            nodes.clear();
            edges.clear();
            selectedNode = null;
            graphPanel.repaint();
        });

        nodePanel.add(addNodeBtn);
        nodePanel.add(deleteNodeBtn);
        nodePanel.add(clearBtn);

        // 邊操作
        JPanel edgePanel = new JPanel(new GridLayout(2, 1, 5, 5));
        edgePanel.setBorder(BorderFactory.createTitledBorder("邊操作"));

        JButton addEdgeBtn = new JButton("添加邊");
        addEdgeBtn.addActionListener(e -> {
            addEdgeMode = true;
            addNodeMode = false;
            edgeStartNode = null;
            resetNodeColors();
            JOptionPane.showMessageDialog(this, "請依序點擊兩個節點來創建邊");
        });

        JButton deleteEdgeBtn = new JButton("刪除邊");
        deleteEdgeBtn.addActionListener(e -> deleteSelectedEdge());

        edgePanel.add(addEdgeBtn);
        edgePanel.add(deleteEdgeBtn);

        // 算法演示
        JPanel algoPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        algoPanel.setBorder(BorderFactory.createTitledBorder("算法演示"));

        // 起點選擇
        JPanel startNodePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        startNodePanel.add(new JLabel("起點:"));
        startNodeCombo = new JComboBox<>();
        updateStartNodeCombo(startNodeCombo);
        startNodePanel.add(startNodeCombo);

        JButton dfsBtn = new JButton("深度優先搜索");
        dfsBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "深度優先搜索節點將以 🔴 紅色顯示。", "提示", JOptionPane.INFORMATION_MESSAGE);
            performDFS(startNodeCombo);
        });

        JButton bfsBtn = new JButton("廣度優先搜索");
        bfsBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "廣度優先搜索節點將以 🟢 綠色顯示。", "提示", JOptionPane.INFORMATION_MESSAGE);
            performBFS(startNodeCombo);
        });

        JButton dfsWithResultBtn = new JButton("DFS顯示結果");
        dfsWithResultBtn.addActionListener(e -> performDFSWithResult(startNodeCombo));

        JButton bfsWithResultBtn = new JButton("BFS顯示結果");
        bfsWithResultBtn.addActionListener(e -> performBFSWithResult(startNodeCombo));

        JButton shortestPathBtn = new JButton("最短路徑");
        shortestPathBtn.addActionListener(e -> findShortestPath());

        algoPanel.add(startNodePanel);
        algoPanel.add(dfsBtn);
        algoPanel.add(bfsBtn);
        algoPanel.add(dfsWithResultBtn);
        algoPanel.add(bfsWithResultBtn);
        algoPanel.add(shortestPathBtn);

        // 自動生成面板
        JPanel generatePanel = new JPanel(new GridLayout(4, 1, 5, 5));
        generatePanel.setBorder(BorderFactory.createTitledBorder("自動生成"));

        JPanel nodeCountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        nodeCountPanel.add(new JLabel("節點數量:"));
        JSpinner nodeCountSpinner = new JSpinner(new SpinnerNumberModel(5, 3, 20, 1));
        nodeCountPanel.add(nodeCountSpinner);

        JPanel edgeCountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        edgeCountPanel.add(new JLabel("邊數量:"));
        JSpinner edgeCountSpinner = new JSpinner(new SpinnerNumberModel(7, 0, 50, 1));
        edgeCountPanel.add(edgeCountSpinner);

        JButton generateBtn = new JButton("自動生成圖");
        generateBtn.addActionListener(e -> {
            int nodeCount = (Integer) nodeCountSpinner.getValue();
            int edgeCount = (Integer) edgeCountSpinner.getValue();
            generateRandomGraph(nodeCount, edgeCount);
        });

        generatePanel.add(nodeCountPanel);
        generatePanel.add(edgeCountPanel);
        generatePanel.add(generateBtn);
        generatePanel.add(Box.createVerticalGlue());

        // 重置按鈕
        JButton resetBtn = new JButton("重置顏色");
        resetBtn.addActionListener(e -> {
            resetNodeColors();
            resetEdgeColors();
            graphPanel.repaint();
        });

        panel.add(nodePanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(edgePanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(generatePanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(algoPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(resetBtn);

        return panel;
    }

    private void deleteSelectedNode() {
        if (selectedNode != null) {
            nodes.remove(selectedNode);
            edges.removeIf(edge -> edge.from == selectedNode || edge.to == selectedNode);
            selectedNode = null;
            graphPanel.repaint();
        } else {
            JOptionPane.showMessageDialog(this, "請先選擇一個節點");
        }
    }

    private void deleteSelectedEdge() {
        if (selectedNode != null) {
            boolean removed = edges.removeIf(edge ->
                    edge.from == selectedNode || edge.to == selectedNode);
            if (removed) {
                graphPanel.repaint();
            } else {
                JOptionPane.showMessageDialog(this, "所選節點沒有相關邊");
            }
        } else {
            JOptionPane.showMessageDialog(this, "請先選擇一個節點");
        }
    }

    private void updateStartNodeCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        combo.addItem("選擇起點");
        for (Node node : nodes) {
            combo.addItem(node.label);
        }
    }

    private void performDFS(JComboBox<String> startCombo) {
        String selectedStart = (String) startCombo.getSelectedItem();
        if (selectedStart == null || selectedStart.equals("選擇起點")) {
            if (nodes.isEmpty()) return;
            selectedStart = nodes.get(0).label;
        }
        final String startLabel = selectedStart;
        Node startNode = nodes.stream()
                .filter(n -> n.label.equals(startLabel))
                .findFirst().orElse(nodes.get(0));

        resetNodeColors();
        Set<Node> visited = new HashSet<>();
        List<Node> dfsOrder = new ArrayList<>();

        dfsRecursive(startNode, visited, dfsOrder);

        javax.swing.Timer timer = new javax.swing.Timer(1000, null);
        final int[] index = {0};
        timer.addActionListener(e -> {
            if (index[0] < dfsOrder.size()) {
                dfsOrder.get(index[0]).color = Color.RED;
                graphPanel.repaint();
                index[0]++;
            } else {
                timer.stop();
            }
        });
        timer.start();
    }

    private void performBFS(JComboBox<String> startCombo) {
        String selectedStart = (String) startCombo.getSelectedItem();
        if (selectedStart == null || selectedStart.equals("選擇起點")) {
            if (nodes.isEmpty()) return;
            selectedStart = nodes.get(0).label;
        }
        final String startLabel = selectedStart;
        Node startNode = nodes.stream()
                .filter(n -> n.label.equals(startLabel))
                .findFirst().orElse(nodes.get(0));

        resetNodeColors();
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();
        List<Node> bfsOrder = new ArrayList<>();

        queue.offer(startNode);
        visited.add(startNode);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            bfsOrder.add(current);

            for (Edge edge : edges) {
                Node neighbor = null;
                if (edge.from == current && !visited.contains(edge.to)) {
                    neighbor = edge.to;
                } else if (edge.to == current && !visited.contains(edge.from)) {
                    neighbor = edge.from;
                }

                if (neighbor != null) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        javax.swing.Timer timer = new javax.swing.Timer(1000, null);
        final int[] index = {0};
        timer.addActionListener(e -> {
            if (index[0] < bfsOrder.size()) {
                bfsOrder.get(index[0]).color = Color.GREEN;
                graphPanel.repaint();
                index[0]++;
            } else {
                timer.stop();
            }
        });
        timer.start();
    }

    private void performDFSWithResult(JComboBox<String> startCombo) {
        String selectedStart = (String) startCombo.getSelectedItem();
        if (selectedStart == null || selectedStart.equals("選擇起點")) {
            if (nodes.isEmpty()) return;
            selectedStart = nodes.get(0).label;
        }
        final String startLabel = selectedStart;
        Node startNode = nodes.stream()
                .filter(n -> n.label.equals(startLabel))
                .findFirst().orElse(nodes.get(0));

        resetNodeColors();
        Set<Node> visited = new HashSet<>();
        List<Node> dfsOrder = new ArrayList<>();
        List<String> dfsSteps = new ArrayList<>();

        dfsRecursiveWithSteps(startNode, visited, dfsOrder, dfsSteps, "");

        // 顯示遍歷結果
        StringBuilder result = new StringBuilder();
        result.append("深度優先搜索 (DFS) 結果\n");
        result.append("起點: ").append(startNode.label).append("\n\n");
        result.append("遍歷順序: ");
        for (int i = 0; i < dfsOrder.size(); i++) {
            result.append(dfsOrder.get(i).label);
            if (i < dfsOrder.size() - 1) result.append(" → ");
        }
        result.append("\n\n詳細步驟:\n");
        for (int i = 0; i < dfsSteps.size(); i++) {
            result.append((i + 1)).append(". ").append(dfsSteps.get(i)).append("\n");
        }

        // 高亮顯示遍歷路徑
        for (int i = 0; i < dfsOrder.size(); i++) {
            dfsOrder.get(i).color = new Color(255, 100 + i * 10, 100 + i * 10);
        }
        graphPanel.repaint();

        // 顯示結果對話框
        JTextArea textArea = new JTextArea(result.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "DFS搜索結果", JOptionPane.INFORMATION_MESSAGE);
    }

    private void performBFSWithResult(JComboBox<String> startCombo) {
        String selectedStart = (String) startCombo.getSelectedItem();
        if (selectedStart == null || selectedStart.equals("選擇起點")) {
            if (nodes.isEmpty()) return;
            selectedStart = nodes.get(0).label;
        }
        final String startLabel = selectedStart;
        Node startNode = nodes.stream()
                .filter(n -> n.label.equals(startLabel))
                .findFirst().orElse(nodes.get(0));

        resetNodeColors();
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();
        List<Node> bfsOrder = new ArrayList<>();
        List<String> bfsSteps = new ArrayList<>();

        queue.offer(startNode);
        visited.add(startNode);
        bfsSteps.add("將起點 " + startNode.label + " 加入佇列");

        int step = 1;
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            bfsOrder.add(current);
            bfsSteps.add("從佇列取出節點 " + current.label + " 進行訪問");

            List<Node> neighbors = new ArrayList<>();
            for (Edge edge : edges) {
                Node neighbor = null;
                if (edge.from == current && !visited.contains(edge.to)) {
                    neighbor = edge.to;
                } else if (edge.to == current && !visited.contains(edge.from)) {
                    neighbor = edge.from;
                }

                if (neighbor != null) {
                    neighbors.add(neighbor);
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }

            if (!neighbors.isEmpty()) {
                StringBuilder neighborStr = new StringBuilder();
                for (int i = 0; i < neighbors.size(); i++) {
                    neighborStr.append(neighbors.get(i).label);
                    if (i < neighbors.size() - 1) neighborStr.append(", ");
                }
                bfsSteps.add("將相鄰未訪問節點 [" + neighborStr + "] 加入佇列");
            }
            step++;
        }

        // 顯示遍歷結果
        StringBuilder result = new StringBuilder();
        result.append("廣度優先搜索 (BFS) 結果\n");
        result.append("起點: ").append(startNode.label).append("\n\n");
        result.append("遍歷順序: ");
        for (int i = 0; i < bfsOrder.size(); i++) {
            result.append(bfsOrder.get(i).label);
            if (i < bfsOrder.size() - 1) result.append(" → ");
        }
        result.append("\n\n詳細步驟:\n");
        for (int i = 0; i < bfsSteps.size(); i++) {
            result.append((i + 1)).append(". ").append(bfsSteps.get(i)).append("\n");
        }

        // 高亮顯示遍歷路徑
        for (int i = 0; i < bfsOrder.size(); i++) {
            bfsOrder.get(i).color = new Color(100 + i * 10, 255, 100 + i * 10);
        }
        graphPanel.repaint();

        // 顯示結果對話框
        JTextArea textArea = new JTextArea(result.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "BFS搜索結果", JOptionPane.INFORMATION_MESSAGE);
    }

    private void dfsRecursiveWithSteps(Node node, Set<Node> visited, List<Node> order, List<String> steps, String path) {
        visited.add(node);
        order.add(node);

        String currentPath = path.isEmpty() ? node.label : path + " → " + node.label;
        steps.add("訪問節點 " + node.label + " (路徑: " + currentPath + ")");

        for (Edge edge : edges) {
            Node neighbor = null;
            if (edge.from == node && !visited.contains(edge.to)) {
                neighbor = edge.to;
            } else if (edge.to == node && !visited.contains(edge.from)) {
                neighbor = edge.from;
            }

            if (neighbor != null) {
                steps.add("從 " + node.label + " 深入探索相鄰節點 " + neighbor.label);
                dfsRecursiveWithSteps(neighbor, visited, order, steps, currentPath);
                steps.add("回溯到節點 " + node.label);
            }
        }
    }

    private void dfsRecursive(Node node, Set<Node> visited, List<Node> order) {
        visited.add(node);
        order.add(node);

        for (Edge edge : edges) {
            if (edge.from == node && !visited.contains(edge.to)) {
                dfsRecursive(edge.to, visited, order);
            } else if (edge.to == node && !visited.contains(edge.from)) {
                dfsRecursive(edge.from, visited, order);
            }
        }
    }

    private void findShortestPath() {
        if (nodes.size() < 2) {
            JOptionPane.showMessageDialog(this, "需要至少兩個節點來計算最短路徑");
            return;
        }

        String[] nodeLabels = nodes.stream().map(n -> n.label).toArray(String[]::new);
        String startLabel = (String) JOptionPane.showInputDialog(this,
                "選擇起始節點:", "最短路徑", JOptionPane.QUESTION_MESSAGE,
                null, nodeLabels, nodeLabels[0]);

        String endLabel = (String) JOptionPane.showInputDialog(this,
                "選擇目標節點:", "最短路徑", JOptionPane.QUESTION_MESSAGE,
                null, nodeLabels, nodeLabels[1]);

        if (startLabel == null || endLabel == null) return;

        Node startNode = nodes.stream().filter(n -> n.label.equals(startLabel)).findFirst().orElse(null);
        Node endNode = nodes.stream().filter(n -> n.label.equals(endLabel)).findFirst().orElse(null);

        if (startNode != null && endNode != null) {
            List<Node> path = dijkstra(startNode, endNode);
            if (path != null) {
                resetNodeColors();
                resetEdgeColors();

                // 高亮顯示路徑
                for (Node node : path) {
                    node.color = Color.ORANGE;
                }

                // 高亮顯示路徑上的邊
                for (int i = 0; i < path.size() - 1; i++) {
                    Node from = path.get(i);
                    Node to = path.get(i + 1);
                    for (Edge edge : edges) {
                        if ((edge.from == from && edge.to == to) ||
                                (edge.from == to && edge.to == from)) {
                            edge.color = Color.ORANGE;
                            break;
                        }
                    }
                }

                graphPanel.repaint();

                int totalWeight = 0;
                for (int i = 0; i < path.size() - 1; i++) {
                    Node from = path.get(i);
                    Node to = path.get(i + 1);
                    for (Edge edge : edges) {
                        if ((edge.from == from && edge.to == to) ||
                                (edge.from == to && edge.to == from)) {
                            totalWeight += edge.weight;
                            break;
                        }
                    }
                }

                JOptionPane.showMessageDialog(this,
                        "最短路徑長度: " + totalWeight + "\n路徑已用橙色高亮顯示");
            } else {
                JOptionPane.showMessageDialog(this, "找不到路徑");
            }
        }
    }

    private List<Node> dijkstra(Node start, Node end) {
        Map<Node, Integer> distances = new HashMap<>();
        Map<Node, Node> previous = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>((a, b) ->
                distances.getOrDefault(a, Integer.MAX_VALUE) -
                        distances.getOrDefault(b, Integer.MAX_VALUE));

        for (Node node : nodes) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(start, 0);
        queue.offer(start);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current == end) {
                List<Node> path = new ArrayList<>();
                Node node = end;
                while (node != null) {
                    path.add(0, node);
                    node = previous.get(node);
                }
                return path;
            }

            for (Edge edge : edges) {
                Node neighbor = null;
                if (edge.from == current) {
                    neighbor = edge.to;
                } else if (edge.to == current) {
                    neighbor = edge.from;
                }

                if (neighbor != null) {
                    int newDist = distances.get(current) + edge.weight;
                    if (newDist < distances.get(neighbor)) {
                        distances.put(neighbor, newDist);
                        previous.put(neighbor, current);
                        queue.offer(neighbor);
                    }
                }
            }
        }

        return null;
    }

    private void generateRandomGraph(int nodeCount, int maxEdgeCount) {
        // 清除現有的圖
        nodes.clear();
        edges.clear();
        selectedNode = null;

        Random random = new Random();

        // 生成節點
        for (int i = 0; i < nodeCount; i++) {
            int x, y;
            boolean validPosition;
            int attempts = 0;

            do {
                x = 50 + random.nextInt(500);
                y = 50 + random.nextInt(400);
                validPosition = true;
                attempts++;

                // 確保新節點不會與現有節點重疊
                for (Node existingNode : nodes) {
                    double distance = Math.sqrt((x - existingNode.x) * (x - existingNode.x) +
                            (y - existingNode.y) * (y - existingNode.y));
                    if (distance < 80) {
                        validPosition = false;
                        break;
                    }
                }
            } while (!validPosition && attempts < 50);

            nodes.add(new Node(x, y, String.valueOf((char)('A' + i))));
        }

        // 計算最大可能的邊數（完全圖）
        int maxPossibleEdges = nodeCount * (nodeCount - 1) / 2;
        int actualEdgeCount = Math.min(maxEdgeCount, maxPossibleEdges);

        // 先確保圖是連通的 - 創建一個生成樹
        List<Node> unconnected = new ArrayList<>(nodes);
        List<Node> connected = new ArrayList<>();

        if (!unconnected.isEmpty()) {
            connected.add(unconnected.remove(random.nextInt(unconnected.size())));

            while (!unconnected.isEmpty()) {
                Node fromNode = connected.get(random.nextInt(connected.size()));
                Node toNode = unconnected.remove(random.nextInt(unconnected.size()));

                int weight = 1 + random.nextInt(10);
                edges.add(new Edge(fromNode, toNode, weight));
                connected.add(toNode);
            }
        }

        // 添加額外的隨機邊
        int currentEdgeCount = edges.size();
        Set<String> existingEdges = new HashSet<>();

        // 記錄已存在的邊
        for (Edge edge : edges) {
            String edgeKey1 = edge.from.label + "-" + edge.to.label;
            String edgeKey2 = edge.to.label + "-" + edge.from.label;
            existingEdges.add(edgeKey1);
            existingEdges.add(edgeKey2);
        }

        while (currentEdgeCount < actualEdgeCount) {
            Node fromNode = nodes.get(random.nextInt(nodes.size()));
            Node toNode = nodes.get(random.nextInt(nodes.size()));

            if (fromNode != toNode) {
                String edgeKey1 = fromNode.label + "-" + toNode.label;
                String edgeKey2 = toNode.label + "-" + fromNode.label;

                if (!existingEdges.contains(edgeKey1) && !existingEdges.contains(edgeKey2)) {
                    int weight = 1 + random.nextInt(10);
                    edges.add(new Edge(fromNode, toNode, weight));
                    existingEdges.add(edgeKey1);
                    existingEdges.add(edgeKey2);
                    currentEdgeCount++;
                }
            }
        }

        resetNodeColors();
        resetEdgeColors();
        updateStartNodeCombo(startNodeCombo);
        graphPanel.repaint();

        JOptionPane.showMessageDialog(this,
                String.format("已生成隨機圖:\n節點數: %d\n邊數: %d",
                        nodes.size(), edges.size()));
    }

    private void resetNodeColors() {
        for (Node node : nodes) {
            node.color = Color.CYAN;
        }
    }

    private void resetEdgeColors() {
        for (Edge edge : edges) {
            edge.color = Color.BLACK;
        }
    }
    class GraphPanel extends JPanel {
        public GraphPanel() {
            setBackground(Color.WHITE);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    handleMousePressed(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    draggedNode = null;
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (draggedNode != null) {
                        draggedNode.x = e.getX();
                        draggedNode.y = e.getY();
                        repaint();
                    }
                }
            });
        }

        private void handleMousePressed(MouseEvent e) {
            Node clickedNode = findNodeAt(e.getX(), e.getY());

            if (addNodeMode) {
                if (clickedNode == null) {
                    String label = JOptionPane.showInputDialog("輸入節點標籤:");
                    if (label != null && !label.trim().isEmpty()) {
                        nodes.add(new Node(e.getX(), e.getY(), label.trim()));
                        repaint();
                    }
                }
                addNodeMode = false;
            } else if (addEdgeMode) {
                if (clickedNode != null) {
                    if (edgeStartNode == null) {
                        edgeStartNode = clickedNode;
                        clickedNode.color = Color.YELLOW;
                        repaint();
                    } else if (clickedNode != edgeStartNode) {
                        String weightStr = JOptionPane.showInputDialog("輸入邊的權重:", "1");
                        if (weightStr != null) {
                            try {
                                int weight = Integer.parseInt(weightStr);
                                edges.add(new Edge(edgeStartNode, clickedNode, weight));
                                resetNodeColors();
                                repaint();
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(GraphGUI.this, "請輸入有效的數字");
                            }
                        }
                        edgeStartNode = null;
                        addEdgeMode = false;
                    }
                }
            } else {
                if (clickedNode != null) {
                    selectedNode = clickedNode;
                    draggedNode = clickedNode;

                    // 高亮選中的節點
                    resetNodeColors();
                    clickedNode.color = Color.PINK;
                    repaint();
                }
            }
        }

        private Node findNodeAt(int x, int y) {
            for (Node node : nodes) {
                if (node.contains(x, y)) {
                    return node;
                }
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 繪製邊
            for (Edge edge : edges) {
                g2d.setColor(edge.color);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(edge.from.x, edge.from.y, edge.to.x, edge.to.y);

                // 繪製權重
                int midX = (edge.from.x + edge.to.x) / 2;
                int midY = (edge.from.y + edge.to.y) / 2;
                g2d.setColor(Color.BLUE);
                g2d.drawString(String.valueOf(edge.weight), midX, midY);
            }

            // 繪製節點
            for (Node node : nodes) {
                g2d.setColor(node.color);
                g2d.fillOval(node.x - 25, node.y - 25, 50, 50);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(node.x - 25, node.y - 25, 50, 50);

                // 繪製標籤
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(node.label);
                int textHeight = fm.getHeight();
                g2d.drawString(node.label,
                        node.x - textWidth / 2,
                        node.y + textHeight / 4);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            new GraphGUI().setVisible(true);
        });
    }
}