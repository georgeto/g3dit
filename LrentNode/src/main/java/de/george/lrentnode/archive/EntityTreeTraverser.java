package de.george.lrentnode.archive;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.TreeTraverser;

public class EntityTreeTraverser<T extends eCEntity> extends TreeTraverser<T> {

	@Override
	public Iterable<T> children(eCEntity root) {
		return root.getChilds();
	}

	public static final <R extends eCEntity> FluentIterable<R> traversePreOrder(R root) {
		return new EntityTreeTraverser<R>().preOrderTraversal(root);
	}

	public static final <R extends eCEntity> FluentIterable<R> traversePostOrder(R root) {
		return new EntityTreeTraverser<R>().postOrderTraversal(root);
	}

	public static final <R extends eCEntity> FluentIterable<R> traverseBreadthFirst(R root) {
		return new EntityTreeTraverser<R>().breadthFirstTraversal(root);
	}
}
