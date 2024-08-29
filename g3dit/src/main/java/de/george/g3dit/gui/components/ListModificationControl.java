package de.george.g3dit.gui.components;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.google.common.collect.ImmutableList;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.EventList;
import de.george.g3dit.util.FileChangeMonitor;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.ListUtil;
import net.miginfocom.swing.MigLayout;

public class ListModificationControl<T> extends JPanel {
	public ListModificationControl(FileChangeMonitor changeMonitor, JEventList<T> list, EventList<T> source, Supplier<Optional<T>> onAdd) {
		this(changeMonitor, list, source, onAdd, null);
	}

	public ListModificationControl(FileChangeMonitor changeMonitor, JEventList<T> list, EventList<T> source, Supplier<Optional<T>> onAdd,
			Predicate<T> onDelete) {
		this(changeMonitor, list, source, onAdd, null, onDelete, null);
	}

	public ListModificationControl(FileChangeMonitor changeMonitor, JEventList<T> list, EventList<T> source, Supplier<Optional<T>> onAdd,
			Function<T, Optional<T>> onClone, Predicate<T> onDelete, Predicate<List<T>> onMultiDelete) {
		setLayout(new MigLayout("fillx, insets 0"));

		JButton btnAdd = new JButton(I.tr("Add"), Icons.getImageIcon(Icons.Action.ADD));
		btnAdd.addActionListener(a -> {
			Optional<T> entry = onAdd.get();
			if (entry.isPresent()) {
				int index = source.size();
				source.add(index, entry.get());
				list.setSelectedIndex(index);
				list.ensureIndexIsVisible(index);
				changeMonitor.fileChanged();
			}
		});
		add(btnAdd, "sg tmcbtn, growx, pushx");

		if (onClone != null) {
			JButton btnClone = new JButton(I.tr("Clone"), Icons.getImageIcon(Icons.Action.CLONE));
			btnClone.addActionListener(a -> {
				List<T> selectedElements = ImmutableList.copyOf(list.getSelectionModel().getSelected());
				for (T selected : selectedElements) {
					Optional<T> entry = onClone.apply(selected);
					if (entry.isPresent()) {
						int index = source.size();
						source.add(index, entry.get());
						list.setSelectedIndex(index);
						list.ensureIndexIsVisible(index);
						changeMonitor.fileChanged();
					}
				}
			});
			add(btnClone, "sg tmcbtn, growx, pushx");
			ListUtil.enableOnGreaterEqual(list, btnClone, 1);
		}

		JLabel lblCount = new JLabel(Integer.toString(list.getModel().getSize()));
		lblCount.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblCount, "width 30!");
		source.addListEventListener(e -> lblCount.setText(Integer.toString(list.getModel().getSize())));

		JButton btnRemove = new JButton(I.tr("Delete"), Icons.getImageIcon(Icons.Action.DELETE));
		add(btnRemove, "sg tmcbtn, growx, pushx");
		btnRemove.addActionListener(a -> {
			int[] selected = list.getSelectedIndices();
			List<T> selectedElements = ImmutableList.copyOf(list.getSelectionModel().getSelected());
			if (selected.length > 0) {
				if (onMultiDelete != null) {
					if (!onMultiDelete.test(selectedElements)) {
						return;
					}
				} else if (onDelete != null) {
					if (!onDelete.test(selectedElements.get(0))) {
						return;
					}
				}

				for (int i = selected.length - 1; i >= 0; i--) {
					source.remove(selected[i]);
				}
				changeMonitor.fileChanged();
			}
		});
		ListUtil.enableOnGreaterEqual(list, btnRemove, 1);
	}
}
