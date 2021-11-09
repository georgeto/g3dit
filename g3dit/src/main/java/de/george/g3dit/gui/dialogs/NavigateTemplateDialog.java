package de.george.g3dit.gui.dialogs;

import java.awt.Component;
import java.awt.Window;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jidesoft.dialog.ButtonPanel;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;
import de.george.g3dit.gui.components.JEventList;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.ListUtil;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import net.miginfocom.swing.MigLayout;

public class NavigateTemplateDialog extends ExtStandardDialog {
	private JEventList<TemplateCacheEntry> list;
	private UndoableTextField tfSearch;
	private Comparator<String> sortOrder;

	public NavigateTemplateDialog(Window owner, EditorContext ctx) {
		this(owner, ctx, e -> true);
	}

	public NavigateTemplateDialog(Window owner, EditorContext ctx, Predicate<TemplateCacheEntry> filter) {
		super(owner, I.tr("Open template"), true);
		setSize(600, 400);

		tfSearch = SwingUtils.createUndoTF();
		tfSearch.getDocument().addDocumentListener(SwingUtils.createDocumentListener(this::updateSortOrder));
		updateSortOrder();

		BasicEventList<TemplateCacheEntry> entries = Caches.template(ctx).getAllEntities().filter(filter).collect(BasicEventList::new,
				List::add, List::addAll);

		Map<String, String> focusNames = Caches.stringtable(ctx).getFocusNamesOrEmpty();
		TextComponentMatcherEditor<TemplateCacheEntry> filteredEntriesMatcherEditor = new TextComponentMatcherEditor<>(tfSearch,
				(baseList, entry) -> {
					baseList.add(entry.getName());
					baseList.add(entry.getGuid());
					String focusName = focusNames.get(entry.getName());
					if (focusName != null) {
						baseList.add(focusName);
					}
				});
		FilterList<TemplateCacheEntry> filtererdEntries = new FilterList<>(entries, filteredEntriesMatcherEditor);

		SortedList<TemplateCacheEntry> sortedEntries = new SortedList<>(filtererdEntries,
				(e1, e2) -> sortOrder.compare(e1.getName(), e2.getName()));

		list = new JEventList<>(sortedEntries);
		list.setCellRenderer(new TemplateCacheEntryListCellRenderer());
		ListUtil.autoSelectOnChange(list);

	}

	public List<TemplateCacheEntry> getSelectedEntries() {
		return list.getSelectedValuesList();
	}

	@Override
	public JComponent createContentPanel() {
		JPanel panel = new JPanel(new MigLayout("insets 10, fill", "[fill]", "[][]10[fill, grow]"));
		panel.add(new JLabel(I.tr("Enter template name/guid")), "wrap");
		panel.add(tfSearch, "wrap");
		panel.add(new JScrollPane(list));

		return panel;
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();

		Action okAction = SwingUtils.createAction(I.tr("Open"), () -> {
			if (!list.getSelectedValuesList().isEmpty()) {
				setDialogResult(RESULT_AFFIRMED);
			}
			dispose();
		});
		okAction.setEnabled(false);

		addButton(buttonPanel, okAction, ButtonPanel.AFFIRMATIVE_BUTTON);
		addDefaultCancelButton(buttonPanel);

		list.addListSelectionListener(e -> okAction.setEnabled(!list.getSelectedValuesList().isEmpty()));
		list.addMouseListener(ListUtil.createDoubleClickListener(i -> {
			setDialogResult(RESULT_AFFIRMED);
			dispose();
		}));

		return buttonPanel;
	}

	private void updateSortOrder() {
		sortOrder = new StringAutoCompleteSorter(tfSearch.getText(), false);
	}

	private static class TemplateCacheEntryListCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			TemplateCacheEntry entry = (TemplateCacheEntry) value;
			if (entry.isHelperParent()) {
				setIcon(Icons.getImageIcon(Icons.Document.LETTER_I));
				setToolTipText(SwingUtils.getMultilineText(I.tr("Helper template entity whose guid is registered in .lrtpldatasc."),
						I.tr("Also referred to as item guid in the template general tab and in Tiny Hexer.")));
			} else if (entry.getRefTemplate() != null) {
				setIcon(Icons.getImageIcon(Icons.Document.LETTER_R));
				setToolTipText(SwingUtils.getMultilineText(I.tr("Template entity refers to other template entity."),
						I.tr("Mainly used in object groups.")));
			} else {
				setIcon(Icons.getImageIcon(Icons.Document.LETTER_T));
				setToolTipText(SwingUtils.getMultilineText(
						I.tr("Template entity with property sets that serves as a template for world data entities."),
						I.tr("Also referred to as reference guid in the template general tab and in Tiny Hexer.")));
			}

			return this;
		}

	}
}
