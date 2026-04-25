import java.util.*;
import java.io.*;

// =============================================================================
// Kruskal.java
// Contains: Kruskal's MST algorithm with Union-Find (disjoint sets)
//           Optional: path compression in find()
//           Edges sorted with Arrays.sort (QuickSort-based in Java stdlib)
// Usage: java Kruskal
//   -> prompts for filename (same format as GraphAlgorithms.java)
// =============================================================================

public class Kruskal {

    // =========================================================================
    // Edge: a weighted undirected edge (u, v, weight)
    // =========================================================================
    static class Edge implements Comparable<Edge> {
        int u, v, weight;
        String uName, vName;

        Edge(int u, int v, int w, String un, String vn) {
            this.u = u; this.v = v; this.weight = w;
            this.uName = un; this.vName = vn;
        }

        // Natural ordering: by weight ascending (used by Arrays.sort / QuickSort)
        @Override
        public int compareTo(Edge other) {
            return Integer.compare(this.weight, other.weight);
        }

        @Override
        public String toString() {
            return uName + " -- " + vName + " (" + weight + ")";
        }
    }

    // =========================================================================
    // UnionFind: disjoint-set forest with union by rank and path compression
    // =========================================================================
    static class UnionFind {
        private int[] parent;
        private int[] rank;
        private int count; // number of components

        UnionFind(int n) {
            parent = new int[n];
            rank   = new int[n];
            count  = n;
            for (int i = 0; i < n; i++) parent[i] = i; // each vertex is its own root
        }

        // find with PATH COMPRESSION (simple recursive version)
        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // path compression
            }
            return parent[x];
        }

        // union by rank
        boolean union(int x, int y) {
            int rx = find(x);
            int ry = find(y);
            if (rx == ry) return false; // already in same set -> would form a cycle

            // attach smaller-rank tree under larger-rank tree
            if (rank[rx] < rank[ry])      parent[rx] = ry;
            else if (rank[rx] > rank[ry]) parent[ry] = rx;
            else { parent[ry] = rx; rank[rx]++; }

            count--;
            return true;
        }

        int components() { return count; }

        // Print all current sets (for step-by-step output)
        void printSets(int n, String[] names) {
            Map<Integer, List<String>> sets = new LinkedHashMap<>();
            for (int i = 0; i < n; i++) {
                int root = find(i);
                sets.computeIfAbsent(root, k -> new ArrayList<>()).add(names[i]);
            }
            System.out.print("    Sets: ");
            for (List<String> s : sets.values()) {
                System.out.print("{" + String.join(",", s) + "} ");
            }
            System.out.println();
        }
    }

    // =========================================================================
    // KruskalMST: main algorithm
    // =========================================================================
    static void kruskalMST(Edge[] edges, int V, String[] names) {
        System.out.println("\n=== Kruskal's MST ===");

        // Step 1: Sort all edges by weight (Java's Arrays.sort uses Dual-Pivot QuickSort)
        Arrays.sort(edges);

        System.out.println("Edges sorted by weight:");
        for (Edge e : edges) {
            System.out.println("  " + e);
        }

        // Step 2: Process edges in order
        UnionFind uf = new UnionFind(V);
        List<Edge> mst = new ArrayList<>();
        int totalWeight = 0;

        System.out.println("\nProcessing edges:");
        int step = 0;
        for (Edge e : edges) {
            step++;
            System.out.println("\n  Step " + step + ": Consider " + e);

            int ru = uf.find(e.u);
            int rv = uf.find(e.v);

            if (ru == rv) {
                // Same component – adding this edge would create a cycle
                System.out.println("    -> REJECTED (would form a cycle: both in component rooted at "
                        + names[ru] + ")");
            } else {
                // Different components – add to MST
                uf.union(e.u, e.v);
                mst.add(e);
                totalWeight += e.weight;
                System.out.println("    -> ACCEPTED  (merges components)");
            }
            uf.printSets(V, names);

            // MST complete when it has V-1 edges
            if (mst.size() == V - 1) break;
        }

        // Step 3: Print result
        System.out.println("\n  Kruskal MST Edges:");
        for (Edge e : mst) {
            System.out.println("    " + e);
        }
        System.out.println("  Total MST weight: " + totalWeight);
    }

    // =========================================================================
    // readGraph: same file format as GraphAlgorithms.java
    //   Line 1:  numVertices
    //   Line 2:  label0 label1 ... (space-separated)
    //   Lines 3+: u v weight
    // Returns edge array and names array.
    // =========================================================================
    static Object[] readGraph(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        int V = Integer.parseInt(br.readLine().trim());
        String[] names = br.readLine().trim().split("\\s+");

        List<Edge> edgeList = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            int u = Integer.parseInt(parts[0]);
            int v = Integer.parseInt(parts[1]);
            int w = Integer.parseInt(parts[2]);
            edgeList.add(new Edge(u, v, w, names[u], names[v]));
        }
        br.close();
        return new Object[]{ edgeList.toArray(new Edge[0]), V, names };
    }

    // =========================================================================
    // main
    // =========================================================================
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter graph filename: ");
        String filename = sc.nextLine().trim();
        sc.close();

        Object[] data = readGraph(filename);
        Edge[]   edges = (Edge[]) data[0];
        int      V     = (int)    data[1];
        String[] names = (String[]) data[2];

        // Print edge list
        System.out.println("Graph has " + V + " vertices and " + edges.length + " edges.");
        kruskalMST(edges, V, names);
    }
}
