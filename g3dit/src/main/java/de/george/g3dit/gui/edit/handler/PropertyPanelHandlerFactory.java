package de.george.g3dit.gui.edit.handler;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.edit.PropertyPanelDef;
import de.george.g3dit.gui.edit.adapter.PropertyAdapter;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.properties.bCPropertyID;
import de.george.lrentnode.properties.bCString;
import de.george.lrentnode.properties.bTObjArray_eCEntityProxy;
import de.george.lrentnode.properties.bTPropertyContainer;
import de.george.lrentnode.properties.eCEntityProxy;
import de.george.lrentnode.properties.gBool;
import de.george.lrentnode.properties.gFloat;
import de.george.lrentnode.properties.gInt;
import de.george.lrentnode.properties.gLong;

public class PropertyPanelHandlerFactory {
	public static PropertyHandler<?> create(EditorContext ctx, PropertyPanelDef def) {
		if (def.hasTableCoumns()) {
			return new GenericArrayPropertyHandler(def, ctx);
		}

		PropertyAdapter<?> adapter = def.getAdapter();
		if (adapter.getDataType() == bTPropertyContainer.class) {
			return new EnumPropertyHandler<>(def, G3Enums.byG3Type(adapter.getDataTypeName()));
		} else if (adapter.getDataType() == gBool.class) {
			return new BooleanPropertyHandler(def);
		} else if (adapter.getDataType() == bCString.class) {
			if (def.hasValueList()) {
				return new SelectPropertyHandler(def);
			}
			return new TextPropertyHandler(def);
		} else if (adapter.getDataType() == gInt.class) {
			return new IntPropertyHandler(def);
		} else if (adapter.getDataType() == gLong.class) {
			return new LongPropertyHandler(def);
		} else if (adapter.getDataType() == gFloat.class) {
			return new FloatPropertyHandler(def);
		} else if (adapter.getDataType() == eCEntityProxy.class || adapter.getDataType() == bCPropertyID.class) {
			boolean template = adapter.getDataTypeName().equals("eCTemplateEntityProxy");
			return new NamedGuidPropertyHandler(def, ctx, template);
		} else if (adapter.getDataType() == bTObjArray_eCEntityProxy.class) {
			boolean template = adapter.getDataTypeName().contains("eCTemplateEntityProxy");
			return new ArrayNamedGuidPropertyHandler(def, ctx, template);
		} else if (adapter.getDataType() == bCVector.class) {
			return new VectorPropertyHandler(def);
		} else {
			throw new IllegalArgumentException("Unable to create editor for unsupported property type: " + adapter.getDataTypeName());
		}
	}
}
