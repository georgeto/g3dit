package de.george.g3dit.gui.components.search;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.EntityCache;
import de.george.g3dit.entitytree.filter.NameEntityFilter.MatchMode;

@SearchFilterBuilderDesc(title = "Name")
public class EntityNameSearchFilterBuilder extends NameSearchFilterBuilder {
	public EntityNameSearchFilterBuilder(EditorContext ctx) {
		super(ctx);
	}

	@Override
	public boolean loadFromString(String text) {
		EntityCache entityCache = Caches.entity(ctx);
		if (entityCache.getUniqueNames().contains(text)) {
			cbMatchMode.setSelectedItem(MatchMode.Name);
			tfFilter.setText(text);
			return true;
		}

		return super.loadFromString(text);
	}
}
