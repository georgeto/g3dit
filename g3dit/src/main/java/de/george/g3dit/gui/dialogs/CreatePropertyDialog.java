package de.george.g3dit.gui.dialogs;

import java.awt.Window;
import java.util.Optional;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jidesoft.dialog.ButtonPanel;
import com.teamunify.i18n.I;

import de.george.g3utils.gui.SwingUtils;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.classes.desc.ClassDescriptor;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.properties.PropertyInstantiator;
import net.miginfocom.swing.MigLayout;

public class CreatePropertyDialog extends ExtStandardDialog {
	private Class<ClassDescriptor> propertySet;
	private JComboBox<String> cbName;
	private JComboBox<String> cbType;

	public CreatePropertyDialog(Window owner, String propertySetName) {
		super(owner, I.trf("Property erstellen für {0}", propertySetName), true);
		propertySet = CD.getClassDescriptor(propertySetName).get();
		setSize(500, 170);

		cbName = new JComboBox<>(ClassDescriptor.getAllProperties(propertySet).map(PropertyDescriptor::getName).toArray(String.class));
		cbName.setEditable(true);

		cbType = new JComboBox<>(PropertyInstantiator.getKnownTypes().stream().sorted().toArray(String[]::new));
		cbType.setEditable(true);

		cbName.addActionListener(e -> handleSelectProperty());
	}

	@Override
	public JComponent createContentPanel() {
		JPanel panel = new JPanel(new MigLayout("insets 10, fillx", "[fill, grow]"));

		panel.add(new JLabel(I.tr("Name")), "wrap");
		panel.add(cbName, "wrap");

		panel.add(new JLabel(I.tr("Type")), "wrap");
		panel.add(cbType, "");

		return panel;
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();

		Action okAction = SwingUtils.createAction(I.tr("Ok"), () -> {
			if (isValidSelection()) {
				setDialogResult(RESULT_AFFIRMED);
			}
			dispose();
		});
		okAction.setEnabled(false);

		addButton(buttonPanel, okAction, ButtonPanel.AFFIRMATIVE_BUTTON);
		addDefaultCancelButton(buttonPanel);

		cbName.addActionListener(e -> okAction.setEnabled(isValidSelection()));
		cbType.addActionListener(e -> okAction.setEnabled(isValidSelection()));

		return buttonPanel;

	}

	private boolean isValidSelection() {
		return !getPropertyName().isEmpty() && !getPropertyType().isEmpty();
	}

	private void handleSelectProperty() {
		String name = getPropertyName();
		Optional<PropertyDescriptor<?>> property = ClassDescriptor.getAllProperties(propertySet)
				.filterBy(PropertyDescriptor::getName, name).findAny();
		property.ifPresent(propertyDescriptor -> cbType.setSelectedItem(propertyDescriptor.getDataTypeName()));
	}

	public String getPropertyName() {
		return (String) cbName.getSelectedItem();
	}

	public String getPropertyType() {
		return (String) cbType.getSelectedItem();
	}
}
