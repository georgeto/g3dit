package de.george.lrentnode.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import com.google.common.base.Function;

import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCVector;
import one.util.streamex.StreamEx;

public class AABBTree<T extends AABBTreePrimitive> {
	public static abstract class AABBNode {
		protected bCBox bounds;

		public AABBNode(bCBox bounds) {
			this.bounds = bounds;
		}

		public bCBox getBounds() {
			return bounds;
		}

		public abstract int getPrimitiveCount();

		public abstract boolean isLeaf();
	}

	public static class AABBLeafNode extends AABBNode {
		private int primitivesBegin;
		private int primitivesEnd;

		public AABBLeafNode(bCBox bounds, int primitivesBegin, int primitivesEnd) {
			super(bounds);
			this.primitivesBegin = primitivesBegin;
			this.primitivesEnd = primitivesEnd;
		}

		@Override
		public int getPrimitiveCount() {
			return primitivesEnd - primitivesBegin;
		}

		@Override
		public boolean isLeaf() {
			return true;
		}

		public int getPrimitivesBegin() {
			return primitivesBegin;
		}

		public int getPrimitivesEnd() {
			return primitivesEnd;
		}
	}

	public static class AABBSplitNode extends AABBNode {
		private AABBNode left;
		private AABBNode right;

		public AABBSplitNode(bCBox bounds, AABBNode left, AABBNode right) {
			super(bounds);
			this.left = left;
			this.right = right;
		}

		@Override
		public int getPrimitiveCount() {
			return left.getPrimitiveCount() + right.getPrimitiveCount();
		}

		@Override
		public boolean isLeaf() {
			return false;
		}

		public AABBNode getLeft() {
			return left;
		}

		public AABBNode getRight() {
			return right;
		}
	}

	private static class SearchEntry implements Comparable<SearchEntry> {
		private float sqrDistance;
		private AABBNode node;

		public SearchEntry(float sqrDistance, AABBNode node) {
			this.sqrDistance = sqrDistance;
			this.node = node;
		}

		@Override
		public int compareTo(SearchEntry o) {
			return Float.compare(sqrDistance, o.sqrDistance);
		}
	}

	private class ResultEntry implements Comparable<ResultEntry> {
		private float sqrDistance;
		private T primitive;

		public ResultEntry(float sqrDistance, T primitive) {
			this.sqrDistance = sqrDistance;
			this.primitive = primitive;
		}

		@Override
		public int compareTo(ResultEntry o) {
			return Float.compare(o.sqrDistance, sqrDistance);
		}
	}

	private List<T> primitives = new ArrayList<>();
	private int maxDepth;
	private int minSize;
	private AABBNode root = null;
	private boolean completed = false;

	public AABBTree() {
		this.maxDepth = 20;
		this.minSize = 2;
	}

	public AABBTree(int maxDepth, int minSize) {
		this.maxDepth = maxDepth;
		this.minSize = minSize;
	}

	public List<T> getPrimitives() {
		return Collections.unmodifiableList(primitives);
	}

	public boolean isCompleted() {
		return completed;
	}

	public AABBNode getRoot() {
		if (!isCompleted()) {
			throw new IllegalStateException("AABBTree is not completed.");
		}

		return root;
	}

	public void clear() {
		primitives.clear();
		root = null;
		completed = false;
	}

	public void insert(T primitive) {
		primitives.add(primitive);
		completed = false;
	}

	public void complete() {
		// Bestehenden Baum "freigeben"
		root = null;

		bCBox bounds = computeBounds(0, primitives.size());
		root = build(0, primitives.size(), bounds, 0);

		completed = true;

	}

	private bCBox computeBounds(int begin, int end) {
		bCBox bounds = new bCBox();
		for (int i = begin; i < end; i++) {
			bounds.merge(primitives.get(i).getBounds());
		}
		return bounds;
	}

	private AABBNode build(int begin, int end, bCBox bounds, int depth) {
		if (depth >= maxDepth || end - begin <= minSize) {
			return new AABBLeafNode(bounds, begin, end);
		}

		bCVector extent = bounds.getExtent();
		Function<bCVector, Float> axis = bCVector::getX;
		float maxExtent = extent.getX();
		if (extent.getY() > maxExtent) {
			axis = bCVector::getY;
			maxExtent = extent.getY();
		}
		if (extent.getZ() > maxExtent) {
			axis = bCVector::getZ;
			maxExtent = extent.getZ();
		}

		// TODO: Optimize sort peformance (kth order statistic)
		Function<bCVector, Float> axisGetter = axis;
		primitives.subList(begin, end)
				.sort((p1, p2) -> Float.compare(axisGetter.apply(p1.getReferencePoint()), axisGetter.apply(p2.getReferencePoint())));

		int mid = begin + (end - begin) / 2;
		bCBox leftBounds = computeBounds(begin, mid);
		bCBox rightBounds = computeBounds(mid, end);

		return new AABBSplitNode(bounds, build(begin, mid, leftBounds, depth + 1), build(mid, end, rightBounds, depth + 1));
	}

	public List<T> closestPrimitives(int k, bCBox q) {
		PriorityQueue<SearchEntry> qmin = new PriorityQueue<>();
		PriorityQueue<ResultEntry> kBest = new PriorityQueue<>();
		AABBNode node = getRoot();
		do {
			AABBLeafNode leafNode = considerPath(node, q, qmin);
			for (int i = leafNode.getPrimitivesBegin(); i < leafNode.getPrimitivesEnd(); i++) {
				T prim = primitives.get(i);
				float dist = prim.getBounds().sqrDistance(q);
				if (kBest.size() < k) {
					kBest.add(new ResultEntry(dist, prim));
					continue;
				}

				if (kBest.peek().sqrDistance > dist) {
					kBest.poll();
					kBest.add(new ResultEntry(dist, prim));
				}
			}

			if (qmin.isEmpty()) {
				break;
			}

			SearchEntry next = qmin.poll();
			if (next.sqrDistance > kBest.peek().sqrDistance && kBest.size() == k) {
				break;
			}

			node = next.node;
		} while (true);

		List<T> result = new ArrayList<>(kBest.size());
		StreamEx.of(kBest).map(r -> r.primitive).forEach(p -> result.add(0, p));
		return result;
	}

	private AABBLeafNode considerPath(AABBNode node, bCBox q, PriorityQueue<SearchEntry> qmin) {
		while (!node.isLeaf()) {
			AABBSplitNode splitNode = (AABBSplitNode) node;
			float leftDistance = splitNode.getLeft().getBounds().sqrDistance(q);
			float rightDistance = splitNode.getRight().getBounds().sqrDistance(q);
			if (leftDistance < rightDistance) {
				node = splitNode.getLeft();
				qmin.add(new SearchEntry(rightDistance, splitNode.getRight()));
			} else {
				node = splitNode.getRight();
				qmin.add(new SearchEntry(leftDistance, splitNode.getLeft()));
			}
		}
		return (AABBLeafNode) node;
	}
}
