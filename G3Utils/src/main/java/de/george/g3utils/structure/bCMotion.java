package de.george.g3utils.structure;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class bCMotion implements G3Serializable {
	private bCVector position;
	private bCQuaternion rotation;

	public bCMotion(bCVector position, bCQuaternion rotation) {
		this.position = position;
		this.rotation = rotation;
	}

	public bCVector getPosition() {
		return position;
	}

	public void setPosition(bCVector position) {
		this.position = position;
	}

	public bCQuaternion getRotation() {
		return rotation;
	}

	public void setRotation(bCQuaternion rotation) {
		this.rotation = rotation;
	}

	@Override
	public void read(G3FileReader reader) {
		position = reader.readVector();
		rotation = reader.read(bCQuaternion.class);

	}

	@Override
	public void write(G3FileWriter writer) {
		writer.write(position).write(rotation);
	}

	@Override
	public bCMotion clone() {
		return new bCMotion(position.clone(), rotation.clone());
	}

	@Override
	public String toString() {
		return "position=" + position + ", rotation=" + rotation;
	}
}
