package de.george.g3dit.gui.components.search;

import javax.swing.JComponent;

import de.george.g3dit.EditorContext;

public interface SearchFilterBuilder<T> {
	public JComponent getComponent();

	public void initFocus();

	public SearchFilter<T> buildFilter();

	public boolean loadFilter(SearchFilter<T> filter);

	default public boolean loadFromString(String text) {
		return false;
	}

	default public String storeToString() {
		return null;
	}

	public static <B> String getTitle(Class<SearchFilterBuilder<B>> builder) {
		SearchFilterBuilderDesc desc = builder.getDeclaredAnnotation(SearchFilterBuilderDesc.class);
		return desc != null ? desc.title() : null;
	}

	public static <B> SearchFilterBuilder<B> newInstance(Class<SearchFilterBuilder<B>> builder) {
		try {
			return builder.getConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public static <B> SearchFilterBuilder<B> newInstance(Class<SearchFilterBuilder<B>> builder, EditorContext ctx) {
		try {
			return builder.getConstructor(EditorContext.class).newInstance(ctx);
		} catch (ReflectiveOperationException e) {
			return newInstance(builder);
		}
	}
}
