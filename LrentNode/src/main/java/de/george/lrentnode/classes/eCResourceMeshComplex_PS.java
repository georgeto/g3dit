package de.george.lrentnode.classes;

import java.util.List;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.structure.bCBox;
import de.george.lrentnode.structures.eCMeshElement;

public final class eCResourceMeshComplex_PS extends eCResourceBase_PS {
	private float resourcePriority;
	public List<eCMeshElement> meshElements;

	public eCResourceMeshComplex_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		super.readPostClassVersion(reader);

		if (classVersion < 0x22) {
			throw new UnsupportedOperationException("eCResourceMeshComplex_PS::Read(): Old format is not supported.");
		}

		resourcePriority = reader.readFloat();
		meshElements = reader.readList(eCMeshElement.class);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		super.writePostClassVersion(writer);
		writer.writeFloat(resourcePriority);
		writer.writeList(meshElements);
	}

	public bCBox getBoundingBox() {
		return meshElements.stream().map(eCMeshElement::getBoundingBox).reduce(bCBox::merge).orElseGet(bCBox::new);
	}
}
