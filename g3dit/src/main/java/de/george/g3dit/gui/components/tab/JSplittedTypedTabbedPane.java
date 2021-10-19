package de.george.g3dit.gui.components.tab;

import java.awt.Component;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JSplitPane;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.jidesoft.utils.JideFocusTracker;

import de.george.g3dit.util.event.EventBusProvider;
import de.george.g3utils.gui.SwingUtils;

public class JSplittedTypedTabbedPane<T extends ITypedTab> extends EventBusProvider {
	public enum Side {
		LEFT,
		RIGHT;

		static {
			LEFT.internal = TabbedPanePosition.LEFT;
			RIGHT.internal = TabbedPanePosition.RIGHT;
		}

		private TabbedPanePosition internal;

		private TabbedPanePosition getInternal() {
			return internal;
		}
	}

	private enum TabbedPanePosition {
		LEFT,
		RIGHT,
		INVALID;

		static {
			LEFT.opposite = RIGHT;
			RIGHT.opposite = LEFT;
			INVALID.opposite = INVALID;
		}

		private TabbedPanePosition opposite;

		public TabbedPanePosition getOpposite() {
			return opposite;
		}

	}

	private JSplitPane splitPane;

	private JTypedTabbedPane<T> leftTabs, rightTabs;
	private TabbedPanePosition lastGainedFocus = TabbedPanePosition.INVALID;
	private boolean lockTabSelectEvent = false;

	private List<T> tabs;

	public JSplittedTypedTabbedPane(boolean closeable) {
		leftTabs = new JTypedTabbedPane<>(closeable);
		rightTabs = new JTypedTabbedPane<>(closeable);
		splitPane = new JSplitPane();

		splitPane.setLeftComponent(leftTabs.getComponent());
		splitPane.setRightComponent(rightTabs.getComponent());

		tabs = new ArrayList<>();

		JideFocusTracker leftTracker = new JideFocusTracker(leftTabs.getComponent());
		TabbedPaneHandler leftHandler = new TabbedPaneHandler(TabbedPanePosition.LEFT);
		leftTracker.addFocusListener(leftHandler);
		leftTabs.eventBus().register(leftHandler);

		JideFocusTracker rightTracker = new JideFocusTracker(rightTabs.getComponent());
		TabbedPaneHandler rightHandler = new TabbedPaneHandler(TabbedPanePosition.RIGHT);
		rightTracker.addFocusListener(rightHandler);
		rightTabs.eventBus().register(rightHandler);

		layoutSplitPaneDivider();

	}

	public void addTab(T tab) {
		addTab(tab, Side.LEFT);
	}

	public void addTab(T tab, Side side) {
		if (!tabs.contains(tab)) {
			if (side == Side.LEFT) {
				leftTabs.addTab(tab);
			} else {
				rightTabs.addTab(tab);
			}
			// splitPane.addTab(tab.getTabTitle(), tab.getTabIcon(), tab.getTabContent());
			afterTabAdded(tab);
		}

	}

	public void insertTab(T tab, int index) {
		insertTab(tab, Side.LEFT, index);
	}

	public void insertTab(T tab, Side side, int index) {
		if (!tabs.contains(tab)) {
			if (side == Side.LEFT) {
				leftTabs.insertTab(tab, index);
			} else {
				rightTabs.insertTab(tab, index);
			}
			afterTabAdded(tab);
		}

	}

	public boolean containsTab(T tab) {
		return tabs.contains(tab);
	}

	public void removeTab(T tab) {
		if (tabs.remove(tab)) {
			Optional<TabIndex> index = getTabIndex(tab);
			index.ifPresent(tabIndex -> tabIndex.getTabbedPane().removeTab(tab));
			layoutSplitPaneDivider();
		}

	}

	public boolean closeTab(T tab) {
		Optional<TabIndex> index = getTabIndex(tab);
		return index.isPresent() && index.get().getTabbedPane().closeTab(tab);
	}

	public void updateTab(T tab) {
		Optional<TabIndex> index = getTabIndex(tab);
		index.ifPresent(tabIndex -> tabIndex.getTabbedPane().updateTab(tab));
	}

	public void selectTab(T tab) {
		Optional<TabIndex> index = getTabIndex(tab);
		if (index.isPresent()) {
			JTypedTabbedPane<T> tabbedPane = index.get().getTabbedPane();
			tabbedPane.selectTab(tab);
		}

	}

	public Optional<T> getSelectedTab() {
		if (lastGainedFocus == TabbedPanePosition.INVALID) {
			return Optional.empty();
		}
		return getTabbedPane(lastGainedFocus).getSelectedTab();
	}

	public int getTabCount() {
		return tabs.size();
	}

	public void addTabAction(T tab, Action action, Icon icon) {
		addTabAction(tab, action, icon, false);
	}

	public void addTabAction(T tab, Action action, Icon icon, boolean front) {
		getTabIndex(tab).ifPresent(tabIndex -> tabIndex.getTabbedPane().addTabAction(tab, action, icon, front));
	}

	protected void afterTabAdded(final T tab) {
		tabs.add(tab);

		addTabAction(tab, SwingUtils.createAction(() -> {
			Optional<TabIndex> tabIndex = getTabIndex(tab);
			if (tabIndex.isPresent()) {
				Side side = tabIndex.get().getPosition() == TabbedPanePosition.LEFT ? Side.RIGHT : Side.LEFT;
				lockTabSelectEvent = true;
				removeTab(tab);
				addTab(tab, side);
				lockTabSelectEvent = false;
				selectTab(tab);
			}
		}), new ImageIcon(JSplittedTypedTabbedPane.class.getResource("/res/buttonpopout.png")), true);

		layoutSplitPaneDivider();
	}

	public Component getComponent() {
		return splitPane;
	}

	public <L extends DropTargetListener> L addDropListener(Side side, L listener) {
		JTypedTabbedPane<T> tabbedPane = getTabbedPane(side.getInternal());
		new DropTarget(tabbedPane.getComponent(), listener);
		return listener;
	}

	private void layoutSplitPaneDivider() {
		int leftTabCount = getTabbedPane(TabbedPanePosition.LEFT).getTabCount();
		int rightTabCount = getTabbedPane(TabbedPanePosition.RIGHT).getTabCount();

		boolean layoutDivider = leftTabCount > 0 && rightTabCount > 0
				&& (splitPane.getLeftComponent() == null || splitPane.getRightComponent() == null);

		if (leftTabCount > 0 && splitPane.getLeftComponent() == null || leftTabCount == 0 && splitPane.getLeftComponent() != null) {
			splitPane.setLeftComponent(leftTabCount == 0 ? null : leftTabs.getComponent());
		}

		if (rightTabCount > 0 && splitPane.getRightComponent() == null || rightTabCount == 0 && splitPane.getRightComponent() != null) {
			splitPane.setRightComponent(rightTabCount == 0 ? null : rightTabs.getComponent());
		}

		if (layoutDivider) {
			splitPane.setDividerSize(3);
			splitPane.setDividerLocation(0.5);
			splitPane.setResizeWeight(0.5);
		}

		splitPane.revalidate();
	}

	private Optional<TabIndex> getTabIndex(T tab) {
		int index = leftTabs.getTabIndex(tab);
		if (index != -1) {
			return Optional.of(new TabIndex(TabbedPanePosition.LEFT, index));
		}
		index = rightTabs.getTabIndex(tab);
		if (index != -1) {
			return Optional.of(new TabIndex(TabbedPanePosition.RIGHT, index));
		}
		return Optional.empty();
	}

	private JTypedTabbedPane<T> getTabbedPane(TabbedPanePosition position) {
		return switch (position) {
			case LEFT -> leftTabs;
			case RIGHT -> rightTabs;
			default -> null;
		};
	}

	/**
	 * Feuert folgende Events
	 * <li>{@link TabSelectedEvent}
	 * <li>{@link TabCloseEvent}
	 */
	@Override
	public EventBus eventBus() {
		return super.eventBus();
	}

	private class TabIndex {
		private TabbedPanePosition position;
		private int index;

		public TabIndex(TabbedPanePosition position, int index) {
			this.position = position;
			this.index = index;
		}

		public JTypedTabbedPane<T> getTabbedPane() {
			return position == TabbedPanePosition.LEFT ? leftTabs : rightTabs;
		}

		public TabbedPanePosition getPosition() {
			return position;
		}

		public int getIndex() {
			return index;
		}
	}

	private class TabbedPaneHandler implements FocusListener {

		private TabbedPanePosition postion;

		public TabbedPaneHandler(TabbedPanePosition postion) {
			this.postion = postion;
		}

		@Override
		public void focusGained(FocusEvent e) {
			// Nur wenn bereits selektierter Tab auf der anderen angegklickt wurde (Ansonsten wird
			// zuerst
			// onSelectTab ausgeführt, setzt lastGainedFocus = position )
			if (lastGainedFocus != postion) {
				Optional<T> previousTab = getSelectedTab();
				applyNewFocus();
				// Tab Select Event
				eventBus().post(new TabSelectedEvent<>(previousTab, getSelectedTab()));
			}
		}

		@Override
		public void focusLost(FocusEvent e) {

		}

		@Subscribe
		public void onTabClose(TabCloseEvent<T> event) {
			eventBus().post(event);
			if (!event.isCancelled()) {
				tabs.remove(event.getTab());
			}
		}

		@Subscribe
		public void onTabSelect(TabSelectedEvent<T> event) {
			if (lockTabSelectEvent) {
				return;
			}

			Optional<T> previousTab = getSelectedTab();

			// focusGained TabSelectedEvent wird nicht gefeuert, da lastGainedFocus auf aktuelle
			// Seite
			// umgestellt wird
			applyNewFocus();
			// Focus auf Seite, die den ausgewählten Tab enthält, umstellen
			getTabbedPane(postion).getComponent().requestFocus();

			eventBus().post(new TabSelectedEvent<>(previousTab.isPresent() ? previousTab : event.getPreviousTab(), event.getTab()));
		}

		@Subscribe
		public void onTabRemoved(TabRemovedEvent<T> event) {
			layoutSplitPaneDivider();
		}

		private void applyNewFocus() {
			lastGainedFocus = postion;
			getTabbedPane(postion.getOpposite()).setSelectedTabBoldFont(false);
			getTabbedPane(postion).setSelectedTabBoldFont(true);
		}
	}
}
