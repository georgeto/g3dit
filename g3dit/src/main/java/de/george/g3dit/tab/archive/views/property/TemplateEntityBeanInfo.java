package de.george.g3dit.tab.archive.views.property;

import de.george.lrentnode.template.TemplateEntity;

public class TemplateEntityBeanInfo extends EntityBeanInfo {

	public TemplateEntityBeanInfo() {
		super(TemplateEntity.class);
	}

	@Override
	protected void initProperties() {
		super.initProperties();
		addProperty("FileName").setCategory("Template").setShortDescription("bCString");
		addProperty("HelperParent").setCategory("Template").setShortDescription("bool");
		addProperty("RefTemplate").setCategory("Template").setShortDescription("eCTemplateEntityProxy");
		addProperty("Deleted").setCategory("Template").setShortDescription("bool");
	}
}
