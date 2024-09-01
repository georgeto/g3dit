package de.george.g3dit.gui.components;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.EventList;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.util.FileChangeMonitor;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.ListTableModel;
import net.miginfocom.swing.MigLayout;

public class TableModificationControl<T> extends JPanel {
	public TableModificationControl(FileChangeMonitor changeMonitor, JTable table, ListTableModel<T> model, Supplier<T> entrySupplier,
			Function<T, Optional<T>> cloneEntry) {
		this(changeMonitor, table, entrySupplier, cloneEntry, model::addEntry, model::getEntry, model::removeEntries, null, null);
	}

	public TableModificationControl(FileChangeMonitor changeMonitor, JTable table, ListTableModel<T> model, Supplier<T> entrySupplier) {
		this(changeMonitor, table, model, entrySupplier, null);
	}

	public TableModificationControl(FileChangeMonitor changeMonitor, JTable table, EventList<T> source, Supplier<T> entrySupplier,
			Function<T, Optional<T>> cloneEntry, boolean movable) {
		this(changeMonitor, table, entrySupplier, cloneEntry, source::add, source::get, rows -> {
			ImmutableSortedSet<Integer> sortedRows = ImmutableSortedSet.<Integer>reverseOrder().addAll(rows).build();
			for (int row : sortedRows) {
				source.remove(row);
			}
			return !sortedRows.isEmpty();
		}, movable ? row -> {
			if (row > 0) {
				source.add(row - 1, source.remove((int) row));
				table.getSelectionModel().setSelectionInterval(row - 1, row - 1);
			}
		} : null, movable ? row -> {
			if (row < source.size() - 1) {
				T entry = source.remove((int) row);
				if (row == source.size())
					source.add(entry);
				else
					source.add(row + 1, entry);
				table.getSelectionModel().setSelectionInterval(row + 1, row + 1);
			}
		} : null);

	}

	public TableModificationControl(FileChangeMonitor changeMonitor, JTable table, EventList<T> source, Supplier<T> entrySupplier) {
		this(changeMonitor, table, source, entrySupplier, null, false);
	}

	public TableModificationControl(FileChangeMonitor changeMonitor, JTable table, Supplier<T> entrySupplier,
			Function<T, Optional<T>> cloneEntry, Consumer<T> addEntry, Function<Integer, T> getEntry,
			Function<Iterable<Integer>, Boolean> removeEntries, Consumer<Integer> moveUp, Consumer<Integer> moveDown) {
		setLayout(new MigLayout("fillx, insets 0"));

		TableModel model = table.getModel();

		if (entrySupplier != null) {
			JButton btnAdd = new JButton(I.tr("Add"), Icons.getImageIcon(Icons.Action.ADD));
			btnAdd.addActionListener(a -> {
				T newEntry = entrySupplier.get();
				if (newEntry != null) {
					addEntry.accept(newEntry);
					if (changeMonitor != null)
						changeMonitor.fileChanged();
				}
			});
			add(btnAdd, "sg tmcbtn, growx, pushx");
		}

		if (cloneEntry != null) {
			JButton btnClone = new JButton(I.tr("Clone"), Icons.getImageIcon(Icons.Action.CLONE));
			btnClone.addActionListener(a -> {
				List<T> selectedEntries = ImmutableList.copyOf(TableUtil.getSelectedRows(table).map(getEntry));
				for (T selected : selectedEntries) {
					Optional<T> entry = cloneEntry.apply(selected);
					if (entry.isPresent()) {
						addEntry.accept(entry.get());
						if (changeMonitor != null)
							changeMonitor.fileChanged();
					}
				}
			});
			add(btnClone, "sg tmcbtn, growx, pushx");
			TableUtil.enableOnGreaterEqual(table, btnClone, 1);
		}

		JLabel lblCount = new JLabel(Integer.toString(model.getRowCount()));
		lblCount.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblCount, "width 30!");
		model.addTableModelListener(e -> lblCount.setText(Integer.toString(model.getRowCount())));

		JButton btnRemove = new JButton(I.tr("Delete"), Icons.getImageIcon(Icons.Action.DELETE));
		add(btnRemove, "sg tmcbtn, growx, pushx");
		btnRemove.addActionListener(a -> {
			if (removeEntries.apply(TableUtil.getSelectedRows(table))) {
				if (changeMonitor != null)
					changeMonitor.fileChanged();
			}
		});

		if (moveUp != null) {
			JButton btnMoveUp = new JButton(I.tr("Move up"), Icons.getImageIcon(Icons.Arrow.UP));
			add(btnMoveUp, "sg tmcbtn, growx, pushx");
			btnMoveUp.addActionListener(a -> {
				TableUtil.withSelectedRow(table, moveUp);
				if (changeMonitor != null)
					changeMonitor.fileChanged();
			});
			TableUtil.enableOnEqual(table, btnMoveUp, 1);
		}

		if (moveDown != null) {
			JButton btnMoveDown = new JButton(I.tr("Move down"), Icons.getImageIcon(Icons.Arrow.DOWN));
			add(btnMoveDown, "sg tmcbtn, growx, pushx");
			btnMoveDown.addActionListener(a -> {
				TableUtil.withSelectedRow(table, moveDown);
				if (changeMonitor != null)
					changeMonitor.fileChanged();
			});
			TableUtil.enableOnEqual(table, btnMoveDown, 1);
		}
	}
}
