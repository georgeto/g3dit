package de.george.g3dit.gui.dialogs;

import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.swing.text.JTextComponent;

import com.jidesoft.hints.AbstractListIntelliHints;

import de.george.g3dit.cache.TemplateCache;
import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;

/**
 * <code>ListDataIntelliHints</code> is a concrete implementation of
 * {@link com.jidesoft.hints.IntelliHints}. It provides hints from a known list of data. It is
 * similar to auto complete text field except the list will be filtered depending on what user types
 * in so far.
 */
public class TemplateIntelliHints extends AbstractListIntelliHints {

	private boolean _caseSensitive = false;
	private TemplateCache cache;
	private Predicate<TemplateCacheEntry> filter;
	private boolean all;

	public TemplateIntelliHints(JTextComponent comp, TemplateCache cache) {
		this(comp, cache, null, false);
	}

	public TemplateIntelliHints(JTextComponent comp, TemplateCache cache, Predicate<TemplateCacheEntry> filter, boolean all) {
		super(comp);
		this.cache = cache;
		this.filter = filter;
		this.all = all;
	}

	@Override
	public boolean updateHints(Object context) {
		if (context == null || !cache.isValid()) {
			return false;
		}

		Stream<TemplateCacheEntry> tpleStream = cache.getEntities(all);
		if (filter != null) {
			tpleStream = tpleStream.filter(filter);
		}
		Object[] objects = tpleStream.filter(e -> compare(context, e.getName())).map(e -> e.getName())
				.sorted(new StringAutoCompleteSorter((String) context, _caseSensitive)).toArray();
		setListData(objects);
		return objects.length > 0;
	}

	/**
	 * Compares the context with the object in the completion list.
	 *
	 * @param context the context returned from {@link #getContext()} method.
	 * @param o the object in the completion list.
	 * @return true if the context matches with the object. Otherwise false.
	 */
	protected boolean compare(Object context, String o) {
		String listEntry = o == null ? "" : o;
		String s = context.toString();
		if (!isCaseSensitive()) {
			listEntry = listEntry.toLowerCase();
			s = s.toLowerCase();
		}

		return listEntry.contains(s);
	}

	/**
	 * Checks if it used case sensitive search. By default it's false.
	 *
	 * @return if it's case sensitive.
	 */
	public boolean isCaseSensitive() {
		return _caseSensitive;
	}

	/**
	 * Sets the case sensitive flag. By default, it's false meaning it's a case insensitive search.
	 *
	 * @param caseSensitive true or false.
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		_caseSensitive = caseSensitive;
	}

}
