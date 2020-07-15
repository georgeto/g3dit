package de.george.g3dit.gui.dialogs;

import java.util.Comparator;

public class StringAutoCompleteSorter implements Comparator<String> {
	private final String context;
	private final boolean caseSensitive;

	public StringAutoCompleteSorter(String context, boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		this.context = applyCase(context);
	}

	private String applyCase(String value) {
		return caseSensitive ? value.toLowerCase() : value;
	}

	@Override
	public int compare(String o1, String o2) {
		o1 = applyCase(o1);
		o2 = applyCase(o2);

		if (o1.startsWith(context)) {
			if (!o2.startsWith(context)) {
				return -1;
			}
		} else if (o2.startsWith(context)) {
			return 1;
		}

		return Integer.compare(o1.length(), o2.length());
	}

}
