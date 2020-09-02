package de.george.g3dit.entitytree.filter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.teamunify.i18n.I;

import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;

public class NameEntityFilter extends AbstractEntityFilter {
	public enum MatchMode {
		Name,
		Position,
		FocusName,
	}

	private MatchMode matchMode;
	private boolean regex;
	private String textToMatch;
	private String lowerTextToMatch;
	private Pattern pattern;
	private eCEntity ignoredByFilter;
	private Set<String> namesToMatch;

	public NameEntityFilter(String text, boolean regex) {
		this(text, regex, null);
	}

	public NameEntityFilter(String text, boolean regex, Map<String, String> focusNames) {
		this(text, regex, null, focusNames);
	}

	public NameEntityFilter(String text, boolean regex, ArchiveFile aFile, Map<String, String> focusNames) {
		if (text.startsWith("#f#")) {
			initFilter(MatchMode.FocusName, text.substring(3), regex, focusNames);
		} else if (text.startsWith("#") && aFile != null) {
			matchMode = MatchMode.Position;
			try {
				int number = Integer.valueOf(text.substring(1));
				if (number < aFile.getEntityCount()) {
					ignoredByFilter = aFile.getEntityByPosition(number);
				}
			} catch (NumberFormatException e) {
				// Nothing
			}
		} else {
			initFilter(MatchMode.Name, text, regex, focusNames);
		}
	}

	public NameEntityFilter(MatchMode mode, String text, boolean regex, Map<String, String> focusNames) {
		initFilter(mode, text, regex, focusNames);
	}

	private void initFilter(MatchMode mode, String text, boolean regex, Map<String, String> focusNames) {
		matchMode = mode;
		textToMatch = text;
		this.regex = regex;
		lowerTextToMatch = text.toLowerCase();
		if (regex) {
			try {
				pattern = Pattern.compile(text);
			} catch (PatternSyntaxException e) {
				return;
			}
		}

		if (matchMode == MatchMode.FocusName) {
			namesToMatch = new HashSet<>();
			if (!lowerTextToMatch.isEmpty() && focusNames != null) {
				for (Map.Entry<String, String> nameMapping : focusNames.entrySet()) {
					String name = nameMapping.getKey();
					String focusName = nameMapping.getValue();
					if (regex ? pattern.matcher(focusName).find() : focusName.toLowerCase().contains(lowerTextToMatch)) {
						namesToMatch.add(name);
					}
				}
			}
		}
	}

	@Override
	public boolean matches(eCEntity entity) {
		return switch (matchMode) {
			case Name -> regex ? pattern.matcher(entity.toString()).find() : entity.toString().toLowerCase().contains(lowerTextToMatch);
			case Position -> ignoredByFilter == null || ignoredByFilter.equals(entity);
			case FocusName -> namesToMatch.contains(entity.getName());
			default -> throw new IllegalStateException();
		};
	}

	@Override
	public boolean isValid() {
		return switch (matchMode) {
			case Name -> regex ? pattern != null : !lowerTextToMatch.isEmpty();
			case Position -> ignoredByFilter != null;
			case FocusName -> namesToMatch != null;
			default -> throw new IllegalStateException();
		};
	}

	public MatchMode getMatchMode() {
		return matchMode;
	}

	public String getTextToMatch() {
		return textToMatch;
	}

	public boolean isRegex() {
		return regex;
	}

	public static String getToolTipText() {
		// @foff
		return I.tr("<html>"
				+ "Dem Suchbegriff kann einer der folgenden Präfixe vorangestellt werden."
				+ "<ul>"
				+ "<li><b>Kein Präfix</b>: Name enthält Suchbegriff</li>"
				+ "<li><b>#f#</b>: Fokusname enthält Suchbegriff</li>"
				+ "</ul></html>");
		// @fon
	}
}
