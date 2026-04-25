# Graph Algorithms - MST Implementation

A comprehensive Java implementation of **Minimum Spanning Tree (MST)** algorithms with performance benchmarking on large real-world graphs.

## Overview

This project implements and compares two classic MST algorithms:
- **Prim's Algorithm** - Greedy approach using a binary heap (PriorityQueue)
- **Kruskal's Algorithm** - Greedy approach using edge sorting and Union-Find

Both algorithms are tested on large weighted undirected graphs with detailed performance metrics including execution time and memory usage.

## Features

- ✅ **Prim's Algorithm** with binary heap optimization
- ✅ **Kruskal's Algorithm** with Union-Find (path compression + union by rank)
- ✅ **Depth-First Search (DFS)** and **Breadth-First Search (BFS)** implementations
- ✅ Real-world graph benchmarking capability
- ✅ Automatic vertex ID re-mapping (handles non-sequential IDs)
- ✅ Duplicate edge detection and handling
- ✅ Memory and time performance reporting

## Components

### GraphAlgorithms.java
Core graph algorithms library containing:
- Adjacency-list based graph representation
- Prim's MST algorithm
- Kruskal's MST algorithm
- DFS and BFS traversals
- Minimum heap implementation

### Kruskal.java
Standalone implementation of Kruskal's algorithm with Union-Find data structure and detailed testing.

### LargeGraphBenchmark.java
Benchmarking tool that:
- Loads real-world weighted graphs from text files
- Runs both Prim and Kruskal algorithms
- Reports execution time (nanoseconds) and memory usage
- Validates that both algorithms produce the same MST weight

## Usage

### Basic Compilation
```bash
javac *.java
```

### Running Benchmarks
```bash
# With up to 4GB heap memory
java -Xmx4g LargeGraphBenchmark <graph_file.txt>

# Example with included test data
java -Xmx4g LargeGraphBenchmark roadNet-PA.txt
```

## Input Format

Graph files should be in plain text format:
- Lines starting with `#` are treated as comments (skipped)
- Each data line: `u v [weight]`
  - `u`: source vertex ID
  - `v`: destination vertex ID
  - `weight`: edge weight (optional, defaults to 1 if omitted)

**Example:**
```
# Road network of Pennsylvania
123 456 5
456 789 3
123 789 7
```

## Supported Datasets

- **SNAP roadNet-PA** (Pennsylvania): ~1.09M nodes, ~1.54M edges
- **SNAP roadNet-CA** (California): ~1.97M nodes, ~2.77M edges
- **DIMACS USA road networks** (with real distances as weights)

Download from: https://snap.stanford.edu/data/

## Example Output

```
Reading roadNet-PA.txt ...
Loaded 1,088,092 vertices, 1,541,514 undirected edges (skipped 0 duplicate directions) in 8.45 s

=== Prim ===
  Time:   45.234 s (45,234,000,000 ns)
  MST weight: 1,541,513
  Memory delta during run: 125.3 MB

=== Kruskal ===
  Time:   12.567 s (12,567,000,000 ns)
  MST weight: 1,541,513
  Memory delta during run: 256.7 MB

OK – both algorithms produced the same MST weight.
```

## Algorithm Comparison

| Algorithm | Time Complexity | Space Complexity | Best Use Case |
|-----------|-----------------|------------------|---------------|
| **Prim** | O((V + E) log V) | O(V + E) | Dense graphs, incremental MST |
| **Kruskal** | O(E log E) | O(V + E) | Sparse graphs, parallel-friendly |

## Implementation Notes

- **Union-Find**: Uses path compression (iterative halving) and union by rank for O(α(n)) amortized operations
- **Adjacency List**: Memory-efficient representation for sparse graphs
- **Deduplication**: Automatically handles duplicate edges in undirected graphs
- **ID Remapping**: Supports non-sequential vertex IDs from datasets

## Requirements

- Java 8 or higher
- Sufficient heap memory for large graphs (recommended: 4GB+)

## Author

Graph Algorithms Assignment - DF, BF, and MST Implementation
