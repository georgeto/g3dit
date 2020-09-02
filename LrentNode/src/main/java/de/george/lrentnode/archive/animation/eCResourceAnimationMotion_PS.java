package de.george.lrentnode.archive.animation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3FileWriterEx;
import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.io.GenomeFile;
import de.george.g3utils.structure.bCDateTime;
import de.george.lrentnode.archive.animation.Chunks.Chunk;

public class eCResourceAnimationMotion_PS extends GenomeFile {
	public static class eSFrameEffect implements G3Serializable {
		public int keyFrame;
		public String effectName;

		@Override
		public void read(G3FileReader reader) {
			keyFrame = reader.readUnsignedShort();
			effectName = reader.readEntry();
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeUnsignedShort(keyFrame);
			writer.writeEntry(effectName);
		}
	}

	public static class eCWrapper_emfx2Motion implements G3Serializable {
		public int highVersion; // high version (2 in case of v2.34)
		public int lowVersion; // low version (34 in case of v2.34)
		/**
		 * A key frame chunk applies to the most recent motion part chunk.
		 */
		public List<Chunk> chunks;

		@Override
		public void read(G3FileReader reader) {
			int offsetEnd = reader.readInt() + reader.getPos();
			if (!reader.readString(4).equals("LMA ")) {
				throw new IllegalArgumentException("Invalid eCWrapper_emfx2Motion.");
			}

			highVersion = reader.readUnsignedByte();
			lowVersion = reader.readUnsignedByte();

			if (highVersion != 1 || lowVersion != 1) {
				throw new IllegalArgumentException("Invalid eCWrapper_emfx2Motion.");
			}

			// is this an actor? (if false, it's a motion)
			if (reader.readBool()) {
				throw new IllegalArgumentException("Invalid eCWrapper_emfx2Motion.");
			}

			chunks = Chunks.readChunks(reader, offsetEnd);
		}

		@Override
		public void write(G3FileWriter writer) {
			int sizeOffset = writer.position();
			writer.writeInt(0);
			writer.writeString("LMA ");
			writer.writeUnsignedByte(highVersion).writeUnsignedByte(lowVersion);
			writer.writeBool(false);
			Chunks.writeChunks(writer, chunks);
			writer.writeInt(sizeOffset, writer.position() - sizeOffset - 4);
		}
	}

	public long resourceSize;
	public float resourcePriority;
	public bCDateTime nativeFileTime;
	public long nativeFileSize;
	public bCDateTime unkFileTime; // Maybe actor?
	public List<eSFrameEffect> frameEffects;
	public eCWrapper_emfx2Motion motion;

	public eCResourceAnimationMotion_PS(G3FileReaderEx reader) throws IOException {
		read(reader);
	}

	@Override
	protected void readInternal(G3FileReaderEx reader) throws IOException {
		int version = reader.readUnsignedShort();
		resourceSize = reader.readUnsignedInt();
		resourcePriority = reader.readFloat();
		nativeFileTime = reader.read(bCDateTime.class);
		nativeFileSize = reader.readUnsignedInt();
		unkFileTime = version >= 3 ? reader.read(bCDateTime.class) : nativeFileTime;
		frameEffects = version >= 2 ? reader.readList(eSFrameEffect.class, reader.readUnsignedShort()) : new ArrayList<>();
		motion = reader.read(eCWrapper_emfx2Motion.class);
	}

	@Override
	protected void writeInternal(G3FileWriterEx writer) throws IOException {
		writer.writeUnsignedShort(5);
		writer.writeUnsignedInt(resourceSize);
		writer.writeFloat(resourcePriority);
		writer.write(nativeFileTime).writeUnsignedInt(nativeFileSize);
		writer.write(unkFileTime);
		writer.writeUnsignedShort(frameEffects.size()).write(frameEffects);
		writer.write(motion);
	}

}
