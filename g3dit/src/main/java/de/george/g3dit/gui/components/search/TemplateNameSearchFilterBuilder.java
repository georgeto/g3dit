package de.george.g3dit.gui.components.search;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.TemplateCache;
import de.george.g3dit.entitytree.filter.NameEntityFilter.MatchMode;

@SearchFilterBuilderDesc(title = "Name")
public class TemplateNameSearchFilterBuilder extends NameSearchFilterBuilder {
	public TemplateNameSearchFilterBuilder(EditorContext ctx) {
		super(ctx);
	}

	@Override
	public boolean loadFromString(String text) {
		TemplateCache tpleCache = Caches.template(ctx);
		if (tpleCache.getEntryByName(text).isPresent()) {
			cbMatchMode.setSelectedItem(MatchMode.Name);
			tfFilter.setText(text);
			return true;
		}

		return super.loadFromString(text);
	}
}
