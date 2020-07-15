package de.george.g3dit.util;

import com.google.common.collect.Ordering;

public class StringLengthComparator extends Ordering<String> {
	@Override
	public int compare(String left, String right) {
		int compare = Integer.compare(left.length(), right.length());
		return -(compare == 0 ? left.compareTo(right) : compare);
	}
}
