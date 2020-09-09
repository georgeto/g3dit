package de.george.g3dit.gui.components;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.TemplateCache;
import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;
import de.george.g3dit.entitytree.filter.GuidEntityFilter;
import de.george.g3dit.gui.dialogs.TemplateIntelliHints;
import de.george.g3dit.gui.dialogs.TemplateSearchDialog;
import de.george.g3dit.util.Icons;

public class JTemplateGuidField extends JSearchNamedGuidField {
	private boolean all;
	private Predicate<TemplateCacheEntry> filter;
	private TemplateCache cache;

	public JTemplateGuidField(EditorContext ctx) {
		this(ctx, null, false);
	}

	public JTemplateGuidField(EditorContext ctx, Predicate<TemplateCacheEntry> filter, boolean all) {
		super(ctx);
		this.filter = filter;
		this.all = all;

		cache = Caches.template(ctx);
		// Add name auto completion to guid field
		new TemplateIntelliHints(getOrCreateTextFieldName(), cache);
		// Update name when cache changes
		cache.addUpdateListener(this, c -> lookupName(getText()));
	}

	@Override
	protected Optional<String> guidToName(String guid) {
		if (cache.isValid()) {
			return cache.getEntryByGuid(guid, getTemplateEntities()).map(TemplateCacheEntry::getName);
		} else {
			return Optional.empty();
		}
	}

	@Override
	protected Optional<String> nameToGuid(String name) {
		return cache.getEntryByName(name).map(TemplateCacheEntry::getGuid);
	}

	private Stream<TemplateCacheEntry> getTemplateEntities() {
		Stream<TemplateCacheEntry> entities = cache.getEntities(all);
		if (filter != null) {
			entities = entities.filter(filter);
		}
		return entities;
	}

	@Override
	protected void addDefaultMenuItem() {
		addMenuItem("Template zu dieser Guid öffnen", Icons.getImageIcon(Icons.Arrow.CURVE),
				(ctx, text) -> ctx.getEditor().openTemplate(text));
		addMenuItem("Nach Template zu dieser Guid suchen", Icons.getImageIcon(Icons.Action.FIND),
				(ctx, text) -> TemplateSearchDialog.openTemplateSearchGuid(ctx, GuidEntityFilter.MatchMode.Guid, text));
	}

}
