package de.george.lrentnode.properties.compare;

public enum CompareOperation {
	Equals,
	NotEquals,
	GreaterThanEquals,
	LessThanEquals,
	Greater,
	Less,
	NotEqualsIgnoreCase,
	EqualsIgnoreCase,
	Contains,
	ContainsIgnoreCase,
	StartsWith,
	StartsWithIgnoreCase,
	EndsWith,
	EndsWithIgnoreCase,
	Regex,
	RegexIgnoreCase,
	ContainsElement,
	Similar,
	NotSimilar;

	public boolean isRegex() {
		return this == CompareOperation.Regex || this == CompareOperation.RegexIgnoreCase;
	}
}
