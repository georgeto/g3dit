package de.george.lrentnode.diff;

import java.util.Objects;

import de.danielbechler.diff.access.Instances;
import de.danielbechler.diff.differ.Differ;
import de.danielbechler.diff.node.DiffNode;

public class ArrayDiffer implements Differ {
	@Override
	public boolean accepts(Class<?> type) {
		return type.isArray();
	}

	@Override
	public DiffNode compare(DiffNode parentNode, Instances instances) {
		final DiffNode node = new DiffNode(parentNode, instances.getSourceAccessor(), instances.getType());
		if (instances.hasBeenAdded()) {
			node.setState(DiffNode.State.ADDED);
		} else if (instances.hasBeenRemoved()) {
			node.setState(DiffNode.State.REMOVED);
		} else if (!Objects.deepEquals(instances.getBase(), instances.getWorking())) {
			node.setState(DiffNode.State.CHANGED);
		}
		return node;
	}
}
