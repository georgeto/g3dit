package de.george.lrentnode.classes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.structure.bCRect;
import de.george.lrentnode.structures.bCGuid;

public abstract class eCShaderEllementBase extends G3Class {
	private static final Logger logger = LoggerFactory.getLogger(eCShaderEllementBase.class);

	protected bCGuid token;
	protected bCRect editorLayout;

	public eCShaderEllementBase(String className, G3FileReader reader) {
		super(className, reader);
	}

	public bCGuid getToken() {
		return token;
	}

	public void setToken(bCGuid token) {
		this.token = token;
	}

	public bCRect getEditorLayout() {
		return editorLayout;
	}

	public void setEditorLayout(bCRect editorLayout) {
		this.editorLayout = editorLayout;
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		if (reader.readShort() != 1) {
			reader.warn(logger, "Unsupported eCShaderEllementBase version.");
		}

		token = reader.read(bCGuid.class);
		editorLayout = reader.read(bCRect.class);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.writeUnsignedShort(1);
		writer.write(token);
		writer.write(editorLayout);
	}
}
