package de.george.lrentnode.archive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCSphere;
import de.george.lrentnode.util.ClassUtil;

public abstract class ArchiveEntity extends eCEntity {
	private static final Logger logger = LoggerFactory.getLogger(ArchiveEntity.class);

	protected ArchiveFile file;

	protected boolean unkFlag1; // eSEntityFlags:__FIXME_10 @ 0x400
	protected boolean locked;

	protected String creator;

	public ArchiveEntity(boolean initialize) {
		super(initialize);
	}

	@Override
	protected void invalidateFlags() {
		unkFlag1 = false;
		locked = false;
	}

	protected final void invalidateCommons() {
		creator = null;

		// see invalidate() of Lrentdat and NodeEntity
		enabled = true;
		renderingEnabled = true;
		deactivationEnabled = true;
	}

	@Override
	public void read(G3FileReader reader, boolean skipPropertySets) {
		if (!reader.read(4).equals("53000100")) {
			reader.warn(logger, "(1) ArchiveEntity unerwartete Dateistruktur.");
		}
		setGuid(reader.readGUID());
		enabled = reader.readBool();
		renderingEnabled = reader.readBool();
		processingDisabled = reader.readBool();
		deactivationEnabled = reader.readBool();
		unkFlag1 = reader.readBool();
		pickable = reader.readBool();
		collisionEnabled = reader.readBool();
		renderAlphaValue = reader.readFloat();
		insertType = reader.readUnsignedShort();
		lastRenderPriority = reader.readUnsignedByte();
		locked = reader.readBool();
		specialDepthTexPassEnabled = reader.readBool();
		unkFlag2 = reader.readBool();
		name = reader.readEntry();

		worldMatrix = reader.read(bCMatrix.class);
		localMatrix = reader.read(bCMatrix.class);
		worldTreeBoundary = reader.readBox();
		localNodeBoundary = reader.readBox();
		worldNodeBoundary = reader.readBox();
		worldTreeSphere = reader.read(bCSphere.class);
		worldNodeSphere = reader.read(bCSphere.class);

		visualLoDFactor = reader.readFloat();
		unkFlag3 = reader.readBool();
		objectCullFactor = reader.readFloat();
		dataChangedTimeStamp = reader.readUnsignedInt();
		uniformScaling = reader.readFloat();
		rangedObjectCulling = reader.readBool();
		processingRangeOutFadingEnabled = reader.readBool();

		int numberClassEntries = reader.readInt();
		for (int i = 0; i < numberClassEntries; i++) {
			if (!skipPropertySets) {
				addClass(ClassUtil.readClass(reader));
			} else {
				ClassUtil.skipClass(reader);
			}
		}
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.write("53000100");
		writer.write(getGuid());
		writer.writeBool(enabled);
		writer.writeBool(renderingEnabled);
		writer.writeBool(processingDisabled);
		writer.writeBool(deactivationEnabled);
		writer.writeBool(unkFlag1);
		writer.writeBool(pickable);
		writer.writeBool(collisionEnabled);
		writer.writeFloat(renderAlphaValue);
		writer.writeUnsignedShort(insertType);
		writer.writeUnsignedByte(lastRenderPriority);
		writer.writeBool(locked);
		writer.writeBool(specialDepthTexPassEnabled);
		writer.writeBool(unkFlag2);
		writer.writeEntry(name);

		writer.write(worldMatrix);
		writer.write(localMatrix);
		writer.writeBox(worldTreeBoundary);
		writer.writeBox(localNodeBoundary);
		writer.writeBox(worldNodeBoundary);
		writer.write(worldTreeSphere);
		writer.write(worldNodeSphere);

		writer.writeFloat(visualLoDFactor);
		writer.writeBool(unkFlag3);
		writer.writeFloat(objectCullFactor);
		writer.writeUnsignedInt(dataChangedTimeStamp);
		writer.writeFloat(uniformScaling);
		writer.writeBool(rangedObjectCulling);
		writer.writeBool(processingRangeOutFadingEnabled);
		writeClasses(ClassUtil::writeClass, writer);

	}

	public ArchiveFile getFile() {
		return file;
	}

	public void setFile(ArchiveFile file) {
		this.file = file;
	}

	@Override
	public String getCreator() {
		return creator;
	}

	@Override
	public void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * eSEntityFlags:__FIXME_10 @ 0x400
	 */
	public boolean isUnkFlag1() {
		return unkFlag1;
	}

	public void setUnkFlag1(boolean unkFlag1) {
		this.unkFlag1 = unkFlag1;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	@Override
	public void copyEntityPrivateData(eCEntity entity, boolean justCreated) {
		super.copyEntityPrivateData(entity, justCreated);
		// Diese Flags werden für Templates nicht serialisiert, auf Standardwert setzen.
		unkFlag1 = false;
		locked = false;
	}

}
