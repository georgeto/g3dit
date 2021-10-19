package de.george.g3dit.gui.components.search;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.george.g3dit.gui.components.search.ByteSearchFilter.MatchMode;
import de.george.g3utils.gui.JComboBoxExt;
import de.george.g3utils.gui.SwingUtils;
import de.george.lrentnode.archive.eCEntity;
import net.miginfocom.swing.MigLayout;

@SearchFilterBuilderDesc(title = "Bytes")
public class ByteSearchFilterBuilder implements SearchFilterBuilder<eCEntity> {
	private JComboBoxExt<MatchMode> cbMatchMode;
	private JTextField tfFilter;
	private JPanel comp;

	public ByteSearchFilterBuilder() {
		cbMatchMode = new JComboBoxExt<>(MatchMode.values());
		tfFilter = SwingUtils.createUndoTF();

		comp = new JPanel(new MigLayout("ins 0, fillx"));
		comp.add(cbMatchMode);
		comp.add(tfFilter, "pushx, growx");
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
		return new ByteSearchFilter(cbMatchMode.getSelectedItem(), tfFilter.getText());
	}

	@Override
	public boolean loadFilter(SearchFilter<eCEntity> filter) {
		if (filter instanceof ByteSearchFilter typedFilter) {
			cbMatchMode.setSelectedItem(typedFilter.getMatchMode());
			tfFilter.setText(typedFilter.getDataToMatch());
			return true;
		} else {
			return false;
		}
	}
}
