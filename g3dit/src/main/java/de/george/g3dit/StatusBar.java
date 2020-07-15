package de.george.g3dit;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXStatusBar;

import de.george.g3dit.cache.AbstractCache;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.SwingUtils;
import net.miginfocom.swing.MigLayout;

public class StatusBar extends JXStatusBar {

	private JLabel lblFileStatus;
	private JPanel extensionPanel;

	private Map<Class<? extends AbstractCache<?>>, CachePanel> cachePanels = new HashMap<>();

	private EditorContext ctx;

	public StatusBar(EditorContext ctx) {
		this.ctx = ctx;
		lblFileStatus = new JLabel("Keine Datei geöffnet");
		setLayout(new MigLayout("fillx, gapx 15, insets 2 5 2 5", ""));
		add(lblFileStatus);

		extensionPanel = new JPanel();
		extensionPanel.setOpaque(false);
		add(extensionPanel, "");

		JLabel lblGothicRunning = new JLabel();
		ctx.getIpcMonitor().addListener(this, ipcMonitor -> {
			if (ipcMonitor.isAvailable()) {
				lblGothicRunning.setText("Gothic erreichbar (" + ipcMonitor.getStatus().orElse(null) + ")");
			} else {
				lblGothicRunning.setText("Gothic nicht erreichbar");
			}
		}, true, true, true);
		add(lblGothicRunning, "");

		setExtensionPanel(null);
	}

	public void setFileStatus(String text) {
		lblFileStatus.setText(text);
		fixPainting();
	}

	public <T extends AbstractCache<T>> void addCacheStatus(Class<T> cache, Function<T, Integer> entryCountSupplier) {
		CachePanel cachePanel = new CachePanel(cache, entryCountSupplier);
		cachePanels.put(cache, cachePanel);
		add(cachePanel);
	}

	public <T extends AbstractCache<T>> void createCache(Class<T> cache) {
		cachePanels.get(cache).btnCreateCache.doClick();
	}

	public void setExtensionPanel(JPanel panel) {
		if (extensionPanel != null) {
			remove(extensionPanel);
		}
		extensionPanel = panel;
		MigLayout layout = (MigLayout) getLayout();
		if (extensionPanel != null) {
			add(extensionPanel, "push", SwingUtils.getComponentIndex(lblFileStatus) + 1);
			layout.setComponentConstraints(lblFileStatus, "");
		} else {
			layout.setComponentConstraints(lblFileStatus, "push");
		}
		fixPainting();
	}

	private void fixPainting() {
		int width = getWidth();
		int height = getHeight();
		setSize(width - 1, height);
		setSize(width, height);
	}

	private class CachePanel extends JPanel {
		private String name;
		private JLabel lblCacheStatus;
		private JButton btnCreateCache;

		public <T extends AbstractCache<T>> CachePanel(Class<T> cache, Function<T, Integer> entryCountSupplier) {
			name = cache.getSimpleName();
			setLayout(new MigLayout("fillx, insets 0 0 0 0"));
			setOpaque(false);
			lblCacheStatus = new JLabel(cache.getSimpleName() + ": ");
			add(lblCacheStatus);
			btnCreateCache = new JButton(Icons.getImageIcon(Icons.Arrow.CIRCLE_DOUBLE));
			btnCreateCache.setToolTipText("Cache erstellen");
			btnCreateCache.addActionListener(a -> {
				setCacheStatus("Wird erstellt...", null, false);
				ctx.getCacheManager().createCache(cache);
			});
			btnCreateCache.setFocusable(false);
			add(btnCreateCache, "width 23!, height 23!");

			setCacheStatus("Wird geladen...", null, false);

			ctx.getCacheManager().getCache(cache).addUpdateListener(this, n -> {
				if (n.isValid()) {
					setCacheStatus(entryCountSupplier.apply(ctx.getCacheManager().getCache(cache)) + " Einträge",
							"Erstellt am " + n.printCreationTimestamp(), true);
				} else {
					setCacheStatus("Existiert nicht", null, true);
				}
			});
		}

		public void setCacheStatus(String text, String tooltipText, boolean btnStatus) {
			lblCacheStatus.setText(name + ": " + text);
			lblCacheStatus.setToolTipText(tooltipText);
			btnCreateCache.setEnabled(btnStatus);
			fixPainting();
		}
	}
}
