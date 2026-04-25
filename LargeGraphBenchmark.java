import java.util.*;
import java.io.*;

// =============================================================================
// LargeGraphBenchmark.java
//
// Loads a real-world weighted undirected graph and runs Prim and Kruskal on it,
// reporting running time (ns) and memory usage (bytes).
//
// INPUT FORMAT (a single text file, e.g. roadNet.txt):
//   - Lines starting with '#' are skipped (SNAP-style comments).
//   - Each remaining line is "u v" or "u v w".
//     If only "u v" is given, we synthesise a weight of 1 (so it still works
//     for unweighted SNAP datasets like roadNet-PA).
//   - Vertex IDs may be any non-negative integers; we re-map them to 0..N-1.
//   - The graph is treated as undirected; duplicate (u,v)/(v,u) edges are
//     collapsed.
//
// SUGGESTED DATASETS (download as plain .txt):
//   - SNAP roadNet-PA (Pennsylvania road network): ~1.09M nodes, ~1.54M edges
//     https://snap.stanford.edu/data/roadNet-PA.html
//   - SNAP roadNet-CA (California road network):   ~1.97M nodes, ~2.77M edges
//   - DIMACS USA road networks (have real distances as weights)
//
// USAGE:
//   javac LargeGraphBenchmark.java
//   java -Xmx4g LargeGraphBenchmark roadNet-PA.txt
//
// =============================================================================
public class LargeGraphBenchmark {

    // ------------------------------------------------------------------
    // Adjacency-list node (same shape as in GraphAlgorithms.java)
    // ------------------------------------------------------------------
    static class Node {
        int v, w;
        Node next;
        Node(int v, int w, Node n) { this.v = v; this.w = w; this.next = n; }
    }

    // ------------------------------------------------------------------
    // Edge for Kruskal
    // ------------------------------------------------------------------
    static class Edge {
        int u, v, w;
        Edge(int u, int v, int w) { this.u = u; this.v = v; this.w = w; }
    }

    // ------------------------------------------------------------------
    // Union-Find with path compression + union by rank
    // ------------------------------------------------------------------
    static int[] parent, rank_;
    static int find(int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]]; // path compression (iterative halving)
            x = parent[x];
        }
        return x;
    }
    static boolean union(int a, int b) {
        int ra = find(a), rb = find(b);
        if (ra == rb) return false;
        if (rank_[ra] < rank_[rb])      parent[ra] = rb;
        else if (rank_[ra] > rank_[rb]) parent[rb] = ra;
        else { parent[rb] = ra; rank_[ra]++; }
        return true;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java -Xmx4g LargeGraphBenchmark <file>");
            System.exit(1);
        }
        String filename = args[0];

        // ---- 1. Read file, build edge list, re-map vertex IDs to 0..N-1 ----
        System.out.println("Reading " + filename + " ...");
        long t0 = System.nanoTime();
        Map<Long,Integer> idMap = new HashMap<>();
        List<int[]> raw = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line; int dupes = 0;
        // Use a HashSet to dedupe undirected edges
        HashSet<Long> seen = new HashSet<>();
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] p = line.split("\\s+");
            if (p.length < 2) continue;
            long u = Long.parseLong(p[0]);
            long v = Long.parseLong(p[1]);
            int w = (p.length >= 3) ? Integer.parseInt(p[2]) : 1;
            if (u == v) continue; // skip self-loops
            Integer ui = idMap.get(u);
            if (ui == null) { ui = idMap.size(); idMap.put(u, ui); }
            Integer vi = idMap.get(v);
            if (vi == null) { vi = idMap.size(); idMap.put(v, vi); }
            // Canonical key for undirected dedup
            long a = Math.min(ui, vi), b = Math.max(ui, vi);
            long key = (a << 32) | (b & 0xffffffffL);
            if (!seen.add(key)) { dupes++; continue; }
            raw.add(new int[]{ui, vi, w});
        }
        br.close();
        long tRead = System.nanoTime() - t0;
        int V = idMap.size();
        int E = raw.size();
        System.out.printf("Loaded %,d vertices, %,d undirected edges (skipped %,d duplicate directions) in %.2f s%n",
                V, E, dupes, tRead / 1e9);

        // ---- 2. Build adjacency list (for Prim) ----
        Node[] adj = new Node[V];
        for (int[] e : raw) {
            adj[e[0]] = new Node(e[1], e[2], adj[e[0]]);
            adj[e[1]] = new Node(e[0], e[2], adj[e[1]]);
        }

        // ---- 3. Run Prim ----
        System.gc(); // try to give Prim a clean baseline
        long memBefore = usedMem();
        long t1 = System.nanoTime();
        long primWeight = prim(adj, V);
        long primTime = System.nanoTime() - t1;
        long memAfter = usedMem();
        long primMem = Math.max(0, memAfter - memBefore);

        System.out.println();
        System.out.println("=== Prim ===");
        System.out.printf("  Time:   %.3f s (%,d ns)%n", primTime / 1e9, primTime);
        System.out.printf("  MST weight: %,d%n", primWeight);
        System.out.printf("  Memory delta during run: %.1f MB%n", primMem / (1024.0*1024));

        // Drop adjacency list before Kruskal so we measure Kruskal in isolation
        adj = null;
        System.gc();

        // ---- 4. Run Kruskal ----
        Edge[] edges = new Edge[E];
        for (int i = 0; i < E; i++) edges[i] = new Edge(raw.get(i)[0], raw.get(i)[1], raw.get(i)[2]);
        raw = null;
        System.gc();
        memBefore = usedMem();
        long t2 = System.nanoTime();
        long kruskalWeight = kruskal(edges, V);
        long kruskalTime = System.nanoTime() - t2;
        memAfter = usedMem();
        long kruskalMem = Math.max(0, memAfter - memBefore);

        System.out.println();
        System.out.println("=== Kruskal ===");
        System.out.printf("  Time:   %.3f s (%,d ns)%n", kruskalTime / 1e9, kruskalTime);
        System.out.printf("  MST weight: %,d%n", kruskalWeight);
        System.out.printf("  Memory delta during run: %.1f MB%n", kruskalMem / (1024.0*1024));

        // ---- 5. Sanity check ----
        System.out.println();
        if (primWeight == kruskalWeight) {
            System.out.println("OK – both algorithms produced the same MST weight.");
        } else {
            System.out.println("WARNING – different MST weights! "
                    + "(Possible if graph is disconnected; both then return a spanning forest.)");
        }
    }

    // ------------------------------------------------------------------
    // Prim with binary heap (java.util.PriorityQueue), starting from vertex 0
    // Returns total MST weight. Uses long to avoid overflow on huge graphs.
    // ------------------------------------------------------------------
    static long prim(Node[] adj, int V) {
        boolean[] inMST = new boolean[V];
        int[] dist = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[0] = 0;
        PriorityQueue<long[]> pq = new PriorityQueue<>((a,b) -> Long.compare(a[0], b[0]));
        pq.add(new long[]{0L, 0L});
        long total = 0;
        int taken = 0;
        while (!pq.isEmpty() && taken < V) {
            long[] top = pq.poll();
            int u = (int) top[1];
            if (inMST[u]) continue;
            inMST[u] = true;
            total += top[0];
            taken++;
            for (Node n = adj[u]; n != null; n = n.next) {
                if (!inMST[n.v] && n.w < dist[n.v]) {
                    dist[n.v] = n.w;
                    pq.add(new long[]{(long) n.w, (long) n.v});
                }
            }
        }
        return total;
    }

    // ------------------------------------------------------------------
    // Kruskal with Arrays.sort (Dual-Pivot QuickSort) + union-find
    // ------------------------------------------------------------------
    static long kruskal(Edge[] edges, int V) {
        Arrays.sort(edges, (a,b) -> Integer.compare(a.w, b.w));
        parent = new int[V];
        rank_  = new int[V];
        for (int i = 0; i < V; i++) parent[i] = i;
        long total = 0;
        int taken = 0;
        for (Edge e : edges) {
            if (union(e.u, e.v)) {
                total += e.w;
                if (++taken == V - 1) break;
            }
        }
        return total;
    }

    static long usedMem() {
        Runtime r = Runtime.getRuntime();
        return r.totalMemory() - r.freeMemory();
    }
}
