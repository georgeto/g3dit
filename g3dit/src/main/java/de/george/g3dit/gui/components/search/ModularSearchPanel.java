package de.george.g3dit.gui.components.search;

import java.awt.Component;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

import com.google.common.base.Strings;
import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.components.JDynamicMenuItem;
import de.george.g3dit.gui.components.JDynamicPopupMenu;
import de.george.g3dit.gui.renderer.FunctionalListCellRenderer;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.JComboBoxExt;
import de.george.g3utils.util.IOUtils;
import net.miginfocom.swing.MigLayout;
import one.util.streamex.StreamEx;

public class ModularSearchPanel<T> implements SearchFilterBuilder<T> {
	private final EditorContext ctx;
	private final List<Class<SearchFilterBuilder<T>>> filterBuilders;
	private SearchBuilderContainer rootBuilder;
	private JPanel panel;

	@SafeVarargs
	public ModularSearchPanel(EditorContext ctx, Class<SearchFilterBuilder<T>>... filterBuilders) {
		this.ctx = ctx;
		this.filterBuilders = Arrays.stream(filterBuilders).collect(Collectors.toList());
	}

	public ModularSearchPanel(EditorContext ctx, List<Class<SearchFilterBuilder<T>>> filterBuilders) {
		this.ctx = ctx;
		this.filterBuilders = new ArrayList<>(filterBuilders);
	}

	@Override
	public SearchFilter<T> buildFilter() {
		return rootBuilder.buildFilter();
	}

	@Override
	public boolean loadFilter(SearchFilter<T> filter) {
		rootBuilder = new SearchCombinatorContainer();
		if (!rootBuilder.loadFilter(filter)) {
			rootBuilder = new SearchFilterContainer();
			if (!rootBuilder.loadFilter(filter)) {
				updateSearchFilterGraph();
				return false;
			}
		}

		updateSearchFilterGraph();
		return true;
	}

	public void reset(boolean autoDetectFromClipboard) {
		rootBuilder = new SearchFilterContainer();
		if (autoDetectFromClipboard) {
			((SearchFilterContainer) rootBuilder).loadFromClipboard();
		}
		updateSearchFilterGraph();
		initFocus();
	}

	private enum InsertLocation {
		Append,
		RelativeBefore,
		RelativeAfter
	}

	private void appendSearchBuilder(SearchCombinatorContainer combinator, SearchBuilderContainer appendee) {
		combinator.addSearchBuilder(appendee);
		updateSearchFilterGraph();
	}

	private void appendSearchBuilder(SearchCombinatorContainer combinator, SearchBuilderContainer appendee, InsertLocation insertLocation,
			SearchBuilderContainer reference) {
		combinator.addSearchBuilder(appendee, insertLocation, reference);
		updateSearchFilterGraph();
	}

	private void prependSearchCombinator(SearchBuilderContainer container) {
		SearchCombinatorContainer combinator = new SearchCombinatorContainer();
		// Must be the root builder...
		if (container.parent == null) {
			if (container != rootBuilder) {
				throw new IllegalArgumentException();
			}

			combinator.addSearchBuilder(rootBuilder);
			rootBuilder = combinator;
		} else {
			SearchCombinatorContainer parent = container.parent;
			parent.replaceSearchBuilder(container, combinator);
			combinator.addSearchBuilder(container);
		}

		updateSearchFilterGraph();
	}

	private void deleteSearchBuilder(SearchBuilderContainer container) {
		if (container.parent == null) {
			throw new IllegalArgumentException();
		}

		container.parent.removeSearchBuilder(container);
		updateSearchFilterGraph();
	}

	private void dissolveSearchCombinator(SearchCombinatorContainer combinator) {
		if (combinator.parent == null) {
			if (combinator.filterBuilders.size() != 1) {
				throw new IllegalArgumentException();
			}

			if (combinator != rootBuilder) {
				throw new IllegalArgumentException();
			}

			// Use single child builder as rootBuilder
			SearchBuilderContainer onlyChild = combinator.filterBuilders.get(0);
			combinator.removeSearchBuilder(onlyChild);
			rootBuilder = onlyChild;
		} else {
			// Move all childs to parent
			combinator.parent.replaceSearchBuilder(combinator, combinator.filterBuilders);
		}

		updateSearchFilterGraph();
	}

	private void updateSearchFilterGraph() {
		panel.removeAll();

		Map<SearchCombinatorContainer, Integer> startRows = new HashMap<>();
		Deque<SearchBuilderContainer> stack = new ArrayDeque<>();
		stack.push(rootBuilder);

		int row = 0;
		int depth = 0;
		while (!stack.isEmpty()) {
			SearchBuilderContainer container = stack.pop();
			if (container instanceof ModularSearchPanel.SearchCombinatorContainer) {
				SearchCombinatorContainer combinator = (SearchCombinatorContainer) container;
				if (combinator.filterBuilders.isEmpty()) {
					panel.add(combinator, String.format("cell %d %d, gapright 15", depth, row));
					row++;
				} else if (!startRows.containsKey(combinator)) {
					startRows.put(combinator, row);
					stack.push(combinator);
					StreamEx.ofReversed(combinator.filterBuilders).forEach(stack::push);
					depth++;
				} else {
					depth--;
					int startRow = startRows.get(combinator);
					panel.add(combinator,
							String.format("cell %d %d, gapright 15, spany %d, aligny center", depth, startRow, row - startRow));
				}
			} else {
				SearchFilterContainer filter = (SearchFilterContainer) container;
				panel.add(filter, String.format("growx, spanx 100, cell %d %d", depth, row));
				row++;
			}
		}

		panel.revalidate();
		panel.repaint();
	}

	private void createComponent() {
		panel = new JPanel(new MigLayout("ins 0, fill", "[][][][][][][][][grow, fill]"));
		reset(true);
	}

	@Override
	public JComponent getComponent() {
		if (panel == null) {
			createComponent();
		}
		return panel;
	}

	@Override
	public void initFocus() {
		if (rootBuilder instanceof ModularSearchPanel.SearchFilterContainer) {
			((SearchFilterContainer) rootBuilder).initFocus();
		}
	}

	private abstract class SearchBuilderContainer extends JPanel {
		protected SearchCombinatorContainer parent;
		private JPopupMenu popupMenu;

		public SearchBuilderContainer() {
			setLayout(new MigLayout("ins 0, fillx"));
			this.popupMenu = new JDynamicPopupMenu();
			setComponentPopupMenu(popupMenu);
		}

		public abstract SearchFilter<T> buildFilter();

		public abstract boolean loadFilter(SearchFilter<T> filter);

		protected void addMenuItem(String text, String icon, Runnable callback) {
			addMenuItem(text, icon, callback, null);
		}

		protected void addMenuItem(String text, String icon, Runnable callback, Supplier<Boolean> enablePredicate) {
			JMenuItem menuItem = enablePredicate != null ? new JDynamicMenuItem(enablePredicate) : new JMenuItem();
			menuItem.setText(text);
			menuItem.setIcon(Icons.getImageIcon(icon));
			menuItem.addActionListener(a -> callback.run());
			popupMenu.add(menuItem);
		}

		@Override
		protected void addImpl(Component comp, Object constraints, int index) {
			super.addImpl(comp, constraints, index);
			if (comp instanceof JComponent) {
				((JComponent) comp).setComponentPopupMenu(popupMenu);
			}
		}

		protected Runnable ensureHasParent(Runnable runnable) {
			return () -> {
				if (parent == null) {
					prependSearchCombinator(this);
				}
				runnable.run();
			};
		}
	}

	private class SearchCombinatorContainer extends SearchBuilderContainer {
		private JToggleButton tbNegate;
		private JComboBoxExt<BooleanOperator> cbOperator;
		private List<SearchBuilderContainer> filterBuilders;

		public SearchCombinatorContainer() {
			addMenuItem(I.tr("Append filter"), Icons.Node.INSERT_CHILD, () -> appendSearchBuilder(this, new SearchFilterContainer()));
			addMenuItem(I.tr("Prepend combinator"), Icons.Node.INSERT, () -> prependSearchCombinator(this));
			addMenuItem(I.tr("Append combinator"), Icons.Node.INSERT_CHILD,
					() -> appendSearchBuilder(this, new SearchCombinatorContainer()));
			addMenuItem(I.tr("Clone combinator"), Icons.Node.INSERT_NEXT, ensureHasParent(() -> {
				SearchCombinatorContainer newCombinatorContainer = new SearchCombinatorContainer();
				newCombinatorContainer.loadFilter(buildFilter());
				appendSearchBuilder(parent, newCombinatorContainer, InsertLocation.RelativeAfter, this);
			}));
			addMenuItem(I.tr("Delete"), Icons.Action.DELETE, () -> deleteSearchBuilder(this), () -> parent != null);
			addMenuItem(I.tr("Dissolve"), Icons.Misc.CHAIN_MINUS, () -> dissolveSearchCombinator(this),
					() -> parent != null || filterBuilders.size() == 1);

			tbNegate = new JToggleButton("!", false);
			tbNegate.setToolTipText(I.tr("Negate"));
			tbNegate.setFocusable(false);
			cbOperator = new JComboBoxExt<>(BooleanOperator.And, BooleanOperator.Or);

			add(tbNegate, "split 2");
			add(cbOperator, "pushx, grow");

			filterBuilders = new ArrayList<>();
		}

		@Override
		public SearchFilter<T> buildFilter() {
			BooleanSearchFilter<T> filter = new BooleanSearchFilter<>(cbOperator.getSelectedItem(), tbNegate.isSelected());
			filterBuilders.stream().map(SearchBuilderContainer::buildFilter).forEach(filter::addFilter);
			return filter;
		}

		@Override
		public boolean loadFilter(SearchFilter<T> filter) {
			if (filter instanceof BooleanSearchFilter) {
				BooleanSearchFilter<T> typedFilter = (BooleanSearchFilter<T>) filter;
				tbNegate.setSelected(typedFilter.isNegate());
				cbOperator.setSelectedItem(typedFilter.getOperator());

				filterBuilders.clear();
				for (SearchFilter<T> childFilter : typedFilter.getFilters()) {
					SearchBuilderContainer childContainer = childFilter instanceof BooleanSearchFilter ? new SearchCombinatorContainer()
							: new SearchFilterContainer();
					childContainer.parent = this;
					childContainer.loadFilter(childFilter);
					filterBuilders.add(childContainer);
				}

				return true;
			} else {
				return false;
			}
		}

		public void addSearchBuilder(SearchBuilderContainer builder) {
			addSearchBuilder(builder, InsertLocation.Append, null);
		}

		public void addSearchBuilder(SearchBuilderContainer builder, InsertLocation insertLocation, SearchBuilderContainer reference) {
			if (!filterBuilders.contains(builder)) {
				if (insertLocation == InsertLocation.Append) {
					filterBuilders.add(builder);
				} else {
					int refIndex = filterBuilders.indexOf(reference);
					if (refIndex == -1) {
						refIndex = filterBuilders.size();
					} else if (insertLocation == InsertLocation.RelativeAfter) {
						refIndex++;
					}
					filterBuilders.add(refIndex, builder);
				}

				builder.parent = this;
			}
		}

		public void replaceSearchBuilder(SearchBuilderContainer oldBuilder, SearchBuilderContainer newBuilder) {
			int index = filterBuilders.indexOf(oldBuilder);
			filterBuilders.set(index, newBuilder);
			oldBuilder.parent = null;
			newBuilder.parent = this;
		}

		public void replaceSearchBuilder(SearchBuilderContainer oldBuilder, List<SearchBuilderContainer> newBuilders) {
			int index = filterBuilders.indexOf(oldBuilder);
			filterBuilders.remove(index);
			filterBuilders.addAll(index, newBuilders);
			oldBuilder.parent = null;
			newBuilders.forEach(newBuilder -> newBuilder.parent = this);
		}

		public void removeSearchBuilder(SearchBuilderContainer builder) {
			filterBuilders.remove(builder);
			builder.parent = null;
		}
	}

	private class SearchFilterContainer extends SearchBuilderContainer {
		private JComboBoxExt<Class<SearchFilterBuilder<T>>> cbFilter;
		private SearchFilterBuilder<T> filter;

		public SearchFilterContainer() {
			addMenuItem(I.tr("Add filter before"), Icons.Node.INSERT_PREVIOUS,
					ensureHasParent(() -> appendSearchBuilder(parent, new SearchFilterContainer(), InsertLocation.RelativeBefore, this)));
			addMenuItem(I.tr("Add filter after"), Icons.Node.INSERT_NEXT,
					ensureHasParent(() -> appendSearchBuilder(parent, new SearchFilterContainer(), InsertLocation.RelativeAfter, this)));
			addMenuItem(I.tr("Clone filter"), Icons.Node.INSERT_NEXT, ensureHasParent(() -> {
				SearchFilterContainer newFilterContainer = new SearchFilterContainer();
				newFilterContainer.loadFilter(buildFilter());
				appendSearchBuilder(parent, newFilterContainer, InsertLocation.RelativeAfter, this);
			}));
			addMenuItem(I.tr("Prepend combinator"), Icons.Node.INSERT, () -> prependSearchCombinator(this));
			addMenuItem(I.tr("Delete"), Icons.Action.DELETE, () -> deleteSearchBuilder(this), () -> parent != null);

			cbFilter = new JComboBoxExt<>(filterBuilders, Class.class);
			cbFilter.setRenderer(new FunctionalListCellRenderer<Class<SearchFilterBuilder<T>>>(SearchFilterBuilder::getTitle));

			add(cbFilter);

			cbFilter.addItemListener(e -> filterMetaChanged());
			filterMetaChanged();
		}

		@Override
		public SearchFilter<T> buildFilter() {
			return filter.buildFilter();
		}

		@Override
		public boolean loadFilter(SearchFilter<T> filter) {
			for (Class<SearchFilterBuilder<T>> builderClass : filterBuilders) {
				cbFilter.setSelectedItem(builderClass);
				if (this.filter.loadFilter(filter)) {
					return true;
				}
			}
			return false;
		}

		public void loadFromClipboard() {
			String clipboard = IOUtils.getClipboardContent();
			if (!Strings.isNullOrEmpty(clipboard)) {
				for (Class<SearchFilterBuilder<T>> builderClass : filterBuilders) {
					if (SearchFilterBuilder.newInstance(builderClass, ctx).loadFromString(clipboard)) {
						cbFilter.setSelectedItem(builderClass);
					}
				}
			}
		}

		private void filterMetaChanged() {
			String previousText = null;
			if (filter != null) {
				remove(filter.getComponent());
				previousText = filter.storeToString();
			}

			filter = SearchFilterBuilder.newInstance(cbFilter.getSelectedItem(), ctx);

			// Load from previous filter
			if (previousText == null || !filter.loadFromString(previousText)) {
				// Load from clipboard
				String clipboard = IOUtils.getClipboardContent();
				if (!Strings.isNullOrEmpty(clipboard)) {
					filter.loadFromString(clipboard);
				}
			}

			add(filter.getComponent(), "pushx, growx");
			revalidate();
			repaint();
			initFocus();
		}

		public void initFocus() {
			filter.initFocus();
		}
	}
}
