package de.george.g3dit.gui.components;

import java.awt.event.KeyEvent;
import java.util.Optional;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.EntityCache;
import de.george.g3dit.entitytree.filter.GuidEntityFilter;
import de.george.g3dit.gui.dialogs.EntityIntelliHints;
import de.george.g3dit.gui.dialogs.EntitySearchDialog;
import de.george.g3dit.rpc.IpcUtil;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.SwingUtils;
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

		SwingUtils.addKeyStroke(this, "Open", KeyEvent.VK_F3, () -> openEntity(ctx, getText()));
		SwingUtils.addKeyStroke(this, "Search", KeyEvent.VK_F4, () -> searchEntity(ctx, getText()));
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
		addMenuItem(I.tr("Open entity for this guid [F3]"), Icons.getImageIcon(Icons.Arrow.CURVE), this::openEntity);
		addMenuItem(I.tr("Search for entity with this guid [F4]"), Icons.getImageIcon(Icons.Action.FIND), this::searchEntity);
		addMenuItem(I.tr("Teleport to entity with this guid"), Icons.getImageIcon(Icons.Misc.GEOLOCATION),
				(ctx, guid) -> IpcUtil.gotoGuid(guid), (ctx, guid) -> GuidUtil.isValid(guid) && ctx.getIpcMonitor().isAvailable());
	}

	private boolean openEntity(EditorContext ctx, String text) {
		return ctx.getEditor().openEntity(text);
	}

	private EntitySearchDialog searchEntity(EditorContext ctx, String text) {
		return EntitySearchDialog.openEntitySearchGuid(ctx, GuidEntityFilter.MatchMode.Guid, text);
	}

}
