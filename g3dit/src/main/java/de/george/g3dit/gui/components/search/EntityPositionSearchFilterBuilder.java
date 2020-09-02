package de.george.g3dit.gui.components.search;

import java.text.ParseException;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.teamunify.i18n.I;

import de.george.g3dit.entitytree.filter.PositionEntityFilter;
import de.george.g3dit.gui.components.FloatSpinner;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.archive.eCEntity;
import net.miginfocom.swing.MigLayout;

@SearchFilterBuilderDesc(title = "Position")
public class EntityPositionSearchFilterBuilder implements SearchFilterBuilder<eCEntity> {
	private JTextField tfFilter;
	private FloatSpinner fsDistance;
	private JCheckBox cbIgnoreY;
	private JPanel comp;

	public EntityPositionSearchFilterBuilder() {
		tfFilter = SwingUtils.createUndoTF();
		fsDistance = new FloatSpinner(100.0f);
		fsDistance.setVal(100.0f);
		cbIgnoreY = new JCheckBox(I.tr("Ignore Y"), false);

		comp = new JPanel(new MigLayout("ins 0"));
		comp.add(tfFilter, "width 100:300:400");
		comp.add(fsDistance, "width 50:100:150");
		comp.add(cbIgnoreY);
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
		bCVector position = Misc.stringToPosition(tfFilter.getText());
		try {
			fsDistance.commitEdit();
		} catch (ParseException e) {
		}
		return new PositionEntityFilter(position, fsDistance.getVal(), cbIgnoreY.isSelected());
	}

	@Override
	public boolean loadFilter(SearchFilter<eCEntity> filter) {
		if (filter instanceof PositionEntityFilter typedFilter) {
			tfFilter.setText(typedFilter.getPositonToMatch() != null ? typedFilter.getPositonToMatch().toString() : "");
			fsDistance.setVal(typedFilter.getPositionTolerance());
			cbIgnoreY.setSelected(typedFilter.isIgnoreY());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean loadFromString(String text) {
		bCVector position = Misc.stringToPosition(text);
		if (position != null) {
			tfFilter.setText(position.toString());
			return true;
		}
		return false;
	}

	@Override
	public String storeToString() {
		return tfFilter.getText();
	}
}
