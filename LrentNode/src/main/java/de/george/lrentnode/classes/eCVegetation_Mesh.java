package de.george.lrentnode.classes;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.properties.bCString;

public class eCVegetation_Mesh extends G3Class {
	private static final Logger logger = LoggerFactory.getLogger(eCVegetation_Mesh.class);

	private eSVegetationMeshID meshID; // (>> 16 TypeID)
	private String timestamp;
	private String diffuseTexture;
	private bCBox bounds;
	private List<bCVector> positions, normals;
	private List<bCVector2> uvs;
	private List<Integer> indices;

	// Namen cachen
	private transient String name = null;

	public eCVegetation_Mesh(String className, G3FileReader reader) {
		super(className, reader);
	}

	public eSVegetationMeshID getMeshID() {
		return meshID;
	}

	public String getDiffuseTexture() {
		return diffuseTexture;
	}

	public bCBox getBounds() {
		return bounds;
	}

	public List<bCVector> getPositions() {
		return positions;
	}

	public List<bCVector> getNormals() {
		return normals;
	}

	public List<bCVector2> getUVs() {
		return uvs;
	}

	public List<Integer> getIndices() {
		return indices;
	}

	public String getName() {
		if (name == null) {
			Optional<bCString> meshFilePath = propertyNoThrow(CD.eCVegetation_Mesh.MeshFilePath);
			name = meshFilePath.map(m -> m.getString().replaceAll(".*\\\\", "").replace(".xcmsh", "")).orElse("<Fehler beim Auslesen>");
		}
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		meshID = reader.read(eSVegetationMeshID.class);
		timestamp = reader.read(8);
		diffuseTexture = reader.readEntry();
		bounds = reader.readBox();

		positions = reader.readPrefixedList(G3FileReader::readVector);
		normals = reader.readPrefixedList(G3FileReader::readVector);
		uvs = reader.readPrefixedList(bCVector2.class);
		indices = reader.readPrefixedList(G3FileReader::readInt);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		// Stacks
		writer.write(meshID);
		writer.write(timestamp);
		writer.writeEntry(diffuseTexture);
		writer.writeBox(bounds);
		writer.writeBool(true);
		writer.writeList(positions, G3FileWriter::writeVector);
		writer.writeBool(true);
		writer.writeList(normals, G3FileWriter::writeVector);
		writer.writeBool(true);
		writer.writeList(uvs);
		writer.writeBool(true);
		writer.writeList(indices, G3FileWriter::writeInt);
	}

	public static class eSVegetationMeshID implements G3Serializable {
		private int type;
		private int index;

		public eSVegetationMeshID(int type, int index) {
			this.type = type;
			this.index = index;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		@Override
		public void read(G3FileReader reader) {
			type = reader.readUnsignedShort();
			index = reader.readUnsignedShort();
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeUnsignedShort(type).writeUnsignedShort(index);
		}

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof eSVegetationMeshID)) {
				return false;
			}
			eSVegetationMeshID castOther = (eSVegetationMeshID) other;
			return Objects.equals(type, castOther.type) && Objects.equals(index, castOther.index);
		}

		@Override
		public int hashCode() {
			return Objects.hash(type, index);
		}
	}
}
