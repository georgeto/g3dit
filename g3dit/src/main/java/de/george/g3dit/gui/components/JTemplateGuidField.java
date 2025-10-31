package de.george.g3dit.gui.components;

import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.TemplateCache;
import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;
import de.george.g3dit.entitytree.filter.GuidEntityFilter;
import de.george.g3dit.gui.dialogs.TemplateIntelliHints;
import de.george.g3dit.gui.dialogs.TemplateSearchDialog;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.SwingUtils;

public class JTemplateGuidField extends JSearchNamedGuidField {
	private boolean all;
	private Predicate<TemplateCacheEntry> filter;
	private TemplateCache cache;
	private TemplateIntelliHints intelliHints;

	public JTemplateGuidField(EditorContext ctx) {
		this(ctx, null, false);
	}

	public JTemplateGuidField(EditorContext ctx, Predicate<TemplateCacheEntry> filter, boolean all) {
		super(ctx);
		this.filter = filter;
		this.all = all;

		cache = Caches.template(ctx);
		intelliHints = new TemplateIntelliHints(getOrCreateTextFieldName(), cache, filter, all);
		// Update name when cache changes
		cache.addUpdateListener(this, c -> lookupName(getText()));

		SwingUtils.addKeyStroke(this, "Open", KeyEvent.VK_F3, () -> openTemplate(ctx, getText()));
		SwingUtils.addKeyStroke(this, "Search", KeyEvent.VK_F4, () -> searchTemplate(ctx, getText()));
	}

	public void setFilter(Predicate<TemplateCacheEntry> filter) {
		this.filter = filter;
		intelliHints.setFilter(filter);
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
		return cache.getEntryByName(name, getTemplateEntities()).map(TemplateCacheEntry::getGuid);
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
		addMenuItem(I.tr("Open template with this guid [F3]"), Icons.getImageIcon(Icons.Arrow.CURVE), this::openTemplate);
		addMenuItem(I.tr("Search for template with this guid [F4]"), Icons.getImageIcon(Icons.Action.FIND), this::searchTemplate);
	}

	private boolean openTemplate(EditorContext ctx, String text) {
		return ctx.getEditor().openTemplate(text);
	}

	private TemplateSearchDialog searchTemplate(EditorContext ctx, String text) {
		return TemplateSearchDialog.openTemplateSearchGuid(ctx, GuidEntityFilter.MatchMode.Guid, text);
	}
}
