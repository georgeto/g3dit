package de.george.g3dit.gui.components.search;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.george.g3dit.entitytree.filter.GuidEntityFilter;
import de.george.g3dit.entitytree.filter.GuidEntityFilter.MatchMode;
import de.george.g3utils.gui.JComboBoxExt;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.GuidUtil;
import de.george.lrentnode.archive.eCEntity;
import net.miginfocom.swing.MigLayout;

@SearchFilterBuilderDesc(title = "Guid")
public class EntityGuidSearchFilterBuilder implements SearchFilterBuilder<eCEntity> {
	private JComboBoxExt<MatchMode> cbMatchMode;
	private JTextField tfFilter;
	private JPanel comp;

	public EntityGuidSearchFilterBuilder() {
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
		return new GuidEntityFilter(cbMatchMode.getSelectedItem(), tfFilter.getText());
	}

	@Override
	public boolean loadFilter(SearchFilter<eCEntity> filter) {
		if (filter instanceof GuidEntityFilter typedFilter) {
			cbMatchMode.setSelectedItem(typedFilter.getMatchMode());
			tfFilter.setText(typedFilter.getGuidToMatch());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean loadFromString(String text) {
		String guid = GuidUtil.parseGuid(text);
		if (guid != null) {
			tfFilter.setText(guid);
			return true;
		}
		return false;
	}

	@Override
	public String storeToString() {
		return tfFilter.getText();
	}
}
