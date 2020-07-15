package de.george.g3dit.gui.components.search;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.StringtableCache;
import de.george.g3dit.entitytree.filter.NameEntityFilter;
import de.george.g3dit.entitytree.filter.NameEntityFilter.MatchMode;
import de.george.g3utils.gui.JComboBoxExt;
import de.george.g3utils.gui.SwingUtils;
import de.george.lrentnode.archive.eCEntity;
import net.miginfocom.swing.MigLayout;

@SearchFilterBuilderDesc(title = "Name")
public class NameSearchFilterBuilder implements SearchFilterBuilder<eCEntity> {
	protected EditorContext ctx;
	protected JComboBoxExt<MatchMode> cbMatchMode;
	protected JTextField tfFilter;
	private JCheckBox cbRegex;
	private JPanel comp;

	public NameSearchFilterBuilder(EditorContext ctx) {
		this.ctx = ctx;
		cbMatchMode = new JComboBoxExt<>(MatchMode.Name, MatchMode.FocusName);
		tfFilter = SwingUtils.createUndoTF();
		cbRegex = new JCheckBox("Regex", false);

		comp = new JPanel(new MigLayout("ins 0, fillx"));
		comp.add(cbMatchMode);
		comp.add(tfFilter, "pushx, growx");
		comp.add(cbRegex);
	}

	@Override
	public JComponent getComponent() {
		return comp;
	}

	@Override
	public void initFocus() {
		tfFilter.requestFocusInWindow();
	}

	@Override
	public SearchFilter<eCEntity> buildFilter() {
		return new NameEntityFilter(cbMatchMode.getSelectedItem(), tfFilter.getText(), cbRegex.isSelected(),
				Caches.stringtable(ctx).getFocusNamesOrEmpty());
	}

	@Override
	public boolean loadFilter(SearchFilter<eCEntity> filter) {
		if (filter instanceof NameEntityFilter) {
			NameEntityFilter typedFilter = (NameEntityFilter) filter;
			cbMatchMode.setSelectedItem(typedFilter.getMatchMode());
			tfFilter.setText(typedFilter.getTextToMatch());
			cbRegex.setSelected(typedFilter.isRegex());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean loadFromString(String text) {
		StringtableCache stringtableCache = Caches.stringtable(ctx);
		if (stringtableCache.getFocusNameSet().contains(text)) {
			cbMatchMode.setSelectedItem(MatchMode.FocusName);
			tfFilter.setText(text);
			return true;
		}

		return false;
	}

	@Override
	public String storeToString() {
		return tfFilter.getText();
	}
}
