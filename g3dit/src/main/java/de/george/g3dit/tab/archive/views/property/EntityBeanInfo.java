package de.george.g3dit.tab.archive.views.property;

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;

import de.george.lrentnode.archive.eCEntity;

public class EntityBeanInfo extends BaseBeanInfo {

	public EntityBeanInfo() {
		super(eCEntity.class);
		initProperties();
	}

	protected EntityBeanInfo(Class<?> clazz) {
		super(clazz);
		initProperties();
	}

	protected void initProperties() {
		addProperty("Enabled").setCategory("General").setShortDescription("bool");
		addProperty("RenderingEnabled").setCategory("General").setShortDescription("bool");
		addProperty("ProcessingDisabled").setCategory("General").setShortDescription("bool");
		addProperty("DeactivationEnabled").setCategory("General").setShortDescription("bool");
		addProperty("Pickable").setCategory("General").setShortDescription("bool");
		addProperty("CollisionEnabled").setCategory("General").setShortDescription("bool");
		addProperty("RenderAlphaValue").setCategory("General").setShortDescription("float");
		addProperty("InsertType").setCategory("General").setShortDescription("eEInsertType");
		addProperty("LastRenderPriority").setCategory("General").setShortDescription("byte");
		addProperty("SpecialDepthTexPassEnabled").setCategory("General").setShortDescription("bool");
		;
		ExtendedPropertyDescriptor unkFlag2 = addProperty("UnkFlag2").setCategory("General");
		unkFlag2.setDisplayName("UnkFlag2 (Must Compiled Static ?)");
		unkFlag2.setShortDescription("bool");
		addProperty("Name").setCategory("General").setShortDescription("bCString");
		addProperty("VisualLoDFactor").setCategory("General").setShortDescription("float");
		;
		ExtendedPropertyDescriptor unkFlag3 = addProperty("UnkFlag3").setCategory("General");
		unkFlag3.setDisplayName("UnkFlag3 (Derived Template ?)");
		unkFlag3.setShortDescription("bool");
		addProperty("ObjectCullFactor").setCategory("General").setShortDescription("float");
		addProperty("DataChangedTimeStamp").setCategory("General").setShortDescription("unsigned long");
		addProperty("UniformScaling").setCategory("General").setShortDescription("float");
		addProperty("RangedObjectCulling").setCategory("General").setShortDescription("bool");
		addProperty("ProcessingRangeOutFadingEnabled").setCategory("General").setShortDescription("bool");
	}
}
