package de.george.lrentnode.classes;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCVector;

public final class gCCharacterControl_PS extends G3Class {
	private float unk2C;
	private float timeSinceLastPress_TurnWeight_RightLeft;
	private float timeSinceLastPress_TurnWeight_UpDown;
	private float unk38;
	private int timeSinceLastPress_Forward;
	private int timeSinceLastPress_StrafeLeft;
	private int timeSinceLastPress_StrafeRight;
	private int timeSinceLastPress_Backward;
	private int timeSinceLastPress_Up;
	private int timeSinceLastPress_Down;
	private boolean bAnyControlPressed;
	private boolean bTurning;
	private float fMovementConstraints;
	private bCVector input_MovementVector;
	private bCMatrix cachedInvertedCameraViewMatrix;

	public gCCharacterControl_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		if (classVersion < 2)
			return;

		unk2C = reader.readFloat();
		timeSinceLastPress_TurnWeight_RightLeft = reader.readFloat();
		timeSinceLastPress_TurnWeight_UpDown = reader.readFloat();
		unk38 = reader.readFloat();
		timeSinceLastPress_Forward = reader.readInt();
		timeSinceLastPress_StrafeLeft = reader.readInt();
		timeSinceLastPress_StrafeRight = reader.readInt();
		timeSinceLastPress_Backward = reader.readInt();
		timeSinceLastPress_Up = reader.readInt();
		timeSinceLastPress_Down = reader.readInt();
		bAnyControlPressed = reader.readBool();
		bTurning = reader.readBool();
		fMovementConstraints = reader.readFloat();
		input_MovementVector = reader.readVector();
		cachedInvertedCameraViewMatrix = reader.read(bCMatrix.class);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		if (classVersion < 2)
			return;

		writer.writeFloat(unk2C);
		writer.writeFloat(timeSinceLastPress_TurnWeight_RightLeft);
		writer.writeFloat(timeSinceLastPress_TurnWeight_UpDown);
		writer.writeFloat(unk38);
		writer.writeInt(timeSinceLastPress_Forward);
		writer.writeInt(timeSinceLastPress_StrafeLeft);
		writer.writeInt(timeSinceLastPress_StrafeRight);
		writer.writeInt(timeSinceLastPress_Backward);
		writer.writeInt(timeSinceLastPress_Up);
		writer.writeInt(timeSinceLastPress_Down);
		writer.writeBool(bAnyControlPressed);
		writer.writeBool(bTurning);
		writer.writeFloat(fMovementConstraints);
		writer.writeVector(input_MovementVector);
		writer.write(cachedInvertedCameraViewMatrix);
	}
}
