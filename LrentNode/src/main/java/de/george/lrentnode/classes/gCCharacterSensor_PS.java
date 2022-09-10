package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.structure.bCMotion;
import de.george.g3utils.structure.bCVector;

public class gCCharacterSensor_PS extends G3Class {
	public bCVector position;
	public bCVector unsmoothedPosition;
	public bCVector savedFrameStatePosition;
	public boolean unk38;
	public boolean unk39;
	public boolean goalChanged;
	public boolean unk41;
	public bCMotion goalPose;

	public gCCharacterSensor_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		if (classVersion < 2)
			return;

		position = reader.readVector();
		unsmoothedPosition = reader.readVector();
		savedFrameStatePosition = reader.readVector();
		unk38 = reader.readBool();
		unk39 = reader.readBool();
		goalChanged = reader.readBool();
		unk41 = reader.readBool();
		goalPose = reader.read(bCMotion.class);

	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		if (classVersion < 2)
			return;

		writer.writeVector(position);
		writer.writeVector(unsmoothedPosition);
		writer.writeVector(savedFrameStatePosition);
		writer.writeBool(unk38);
		writer.writeBool(unk39);
		writer.writeBool(goalChanged);
		writer.writeBool(unk41);
		writer.write(goalPose);
	}
}
