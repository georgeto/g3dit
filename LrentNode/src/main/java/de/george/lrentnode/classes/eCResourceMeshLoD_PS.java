package de.george.lrentnode.classes;

import java.util.List;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;

public final class eCResourceMeshLoD_PS extends eCResourceBase_PS {
	private List<String> meshes;

	public eCResourceMeshLoD_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	public List<String> getMeshes() {
		return meshes;
	}

	public void setMeshes(List<String> meshes) {
		this.meshes = meshes;
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		super.readPostClassVersion(reader);
		meshes = reader.readList(G3FileReader::readEntry);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		super.writePostClassVersion(writer);
		writer.writeList(meshes, G3FileWriter::writeEntry);
	}
}
