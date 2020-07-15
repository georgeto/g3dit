package de.george.lrentnode.archive.animation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3FileWriterEx;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.io.GenomeFile;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCDateTime;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.archive.animation.Chunks.Chunk;

public class eCResourceAnimationActor_PS extends GenomeFile {

	public static class Illumination implements G3Serializable {
		public static class MaterialReference implements G3Serializable {
			public int index;
			public String name;

			@Override
			public void read(G3FileReader reader) {
				reader.skip(2);
				index = reader.readUnsignedShort();
				try {
					name = reader.readEntry();
				} catch (IndexOutOfBoundsException e) {
					// Workaround for broken Rimy3D meshes (they don't have a stringtable, but still
					// refer to stringtable entries here)
					name = "";
				}
			}

			@Override
			public void write(G3FileWriter writer) {
				writer.writeUnsignedShort(0).writeUnsignedShort(index).writeEntry(name);
			}
		}

		public List<MaterialReference> materials;
		// eCWrapper_emfx2Actor::CalculateAmbientOcclusion | ColorVertexAttribute
		public List<List<Integer>> ambientOcclusion;
		public List<List<bCVector>> tangentVertices; // TangentVertexAttribute

		@Override
		public void read(G3FileReader reader) {
			materials = reader.readList(MaterialReference.class);

			// Per LoD
			ambientOcclusion = reader.readPrefixedList(r -> r.readPrefixedList(G3FileReader::readInt));
			tangentVertices = new ArrayList<>(ambientOcclusion.size());
			for (int i = 0; i < ambientOcclusion.size(); i++) {
				int totalVertexCount = ambientOcclusion.get(i).size();
				tangentVertices.add(reader.readList(bCVector.class, totalVertexCount));
			}
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeList(materials);
			writer.writeInt(ambientOcclusion.size());
			writer.writePrefixedList(ambientOcclusion, (w, v) -> w.writePrefixedList(v, G3FileWriter::writeInt));
			writer.write(tangentVertices, G3FileWriter::write);
		}
	}

	public static class eSLookAtConstraintData implements G3Serializable {
		public String nodeName;
		public float interpolationSpeed;
		public bCVector minConstraints;
		public bCVector maxConstraints;

		@Override
		public void read(G3FileReader reader) {
			nodeName = reader.readEntry();
			interpolationSpeed = reader.readFloat();
			minConstraints = reader.readVector();
			maxConstraints = reader.readVector();
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeEntry(nodeName);
			writer.writeFloat(interpolationSpeed);
			writer.writeVector(minConstraints);
			writer.writeVector(maxConstraints);
		}
	};

	public static class eCWrapper_emfx2Actor implements G3Serializable {
		private int highVersion, lowVersion;
		public List<Chunk> chunks;
		public Illumination illumination;

		@SuppressWarnings("unchecked")
		public <T extends Chunk> T getChunkByType(int chunkId) {
			return (T) chunks.stream().filter(c -> c.getChunkId() == chunkId).findFirst().get();
		}

		@SuppressWarnings("unchecked")
		public <T extends Chunk> List<T> getChunksByType(int chunkId) {
			return (List<T>) chunks.stream().filter(c -> c.getChunkId() == chunkId).collect(Collectors.toList());
		}

		@Override
		public void read(G3FileReader reader) {
			if (reader.readInt() != 0x616E6567) {
				throw new IllegalArgumentException("Ungültiger eCWrapper_emfx2Actor.");
			}

			int version = reader.readUnsignedShort();
			if (version != 4) {
				throw new IllegalArgumentException("Version != 4 wird nicht unterstützt.");
			}

			int offsetEnd = reader.readInt() + reader.getPos();
			if (!reader.readString(4).equals("FXA ")) {
				throw new IllegalArgumentException("Ungültiger eCWrapper_emfx2Actor.");
			}
			highVersion = reader.readUnsignedByte();
			lowVersion = reader.readUnsignedByte();

			if (highVersion != 1 || lowVersion != 1) {
				throw new IllegalArgumentException("Ungültiger eCWrapper_emfx2Actor.");
			}

			chunks = Chunks.readChunks(reader, offsetEnd);
			illumination = reader.read(Illumination.class);
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeInt(0x616E6567).writeUnsignedShort(4);
			int sizeOffset = writer.position();
			writer.writeInt(0);
			writer.writeString("FXA ");
			writer.writeUnsignedByte(highVersion).writeUnsignedByte(lowVersion);
			Chunks.writeChunks(writer, chunks);
			writer.writeInt(sizeOffset, writer.position() - sizeOffset - 4);
			writer.write(illumination);
		}
	}

	public long resourceSize;
	public float resourcePriority;
	public bCDateTime nativeFileTime;
	public long nativeFileSize;
	public bCBox boundary;

	public List<eSLookAtConstraintData> lookAtConstraints;
	public List<eCWrapper_emfx2Actor> lods;
	public eCWrapper_emfx2Actor actor;

	public eCResourceAnimationActor_PS(G3FileReaderEx reader) throws IOException {
		read(reader);
	}

	@Override
	protected void readInternal(G3FileReaderEx reader) throws IOException {
		int version = reader.readUnsignedShort();
		if (version != 54) {
			throw new IllegalArgumentException("Version != 54 wird nicht unterstützt.");
		}

		resourceSize = reader.readUnsignedInt();
		resourcePriority = reader.readFloat();
		nativeFileTime = reader.read(bCDateTime.class);
		nativeFileSize = reader.readUnsignedInt();
		boundary = reader.readBox();

		lookAtConstraints = reader.readList(eSLookAtConstraintData.class);
		lods = reader.readList(eCWrapper_emfx2Actor.class);
		actor = reader.read(eCWrapper_emfx2Actor.class);

	}

	@Override
	protected void writeInternal(G3FileWriterEx writer) {
		writer.writeUnsignedShort(54);
		writer.writeUnsignedInt(resourceSize);
		writer.writeFloat(resourcePriority);
		writer.write(nativeFileTime).writeUnsignedInt(nativeFileSize);
		writer.write(boundary);
		writer.writeList(lookAtConstraints);
		writer.writeList(lods);
		writer.write(actor);

	}
}
