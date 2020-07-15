package de.george.g3dit.util;

import javax.swing.JButton;

import com.google.common.base.Strings;

import de.george.g3dit.EditorContext;
import de.george.g3dit.entitytree.filter.GuidEntityFilter.MatchMode;
import de.george.g3dit.gui.dialogs.EntitySearchDialog;
import de.george.g3utils.gui.JGuidField;

public class ComplexCompFactory {
	public static JButton createListEnclaveMemberButton(JGuidField tfEnclaveGuid, EditorContext ctx) {
		JButton btnListEnclaveMembers = new JButton(Icons.getImageIcon(Icons.Misc.GLOBE));
		btnListEnclaveMembers.setToolTipText("Alle Mitglieder der Enclave auflisten");
		tfEnclaveGuid.addGuidFiedListener(g -> btnListEnclaveMembers.setEnabled(!Strings.isNullOrEmpty(g)));
		btnListEnclaveMembers
				.addActionListener(a -> EntitySearchDialog.openEntitySearchGuid(ctx, MatchMode.Enclave, tfEnclaveGuid.getText()));
		return btnListEnclaveMembers;
	}
}
