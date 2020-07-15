package de.george.lrentnode.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public abstract class bTArray<T extends G3Serializable> implements G3Serializable {
	protected List<T> entries;

	public bTArray() {
		entries = new ArrayList<>();
	}

	public List<T> getEntries() {
		return entries;
	}

	public <R> List<R> getEntries(Function<? super T, ? extends R> mapper) {
		return entries.stream().map(mapper).collect(Collectors.toList());
	}

	public void setEntries(List<T> entries) {
		this.entries = entries;
	}

	public <R> void setEntries(List<R> entries, Function<? super R, ? extends T> mapper) {
		this.entries = entries.stream().map(mapper).collect(Collectors.toList());
	}

	public void clear() {
		entries = new ArrayList<>(0);
	}

	@Override
	public final void read(G3FileReader reader) {
		entries = reader.readPrefixedList(getEntryType());
	}

	@Override
	public final void write(G3FileWriter writer) {
		writer.writePrefixedList(entries);
	}

	public abstract Class<T> getEntryType();

	@Override
	public String toString() {
		return entries.toString();
	}
}
