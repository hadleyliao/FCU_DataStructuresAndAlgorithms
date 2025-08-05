import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MSTSolver extends JFrame {

    // 邊的類別
    static class Edge implements Comparable<Edge> {
        int src, dest, weight;

        Edge(int src, int dest, int weight) {
            this.src = src;
            this.dest = dest;
            this.weight = weight;
        }

        @Override
        public int compareTo(Edge other) {
            return Integer.compare(this.weight, other.weight);
        }

        @Override
        public String toString() {
            return "節點" + (src + 1) + "-節點" + (dest + 1) + " (權重: " + weight + ")";
        }
    }

    // Union-Find 資料結構
    static class UnionFind {
        int[] parent, rank;

        UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }

        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }

        boolean union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);

            if (rootX != rootY) {
                if (rank[rootX] < rank[rootY]) {
                    parent[rootX] = rootY;
                } else if (rank[rootX] > rank[rootY]) {
                    parent[rootY] = rootX;
                } else {
                    parent[rootY] = rootX;
                    rank[rootX]++;
                }
                return true;
            }
            return false;
        }
    }

    // GUI 組件
    private JSpinner nodeCountSpinner;
    private JTable edgeTable;
    private DefaultTableModel edgeTableModel;
    private JTextArea resultArea;
    private JComboBox<String> algorithmCombo;
    private List<Edge> currentEdges;

    public MSTSolver() {
        currentEdges = new ArrayList<>();
        initializeGUI();
        loadDefaultExample();
    }

    private void initializeGUI() {
        setTitle("最小生成樹求解器 (MST Solver)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 創建主面板
        createTopPanel();
        createCenterPanel();
        createBottomPanel();

        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("圖形設定"));

        topPanel.add(new JLabel("節點數量:"));
        nodeCountSpinner = new JSpinner(new SpinnerNumberModel(5, 2, 20, 1));
        topPanel.add(nodeCountSpinner);

        JButton updateTableButton = new JButton("更新表格");
        JButton loadExampleButton = new JButton("載入範例");
        JButton clearButton = new JButton("清空");
        updateTableButton.addActionListener(e -> updateEdgeTable());
        loadExampleButton.addActionListener(e -> loadDefaultExample());
        clearButton.addActionListener(e -> clearTable());
        topPanel.add(updateTableButton);
        topPanel.add(loadExampleButton);
        topPanel.add(clearButton);

        add(topPanel, BorderLayout.NORTH);
    }

    private void createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());

        // 左側 - 邊輸入表格
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("邊的設定"));

        String[] columnNames = {"起點", "終點", "權重"};
        edgeTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return Integer.class;
            }
        };

        edgeTable = new JTable(edgeTableModel);
        edgeTable.setPreferredScrollableViewportSize(new Dimension(300, 200));
        // 文字致中
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < edgeTable.getColumnCount(); i++) {
            edgeTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane tableScrollPane = new JScrollPane(edgeTable);
        leftPanel.add(tableScrollPane, BorderLayout.CENTER);

        JPanel tableButtonPanel = new JPanel(new FlowLayout());
        JButton addRowButton = new JButton("新增邊");
        JButton deleteRowButton = new JButton("刪除選中邊");
        addRowButton.addActionListener(e -> addNewRow());
        deleteRowButton.addActionListener(e -> deleteSelectedRow());
        tableButtonPanel.add(addRowButton);
        tableButtonPanel.add(deleteRowButton);
        leftPanel.add(tableButtonPanel, BorderLayout.SOUTH);

        // 右側 - 結果顯示
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("計算結果"));

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultScrollPane.setPreferredSize(new Dimension(400, 300));

        rightPanel.add(resultScrollPane, BorderLayout.CENTER);

        // 分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(350);
        centerPanel.add(splitPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("演算法選擇"));

        bottomPanel.add(new JLabel("選擇演算法:"));
        algorithmCombo = new JComboBox<>(new String[]{"Kruskal演算法", "Prim演算法", "兩種演算法比較"});
        bottomPanel.add(algorithmCombo);

        JButton calculateButton = new JButton("計算最小生成樹");
        calculateButton.addActionListener(e -> calculateMST());
        bottomPanel.add(calculateButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void updateEdgeTable() {
        int nodeCount = (Integer) nodeCountSpinner.getValue();
        edgeTableModel.setRowCount(0);

        // 為新的節點數量添加一些空行
        for (int i = 0; i < Math.max(5, nodeCount * 2); i++) {
            edgeTableModel.addRow(new Object[]{null, null, null});
        }
    }

    private void addNewRow() {
        edgeTableModel.addRow(new Object[]{null, null, null});
    }

    private void deleteSelectedRow() {
        int selectedRow = edgeTable.getSelectedRow();
        if (selectedRow >= 0) {
            edgeTableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "請先選擇要刪除的行", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearTable() {
        edgeTableModel.setRowCount(0);
        resultArea.setText("");
    }

    private void loadDefaultExample() {
        nodeCountSpinner.setValue(5);
        edgeTableModel.setRowCount(0);

        // 載入你原始圖的資料
        Object[][] defaultEdges = {
                {1, 2, 2},
                {2, 3, 8},
                {1, 4, 4},
                {2, 4, 3},
                {2, 3, 6},
                {4, 5, 15},
                {3, 5, 5},
                {3, 4, 10}
        };

        for (Object[] edge : defaultEdges) {
            edgeTableModel.addRow(edge);
        }

        // 添加一些空行供用戶編輯
        for (int i = 0; i < 5; i++) {
            edgeTableModel.addRow(new Object[]{null, null, null});
        }
    }

    private List<Edge> getEdgesFromTable() {
        List<Edge> edges = new ArrayList<>();
        int nodeCount = (Integer) nodeCountSpinner.getValue();

        for (int i = 0; i < edgeTableModel.getRowCount(); i++) {
            Object src = edgeTableModel.getValueAt(i, 0);
            Object dest = edgeTableModel.getValueAt(i, 1);
            Object weight = edgeTableModel.getValueAt(i, 2);

            if (src != null && dest != null && weight != null) {
                try {
                    int srcNode = (Integer) src - 1; // 轉換為0-based
                    int destNode = (Integer) dest - 1;
                    int edgeWeight = (Integer) weight;

                    if (srcNode >= 0 && srcNode < nodeCount &&
                            destNode >= 0 && destNode < nodeCount &&
                            srcNode != destNode && edgeWeight > 0) {
                        edges.add(new Edge(srcNode, destNode, edgeWeight));
                    }
                } catch (ClassCastException e) {
                    // 忽略無效的行
                }
            }
        }

        return edges;
    }

    private void calculateMST() {
        currentEdges = getEdgesFromTable();
        int nodeCount = (Integer) nodeCountSpinner.getValue();

        if (currentEdges.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請先輸入邊的資料！", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 檢查圖是否連通
        if (!isGraphConnected(currentEdges, nodeCount)) {
            JOptionPane.showMessageDialog(this, "圖不連通，無法計算最小生成樹！", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder result = new StringBuilder();
        String selectedAlgorithm = (String) algorithmCombo.getSelectedItem();

        if (selectedAlgorithm.equals("Kruskal演算法") || selectedAlgorithm.equals("兩種演算法比較")) {
            result.append("=== Kruskal演算法 ===\n");
            List<Edge> kruskalResult = kruskalMST(new ArrayList<>(currentEdges), nodeCount, result);
            displayResult(kruskalResult, "Kruskal", result);
        }

        if (selectedAlgorithm.equals("Prim演算法") || selectedAlgorithm.equals("兩種演算法比較")) {
            if (selectedAlgorithm.equals("兩種演算法比較")) {
                result.append("\n\n");
            }
            result.append("=== Prim演算法 ===\n");
            int[][] graph = buildAdjacencyMatrix(currentEdges, nodeCount);
            List<Edge> primResult = primMST(graph, nodeCount, result);
            displayResult(primResult, "Prim", result);
        }

        resultArea.setText(result.toString());
        resultArea.setCaretPosition(0);
    }

    private boolean isGraphConnected(List<Edge> edges, int nodeCount) {
        UnionFind uf = new UnionFind(nodeCount);
        for (Edge edge : edges) {
            uf.union(edge.src, edge.dest);
        }

        int root = uf.find(0);
        for (int i = 1; i < nodeCount; i++) {
            if (uf.find(i) != root) {
                return false;
            }
        }
        return true;
    }

    private List<Edge> kruskalMST(List<Edge> edges, int vertices, StringBuilder log) {
        Collections.sort(edges);
        UnionFind uf = new UnionFind(vertices);
        List<Edge> mst = new ArrayList<>();

        log.append("排序後的邊：\n");
        for (Edge e : edges) {
            log.append("  ").append(e).append("\n");
        }
        log.append("\n執行過程：\n");

        for (Edge edge : edges) {
            if (uf.union(edge.src, edge.dest)) {
                mst.add(edge);
                log.append("✓ 加入: ").append(edge).append("\n");
                if (mst.size() == vertices - 1) break;
            } else {
                log.append("✗ 跳過: ").append(edge).append(" (會形成環)\n");
            }
        }

        return mst;
    }

    private int[][] buildAdjacencyMatrix(List<Edge> edges, int nodeCount) {
        int[][] graph = new int[nodeCount][nodeCount];
        for (Edge e : edges) {
            if (graph[e.src][e.dest] == 0 || graph[e.src][e.dest] > e.weight) {
                graph[e.src][e.dest] = e.weight;
                graph[e.dest][e.src] = e.weight;
            }
        }
        return graph;
    }

    private List<Edge> primMST(int[][] graph, int vertices, StringBuilder log) {
        boolean[] inMST = new boolean[vertices];
        int[] key = new int[vertices];
        int[] parent = new int[vertices];

        Arrays.fill(key, Integer.MAX_VALUE);
        key[0] = 0;
        parent[0] = -1;

        List<Edge> mst = new ArrayList<>();
        log.append("從節點1開始\n\n");

        for (int count = 0; count < vertices - 1; count++) {
            int u = -1;
            for (int v = 0; v < vertices; v++) {
                if (!inMST[v] && (u == -1 || key[v] < key[u])) {
                    u = v;
                }
            }

            inMST[u] = true;

            if (parent[u] != -1) {
                Edge edge = new Edge(parent[u], u, key[u]);
                mst.add(edge);
                log.append("✓ 加入: ").append(edge).append("\n");
            }

            for (int v = 0; v < vertices; v++) {
                if (graph[u][v] != 0 && !inMST[v] && graph[u][v] < key[v]) {
                    parent[v] = u;
                    key[v] = graph[u][v];
                }
            }
        }

        return mst;
    }

    private void displayResult(List<Edge> mst, String algorithmName, StringBuilder result) {
        result.append("\n").append(algorithmName).append("演算法結果：\n");
        result.append("最小生成樹包含的邊：\n");

        int totalWeight = 0;
        for (Edge e : mst) {
            result.append("  ").append(e).append("\n");
            totalWeight += e.weight;
        }

        result.append("\n總權重: ").append(totalWeight).append("\n");
        result.append("邊數: ").append(mst.size()).append("\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("設定外觀失敗: " + e.getMessage());
            }

            new MSTSolver().setVisible(true);
        });
    }
}