
package de.george.navmap.util;

public class UnionFind {
	private int[] id; // id[i] = parents of node i
	private int[] sz; // sz[i] = size of node i
	private int count; // number of connected components

	public UnionFind(int n) {
		count = n;
		id = new int[n];
		sz = new int[n];
		for (int i = 0; i < n; ++i) {
			id[i] = i;
			sz[i] = 1;
		}
	}

	public int count() {
		return count;
	}

	public void union(int p, int q) {
		int rootp = find(p);
		int rootq = find(q);
		if (rootp == rootq) {
			return;
		}
		if (sz[rootp] < sz[rootq]) {
			id[rootp] = rootq;
			sz[rootq] += sz[rootp];
		} else {
			id[rootq] = rootp;
			sz[rootp] += sz[rootq];
		}
		count--;
	}

	public int find(int p) {
		while (id[p] != p) {
			id[p] = id[id[p]]; // path compression
			p = id[p];
		}
		return p;
	}

	public int size(int p) {
		return sz[find(p)];
	}

	public boolean connected(int p, int q) {
		return find(p) == find(q);
	}
}
