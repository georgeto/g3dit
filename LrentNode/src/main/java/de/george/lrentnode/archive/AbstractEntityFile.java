package de.george.lrentnode.archive;

import java.util.Iterator;
import java.util.Optional;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

import de.george.g3utils.io.GenomeFile;

public abstract class AbstractEntityFile<T extends eCEntity> extends GenomeFile implements Iterable<T> {
	protected T graph;

	public FluentIterable<T> getEntities() {
		return EntityTreeTraverser.traversePreOrder(graph);
	}

	@Override
	public Iterator<T> iterator() {
		return getEntities().iterator();
	}

	public Optional<T> getEntityByName(String name) {
		return getEntities().firstMatch(e -> e.getName().equals(name)).toJavaUtil();
	}

	public Optional<T> getEntityByGuid(String guid) {
		return getEntities().firstMatch(e -> e.getGuid().equals(guid)).toJavaUtil();
	}

	public int getEntityCount() {
		return getEntities().size();
	}

	public T getEntityByPosition(int position) {
		return getEntities().get(position);
	}

	public int getEntityPosition(T entity) {
		return Iterables.indexOf(getEntities(), e -> e.equals(entity));
	}

	public T getGraph() {
		return graph;
	}

	public void setGraph(T graph) {
		this.graph = graph;
	}
}
