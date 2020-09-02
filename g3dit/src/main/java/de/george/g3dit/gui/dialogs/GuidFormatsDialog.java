package de.george.g3dit.gui.dialogs;

import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.teamunify.i18n.I;

import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.structure.GuidUtil;
import net.miginfocom.swing.MigLayout;

public class GuidFormatsDialog extends ExtStandardDialog {
	public GuidFormatsDialog(Window owner) {
		super(owner, I.tr("Guid Formate"), false);
		setType(Type.UTILITY);
		setSize(400, 300);
		setDefaultCancelAction(SwingUtils.createAction(this::cancel));
	}

	@Override
	public JComponent createContentPanel() {
		JPanel panel = new JPanel(new MigLayout("insets 10, fill", "[fill, grow]"));

		panel.add(new JLabel(I.tr("Guid eingeben")), "wrap");
		UndoableTextField tfEnterGuid = SwingUtils.createUndoTF();
		panel.add(tfEnterGuid, "wrap");

		JTextField tfHex = new JTextField();
		tfHex.setEditable(false);

		JTextField tfGuidText = new JTextField();
		tfGuidText.setEditable(false);

		JTextField tfGroup = new JTextField();
		tfGroup.setEditable(false);

		JTextField tfPlain = new JTextField();
		tfPlain.setEditable(false);

		panel.add(new JLabel(I.trc("Guid", "Hex")), "gaptop 15,wrap");
		panel.add(tfHex, "wrap");
		panel.add(new JLabel(I.trc("Guid", "Gothic 3 Text")), "wrap");
		panel.add(tfGuidText, "wrap");
		panel.add(new JLabel(I.trc("Guid", "Group")), "wrap");
		panel.add(tfGroup, "wrap");
		panel.add(new JLabel(I.trc("Guid", "Plain")), "wrap");
		panel.add(tfPlain, "wrap");

		tfEnterGuid.getDocument().addDocumentListener(SwingUtils.createDocumentListener(() -> {
			String guid = GuidUtil.parseGuid(tfEnterGuid.getText());
			tfHex.setText(guid);
			tfGuidText.setText(GuidUtil.hexToGuidText(guid));
			tfGroup.setText(GuidUtil.hexToGroup(guid));
			tfPlain.setText(GuidUtil.hexToPlain(guid));
		}));

		return panel;
	}
}
