package de.george.lrentnode.classes;

import java.util.ArrayList;
import java.util.List;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.structure.bCBox;

public final class eCResourceCollisionMesh_PS extends eCResourceBase_PS {
	private float resourcePriority;
	private boolean convexResource;
	private bCBox boundary;
	private List<bCBox> nxsBoundaries;

	public eCResourceCollisionMesh_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		if (classVersion >= 0x23) {
			convexResource = reader.readBool();
		}
		resourcePriority = reader.readFloat();

		if (classVersion < 0x20) {
			throw new UnsupportedOperationException("eCResourceCollisionMesh_PS::Read(): tried to read old collision data.");
		}

		int meshCount = reader.readInt();
		for (int i = 0; i < meshCount; i++) {
			// Skip NXS Meshes
			int nxsSize = reader.readInt();
			reader.skip(4); // higher part of __int64
			reader.skip(nxsSize);
		}

		super.readPostClassVersion(reader);

		boundary = reader.read(bCBox.class);
		nxsBoundaries = new ArrayList<>();
		for (int i = 0; i < meshCount; i++) {
			nxsBoundaries.add(reader.read(bCBox.class));
		}

		if (classVersion >= 0x2A) {
			convexResource = reader.readBool();
			int meshFlagCount = reader.readInt();
			reader.skip(4 * meshFlagCount);
		}

		// UnscaledResourceModTime timestamp, which comes sometimes with Version 0x40
		if (reader.getPos() < deadcodePosition) {
			reader.skip(8);
		}
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		throw new UnsupportedOperationException();
	}

	public float getResourcePriority() {
		return resourcePriority;
	}

	public void setResourcePriority(float resourcePriority) {
		this.resourcePriority = resourcePriority;
	}

	public boolean isConvexResource() {
		return convexResource;
	}

	public void setConvexResource(boolean convexResource) {
		this.convexResource = convexResource;
	}

	public bCBox getBoundary() {
		return boundary;
	}

	public void setBoundary(bCBox boundary) {
		this.boundary = boundary;
	}

	public List<bCBox> getNxsBoundaries() {
		return nxsBoundaries;
	}

	public void setNxsBoundaries(List<bCBox> nxsBoundaries) {
		this.nxsBoundaries = nxsBoundaries;
	}
}
