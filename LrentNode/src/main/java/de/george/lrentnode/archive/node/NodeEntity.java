package de.george.lrentnode.archive.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCMatrix3;
import de.george.g3utils.structure.bCOrientedBox;
import de.george.lrentnode.archive.ArchiveEntity;
import de.george.lrentnode.archive.eCEntity;

public class NodeEntity extends ArchiveEntity {
	private static final Logger logger = LoggerFactory.getLogger(NodeEntity.class);

	// TODO: Recalculate
	private bCBox visualWorldNodeBoundary;
	private bCOrientedBox visualWorldNodeOOBoundary;

	public NodeEntity(boolean initialize) {
		super(initialize);
		if (initialize) {
			invalidate();
		}
	}

	private void invalidate() {
		// @foff
		/*
		 * Set
		 * 	m_RenderingEnabled
		 * 	m_Enabled
		 * 	m_DeactivationEnabled
		 *
		 * Reset
		 * 	__FIXME_4
		 * 	m_Killed
		 *
		 */
		// @fon
		super.invalidateCommons();

		visualWorldNodeBoundary = new bCBox();
		visualWorldNodeOOBoundary = new bCOrientedBox();
	}

	@Override
	public void read(G3FileReader reader, boolean skipPropertySets) {
		if (reader.readUnsignedShort() != 0x23) {
			reader.warn(logger, "Unsupported eCSpatialEntity version.");
		}

		setCreator(reader.readBool() ? reader.readGUID() : null); // bCPropertyID
		visualWorldNodeBoundary = reader.read(bCBox.class);
		visualWorldNodeOOBoundary = reader.read(bCOrientedBox.class);
		super.read(reader, skipPropertySets);
	}

	@Override
	public void updateLocalNodeBoundary(bCBox newBoundary) {
		super.updateLocalNodeBoundary(newBoundary);

		updateVisualWorldNodeBoundary();
	}

	private void updateVisualWorldNodeBoundary() {
		if (localNodeBoundary.isValid()) {
			visualWorldNodeBoundary = localNodeBoundary.getTransformed(worldMatrix);
			visualWorldNodeOOBoundary = new bCOrientedBox(localNodeBoundary, bCMatrix3.getIdentity());
			visualWorldNodeOOBoundary.transform(worldMatrix);
		} else {
			visualWorldNodeBoundary.invalidate();
			visualWorldNodeOOBoundary.invalidate();
		}
	}

	@Override
	protected void onUpdatedWorldMatrix() {
		super.onUpdatedWorldMatrix();

		updateVisualWorldNodeBoundary();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeUnsignedShort(0x23);
		writer.writeBool(hasCreator());
		if (hasCreator()) {
			writer.write(getCreator());
		}
		writer.write(visualWorldNodeBoundary, visualWorldNodeOOBoundary);
		super.write(writer);
	}

	@Override
	protected eCEntity newInstance(boolean initialize) {
		return new NodeEntity(initialize);
	}

	public bCBox getVisualWorldNodeBoundary() {
		return visualWorldNodeBoundary.clone();
	}

	public bCOrientedBox getVisualWorldNodeOOBoundary() {
		return visualWorldNodeOOBoundary.clone();
	}
}
