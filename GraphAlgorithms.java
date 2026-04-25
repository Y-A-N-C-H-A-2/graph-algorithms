import java.util.*;
import java.io.*;

// =============================================================================
// GraphAlgorithms.java
// Contains: Graph (adjacency list), Heap (min-heap), DFS, BFS, Prim's MST
// Usage: java GraphAlgorithms
//   -> prompts for filename and starting vertex number
// =============================================================================

public class GraphAlgorithms {

    // =========================================================================
    // Node: represents one entry in an adjacency list
    // =========================================================================
    static class Node {
        int vertex;   // destination vertex index
        int weight;   // edge weight
        Node next;    // next node in linked list

        Node(int v, int w) {
            this.vertex = v;
            this.weight = w;
            this.next = null;
        }
    }

    // =========================================================================
    // Graph: adjacency-list graph with DFS, BFS and Prim
    // =========================================================================
    static class Graph {
        private int V;          // number of vertices
        private Node[] adj;     // adjacency list heads
        private String[] name;  // vertex names (labels)

        // ------------------------------------------------------------------
        // Constructor: allocate arrays
        // ------------------------------------------------------------------
        Graph(int v) {
            this.V = v;
            adj  = new Node[v];
            name = new String[v];
        }

        // ------------------------------------------------------------------
        // addEdge: undirected – adds both directions
        // ------------------------------------------------------------------
        void addEdge(int u, int v, int w) {
            // insert at front of u's list
            Node nu = new Node(v, w);
            nu.next = adj[u];
            adj[u] = nu;

            // insert at front of v's list
            Node nv = new Node(u, w);
            nv.next = adj[v];
            adj[v] = nv;
        }

        // ------------------------------------------------------------------
        // setName: assign a label to vertex index i
        // ------------------------------------------------------------------
        void setName(int i, String n) {
            name[i] = n;
        }

        // ------------------------------------------------------------------
        // printGraph: display the adjacency list
        // ------------------------------------------------------------------
        void printGraph() {
            System.out.println("\n=== Adjacency List ===");
            for (int i = 0; i < V; i++) {
                System.out.print(name[i] + ": ");
                Node cur = adj[i];
                while (cur != null) {
                    System.out.print(name[cur.vertex] + "(" + cur.weight + ") ");
                    cur = cur.next;
                }
                System.out.println();
            }
        }

        // ==================================================================
        // DEPTH-FIRST SEARCH  (Cormen's version – recursive)
        // Colour coding: WHITE=0, GREY=1, BLACK=2
        // ==================================================================
        private int[] colour;
        private int[] parent;
        private int time;

        void DFS(int start) {
            System.out.println("\n=== Depth First Search (Cormen's recursive) starting from " + name[start] + " ===");
            colour = new int[V];
            parent = new int[V];
            time   = 0;
            int[] d = new int[V]; // discovery time
            int[] f = new int[V]; // finish time

            // Initialise all vertices WHITE
            Arrays.fill(colour, 0);
            Arrays.fill(parent, -1);

            // Visit start vertex first, then any remaining unvisited (forest)
            dfsVisit(start, d, f);
            for (int u = 0; u < V; u++) {
                if (colour[u] == 0) dfsVisit(u, d, f);
            }

            System.out.println("\nDFS discovery/finish times:");
            for (int i = 0; i < V; i++) {
                String par = parent[i] == -1 ? "none" : name[parent[i]];
                System.out.printf("  %s: d=%d  f=%d  parent=%s%n", name[i], d[i], f[i], par);
            }
        }

        private void dfsVisit(int u, int[] d, int[] f) {
            colour[u] = 1; // GREY – discovered
            time++;
            d[u] = time;
            System.out.println("  Discovered " + name[u] + " at time " + time);

            Node cur = adj[u];
            while (cur != null) {
                int v = cur.vertex;
                if (colour[v] == 0) {          // WHITE – tree edge
                    parent[v] = u;
                    System.out.println("    Tree edge: " + name[u] + " -> " + name[v]);
                    dfsVisit(v, d, f);
                }
                cur = cur.next;
            }

            colour[u] = 2; // BLACK – finished
            time++;
            f[u] = time;
            System.out.println("  Finished   " + name[u] + " at time " + time);
        }

        // ==================================================================
        // BREADTH-FIRST SEARCH  (Cormen's version – uses a queue)
        // ==================================================================
        void BFS(int start) {
            System.out.println("\n=== Breadth First Search (Cormen's queue-based) starting from " + name[start] + " ===");

            int[] colour = new int[V];   // WHITE=0, GREY=1, BLACK=2
            int[] dist   = new int[V];
            int[] par    = new int[V];

            Arrays.fill(colour, 0);
            Arrays.fill(dist,   Integer.MAX_VALUE);
            Arrays.fill(par,   -1);

            // Initialise source
            colour[start] = 1;   // GREY
            dist[start]   = 0;

            Queue<Integer> queue = new LinkedList<>();
            queue.add(start);
            System.out.println("  Enqueued source: " + name[start]);

            while (!queue.isEmpty()) {
                int u = queue.poll();
                System.out.println("  Dequeued: " + name[u] + "  (dist=" + dist[u] + ")");

                Node cur = adj[u];
                while (cur != null) {
                    int v = cur.vertex;
                    if (colour[v] == 0) {        // WHITE – not yet visited
                        colour[v] = 1;           // GREY
                        dist[v]   = dist[u] + 1;
                        par[v]    = u;
                        queue.add(v);
                        System.out.println("    Enqueued neighbour: " + name[v]
                                + "  (dist=" + dist[v] + ", parent=" + name[u] + ")");
                    }
                    cur = cur.next;
                }
                colour[u] = 2;  // BLACK – done
            }

            System.out.println("\nBFS distances from " + name[start] + ":");
            for (int i = 0; i < V; i++) {
                String p = par[i] == -1 ? "none" : name[par[i]];
                System.out.printf("  %s: dist=%d  parent=%s%n", name[i], dist[i], p);
            }
        }

        // ==================================================================
        // PRIM'S MST  (adjacency list + min-heap / priority queue)
        // ==================================================================
        void Prim(int start) {
            System.out.println("\n=== Prim's MST starting from " + name[start] + " ===");

            int[] distP  = new int[V];   // minimum edge weight to tree
            int[] parentP = new int[V];  // MST parent
            boolean[] inMST = new boolean[V]; // true once vertex is in MST

            Arrays.fill(distP,   Integer.MAX_VALUE);
            Arrays.fill(parentP, -1);

            distP[start] = 0;

            // Priority queue: [distance, vertex]
            // Java PriorityQueue ordered by distance (min-heap)
            PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
            pq.add(new int[]{0, start});

            System.out.println("  Initial heap entry: (" + name[start] + ", dist=0)");
            System.out.printf("  %-20s %-35s %-35s%n", "Step", "dist[] array", "parent[] array");

            int step = 0;
            while (!pq.isEmpty()) {
                int[] top = pq.poll();
                int u = top[1];

                if (inMST[u]) continue; // stale entry
                inMST[u] = true;

                // Print current state
                step++;
                StringBuilder distStr = new StringBuilder();
                StringBuilder parStr  = new StringBuilder();
                for (int i = 0; i < V; i++) {
                    distStr.append(name[i]).append("=")
                           .append(distP[i] == Integer.MAX_VALUE ? "∞" : distP[i]).append(" ");
                    parStr.append(name[i]).append("=")
                          .append(parentP[i] == -1 ? "-" : name[parentP[i]]).append(" ");
                }
                System.out.printf("  Step %-3d  Added %-4s  dist[]: %s%n", step, name[u], distStr);
                System.out.printf("  %24s parent[]: %s%n", "", parStr);

                // Relax neighbours
                Node cur = adj[u];
                while (cur != null) {
                    int v = cur.vertex;
                    int w = cur.weight;
                    if (!inMST[v] && w < distP[v]) {
                        distP[v]   = w;
                        parentP[v] = u;
                        pq.add(new int[]{w, v});
                        System.out.println("    Relaxed: " + name[v]
                                + "  new dist=" + w + "  via " + name[u]);
                    }
                    cur = cur.next;
                }
            }

            // Print MST edges and total weight
            System.out.println("\n  MST Edges:");
            int total = 0;
            for (int i = 0; i < V; i++) {
                if (parentP[i] != -1) {
                    System.out.printf("    %s -- %s  weight=%d%n",
                            name[parentP[i]], name[i], distP[i]);
                    total += distP[i];
                }
            }
            System.out.println("  Total MST weight: " + total);
        }
    }

    // =========================================================================
    // MinHeap: binary min-heap used as a standalone helper (index-aware)
    // Stores [key, vertexIndex] pairs; supports decreaseKey.
    // =========================================================================
    static class MinHeap {
        private int[][] heap;    // [key, vertex]
        private int[] pos;       // pos[v] = index of vertex v in heap
        private int size;

        MinHeap(int capacity) {
            heap = new int[capacity][2];
            pos  = new int[capacity];
            Arrays.fill(pos, -1);
            size = 0;
        }

        boolean isEmpty() { return size == 0; }

        void insert(int key, int vertex) {
            heap[size][0] = key;
            heap[size][1] = vertex;
            pos[vertex]   = size;
            size++;
            siftUp(size - 1);
        }

        int[] extractMin() {
            int[] min = {heap[0][0], heap[0][1]};
            pos[heap[0][1]] = -1;
            size--;
            if (size > 0) {
                heap[0] = heap[size];
                pos[heap[0][1]] = 0;
                siftDown(0);
            }
            return min;
        }

        void decreaseKey(int vertex, int newKey) {
            int i = pos[vertex];
            if (i == -1 || newKey >= heap[i][0]) return;
            heap[i][0] = newKey;
            siftUp(i);
        }

        boolean contains(int vertex) { return pos[vertex] != -1; }

        private void siftUp(int i) {
            while (i > 0) {
                int parent = (i - 1) / 2;
                if (heap[parent][0] > heap[i][0]) {
                    swap(i, parent);
                    i = parent;
                } else break;
            }
        }

        private void siftDown(int i) {
            while (true) {
                int smallest = i;
                int l = 2*i+1, r = 2*i+2;
                if (l < size && heap[l][0] < heap[smallest][0]) smallest = l;
                if (r < size && heap[r][0] < heap[smallest][0]) smallest = r;
                if (smallest == i) break;
                swap(i, smallest);
                i = smallest;
            }
        }

        private void swap(int i, int j) {
            pos[heap[i][1]] = j;
            pos[heap[j][1]] = i;
            int[] tmp = heap[i];
            heap[i] = heap[j];
            heap[j] = tmp;
        }
    }

    // =========================================================================
    // readGraph: parse adjacency-list text file
    //
    // Expected format:
    //   Line 1:  numVertices
    //   Line 2:  vertexLabel0 vertexLabel1 ... (space-separated, 0-indexed)
    //   Lines 3+: u v weight   (one undirected edge per line, 0-indexed vertex numbers)
    //
    // Example wGraph1.txt:
    //   13
    //   A B C D E F G H I J K L M
    //   0 1 1
    //   0 2 6
    //   ...
    // =========================================================================
    static Graph readGraph(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        int V = Integer.parseInt(br.readLine().trim());
        Graph g = new Graph(V);

        String[] labels = br.readLine().trim().split("\\s+");
        for (int i = 0; i < V; i++) g.setName(i, labels[i]);

        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            int u = Integer.parseInt(parts[0]);
            int v = Integer.parseInt(parts[1]);
            int w = Integer.parseInt(parts[2]);
            g.addEdge(u, v, w);
        }
        br.close();
        return g;
    }

    // =========================================================================
    // main
    // =========================================================================
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter graph filename: ");
        String filename = sc.nextLine().trim();

        Graph g = readGraph(filename);
        g.printGraph();

        System.out.print("Enter starting vertex number (0-indexed): ");
        int start = Integer.parseInt(sc.nextLine().trim());

        g.DFS(start);
        g.BFS(start);
        g.Prim(start);

        sc.close();
    }
}
