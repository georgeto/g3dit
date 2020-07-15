package de.george.lrentnode.properties.compare;

import java.util.regex.Pattern;

import de.george.g3utils.io.G3Serializable;

public interface TextPropertyComparator<T extends G3Serializable> {
	public boolean contains(T haystack, T pattern);

	public boolean containsIgnoreCase(T haystack, T pattern);

	public boolean equalsIgnoreCase(T haystack, T pattern);

	public boolean startsWith(T haystack, T pattern);

	public boolean startsWithIgnoreCase(T haystack, T pattern);

	public boolean endsWith(T haystack, T pattern);

	public boolean endsWithIgnoreCase(T haystack, T pattern);

	public boolean regex(T haystack, Pattern pattern);
}
