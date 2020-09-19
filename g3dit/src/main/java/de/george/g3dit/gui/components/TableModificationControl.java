package de.george.g3dit.gui.components;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;

import com.google.common.collect.ImmutableSortedSet;

import ca.odell.glazedlists.EventList;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.util.FileChangeMonitor;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.ListTableModel;
import net.miginfocom.swing.MigLayout;

public class TableModificationControl<T> extends JPanel {
	public TableModificationControl(FileChangeMonitor changeMonitor, JTable table, ListTableModel<T> model, Supplier<T> entrySupplier) {
		this(changeMonitor, table, entrySupplier, model::addEntry, model::removeEntries);
	}

	public TableModificationControl(FileChangeMonitor changeMonitor, JTable table, EventList<T> source, Supplier<T> entrySupplier) {
		this(changeMonitor, table, entrySupplier, source::add, rows -> {
			ImmutableSortedSet<Integer> sortedRows = ImmutableSortedSet.<Integer>reverseOrder().addAll(rows).build();
			for (int row : sortedRows) {
				source.remove(row);
			}
			return !sortedRows.isEmpty();
		});
	}

	public TableModificationControl(FileChangeMonitor changeMonitor, JTable table, Supplier<T> entrySupplier, Consumer<T> addEntry,
			Function<Iterable<Integer>, Boolean> removeEntries) {
		setLayout(new MigLayout("fillx, insets 0", "[grow][][grow]"));

		TableModel model = table.getModel();

		JButton btnAdd = new JButton("Hinzufügen", Icons.getImageIcon(Icons.Action.ADD));
		btnAdd.addActionListener(a -> {
			T newEntry = entrySupplier.get();
			if (newEntry != null) {
				addEntry.accept(newEntry);
				if (changeMonitor != null) {
					changeMonitor.fileChanged();
				}
			}
		});
		add(btnAdd, "sg tmcbtn, growx");

		JLabel lblCount = new JLabel(Integer.toString(model.getRowCount()));
		lblCount.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblCount, "width 30!");
		model.addTableModelListener(e -> lblCount.setText(Integer.toString(model.getRowCount())));

		JButton btnRemove = new JButton("Löschen", Icons.getImageIcon(Icons.Action.DELETE));
		add(btnRemove, "sg tmcbtn, growx");
		btnRemove.addActionListener(a -> {
			if (removeEntries.apply(TableUtil.getSelectedRows(table))) {
				if (changeMonitor != null) {
					changeMonitor.fileChanged();
				}
			}
		});
	}
}
