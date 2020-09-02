package de.george.lrentnode.archive.lrentdat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.lrentnode.archive.ArchiveEntity;
import de.george.lrentnode.archive.eCEntity;

public class LrentdatEntity extends ArchiveEntity {
	private static final Logger logger = LoggerFactory.getLogger(LrentdatEntity.class);

	public LrentdatEntity(boolean initialize) {
		super(initialize);
		if (initialize) {
			invalidate();
		}
	}

	private void invalidate() {
		// @foff
		/*
		 * Flags
		 * Set
		 * 		m_RenderingEnabled
		 * 		m_Enabled
		 * 		m_DeactivationEnabled
		 * 		__FIXME_23
		 * 		Reset
		 * 		__FIXME_4
		 */
		// @fon
		super.invalidateCommons();
	}

	@Override
	public void read(G3FileReader reader, boolean skipPropertySets) {
		if (reader.readUnsignedShort() != 0x40)
			reader.warn(logger, "Unsupported gCEntity version.");

		if (reader.readUnsignedShort() != 0x53)
			reader.warn(logger, "Unsupported eCDynamicEntity version.");

		setCreator(reader.readBool() ? reader.readGUID() : null);

		super.read(reader, skipPropertySets);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeUnsignedShort(0x40).writeUnsignedShort(0x53);
		writer.writeBool(hasCreator());
		if (hasCreator()) {
			writer.write(getCreator());
		}
		super.write(writer);
	}

	@Override
	protected eCEntity newInstance(boolean initialize) {
		return new LrentdatEntity(initialize);
	}
}
