package de.george.g3dit.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class ListUtil {
	public static Runnable enableOnEqual(JList<?> list, JComponent comp, int value) {
		return enableOnEqual(list, comp, value, () -> true);
	}

	public static Runnable enableOnLesserEqual(JList<?> list, JComponent comp, int max) {
		return enableOnLesserEqual(list, comp, max, () -> true);
	}

	public static Runnable enableOnGreaterEqual(JList<?> list, JComponent comp, int min) {
		return enableOnGreaterEqual(list, comp, min, () -> true);
	}

	public static Runnable enableOnEqual(JList<?> list, JComponent comp, int value, Supplier<Boolean> condition) {
		comp.setEnabled(false);
		return addListSelectionListener(list, () -> comp.setEnabled(list.getSelectedValuesList().size() == value && condition.get()));
	}

	public static Runnable enableOnLesserEqual(JList<?> list, JComponent comp, int max, Supplier<Boolean> condition) {
		comp.setEnabled(false);
		return addListSelectionListener(list, () -> comp.setEnabled(list.getSelectedValuesList().size() <= max && condition.get()));
	}

	public static Runnable enableOnGreaterEqual(JList<?> list, JComponent comp, int min, Supplier<Boolean> condition) {
		comp.setEnabled(false);
		return addListSelectionListener(list, () -> comp.setEnabled(list.getSelectedValuesList().size() >= min && condition.get()));
	}

	private static Runnable addListSelectionListener(JList<?> list, Runnable listener) {
		list.getSelectionModel().addListSelectionListener(x -> listener.run());
		return listener;
	}

	public static <T> MouseListener createDoubleClickListener(Consumer<T> itemConsumer) {
		return new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				@SuppressWarnings("unchecked")
				JList<T> list = (JList<T>) me.getSource();
				if (me.getClickCount() == 2) {
					int index = list.locationToIndex(me.getPoint());
					if (index != -1) {
						itemConsumer.accept(list.getModel().getElementAt(index));
					}
				}
			}
		};
	}

	public static ListDataListener createDataChangeListener(Consumer<ListDataEvent> listener) {
		return new ListDataListener() {

			@Override
			public void intervalRemoved(ListDataEvent e) {
				listener.accept(e);
			}

			@Override
			public void intervalAdded(ListDataEvent e) {
				listener.accept(e);
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				listener.accept(e);
			}
		};
	}

	public static void autoSelectOnChange(JList<?> list) {
		list.getModel().addListDataListener(ListUtil.createDataChangeListener(e -> SwingUtilities.invokeLater(() -> {
			if (list.getSelectionModel().isSelectionEmpty() && list.getModel().getSize() > 0) {
				list.setSelectedIndex(0);
			}

			int selectedIndex = list.getSelectedIndex();
			if (selectedIndex != -1) {
				list.ensureIndexIsVisible(selectedIndex);
			}
		})));
	}
}
