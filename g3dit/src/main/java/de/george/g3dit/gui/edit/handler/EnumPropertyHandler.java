package de.george.g3dit.gui.edit.handler;

import javax.swing.JPanel;

import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.gui.edit.PropertyPanelDef;
import de.george.lrentnode.enums.G3Enums.G3Enum;
import de.george.lrentnode.properties.bTPropertyContainer;

public class EnumPropertyHandler<T extends bTPropertyContainer<S>, S extends G3Enum> extends TitledPropertyHandler<T> {
	private final Class<S> enumType;
	private JEnumComboBox<S> cbValue;

	public EnumPropertyHandler(PropertyPanelDef def, Class<S> enumType) {
		super(def);
		this.enumType = enumType;
	}

	@Override
	protected void addValueComponent(JPanel content) {
		cbValue = new JEnumComboBox<>(enumType, true);
		cbValue.setToolTipText(def.getTooltip());
		cbValue.setEnabled(def.isEditable());
		def.apply(cbValue);
		content.add(cbValue, "width 100:100:, grow");
	}

	@Override
	protected void load(T value) {
		cbValue.setSelectedValue(value.getEnumValue());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected T save() {
		return (T) new bTPropertyContainer<S>(cbValue.getSelectedValue());
	}
}
