/*******************************************************************************************
 * 對應課程: Chapter 7
 * CourseWork1: 實做簡單版及快速版找關節點方法(Claude)
 * 目標: 比較二者效能
 * 回答問題: 在圖形規模在多大的時候(多少個邊(Edge)、多少個頂點(Vertex))用快速版比較好, 其他用簡單版比較好
 * 評估標準: 時間效能
 * 依程式實測評估結果進行決定
 * 請準備PPT報告你的最後決定
 * 時間: 一小時
 * 將PPT上傳至教學平台作業區”8/11上課報告一”
 *******************************************************************************************/

package _20250811;

import java.util.*;

public class ArticulationPointComparison {
    // 簡單版：移除每個節點後檢查圖是否連通
    public static Set<Integer> findArticulationPointsSimple(int n, List<List<Integer>> graph) {
        Set<Integer> result = new HashSet<>();
        for (int v = 0; v < n; v++) {
            boolean[] visited = new boolean[n];
            visited[v] = true; // 移除節點 v
            int start = (v == 0) ? 1 : 0;
            dfsSimple(start, visited, graph);
            for (int i = 0; i < n; i++) {
                if (!visited[i]) {
                    result.add(v);
                    break;
                }
            }
        }
        return result;
    }

    private static void dfsSimple(int u, boolean[] visited, List<List<Integer>> graph) {
        visited[u] = true;
        for (int v : graph.get(u)) {
            if (!visited[v]) {
                dfsSimple(v, visited, graph);
            }
        }
    }

    // 快速版：Tarjan 演算法
    public static Set<Integer> findArticulationPointsTarjan(int n, List<List<Integer>> graph) {
        Set<Integer> result = new HashSet<>();
        int[] disc = new int[n];
        int[] low = new int[n];
        int[] parent = new int[n];
        Arrays.fill(disc, -1);
        Arrays.fill(low, -1);
        Arrays.fill(parent, -1);
        for (int i = 0; i < n; i++) {
            if (disc[i] == -1) {
                dfsTarjan(i, 0, disc, low, parent, graph, result);
            }
        }
        return result;
    }

    private static void dfsTarjan(int u, int time, int[] disc, int[] low, int[] parent, List<List<Integer>> graph, Set<Integer> result) {
        disc[u] = low[u] = ++time;
        int children = 0;
        for (int v : graph.get(u)) {
            if (disc[v] == -1) {
                children++;
                parent[v] = u;
                dfsTarjan(v, time, disc, low, parent, graph, result);
                low[u] = Math.min(low[u], low[v]);
                if (parent[u] == -1 && children > 1) result.add(u);
                if (parent[u] != -1 && low[v] >= disc[u]) result.add(u);
            } else if (v != parent[u]) {
                low[u] = Math.min(low[u], disc[v]);
            }
        }
    }

    // 隨機產生無向圖
    public static List<List<Integer>> generateRandomGraph(int n, int m) {
        List<List<Integer>> graph = new ArrayList<>();
        for (int i = 0; i < n; i++) graph.add(new ArrayList<>());
        Set<String> edges = new HashSet<>();
        Random rand = new Random();
        while (edges.size() < m) {
            int u = rand.nextInt(n);
            int v = rand.nextInt(n);
            if (u != v) {
                String key = u < v ? u + "," + v : v + "," + u;
                if (!edges.contains(key)) {
                    edges.add(key);
                    graph.get(u).add(v);
                    graph.get(v).add(u);
                }
            }
        }
        return graph;
    }

    public static void main(String[] args) {
        int[] ns = {10, 20, 50, 100, 200, 500, 1000};
        int[] ms = {15, 40, 120, 300, 800, 2000, 5000};
        System.out.printf("%-8s %-8s %-18s %-18s\n", "Vertex", "Edge", "Simple(ns)", "Tarjan(ns)");
        // 輸出到CSV
        try (java.io.PrintWriter writer = new java.io.PrintWriter("articulation_point_result.csv")) {
            writer.println("Vertex,Edge,Simple(ns),Tarjan(ns)");
            for (int i = 0; i < ns.length; i++) {
                int n = ns[i], m = ms[i];
                List<List<Integer>> graph = generateRandomGraph(n, m);
                long t1 = System.nanoTime();
                findArticulationPointsSimple(n, graph);
                long t2 = System.nanoTime();
                findArticulationPointsTarjan(n, graph);
                long t3 = System.nanoTime();
                System.out.printf("%-8d %-8d %-18d %-18d\n", n, m, t2-t1, t3-t2);
                writer.printf("%d,%d,%d,%d\n", n, m, t2-t1, t3-t2);
            }
        } catch (Exception e) {
            System.out.println("寫入CSV失敗: " + e.getMessage());
        }
    }
}
