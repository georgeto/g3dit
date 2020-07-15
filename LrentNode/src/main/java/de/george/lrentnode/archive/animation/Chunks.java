package de.george.lrentnode.archive.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.structure.bCQuaternion;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;
import de.george.lrentnode.structures.FloatColor;

public abstract class Chunks {

	public static class LMA_CHUNK {
		// a node (LMA_Node) coming up next
		public static final int LMA_CHUNK_NODE = 0;
		// a motion part (LMA_MotionPart) coming up next
		public static final int LMA_CHUNK_MOTIONPART = 1;
		// an animation (LMA_Anim) coming up next
		public static final int LMA_CHUNK_ANIM = 2;
		// a mesh (LMA_Mesh) coming up next
		public static final int LMA_CHUNK_MESH = 3;
		// skinning information (LMA_SkinInfluence)
		public static final int LMA_CHUNK_SKINNINGINFO = 4;
		// a collision mesh
		public static final int LMA_CHUNK_COLLISIONMESH = 5;
		// a material (LMA_Material)
		public static final int LMA_CHUNK_MATERIAL = 6;
		// a material layer (LMA_MaterialLayer)
		public static final int LMA_CHUNK_MATERIALLAYER = 7;
		// a node limit information
		public static final int LMA_CHUNK_LIMIT = 8;
		// physic information
		public static final int LMA_CHUNK_PHYSICSINFO = 9;
		// a mesh expression part
		public static final int LMA_CHUNK_MESHEXPRESSIONPART = 10;
		// a expression motion part
		public static final int LMA_CHUNK_EXPRESSIONMOTIONPART = 11;
		// list of phonemes and keyframe data
		public static final int LMA_CHUNK_PHONEMEMOTIONDATA = 12;
		// a FX material
		public static final int LMA_CHUNK_FXMATERIAL = 13;
		// scene info
		public static final int LMA_CHUNK_SCENE_INFO = 16;
	}

	public static interface Chunk extends G3Serializable {
		public int getChunkId();

		public void setChunkId(int chunkId);

		public int getVersion();

		public void setVersion(int version);
	}

	public static abstract class AbstractChunk implements Chunk {
		private int version;
		private int chunkId;

		@Override
		public int getVersion() {
			return version;
		}

		@Override
		public void setVersion(int version) {
			this.version = version;
		}

		@Override
		public int getChunkId() {
			return chunkId;
		}

		@Override
		public void setChunkId(int chunkId) {
			this.chunkId = chunkId;
		}
	}

	public static class UnknownChunk extends AbstractChunk {
		public byte[] chunkData;

		@Override
		public void read(G3FileReader reader) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void read(G3FileReader reader, int size) {
			chunkData = reader.readByteArray(size);
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.write(chunkData);
		}
	}

	public static class NodeChunk extends AbstractChunk {
		public bCVector position;
		public bCQuaternion rotation;
		public bCVector scale;
		public bCQuaternion scaleOrient;
		public bCVector shear;
		public String name;
		public String parent;

		@Override
		public void read(G3FileReader reader) {
			position = reader.readVector();
			rotation = reader.read(bCQuaternion.class);
			scaleOrient = reader.read(bCQuaternion.class);
			scale = reader.readVector();
			shear = reader.readVector();
			name = reader.readString(reader.readInt());
			parent = reader.readString(reader.readInt());
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.write(position);
			writer.write(rotation);
			writer.write(scaleOrient);
			writer.write(scale);
			writer.write(shear);
			writer.writeInt(name.length()).writeString(name);
			writer.writeInt(parent.length()).writeString(parent);
		}
	}

	public static class Vertex implements G3Serializable {
		public int orgVertex;
		public bCVector position; // Z, Y, X
		public bCVector normal; // Z, Y, X
		public List<bCVector2> uvSets;

		@Override
		public void read(G3FileReader reader) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void read(G3FileReader reader, int size) {
			orgVertex = reader.readInt();
			position = reader.readVector();
			normal = reader.readVector();
			uvSets = reader.readList(bCVector2.class, (size - 28) / 8);
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeInt(orgVertex);
			writer.write(position);
			writer.write(normal);
			writer.write(uvSets);
		}

		public int getOrgVertex() {
			return orgVertex;
		}

		public bCVector getPosition() {
			return position;
		}

		public bCVector getNormal() {
			return normal;
		}

		public List<bCVector2> getUvSets() {
			return uvSets;
		}

		public bCVector getPositionXYZ() {
			return new bCVector(position.getZ(), position.getY(), position.getX());
		}

		public bCVector getNormalXYZ() {
			return new bCVector(normal.getZ(), normal.getY(), normal.getX());
		}
	}

	public static class Submesh implements G3Serializable {
		public int matID;
		public int numUVSets;
		public byte[] padding;
		public List<Vertex> vertices;
		public List<Integer> indices;

		@Override
		public void read(G3FileReader reader) {
			read(reader, -1);
		}

		@Override
		public void read(G3FileReader reader, int overrideNumUVSets) {
			matID = reader.readUnsignedByte();

			// Workaround for broken Rimy3D meshes (does not trip the original MeshChunkProcessor3,
			// because it ignores the numUVSets property of submeshes, and instead uses the one from
			// the mesh)
			numUVSets = reader.readUnsignedByte();
			if (overrideNumUVSets != -1) {
				numUVSets = overrideNumUVSets;
			}

			padding = reader.readByteArray(2);
			int numIndices = reader.readInt();
			int numVerts = reader.readInt();
			int vertexSize = 28 + 8 * numUVSets;
			vertices = reader.readList(r -> r.read(Vertex.class, vertexSize), numVerts);
			indices = reader.readList(G3FileReader::readInt, numIndices);
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeUnsignedByte(matID);
			writer.writeUnsignedByte(numUVSets);
			writer.write(padding);
			writer.writeInt(indices.size());
			writer.writeInt(vertices.size());
			writer.write(vertices);
			writer.write(indices, G3FileWriter::writeInt);
		}
	}

	public static class MeshChunk extends AbstractChunk {
		public int nodeNumber;
		public int numOrgVerts;
		public int totalVerts;
		public int totalIndices;
		public int numUVSets;
		public boolean isCollisionMesh;
		public byte[] padding;
		public List<Submesh> submeshes;

		@Override
		public void read(G3FileReader reader) {
			nodeNumber = reader.readInt();
			numOrgVerts = reader.readInt();
			totalVerts = reader.readInt();
			totalIndices = reader.readInt();
			int numSubMeshes = reader.readInt();
			numUVSets = reader.readInt();
			isCollisionMesh = reader.readBool();
			padding = reader.readByteArray(3);
			submeshes = reader.readList(r -> r.read(Submesh.class, numUVSets), numSubMeshes);
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeInt(nodeNumber);
			writer.writeInt(numOrgVerts);
			writer.writeInt(totalVerts);
			writer.writeInt(totalIndices);
			writer.writeInt(submeshes.size());
			writer.writeInt(numUVSets);
			writer.writeBool(isCollisionMesh);
			writer.write(padding);
			writer.write(submeshes);
		}
	}

	public static class SkinInfluence implements G3Serializable {
		public int nodeIndex;
		public byte[] padding;
		public float weight;

		@Override
		public void read(G3FileReader reader) {
			nodeIndex = reader.readUnsignedShort();
			padding = reader.readByteArray(2);
			weight = reader.readFloat();
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeUnsignedShort(nodeIndex);
			writer.write(padding);
			writer.writeFloat(weight);
		}
	}

	public static class SkinningInfoChunk extends AbstractChunk {
		public int nodeIndex;
		public List<List<SkinInfluence>> influences;

		@Override
		public void read(G3FileReader reader) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void read(G3FileReader reader, int size) {
			int chunkEndOffset = reader.position() + size;
			nodeIndex = reader.readInt();
			influences = new ArrayList<>();
			while (reader.position() < chunkEndOffset) {
				influences.add(reader.readList(SkinInfluence.class, reader.readUnsignedByte()));
			}
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeInt(nodeIndex);
			for (List<SkinInfluence> influence : influences) {
				writer.writeUnsignedByte(influence.size());
				writer.write(influence);
			}
		}
	}

	/**
	 * Applies to most recent {@link MotionPartChunk}.
	 */
	public static class KeyFrameChunk extends AbstractChunk {
		public enum InterpolationType {
			Linear('L'),
			Bezier('B'),
			TCB('T');

			private final char mnemonic;

			private InterpolationType(char mnemonic) {
				this.mnemonic = mnemonic;
			}

			public char toMnemonic() {
				return mnemonic;
			}

			public static InterpolationType fromMnemonic(char mnemonic) {
				return Stream.of(InterpolationType.values()).filter(v -> v.toMnemonic() == mnemonic).findFirst().get();
			}
		}

		public enum AnimationType {
			Position('P'),
			Rotation('R'),
			Scaling('S');

			private final char mnemonic;

			private AnimationType(char mnemonic) {
				this.mnemonic = mnemonic;
			}

			public char toMnemonic() {
				return mnemonic;
			}

			public static AnimationType fromMnemonic(char mnemonic) {
				return Stream.of(AnimationType.values()).filter(v -> v.toMnemonic() == mnemonic).findFirst().get();
			}
		}

		public static abstract class KeyFrame<T extends G3Serializable> implements G3Serializable {
			public float time; // time in seconds
			public T value;

			@Override
			public void read(G3FileReader reader) {
				time = reader.readFloat();
				value = reader.read(getValueType());
			}

			@Override
			public void write(G3FileWriter writer) {
				writer.writeFloat(time);
				writer.write(value);
			}

			protected abstract Class<T> getValueType();
		}

		public static class VectorKeyFrame extends KeyFrame<bCVector> {
			@Override
			protected Class<bCVector> getValueType() {
				return bCVector.class;
			}
		}

		public static class QuaternionKeyFrame extends KeyFrame<bCQuaternion> {
			@Override
			protected Class<bCQuaternion> getValueType() {
				return bCQuaternion.class;
			}
		}

		public InterpolationType interpolationType;
		public AnimationType animationType;
		public List<? extends KeyFrame<?>> track;

		@Override
		public void read(G3FileReader reader) {
			int keyFrameCount = reader.readInt();
			interpolationType = InterpolationType.fromMnemonic(reader.readChar());
			animationType = AnimationType.fromMnemonic(reader.readChar());
			reader.skip(2); // Padding

			switch (animationType) {
				case Position:
				case Scaling:
					track = reader.readList(VectorKeyFrame.class, keyFrameCount);
					break;
				case Rotation:
					track = reader.readList(QuaternionKeyFrame.class, keyFrameCount);
					break;
			}
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeInt(track.size());
			writer.writeChar(interpolationType.toMnemonic());
			writer.writeChar(animationType.toMnemonic());
			writer.writeUnsignedShort(0); // Padding
			writer.write(track);
		}
	}

	public static class MotionPartChunk extends AbstractChunk {
		public bCVector posePosition; // initial pose position
		public bCQuaternion poseRotation; // initial pose rotation
		public bCVector poseScale; // initial pose scale
		public bCVector bindPosePosition; // initial pose position
		public bCQuaternion bindPoseRotation; // initial pose rotation
		public bCVector bindPoseScale; // initial pose scale
		public String name;

		@Override
		public void read(G3FileReader reader) {
			posePosition = reader.readVector();
			poseRotation = reader.read(bCQuaternion.class);
			poseScale = reader.readVector();
			bindPosePosition = reader.readVector();
			bindPoseRotation = reader.read(bCQuaternion.class);
			bindPoseScale = reader.readVector();
			name = reader.readString(reader.readInt());
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.write(posePosition);
			writer.write(poseRotation);
			writer.write(poseScale);
			writer.write(bindPosePosition);
			writer.write(bindPoseRotation);
			writer.write(bindPoseScale);
			writer.writeInt(name.length()).writeString(name);
		}
	}

	public enum TransparencyType {
		Filter('F'),
		Substractive('S'),
		Additive('A'),
		Unknown('U');

		private final char mnemonic;

		private TransparencyType(char mnemonic) {
			this.mnemonic = mnemonic;
		}

		public char toMnemonic() {
			return mnemonic;
		}

		public static TransparencyType fromMnemonic(char mnemonic) {
			return Stream.of(TransparencyType.values()).filter(v -> v.toMnemonic() == mnemonic).findFirst().get();
		}
	}

	public static class MaterialChunk extends AbstractChunk {
		public FloatColor ambientColor;
		public FloatColor diffuseColor;
		public FloatColor specularColor;
		public FloatColor emissiveColor; // self illumination color
		public float shine;
		public float shineStrength;
		public float opacity; // the opacity amount [1.0=full opac, 0.0=full transparent]
		public float ior; // index of refraction
		public boolean doubleSided;
		public boolean wireFrame; // render in wireframe?
		public TransparencyType transparencyType;
		public byte padding;
		public String materialName;
		public String shaderFileName;

		@Override
		public void read(G3FileReader reader) {
			ambientColor = reader.read(FloatColor.class);
			diffuseColor = reader.read(FloatColor.class);
			specularColor = reader.read(FloatColor.class);
			emissiveColor = reader.read(FloatColor.class);
			shine = reader.readFloat();
			shineStrength = reader.readFloat();
			opacity = reader.readFloat();
			ior = reader.readFloat();
			doubleSided = reader.readBool();
			wireFrame = reader.readBool();
			transparencyType = TransparencyType.fromMnemonic(reader.readChar());
			padding = reader.readByte();
			materialName = reader.readString(reader.readInt());
			shaderFileName = reader.readString(reader.readInt());
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.write(ambientColor);
			writer.write(diffuseColor);
			writer.write(specularColor);
			writer.write(emissiveColor);
			writer.writeFloat(shine);
			writer.writeFloat(shineStrength);
			writer.writeFloat(opacity);
			writer.writeFloat(ior);
			writer.writeBool(doubleSided);
			writer.writeBool(wireFrame);
			writer.writeChar(transparencyType.toMnemonic());
			writer.writeByte(padding);
			writer.writeInt(materialName.length()).writeString(materialName);
			writer.writeInt(shaderFileName.length()).writeString(shaderFileName);
		}
	}

	public static Class<? extends Chunk> getChunk(int chunkID, int version) {
		switch (chunkID) {
			case LMA_CHUNK.LMA_CHUNK_NODE:
				if (version == 3) {
					return NodeChunk.class;
				}
				break;
			case LMA_CHUNK.LMA_CHUNK_MOTIONPART:
				if (version == 3) {
					return MotionPartChunk.class;
				}
				break;
			case LMA_CHUNK.LMA_CHUNK_ANIM:
				if (version == 1) {
					return KeyFrameChunk.class;
				}
				break;
			case LMA_CHUNK.LMA_CHUNK_MESH:
				if (version == 3) {
					return MeshChunk.class;
				}
				break;
			case LMA_CHUNK.LMA_CHUNK_SKINNINGINFO:
				if (version == 1) {
					return SkinningInfoChunk.class;
				}
				break;
			case LMA_CHUNK.LMA_CHUNK_MATERIAL:
				if (version == 5) {
					return MaterialChunk.class;
				}
				break;
			default:
				return UnknownChunk.class;
		}

		throw new IllegalArgumentException("ChunkID " + chunkID + " mit Version " + version + " wird nicht unterstützt.");
	}

	public static List<Chunk> readChunks(G3FileReader reader, int offsetEnd) {
		List<Chunk> chunks = new ArrayList<>();
		while (reader.getPos() < offsetEnd) {
			int chunkID = reader.readInt();
			int chunkSize = reader.readInt();
			int chunkVersion = reader.readInt();
			Class<? extends Chunk> chunkClass = Chunks.getChunk(chunkID, chunkVersion);
			Chunk chunk = reader.read(chunkClass, chunkSize);
			chunk.setChunkId(chunkID);
			chunk.setVersion(chunkVersion);
			chunks.add(chunk);
		}
		return chunks;
	}

	public static void writeChunks(G3FileWriter writer, List<Chunk> chunks) {
		for (Chunk chunk : chunks) {
			writer.writeInt(chunk.getChunkId());
			int chunkSizeOffset = writer.position();
			writer.writeInt(0);
			writer.writeInt(chunk.getVersion());
			writer.write(chunk);
			writer.writeInt(chunkSizeOffset, writer.position() - chunkSizeOffset - 8);
		}
	}
}
