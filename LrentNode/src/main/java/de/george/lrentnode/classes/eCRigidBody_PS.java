package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;

public class eCRigidBody_PS extends G3Class {

	private String rawData;

	public eCRigidBody_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		rawData = reader.read(84);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.write(rawData);
	}
}
