package de.george.g3dit.gui.components;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.MatcherEditor;
import de.george.g3dit.util.FileChangeMonitor;
import de.george.g3utils.gui.SwingUtils;
import net.miginfocom.swing.MigLayout;

public class ListManageAndEdit<E> {
	private String searchTooltip;
	private Function<JTextField, MatcherEditor<? super E>> supplyMatcherEditor;
	private ListCellRenderer<? super E> cellRenderer;
	private Supplier<Optional<E>> supplyNewItem;
	private FileChangeMonitor changeMonitor;
	private Consumer<E> onSelect;
	private Predicate<E> onDelete;
	private Predicate<List<E>> onMultiDelete;

	private int orientation = JSplitPane.HORIZONTAL_SPLIT;
	private String title;

	private EventList<E> items;
	private JSplitPane splitPane;
	private ManagingPanel managingPanel;
	private JComponent editingComponent;

	private ListManageAndEdit(EventList<E> items, Supplier<Optional<E>> supplyNewItem, JComponent editingComponent) {
		this.editingComponent = editingComponent;
		this.items = items;
		this.supplyNewItem = supplyNewItem;
	}

	public static <E> ListManageAndEdit<E> create(EventList<E> items, Supplier<Optional<E>> supplyNewItem, JComponent editingComponent) {
		return new ListManageAndEdit<>(items, supplyNewItem, editingComponent);
	}

	public ListManageAndEdit<E> build() {
		splitPane = new JSplitPane();
		splitPane.setOrientation(orientation);
		splitPane.setResizeWeight(0);

		if (changeMonitor == null) {
			changeMonitor = new FileChangeMonitor() {
				@Override
				public boolean isFileChanged() {
					return false;
				}

				@Override
				public void fileChanged() {}
			};
		}

		managingPanel = new ManagingPanel();
		splitPane.setLeftComponent(managingPanel);

		splitPane.setRightComponent(editingComponent);

		return this;
	}

	private class ManagingPanel extends JPanel {
		private JEventList<E> itemsList;

		public ManagingPanel() {
			setLayout(new MigLayout("fill", "[grow, fill]"));

			if (title != null) {
				JLabel lblTitle = SwingUtils.createBoldLabel(title);
				add(lblTitle, "wrap");
			}

			EventList<E> filteredItems = items;
			if (supplyMatcherEditor != null) {
				JTextField tfFilter = SwingUtils.createUndoTF();
				tfFilter.setToolTipText(searchTooltip);
				add(tfFilter, "sgx items, wrap");
				filteredItems = new FilterList<>(items, supplyMatcherEditor.apply(tfFilter));
			}

			itemsList = new JEventList<>(filteredItems);
			itemsList.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			if (cellRenderer != null) {
				itemsList.setCellRenderer(cellRenderer);
			}

			if (onSelect != null) {
				itemsList.addListSelectionListener(e -> {
					if (!e.getValueIsAdjusting()) {
						onSelect.accept(itemsList.getSelectedValue());
					}
				});
			}

			itemsList.getModel().addListDataListener(new ListDataListener() {
				private int previousCount = 0;

				private void updateCount() {
					if (previousCount == 0 && itemsList.getModel().getSize() > 0 && itemsList.getSelectedIndex() == -1) {
						SwingUtilities.invokeLater(() -> {
							if (itemsList.getModel().getSize() > 0) {
								itemsList.setSelectedIndex(0);
							}
						});
					}

					previousCount = itemsList.getModel().getSize();
				}

				@Override
				public void intervalRemoved(ListDataEvent e) {
					updateCount();
				}

				@Override
				public void intervalAdded(ListDataEvent e) {
					updateCount();
				}

				@Override
				public void contentsChanged(ListDataEvent e) {
					updateCount();
				}
			});

			add(new JScrollPane(itemsList), "sgx items, pushy, growy, wrap, gapbottom 5px");
			add(new ListModificationControl<>(changeMonitor, itemsList, filteredItems, supplyNewItem, onDelete, onMultiDelete),
					"sgx items, wrap");

			if (itemsList.getModel().getSize() > 0) {
				itemsList.setSelectedIndex(0);
			}
		}

		public List<E> getSelectedValues() {
			return itemsList.getSelectedValuesList();
		}

		public void setSelectedValue(E value) {
			itemsList.setSelectedValue(value, true);
		}

		public void setSelectedValues(List<E> values) {
			itemsList.clearSelection();
			itemsList.getSelectionModel().getSelected().addAll(values);
		}
	}

	public ListManageAndEdit<E> searchTooltip(String searchTooltip) {
		this.searchTooltip = searchTooltip;
		return this;
	}

	public ListManageAndEdit<E> matcherEditor(Function<JTextField, MatcherEditor<? super E>> supplyMatcherEditor) {
		this.supplyMatcherEditor = supplyMatcherEditor;
		return this;
	}

	public ListManageAndEdit<E> cellRenderer(ListCellRenderer<? super E> cellRenderer) {
		this.cellRenderer = cellRenderer;
		return this;
	}

	public ListManageAndEdit<E> changeMonitor(FileChangeMonitor changeMonitor) {
		this.changeMonitor = changeMonitor;
		return this;
	}

	public ListManageAndEdit<E> onSelect(Consumer<E> onSelect) {
		this.onSelect = onSelect;
		return this;
	}

	public ListManageAndEdit<E> onDelete(Predicate<E> onDelete) {
		this.onDelete = onDelete;
		return this;
	}

	public ListManageAndEdit<E> onMultiDelete(Predicate<List<E>> onMultiDelete) {
		this.onMultiDelete = onMultiDelete;
		return this;
	}

	public ListManageAndEdit<E> orientation(int orientation) {
		this.orientation = orientation;
		return this;
	}

	public ListManageAndEdit<E> title(String title) {
		this.title = title;
		return this;
	}

	public JSplitPane getContent() {
		return splitPane;
	}

	public List<E> getSelectedValues() {
		return managingPanel.getSelectedValues();
	}

	public void setSelectedValue(E value) {
		managingPanel.setSelectedValue(value);
	}

	public void setSelectedValues(List<E> values) {
		managingPanel.setSelectedValues(values);
	}
}
