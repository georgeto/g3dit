package de.george.g3dit.gui.components.tab;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import de.george.g3dit.util.event.EventBusProvider;
import de.george.g3utils.gui.SwingUtils;

public class JTypedTabbedPane<T extends ITypedTab> extends EventBusProvider {
	private JButtonTabbedPane tabbedPane = new JButtonTabbedPane();
	private List<T> tabs = new ArrayList<>();
	private boolean closeable;

	private Optional<T> previousTab = Optional.empty();

	/**
	 * Ersellt ein leeres {@code JTypedTabbedPane}, dessen Tabs nicht schließbar sind.
	 */
	public JTypedTabbedPane() {
		this(false);
	}

	/**
	 * Ersellt ein leeres {@code JTypedTabbedPane}.
	 *
	 * @param closeable Gibt an, ob Tabs geschlossen werden können.
	 */
	public JTypedTabbedPane(boolean closeable) {
		this.closeable = closeable;
		tabbedPane.addChangeListener(e -> fireSelectionChanged());
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}

	/**
	 * @see JTabbedPane#addTab
	 * @param tab
	 * @param index
	 */
	public void addTab(T tab) {
		if (!tabs.contains(tab)) {
			tabbedPane.addTab(tab.getTabTitle(), tab.getTabIcon(), tab.getTabContent(), tab.getTooltip());
			afterTabAdded(tab);
		}

	}

	/**
	 * @see JTabbedPane#insertTab
	 * @param tab
	 * @param index
	 */
	public void insertTab(T tab, int index) {
		if (!tabs.contains(tab)) {
			tabbedPane.insertTab(tab.getTabTitle(), tab.getTabIcon(), tab.getTabContent(), tab.getTooltip(), index);
			afterTabAdded(tab);
		}

	}

	/**
	 * @see JTabbedPane#removeTab
	 * @param tab
	 * @param index
	 */
	public void removeTab(T tab) {
		if (tabs.remove(tab)) {
			// TabCloseEvent event = new TabCloseEvent(tab);
			// this.eventBus().post(event);

			// if(!event.isCancelled()) {
			tabbedPane.remove(tab.getTabContent());
			eventBus().post(new TabRemovedEvent<>(tab));
			// tabs.remove(tab);
			// }
		}
	}

	public void updateTab(T tab) {
		int index = getTabIndex(tab);
		if (index != -1) {
			tabbedPane.setTitleAt(index, tab.getTabTitle());
			tabbedPane.setIconAt(index, tab.getTabIcon());
			tabbedPane.setTitleColorAt(index, tab.getTitleColor());
			tabbedPane.setToolTipTextAt(index, tab.getTooltip());
		}
	}

	public void selectTab(T tab) {
		int tabIndex = getTabIndex(tab);
		if (tabIndex != -1) {
			tabbedPane.setSelectedIndex(tabIndex);
		}
	}

	/**
	 * Gibt den aktuell ausgewählten Tab zurück
	 *
	 * @return
	 */
	public Optional<T> getSelectedTab() {
		Component selected = tabbedPane.getSelectedComponent();
		for (T tab : tabs) {
			if (tab.getTabContent().equals(selected)) {
				return Optional.of(tab);
			}
		}
		return Optional.empty();
	}

	public int getTabCount() {
		return tabs.size();
	}

	public T getTabAt(int index) {
		return tabs.get(index);
	}

	public int getTabIndex(T tab) {
		return tabs.indexOf(tab);
	}

	public List<T> getTabs() {
		return new ArrayList<>(tabs);
	}

	public boolean closeTab(T tab) {
		TabCloseEvent<T> closeEvent = new TabCloseEvent<>(tab);
		eventBus().post(closeEvent);
		if (!closeEvent.isCancelled()) {
			removeTab(tab);
		}

		return !closeEvent.isCancelled();
	}

	public void addTabAction(T tab, Action action, Icon icon) {
		addTabAction(tab, action, icon, false);
	}

	public void addTabAction(T tab, Action action, Icon icon, boolean front) {
		int index = getTabIndex(tab);
		if (index != -1) {
			tabbedPane.addTabAction(index, action, icon, front);
		}
	}

	public void setSelectedTabBoldFont(boolean selectedTabBoldFont) {
		tabbedPane.setSelectedTabBoldFont(selectedTabBoldFont);
	}

	public boolean isSelectedTabBoldFont() {
		return tabbedPane.isSelectedTabBoldFont();
	}

	public JComponent getComponent() {
		return tabbedPane;
	}

	protected void afterTabAdded(final T tab) {
		tabs.add(tab);

		int index = getTabIndex(tab);
		if (index != -1) {
			tabbedPane.setTitleColorAt(index, tab.getTitleColor());
		}

		// Tab Actions hinzufügen
		if (closeable) {
			addTabAction(tab, SwingUtils.createAction(() -> closeTab(tab)),
					new ImageIcon(JTypedTabbedPane.class.getResource("/res/buttonclose.png")));
		}

		// Beim ersten Tab reagiert der ChangeListener nicht
		if (getTabCount() == 1) {
			fireSelectionChanged();
		}
	}

	/**
	 * @see JTabbedPane#setTabPlacement(int)
	 */
	public void setTabPlacement(int tabPlacement) {
		tabbedPane.setTabPlacement(tabPlacement);
	}

	/**
	 * @see JTabbedPane#setTabLayoutPolicy(int)
	 */
	public void setTabLayoutPolicy(int tabLayoutPolicy) {
		tabbedPane.setTabLayoutPolicy(tabLayoutPolicy);
	}

	private void fireSelectionChanged() {
		eventBus().post(new TabSelectedEvent<>(previousTab, getSelectedTab()));
		previousTab = getSelectedTab();
	}
}
