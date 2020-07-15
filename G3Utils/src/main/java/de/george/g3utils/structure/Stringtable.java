package de.george.g3utils.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Stringtable {

	private List<String> entries;
	private HashMap<String, Integer> posMapping;

	public Stringtable() {
		entries = new ArrayList<>();
		posMapping = new HashMap<>();
	}

	public int addEntry(String entry) {
		entries.add(entry);
		posMapping.put(entry, entries.size() - 1);
		return entries.size() - 1;
	}

	public void removeEntry(String entry) {
		if (entries.remove(entry)) {
			posMapping.clear();
			for (int i = 0; i < entries.size(); i++) {
				posMapping.put(entries.get(i), i);
			}
		}
	}

	public String getEntry(int pos) {
		return entries.get(pos);
	}

	public int getPosition(String entry) {
		Integer result = posMapping.get(entry);
		if (result != null) {
			return result;
		} else {
			return -1;
		}
	}

	public int getEntryCount() {
		return entries.size();
	}

	public List<String> getEntries() {
		return entries;
	}

	public void clear() {
		entries.clear();
		posMapping.clear();
	}
}
