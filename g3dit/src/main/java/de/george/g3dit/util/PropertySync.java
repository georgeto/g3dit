package de.george.g3dit.util;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.gui.components.JTemplateGuidField;
import de.george.g3utils.gui.JGuidField;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.G3Enum;
import de.george.lrentnode.properties.bCString;
import de.george.lrentnode.properties.bTPropertyContainer;
import de.george.lrentnode.properties.eCEntityProxy;
import de.george.lrentnode.properties.gBool;
import de.george.lrentnode.properties.gFloat;
import de.george.lrentnode.properties.gInt;
import de.george.lrentnode.properties.gLong;

public class PropertySync {
	private G3Class clazz;

	public static PropertySync wrap(G3Class clazz) {
		return new PropertySync(clazz);
	}

	private PropertySync(G3Class clazz) {
		this.clazz = clazz;
	}

	public <T extends G3Enum> void readEnum(JComboBox<String> cb, Class<T> enumClass, PropertyDescriptor<bTPropertyContainer<T>> prop) {
		cb.setSelectedItem(G3Enums.asString(enumClass, clazz.property(prop).getEnumValue()));
	}

	public <T extends G3Enum> void readEnum(JEnumComboBox<T> cb, PropertyDescriptor<bTPropertyContainer<T>> prop) {
		cb.setSelectedValue(clazz.property(prop));
	}

	public void readString(JComboBox<String> cb, PropertyDescriptor<bCString> prop) {
		cb.setSelectedItem(clazz.property(prop).getString());
	}

	public void readString(JTextComponent tc, PropertyDescriptor<bCString> prop) {
		tc.setText(clazz.property(prop).getString());
	}

	public void readInt(JTextComponent tc, PropertyDescriptor<gInt> prop) {
		tc.setText(String.valueOf(clazz.property(prop).getInt()));
	}

	public void readLong(JTextComponent tc, PropertyDescriptor<gLong> prop) {
		tc.setText(String.valueOf(clazz.property(prop).getLong()));
	}

	public void readGuid(JGuidField gf, PropertyDescriptor<eCEntityProxy> prop) {
		gf.setText(clazz.property(prop).getGuid());
	}

	public void readGuid(JTemplateGuidField gf, PropertyDescriptor<eCEntityProxy> prop) {
		gf.setText(clazz.property(prop).getGuid());
	}

	public void readBool(JCheckBox cb, PropertyDescriptor<gBool> prop) {
		cb.setSelected(clazz.property(prop).isBool());
	}

	public void readFloat(JTextComponent tc, PropertyDescriptor<gFloat> prop) {
		tc.setText(Misc.formatFloat(clazz.property(prop).getFloat()));
	}

	public void readVector(JTextComponent tc, PropertyDescriptor<bCVector> prop) {
		tc.setText(clazz.property(prop).toString());
	}

	public <T extends G3Enum> void writeEnum(JComboBox<String> cb, Class<T> enumClass, PropertyDescriptor<bTPropertyContainer<T>> prop) {
		clazz.property(prop).setEnumValue(G3Enums.asInt(enumClass, cb.getSelectedItem().toString()));
	}

	public <T extends G3Enum> void writeEnum(JEnumComboBox<T> cb, PropertyDescriptor<bTPropertyContainer<T>> prop) {
		clazz.property(prop).setEnumValue(cb.getSelectedValue());
	}

	public void writeString(JComboBox<String> cb, PropertyDescriptor<bCString> prop) {
		clazz.property(prop).setString(cb.getSelectedItem().toString());
	}

	public void writeString(JTextComponent tc, PropertyDescriptor<bCString> prop) {
		clazz.property(prop).setString(tc.getText());
	}

	public void writeInt(JTextComponent tc, PropertyDescriptor<gInt> prop) {
		clazz.property(prop).setInt(Integer.parseInt(tc.getText()));
	}

	public void writeLong(JTextComponent tc, PropertyDescriptor<gLong> prop) {
		clazz.property(prop).setLong(Integer.parseInt(tc.getText()));
	}

	public void writeGuid(JGuidField gf, PropertyDescriptor<eCEntityProxy> prop) {
		clazz.property(prop).setGuid(GuidUtil.parseGuid(gf.getText()));
	}

	public void writeGuid(JTemplateGuidField gf, PropertyDescriptor<eCEntityProxy> prop) {
		clazz.property(prop).setGuid(GuidUtil.parseGuid(gf.getText()));
	}

	public void writeBool(JCheckBox cb, PropertyDescriptor<gBool> prop) {
		clazz.property(prop).setBool(cb.isSelected());
	}

	public void writeFloat(JTextComponent tc, PropertyDescriptor<gFloat> prop) {
		clazz.property(prop).setFloat(Float.parseFloat(tc.getText()));
	}

	public void writeVector(JTextComponent tc, PropertyDescriptor<bCVector> prop) {
		try {
			clazz.property(prop).setTo(bCVector.fromString(tc.getText()));
		} catch (IllegalArgumentException e) {
			// Leave unchaged if input is invalid
		}
	}
}
