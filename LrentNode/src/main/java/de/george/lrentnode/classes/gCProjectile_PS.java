package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.structure.bCVector;

public class gCProjectile_PS extends G3Class {
	private boolean flying;
	private boolean hasCollided;
	private bCVector shootStartPosition;
	private bCVector projectileDirection;
	private float missileFrameTime;
	private float missileDecayTime;

	public gCProjectile_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		flying = reader.readBool();
		if (classVersion >= 0x29)
			projectileDirection = reader.readVector();
		if (classVersion >= 0x2A)
			shootStartPosition = reader.readVector();
		if (classVersion >= 0x4B) {
			missileFrameTime = reader.readFloat();
			missileDecayTime = reader.readFloat();
		}
		if (classVersion >= 0x4D)
			hasCollided = reader.readBool();

		if (classVersion != 0x4D)
			classVersion = 0x4D;
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.writeBool(flying);
		writer.writeVector(projectileDirection);
		writer.writeVector(shootStartPosition);
		writer.writeFloat(missileFrameTime);
		writer.writeFloat(missileDecayTime);
		writer.writeBool(hasCollided);
	}
}
