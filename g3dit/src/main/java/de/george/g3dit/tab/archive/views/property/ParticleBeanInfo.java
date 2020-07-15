package de.george.g3dit.tab.archive.views.property;

import com.l2fprod.common.beans.BaseBeanInfo;

import de.george.lrentnode.classes.eCParticle_PS;

public class ParticleBeanInfo extends BaseBeanInfo {

	public ParticleBeanInfo() {
		super(eCParticle_PS.class);
		initProperties();
	}

	protected ParticleBeanInfo(Class<?> clazz) {
		super(clazz);
		initProperties();
	}

	protected void initProperties() {
		addProperty("SubdivisionScale").setCategory("Texture").setShortDescription("eCFloatScale");
		addProperty("RevolutionScale").setCategory("Revolution").setShortDescription("eCVectorScale");
		addProperty("VelocityScale").setCategory("Movement").setShortDescription("eCVectorScale");
		addProperty("ColorScale").setCategory("Color").setShortDescription("eCColorScale");
		addProperty("SizeScale").setCategory("Size").setShortDescription("eCVectorScale");
	}
}
