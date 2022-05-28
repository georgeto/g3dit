package de.george.g3dit.gui.edit.handler;

import javax.swing.JPanel;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.components.JEntityGuidField;
import de.george.g3dit.gui.components.JSearchNamedGuidField;
import de.george.g3dit.gui.components.JTemplateGuidField;
import de.george.g3dit.gui.edit.PropertyPanelDef;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.GuidUtil;
import de.george.lrentnode.properties.bCPropertyID;
import de.george.lrentnode.properties.eCEntityProxy;

public class NamedGuidPropertyHandler extends TitledPropertyHandler<G3Serializable> {
	private final EditorContext ctx;
	private final boolean template;

	private JSearchNamedGuidField tfValue;

	public NamedGuidPropertyHandler(PropertyPanelDef def, EditorContext ctx, boolean template) {
		super(def);
		this.ctx = ctx;
		this.template = template;
	}

	@Override
	protected void addValueComponent(JPanel content) {
		tfValue = template ? new JTemplateGuidField(ctx) : new JEntityGuidField(ctx);
		tfValue.setToolTipText(def.getTooltip());
		tfValue.setEditable(def.isEditable());
		if (def.hasValidators()) {
			tfValue.initValidation(def.getValidation(), def.getName(), def.getValidators());
		}
		def.customize(tfValue);
		content.add(tfValue, "width 100:300:300");
	}

	@Override
	protected void load(G3Serializable value) {
		if (def.getAdapter().getDataType() == eCEntityProxy.class) {
			tfValue.setText(((eCEntityProxy) value).getGuid());
		} else {
			tfValue.setText(((bCPropertyID) value).getGuid());
		}
	}

	@Override
	protected G3Serializable save() {
		String guid = GuidUtil.parseGuid(tfValue.getText());
		if (def.getAdapter().getDataType() == eCEntityProxy.class) {
			return new eCEntityProxy(guid);
		} else {
			return new bCPropertyID(guid);
		}
	}
}
