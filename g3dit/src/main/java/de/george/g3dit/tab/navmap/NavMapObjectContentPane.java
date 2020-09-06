package de.george.g3dit.tab.navmap;

import java.awt.BorderLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.text.JTextComponent;

import com.google.common.eventbus.Subscribe;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.components.AbstractTextComponentMatcherEditor;
import de.george.g3dit.gui.components.ListManageAndEdit;
import de.george.g3dit.gui.components.ValidationPanelContainer;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.NavMapManager.NavMapLoadedEvent;
import de.george.g3dit.util.ToolbarUtil;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.navmap.sections.NavMap;
import de.george.navmap.util.NavCalc;

public abstract class NavMapObjectContentPane extends ValidationPanelContainer<NavMapObjectContentPane> {
	protected EditorContext ctx;
	protected NavMap navMap;
	protected NavCalc navCalc;

	private ListManageAndEdit<String> edit;

	public NavMapObjectContentPane(EditorContext ctx) {
		this.ctx = ctx;
		updateNavMap();
		ctx.getNavMapManager().eventBus().register(this);
	}

	@Subscribe
	private void onNavMapLoaded(NavMapLoadedEvent e) {
		if (e.isSuccessful()) {
			updateNavMap();
		}
	}

	private void updateNavMap() {
		navMap = ctx.getNavMapManager().getNavMap(true);
		navCalc = ctx.getNavMapManager().getNavCalc(true);
	}

	public void onClose() {
		ctx.getNavMapManager().eventBus().unregister(this);
	}

	public void selectObject(String guid) {
		edit.setSelectedValue(guid);
	}

	public final void initGui() {
		createValiditionPanel();
	}

	@Override
	protected void initComponents() {
		edit = createContent();

		JToolBar toolBar = ToolbarUtil.createTopToolbar();

		JButton btnGoto = SwingUtils.keyStrokeButton("Goto", "Teleportiert den Spieler zu dem ausgewählten Objekt.",
				Icons.getImageIcon(Icons.Misc.GEOLOCATION), KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK,
				() -> edit.getSelectedValues().stream().findFirst().ifPresent(this::onGoto));
		toolBar.add(btnGoto);
		ctx.getIpcMonitor().addListener(this, ipcMonitor -> btnGoto.setEnabled(ipcMonitor.isAvailable()), true, false, true);

		JButton btnShowOnMap = SwingUtils.keyStrokeButton("Karte", "Zeigt ausgewählte Objekte auf Karte an.",
				Icons.getImageIcon(Icons.Misc.MAP), KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK, () -> onShowOnMap(edit.getSelectedValues()));
		toolBar.add(btnShowOnMap);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(toolBar, BorderLayout.NORTH);
		contentPanel.add(edit.getContent().getRightComponent(), BorderLayout.CENTER);
		edit.getContent().setRightComponent(contentPanel);

		setLayout(new BorderLayout());
		add(edit.getContent(), BorderLayout.CENTER);
	}

	protected abstract void onGoto(String guid);

	protected abstract void onShowOnMap(List<String> guid);

	protected abstract ListManageAndEdit<String> createContent();

	protected String getSearchTooltip() {
		return SwingUtils.getMultilineText("Guid ODER Position und Radius im Format x/y/z// r");
	}

	protected class NavMapObjectMatcherEditor extends AbstractTextComponentMatcherEditor<String> {
		private Function<String, Optional<bCVector>> positionExtractor;

		public NavMapObjectMatcherEditor(JTextComponent textComponent, Function<String, Optional<bCVector>> positionExtractor) {
			super(textComponent);
			this.positionExtractor = positionExtractor;
		}

		@Override
		protected void updateFilter(String text) {
			int delimiter = text.lastIndexOf(" ");
			if (delimiter != -1 && delimiter + 1 < text.length()) {
				try {
					bCVector position = bCVector.fromString(text.substring(0, delimiter));
					float range = Misc.parseFloat(text.substring(delimiter + 1));
					fireChanged(objectGuid -> positionExtractor.apply(objectGuid).map(p -> p.getRelative(position).length() <= range)
							.orElse(false));
					return;
				} catch (IllegalArgumentException e) {
					// Nothing, maybe it is not a vector
				}
			}

			String guid = GuidUtil.parseGuidPartial(text);
			if (guid == null) {
				fireMatchAll();
			} else {
				fireChanged(objectGuid -> objectGuid.contains(guid));
			}
		}
	}
}
