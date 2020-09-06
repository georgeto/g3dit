package de.george.g3dit.gui.components;

import java.util.Optional;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.EntityCache;
import de.george.g3dit.entitytree.filter.GuidEntityFilter;
import de.george.g3dit.gui.dialogs.EntityIntelliHints;
import de.george.g3dit.gui.dialogs.EntitySearchDialog;
import de.george.g3dit.rpc.IpcUtil;
import de.george.g3dit.util.Icons;
import de.george.g3utils.structure.GuidUtil;

public class JEntityGuidField extends JSearchNamedGuidField {
	private EntityCache cache;

	public JEntityGuidField(EditorContext ctx) {
		super(ctx);

		cache = Caches.entity(ctx);
		// Add name auto completion to guid field
		new EntityIntelliHints(getOrCreateTextFieldName(), cache);
		// Update name when cache changes
		cache.addUpdateListener(this, c -> lookupName(getText()));
	}

	@Override
	protected Optional<String> guidToName(String guid) {
		if (cache.isValid()) {
			return Optional.ofNullable(cache.getDisplayName(guid));
		} else {
			return Optional.empty();
		}
	}

	@Override
	protected Optional<String> nameToGuid(String name) {
		return cache.getGuidByUniqueName(name);
	}

	@Override
	protected void addDefaultMenuItem() {
		addMenuItem("Nach Entity zu dieser Guid suchen", Icons.getImageIcon(Icons.Action.FIND),
				(ctx, text) -> EntitySearchDialog.openEntitySearchGuid(ctx, GuidEntityFilter.MatchMode.Guid, text));
		addMenuItem("Zu Entity mit dieser Guid teleportieren", Icons.getImageIcon(Icons.Misc.GEOLOCATION),
				(ctx, guid) -> IpcUtil.gotoGuid(guid), (ctx, guid) -> GuidUtil.isValid(guid) && ctx.getIpcMonitor().isAvailable());
	}
}
