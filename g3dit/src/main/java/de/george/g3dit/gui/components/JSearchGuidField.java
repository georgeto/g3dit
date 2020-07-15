package de.george.g3dit.gui.components;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import com.google.common.base.Strings;
import com.jidesoft.swing.JidePopupMenu;

import de.george.g3dit.EditorContext;
import de.george.g3dit.entitytree.filter.GuidEntityFilter.MatchMode;
import de.george.g3dit.gui.dialogs.EntitySearchDialog;
import de.george.g3dit.rpc.IpcUtil;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.JGuidField;
import de.george.g3utils.structure.GuidUtil;

public class JSearchGuidField extends JGuidField {

	private JidePopupMenu omSearch;
	private EditorContext ctx;

	public JSearchGuidField(EditorContext ctx) {
		this(ctx, true);
	}

	public JSearchGuidField(EditorContext ctx, boolean showDefaultMenuItem) {
		this(null, ctx, showDefaultMenuItem);
	}

	public JSearchGuidField(String text, EditorContext ctx) {
		this(text, ctx, true);
	}

	public JSearchGuidField(String text, EditorContext ctx, boolean showDefaultMenuItem) {
		super(text);
		this.ctx = ctx;
		installSearchMenu();
		if (showDefaultMenuItem) {
			addDefaultMenuItem();
		}
	}

	private void installSearchMenu() {
		omSearch = new JDynamicPopupMenu();
		addGuidFiedListener(g -> omSearch.setEnabled(!Strings.isNullOrEmpty(g)));
		setComponentPopupMenu(omSearch);
	}

	private void addDefaultMenuItem() {
		addMenuItem("Nach Entity zu dieser Guid suchen", Icons.getImageIcon(Icons.Action.FIND),
				(ctx, text) -> EntitySearchDialog.openEntitySearchGuid(ctx, MatchMode.Guid, text));
		addMenuItem("Zu Entity mit dieser Guid teleportieren", Icons.getImageIcon(Icons.Misc.GEOLOCATION),
				(ctx, guid) -> IpcUtil.gotoGuid(guid), (ctx, guid) -> GuidUtil.isValid(guid) && ctx.getIpcMonitor().isAvailable());
	}

	public JSearchGuidField addMenuItem(String text, Icon icon, BiConsumer<EditorContext, String> action) {
		return addMenuItem(text, icon, action, (c, g) -> true);
	}

	public JSearchGuidField addMenuItem(String text, Icon icon, BiConsumer<EditorContext, String> action,
			BiPredicate<EditorContext, String> enabledPredicate) {
		JMenuItem miItem = new JDynamicMenuItem(text, icon, () -> enabledPredicate.test(ctx, JSearchGuidField.this.getText()));
		miItem.addActionListener(a -> action.accept(ctx, JSearchGuidField.this.getText()));
		omSearch.add(miItem);
		return this;
	}

	public JSearchGuidField addMenuItem(GuildFieldMenuItem menuItem) {
		return addMenuItem(menuItem.getText(), menuItem.getIcon(), menuItem.getAction());
	}

	public static class GuildFieldMenuItem {
		private String text;
		private Icon icon;
		private BiConsumer<EditorContext, String> action;

		public GuildFieldMenuItem(String text, Icon icon, BiConsumer<EditorContext, String> action) {
			this.text = text;
			this.icon = icon;
			this.action = action;
		}

		public String getText() {
			return text;
		}

		public Icon getIcon() {
			return icon;
		}

		public BiConsumer<EditorContext, String> getAction() {
			return action;
		}
	}
}
