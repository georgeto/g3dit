package de.george.g3dit.gui.components;

import javax.swing.JComboBox;

import com.jidesoft.swing.AutoCompletion;

import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.G3Enum;
import de.george.lrentnode.properties.bTPropertyContainer;

public class JEnumComboBox<T extends G3Enum> extends JComboBox<String> {
	private final boolean stripped;
	private final Class<T> enumType;

	public JEnumComboBox(Class<T> enumType) {
		this(enumType, true);
	}

	public JEnumComboBox(Class<T> enumType, boolean stripped) {
		super(G3Enums.asArray(enumType, stripped));
		this.enumType = enumType;
		this.stripped = stripped;
		new AutoCompletion(this);
	}

	public void setSelectedValue(int value) {
		super.setSelectedItem(G3Enums.asString(enumType, value, stripped));
	}

	public void setSelectedValue(bTPropertyContainer<T> container) {
		super.setSelectedItem(G3Enums.asString(enumType, container.getEnumValue(), stripped));
	}

	public int getSelectedValue() {
		return G3Enums.asInt(enumType, super.getSelectedItem().toString(), stripped);
	}

	@Override
	public String getSelectedItem() {
		return (String) super.getSelectedItem();
	}
}
