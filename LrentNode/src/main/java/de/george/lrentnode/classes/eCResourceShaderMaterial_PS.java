package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.lrentnode.util.ClassUtil;

public final class eCResourceShaderMaterial_PS extends eCResourceBase_PS {
	private eCShaderBase shader;

	public eCResourceShaderMaterial_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	public eCShaderBase getShader() {
		return shader;
	}

	public void setShader(eCShaderBase shader) {
		this.shader = shader;
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		// if(classVersion != 64)
		// reader.warn(logger, "eCResourceShaderMaterial_PS unbekannte Version");

		super.readPostClassVersion(reader);

		shader = (eCShaderBase) ClassUtil.readSubClass(reader);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		super.writePostClassVersion(writer);
		ClassUtil.writeSubClass(writer, shader);
	}
}
