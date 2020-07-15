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
		// START OF ENTITY-DEFINITION
		if (!reader.read(4).equals("40005300")) {
			reader.warn(logger, "(1) LrentdatEntity unerwartete Dateistruktur.");
		}
		setCreator(reader.readBool() ? reader.readGUID() : null);

		super.read(reader, skipPropertySets);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.write("40005300");
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
